package com.johnsnowlabs.nlp.embeddings

import com.johnsnowlabs.nlp.AnnotatorType.{DOCUMENT, TOKEN, WORD_EMBEDDINGS}
import com.johnsnowlabs.nlp.{Annotation, AnnotatorModel, HasPretrained, ParamsAndFeaturesWritable}
import com.johnsnowlabs.nlp.annotators.common.{TokenPieceEmbeddings, TokenizedWithSentence, WordpieceEmbeddingsSentence}
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.ml.util.Identifiable
import org.apache.spark.sql.functions.{col, udf}
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import com.johnsnowlabs.nlp.util.io.ResourceHelper.spark.implicits._
import com.johnsnowlabs.storage.{HasStorage, StorageHelper}

class WordEmbeddingsModel(override val uid: String)
  extends AnnotatorModel[WordEmbeddingsModel]
    with HasEmbeddingsProperties
    with HasStorage[Float]
    with ParamsAndFeaturesWritable {

  def this() = this(Identifiable.randomUID("WORD_EMBEDDINGS_MODEL"))

  override protected val storageHelper: StorageHelper[Float, WordEmbeddingsStorageReader] = EmbeddingsHelper

  override val outputAnnotatorType: AnnotatorType = WORD_EMBEDDINGS
  /** Annotator reference id. Used to identify elements in metadata or to refer to this annotator type */
  override val inputAnnotatorTypes: Array[String] = Array(DOCUMENT, TOKEN)

  private def getEmbeddingsSerializedPath(path: String): Path =
    Path.mergePaths(new Path(path), new Path("/embeddings"))

  private[embeddings] def deserializeEmbeddings(path: String, spark: SparkSession): Unit = {
    if ($(includeStorage)) {
      val src = getEmbeddingsSerializedPath(path)

      if (!storageIsReady) {
        setStorage(EmbeddingsHelper.load(
          src.toUri.toString,
          spark,
          EmbeddingsFormat.SPARKNLP.toString,
          $(caseSensitive),
          $(storageRef)
        ))
      }
    }
  }

  private[embeddings] def serializeEmbeddings(path: String, spark: SparkSession): Unit = {
    val index = new Path(StorageHelper.getLocalPath(getStorageConnection($(caseSensitive)).fileName))

    val uri = new java.net.URI(path)
    val fs = FileSystem.get(uri, spark.sparkContext.hadoopConfiguration)
    val dst = getEmbeddingsSerializedPath(path)

    StorageHelper.save(fs, index, dst)
  }

  override protected def onWrite(path: String, spark: SparkSession): Unit = {
    /** Param only useful for runtime execution */
    if ($(includeStorage))
      serializeEmbeddings(path, spark)
  }

  protected lazy val zeroArray: Array[Float] = Array.fill[Float]($(dimension))(0f)

  /**
    * takes a document and annotations and produces new annotations of this annotator's annotation type
    *
    * @param annotations Annotations that correspond to inputAnnotationCols generated by previous annotators if any
    * @return any number of annotations processed for every input annotation. Not necessary one to one relationship
    */
  override def annotate(annotations: Seq[Annotation]): Seq[Annotation] = {
    val sentences = TokenizedWithSentence.unpack(annotations)
    val withEmbeddings = sentences.map{ s =>
      val tokens = s.indexedTokens.map { token =>
        val vectorOption = getStorageConnection($(caseSensitive)).lookupIndex(token.token)
        TokenPieceEmbeddings(token.token, token.token, -1, true, vectorOption, zeroArray, token.begin, token.end)
      }
      WordpieceEmbeddingsSentence(tokens, s.sentenceIndex)
    }

    WordpieceEmbeddingsSentence.pack(withEmbeddings)
  }

  override protected def afterAnnotate(dataset: DataFrame): DataFrame = {
    getStorageConnection($(caseSensitive)).findLocalDb.close()

    dataset.withColumn(getOutputCol, wrapEmbeddingsMetadata(dataset.col(getOutputCol), $(dimension), Some($(storageRef))))
  }

}

trait ReadablePretrainedWordEmbeddings extends EmbeddingsReadable[WordEmbeddingsModel] with HasPretrained[WordEmbeddingsModel] {
  override val defaultModelName = Some("glove_100d")
  /** Java compliant-overrides */
  override def pretrained(): WordEmbeddingsModel = super.pretrained()
  override def pretrained(name: String): WordEmbeddingsModel = super.pretrained(name)
  override def pretrained(name: String, lang: String): WordEmbeddingsModel = super.pretrained(name, lang)
  override def pretrained(name: String, lang: String, remoteLoc: String): WordEmbeddingsModel = super.pretrained(name, lang, remoteLoc)
}

trait EmbeddingsCoverage {

  case class CoverageResult(covered: Long, total: Long, percentage: Float)

  def withCoverageColumn(dataset: DataFrame, embeddingsCol: String, outputCol: String = "coverage"): DataFrame = {
    val coverageFn = udf((annotatorProperties: Seq[Row]) => {
      val annotations = annotatorProperties.map(Annotation(_))
      val oov = annotations.map(x => if (x.metadata.getOrElse("isOOV", "false") == "false") 1 else 0)
      val covered = oov.sum
      val total = annotations.count(_ => true)
      val percentage = 1f * covered / total
      CoverageResult(covered, total, percentage)
    })
    dataset.withColumn(outputCol, coverageFn(col(embeddingsCol)))
  }

  def overallCoverage(dataset: DataFrame, embeddingsCol: String): CoverageResult = {
    val words = dataset.select(embeddingsCol).flatMap(row => {
      val annotations = row.getAs[Seq[Row]](embeddingsCol)
      annotations.map(annotation => Tuple2(
        annotation.getAs[Map[String, String]]("metadata")("token"),
        if (annotation.getAs[Map[String, String]]("metadata").getOrElse("isOOV", "false") == "false") 1 else 0))
    })
    val oov = words.reduce((a, b) => Tuple2("Total", a._2 + b._2))
    val covered = oov._2
    val total = words.count()
    val percentage = 1f * covered / total
    CoverageResult(covered, total, percentage)
  }
}

object WordEmbeddingsModel extends ReadablePretrainedWordEmbeddings with EmbeddingsCoverage

