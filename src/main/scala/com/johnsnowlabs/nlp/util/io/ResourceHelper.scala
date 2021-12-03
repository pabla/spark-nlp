/*
 * Copyright 2017-2021 John Snow Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.johnsnowlabs.nlp.util.io

import com.databricks.dbutils_v1.DBUtilsHolder.dbutils
import com.johnsnowlabs.nlp.annotators.Tokenizer
import com.johnsnowlabs.nlp.annotators.common.{TaggedSentence, TaggedWord}
import com.johnsnowlabs.nlp.util.io.ReadAs._
import com.johnsnowlabs.nlp.{DocumentAssembler, Finisher}
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

import java.io._
import java.net.{URL, URLDecoder}
import java.nio.file.{Files, Paths}
import java.util.jar.JarFile
import scala.collection.mutable.{ArrayBuffer, Map => MMap}
import scala.io.BufferedSource

/**
 * Helper one-place for IO management. Streams, source and external input should be handled from here
 */
object ResourceHelper {

  def getActiveSparkSession: SparkSession =
    SparkSession.getActiveSession.getOrElse(SparkSession.builder()
      .appName("SparkNLP Default Session")
      .master("local[*]")
      .config("spark.driver.memory", "22G")
      .config("spark.driver.maxResultSize", "0")
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .config("spark.kryoserializer.buffer.max", "1000m")
      .getOrCreate()
    )

  lazy val spark: SparkSession = getActiveSparkSession

  /** Structure for a SourceStream coming from compiled content */
  case class SourceStream(resource: String) {
    val path = new Path(resource)
    val fileSystem: FileSystem = FileSystem.get(path.toUri, spark.sparkContext.hadoopConfiguration)
    if (!fileSystem.exists(path))
      throw new FileNotFoundException(s"file or folder: $resource not found")
    val pipe: Seq[InputStream] = {
      /** Check whether it exists in file system */
      val files = fileSystem.listFiles(path, true)
      val buffer = ArrayBuffer.empty[InputStream]
      while (files.hasNext) buffer.append(fileSystem.open(files.next().getPath))
      buffer
    }
    val openBuffers: Seq[BufferedSource] = pipe.map(pp => {
      new BufferedSource(pp)("UTF-8")
    })
    val content: Seq[Iterator[String]] = openBuffers.map(c => c.getLines())

    def copyToLocal(prefix: String = "sparknlp_tmp_"): String = {
      if (fileSystem.getScheme == "file")
        return resource

      val files = fileSystem.listFiles(path, false)
//      val destination: Path = new Path(Files.createTempDirectory(prefix).toUri)
      val destination = Files.createTempDirectory(prefix).toUri

      fileSystem.getScheme match {
        case "hdfs" => while (files.hasNext) {
          fileSystem.copyToLocalFile(files.next.getPath, new Path(destination))
        }
        case "dbfs" =>
          dbutils.fs.cp(resource, destination.toString, recurse = true)
        case _  => while (files.hasNext) {
          fileSystem.copyFromLocalFile(files.next.getPath, new Path(destination))
        }
      }

      destination.toString
    }

    def close(): Unit = {
      openBuffers.foreach(_.close())
      pipe.foreach(_.close)
    }
  }

  private def fixTarget(path: String): String = {
    val toSearch = s"^.*target\\${File.separator}.*scala-.*\\${File.separator}.*classes\\${File.separator}"
    if (path.matches(toSearch + ".*")) {
      path.replaceFirst(toSearch, "")
    }
    else {
      path
    }
  }

  def copyToLocal(path: String): String = {
    val resource = SourceStream(path)
    resource.copyToLocal()
  }

  /** NOT thread safe. Do not call from executors. */
  def getResourceStream(path: String): InputStream = {
    if (new File(path).exists())
      new FileInputStream(new File(path))
    else {
      Option(getClass.getResourceAsStream(path))
        .getOrElse {
          Option(getClass.getClassLoader.getResourceAsStream(path))
            .getOrElse(throw new IllegalArgumentException(f"Wrong resource path $path"))
        }
    }
  }

  def getResourceFile(path: String): URL = {
    var dirURL = getClass.getResource(path)

    if (dirURL == null)
      dirURL = getClass.getClassLoader.getResource(path)

    dirURL
  }

  def listResourceDirectory(path: String): Seq[String] = {
    val dirURL = getResourceFile(path)

    if (dirURL != null && dirURL.getProtocol.equals("file") && new File(dirURL.toURI).exists()) {
      /* A file path: easy enough */
      return new File(dirURL.toURI).listFiles.sorted.map(_.getPath).map(fixTarget(_))
    } else if (dirURL == null) {
      /* path not in resources and not in disk */
      throw new FileNotFoundException(path)
    }

    if (dirURL.getProtocol.equals("jar")) {
      /* A JAR path */
      val jarPath = dirURL.getPath.substring(5, dirURL.getPath.indexOf("!")) //strip out only the JAR file
      val jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"))
      val entries = jar.entries()
      val result = new ArrayBuffer[String]()

      val pathToCheck = path
        .stripPrefix(File.separator.replaceAllLiterally("\\", "/"))
        .stripSuffix(File.separator) +
        File.separator.replaceAllLiterally("\\", "/")

      while (entries.hasMoreElements) {
        val name = entries.nextElement().getName.stripPrefix(File.separator)
        if (name.startsWith(pathToCheck)) { //filter according to the path
          var entry = name.substring(pathToCheck.length())
          val checkSubdir = entry.indexOf("/")
          if (checkSubdir >= 0) {
            // if it is a subdirectory, we just return the directory name
            entry = entry.substring(0, checkSubdir)
          }
          if (entry.nonEmpty) {
            result.append(pathToCheck + entry)
          }
        }
      }
      return result.distinct.sorted
    }

    throw new UnsupportedOperationException(s"Cannot list files for URL $dirURL")
  }

  /**
   * General purpose key value parser from source
   * Currently read only text files
   *
   * @return
   */
  def parseKeyValueText(
                         er: ExternalResource
                       ): Map[String, String] = {
    er.readAs match {
      case TEXT =>
        val sourceStream = SourceStream(er.path)
        val res = sourceStream.content.flatMap(c => c.map(line => {
          val kv = line.split(er.options("delimiter"))
          (kv.head.trim, kv.last.trim)
        })).toMap
        sourceStream.close()
        res
      case SPARK =>
        import spark.implicits._
        val dataset = spark.read.options(er.options).format(er.options("format"))
          .options(er.options)
          .option("delimiter", er.options("delimiter"))
          .load(er.path)
          .toDF("key", "value")
        val keyValueStore = MMap.empty[String, String]
        dataset.as[(String, String)].foreach { kv => keyValueStore(kv._1) = kv._2 }
        keyValueStore.toMap
      case _ =>
        throw new Exception("Unsupported readAs")
    }
  }

  def parseKeyListValues(externalResource: ExternalResource): Map[String, List[String]] = {
    externalResource.readAs match {
      case TEXT =>
        val sourceStream = SourceStream(externalResource.path)
        val keyValueStore = MMap.empty[String, List[String]]
        sourceStream.content.foreach(content => content.foreach { line => {
          val keyValues = line.split(externalResource.options("delimiter"))
          val key = keyValues.head
          val value = keyValues.drop(1).toList
          val storedValue = keyValueStore.get(key)
          if (storedValue.isDefined && !storedValue.contains(value)) {
            keyValueStore.update(key, storedValue.get ++ value)
          } else keyValueStore(key) = value
        }
        })
        sourceStream.close()
        keyValueStore.toMap
    }
  }

  def parseKeyArrayValues(externalResource: ExternalResource): Map[String, Array[Float]] = {
    externalResource.readAs match {
      case TEXT =>
        val sourceStream = SourceStream(externalResource.path)
        val keyValueStore = MMap.empty[String, Array[Float]]
        sourceStream.content.foreach(content => content.foreach { line => {
          val keyValues = line.split(externalResource.options("delimiter"))
          val key = keyValues.head
          val value = keyValues.drop(1).map(x => x.toFloat)
          if (value.length > 1) {
            keyValueStore(key) = value
          }
        }
        })
        sourceStream.close()
        keyValueStore.toMap
    }
  }

  /**
   * General purpose line parser from source
   * Currently read only text files
   *
   * @return
   */
  def parseLines(
                  er: ExternalResource
                ): Array[String] = {
    er.readAs match {
      case TEXT =>
        val sourceStream = SourceStream(er.path)
        val res = sourceStream.content.flatten.toArray
        sourceStream.close()
        res
      case SPARK =>
        import spark.implicits._
        spark.read.options(er.options).format(er.options("format")).load(er.path).as[String].collect
      case _ =>
        throw new Exception("Unsupported readAs")
    }
  }

  /**
   * General purpose line parser from source
   * Currently read only text files
   *
   * @return
   */
  def parseLinesIterator(
                          er: ExternalResource
                        ): Seq[Iterator[String]] = {
    er.readAs match {
      case TEXT =>
        val sourceStream = SourceStream(er.path)
        sourceStream.content
      case _ =>
        throw new Exception("Unsupported readAs")
    }
  }

  /**
   * General purpose tuple parser from source
   * Currently read only text files
   *
   * @return
   */
  def parseTupleText(
                      er: ExternalResource
                    ): Array[(String, String)] = {
    er.readAs match {
      case TEXT =>
        val sourceStream = SourceStream(er.path)
        val res = sourceStream.content.flatMap(c => c.filter(_.nonEmpty).map(line => {
          val kv = line.split(er.options("delimiter")).map(_.trim)
          (kv.head, kv.last)
        })).toArray
        sourceStream.close()
        res
      case SPARK =>
        import spark.implicits._
        val dataset = spark.read.options(er.options).format(er.options("format")).load(er.path)
        val lineStore = spark.sparkContext.collectionAccumulator[String]
        dataset.as[String].foreach(l => lineStore.add(l))
        val result = lineStore.value.toArray.map(line => {
          val kv = line.toString.split(er.options("delimiter")).map(_.trim)
          (kv.head, kv.last)
        })
        lineStore.reset()
        result
      case _ =>
        throw new Exception("Unsupported readAs")
    }
  }

  /**
   * General purpose tuple parser from source
   * Currently read only text files
   *
   * @return
   */
  def parseTupleSentences(
                           er: ExternalResource
                         ): Array[TaggedSentence] = {
    er.readAs match {
      case TEXT =>
        val sourceStream = SourceStream(er.path)
        val result = sourceStream.content.flatMap(c => c.filter(_.nonEmpty).map(line => {
          line.split("\\s+").filter(kv => {
            val s = kv.split(er.options("delimiter").head)
            s.length == 2 && s(0).nonEmpty && s(1).nonEmpty
          }).map(kv => {
            val p = kv.split(er.options("delimiter").head)
            TaggedWord(p(0), p(1))
          })
        })).toArray
        sourceStream.close()
        result.map(TaggedSentence(_))
      case SPARK =>
        import spark.implicits._
        val dataset = spark.read.options(er.options).format(er.options("format")).load(er.path)
        val result = dataset.as[String].filter(_.nonEmpty).map(line => {
          line.split("\\s+").filter(kv => {
            val s = kv.split(er.options("delimiter").head)
            s.length == 2 && s(0).nonEmpty && s(1).nonEmpty
          }).map(kv => {
            val p = kv.split(er.options("delimiter").head)
            TaggedWord(p(0), p(1))
          })
        }).collect
        result.map(TaggedSentence(_))
      case _ =>
        throw new Exception("Unsupported readAs")
    }
  }

  def parseTupleSentencesDS(
                             er: ExternalResource
                           ): Dataset[TaggedSentence] = {
    er.readAs match {
      case SPARK =>
        import spark.implicits._
        val dataset = spark.read.options(er.options).format(er.options("format")).load(er.path)
        val result = dataset.as[String].filter(_.nonEmpty).map(line => {
          line.split("\\s+").filter(kv => {
            val s = kv.split(er.options("delimiter").head)
            s.length == 2 && s(0).nonEmpty && s(1).nonEmpty
          }).map(kv => {
            val p = kv.split(er.options("delimiter").head)
            TaggedWord(p(0), p(1))
          })
        })
        result.map(TaggedSentence(_))
      case _ =>
        throw new Exception("Unsupported readAs. If you're training POS with large dataset, consider PerceptronApproachDistributed")
    }
  }

  /**
   * For multiple values per keys, this optimizer flattens all values for keys to have constant access
   */
  def flattenRevertValuesAsKeys(er: ExternalResource): Map[String, String] = {
    er.readAs match {
      case TEXT =>
        val m: MMap[String, String] = MMap()
        val sourceStream = SourceStream(er.path)
        sourceStream.content.foreach(c => c.foreach(line => {
          val kv = line.split(er.options("keyDelimiter")).map(_.trim)
          if (kv.length > 1) {
            val key = kv(0)
            val values = kv(1).split(er.options("valueDelimiter")).map(_.trim)
            values.foreach(m(_) = key)
          }
        }))
        sourceStream.close()
        m.toMap
      case SPARK =>
        import spark.implicits._
        val dataset = spark.read.options(er.options).format(er.options("format")).load(er.path)
        val valueAsKeys = MMap.empty[String, String]
        dataset.as[String].foreach(line => {
          val kv = line.split(er.options("keyDelimiter")).map(_.trim)
          if (kv.length > 1) {
            val key = kv(0)
            val values = kv(1).split(er.options("valueDelimiter")).map(_.trim)
            values.foreach(v => valueAsKeys(v) = key)
          }
        })
        valueAsKeys.toMap
      case _ =>
        throw new Exception("Unsupported readAs")
    }
  }

  /**
   * General purpose read saved Parquet
   * Currently read only Parquet format
   *
   * @return
   */
  def readParquetSparkDataFrame(
                                 er: ExternalResource
                               ): DataFrame = {
    er.readAs match {
      case SPARK =>
        val dataset = spark.read.options(er.options).format(er.options("format")).load(er.path)
        dataset
      case _ =>
        throw new Exception("Unsupported readAs - only accepts SPARK")
    }
  }

  def getWordCount(externalResource: ExternalResource,
                   wordCount: MMap[String, Long] = MMap.empty[String, Long].withDefaultValue(0),
                   pipeline: Option[PipelineModel] = None
                  ): MMap[String, Long] = {
    externalResource.readAs match {
      case TEXT =>
        val sourceStream = SourceStream(externalResource.path)
        val regex = externalResource.options("tokenPattern").r
        sourceStream.content.foreach(c => c.foreach { line => {
          val words: List[String] = regex.findAllMatchIn(line).map(_.matched).toList
          words.foreach(w =>
            // Creates a Map of frequency words: word -> frequency based on ExternalResource
            wordCount(w) += 1
          )
        }
        })
        sourceStream.close()
        if (wordCount.isEmpty)
          throw new FileNotFoundException("Word count dictionary for spell checker does not exist or is empty")
        wordCount
      case SPARK =>
        import spark.implicits._
        val dataset = spark.read.options(externalResource.options).format(externalResource.options("format"))
          .load(externalResource.path)
        val transformation = {
          if (pipeline.isDefined) {
            pipeline.get.transform(dataset)
          } else {
            val documentAssembler = new DocumentAssembler()
              .setInputCol("value")
            val tokenizer = new Tokenizer()
              .setInputCols("document")
              .setOutputCol("token")
              .setTargetPattern(externalResource.options("tokenPattern"))
            val finisher = new Finisher()
              .setInputCols("token")
              .setOutputCols("finished")
              .setAnnotationSplitSymbol("--")
            new Pipeline()
              .setStages(Array(documentAssembler, tokenizer, finisher))
              .fit(dataset)
              .transform(dataset)
          }
        }
        val wordCount = MMap.empty[String, Long].withDefaultValue(0)
        transformation
          .select("finished").as[String]
          .foreach(text => text.split("--").foreach(t => {
            wordCount(t) += 1
          }))
        wordCount
      case _ => throw new IllegalArgumentException("format not available for word count")
    }
  }

  def getFilesContentBuffer(externalResource: ExternalResource): Seq[Iterator[String]] = {
    externalResource.readAs match {
      case TEXT =>
        SourceStream(externalResource.path).content
      case _ =>
        throw new Exception("Unsupported readAs")
    }
  }

  def listLocalFiles(path: String): List[File] = {
    val fileSystem = FileSystem.get(spark.sparkContext.hadoopConfiguration)
    if (fileSystem.getScheme == "dbfs" || fileSystem.getScheme == "hdfs") {
      val filesPath = Option(new File(path.replace("file:", "")).listFiles())
      val files = filesPath.getOrElse(throw new FileNotFoundException(s"folder: $path not found"))
      return files.toList
    }
    val filesPath = Option(new File(path).listFiles())
    val files = filesPath.getOrElse(throw new FileNotFoundException(s"folder: $path not found"))
    files.toList
  }

  def validFile(path: String): Boolean = {
    val isValid = Files.exists(Paths.get(path))

    if (isValid) {
      isValid
    } else {
      throw new FileNotFoundException(path)
    }

  }

}
