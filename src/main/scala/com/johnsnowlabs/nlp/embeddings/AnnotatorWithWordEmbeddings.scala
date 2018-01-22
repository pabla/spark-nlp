package com.johnsnowlabs.nlp.embeddings

import java.io.File
import java.nio.file.Files
import java.util.UUID

import com.johnsnowlabs.nlp.{AnnotatorApproach, BaseAnnotatorModel}
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.SparkContext
import org.apache.spark.ml.Model
import org.apache.spark.ml.param.{IntParam, Param}
import org.apache.spark.sql.SparkSession


/**
  * Base class for annotators that uses Word Embeddings.
  * This implementation is based on RocksDB so it has a compact RAM usage
  *
  * 1. User configures Word Embeddings by method 'setWordEmbeddingsSource'.
  * 2. During training Word Embeddings are indexed as RockDB index file.
  * 3. Than this index file is spread across the cluster.
  * 4. Every model 'ModelWithWordEmbeddings' uses local RocksDB as Word Embeddings lookup.
 */
trait AnnotatorWithWordEmbeddings[A <: AnnotatorWithWordEmbeddings[A, M], M <: BaseAnnotatorModel[M]
  with ModelWithWordEmbeddings[M]] extends AutoCloseable {
  this:AnnotatorApproach[M] =>

  val sourceEmbeddingsPath = new Param[String](this, "sourceEmbeddingsPath", "Word embeddings file")
  val embeddingsFormat = new IntParam(this, "embeddingsFormat", "Word vectors file format")
  val embeddingsNDims = new IntParam(this, "embeddingsNDims", "Number of dimensions for word vectors")


  def setEmbeddingsSource(path: String, nDims: Int, format: WordEmbeddingsFormat.Format): A = {
    set(this.sourceEmbeddingsPath, path)
    set(this.embeddingsFormat, format.id)
    set(this.embeddingsNDims, nDims).asInstanceOf[A]
  }

  override def beforeTraining(spark: SparkSession): Unit = {
    if (isDefined(sourceEmbeddingsPath)) {
      indexEmbeddings(localPath, spark.sparkContext)
      WordEmbeddingsClusterHelper.copyIndexToCluster(localPath, spark.sparkContext)
    }
  }


  override def onTrained(model: M, spark: SparkSession): Unit = {
    if (isDefined(sourceEmbeddingsPath)) {
      model.setDims($(embeddingsNDims))
      val fileName = WordEmbeddingsClusterHelper.getClusterFileName(localPath).toString
      model.setIndexPath(fileName)
    }
  }

  lazy val embeddings: Option[WordEmbeddings] = {
    get(sourceEmbeddingsPath).map(_ => WordEmbeddings(localPath, $(embeddingsNDims)))
  }

  private lazy val localPath: String = {
    WordEmbeddingsClusterHelper.createLocalPath
  }

  private def indexEmbeddings(localFile: String, spark: SparkContext): Unit = {
    val formatId = $(embeddingsFormat)

    val fs = FileSystem.get(spark.hadoopConfiguration)

    if (formatId == WordEmbeddingsFormat.Text.id) {
      val tmpFile = Files.createTempFile("embeddings", ".bin").toAbsolutePath.toString()
      fs.copyToLocalFile(new Path($(sourceEmbeddingsPath)), new Path(tmpFile))
      WordEmbeddingsIndexer.indexText(tmpFile, localFile)
    } else if (formatId == WordEmbeddingsFormat.Binary.id) {
      val tmpFile = Files.createTempFile("embeddings", ".bin").toAbsolutePath.toString()
      fs.copyToLocalFile(new Path($(sourceEmbeddingsPath)), new Path(tmpFile))
      WordEmbeddingsIndexer.indexBinary(tmpFile, localFile)
    }
    else if (formatId == WordEmbeddingsFormat.SparkNlp.id) {
      val hdfs = FileSystem.get(spark.hadoopConfiguration)
      hdfs.copyToLocalFile(new Path($(sourceEmbeddingsPath)), new Path(localFile))
    }
  }

  override def close(): Unit = {
    if (embeddings.nonEmpty)
      embeddings.get.close()
  }
}

object WordEmbeddingsClusterHelper {

  def createLocalPath(): String = {
    Files.createTempDirectory(UUID.randomUUID().toString.takeRight(12) + "_idx")
      .toAbsolutePath.toString
  }

  def getClusterFileName(localFile: String): Path = {
    val name = new File(localFile).getName
    Path.mergePaths(new Path("/embeddings"), new Path(name))
  }

  def copyIndexToCluster(localFolder: String, spark: SparkContext): String = {
    val fs = FileSystem.get(spark.hadoopConfiguration)

    val src = new Path(localFolder)
    val dst = Path.mergePaths(fs.getHomeDirectory, getClusterFileName(localFolder))

    fs.copyFromLocalFile(false, true, src, dst)
    fs.deleteOnExit(dst)

    spark.addFile(dst.toString, true)

    dst.toString
  }
}

