package com.johnsnowlabs.nlp.embeddings

import java.io.File

import com.johnsnowlabs.ml.tensorflow.{ReadTensorflowModel, TensorflowBert, TensorflowWrapper, WriteTensorflowModel}
import com.johnsnowlabs.nlp._
import com.johnsnowlabs.nlp.annotators.common.{WordpieceEmbeddingsSentence, WordpieceTokenized}
import com.johnsnowlabs.nlp.pretrained.ResourceDownloader
import org.apache.spark.ml.param.IntParam
import org.apache.spark.ml.util.Identifiable
import org.apache.spark.sql.SparkSession


class BertEmbeddingsModel(override val uid: String) extends
  AnnotatorModel[BertEmbeddingsModel]
  with WriteTensorflowModel
{

  def this() = this(Identifiable.randomUID("BERT_EMBEDDINGS"))

  val sentenceStartTokenId = new IntParam(this, "sentenceStartTokenId", "Id of token that must be placed at the begin of every sentence.")
  val sentenceEndTokenId = new IntParam(this, "sentenceEndTokenId", "Id of token that must be placed at the end of every sentence.")
  val maxSentenceLength = new IntParam(this, "maxSentenceLength", "Max sentence length to process")
  val batchSize = new IntParam(this, "batchSize", "Batch size. Large values allows faster processing but requires more memory.")
  val dim = new IntParam(this, "dim", "Dimension of embeddings")

  setDefault(
    dim -> 768,
    batchSize -> 5,
    sentenceStartTokenId -> 103,
    sentenceEndTokenId -> 104,
    maxSentenceLength -> 100
  )

  var tensorflow: TensorflowWrapper = null

  def setTensorflow(tf: TensorflowWrapper): this.type = {
    this.tensorflow = tf
    this
  }

  def setBatchSize(size: Int): this.type = set(batchSize, size)
  def setDim(value: Int): this.type = set(dim, value)
  def setSetenceStartTokenId(value: Int): this.type = set(sentenceStartTokenId, value)
  def setSetenceEndTokenId(value: Int): this.type = set(sentenceEndTokenId, value)
  def setMaxSentenceLength(value: Int): this.type = set(maxSentenceLength, value)

  @transient
  private var _model: TensorflowBert = null

  def getModel: TensorflowBert = {
    if (_model == null) {
      require(tensorflow != null, "Tensorflow must be set before usage. Use method setTensorflow() for it.")

      _model = new TensorflowBert(
        tensorflow,
        $(sentenceStartTokenId),
        $(sentenceEndTokenId),
        $(maxSentenceLength))
    }

    _model
  }

  /**
    * takes a document and annotations and produces new annotations of this annotator's annotation type
    *
    * @param annotations Annotations that correspond to inputAnnotationCols generated by previous annotators if any
    * @return any number of annotations processed for every input annotation. Not necessary one to one relationship
    */
  override def annotate(annotations: Seq[Annotation]): Seq[Annotation] = {
    val sentences = WordpieceTokenized.unpack(annotations)
    val withEmbeddings = getModel.calculateEmbeddings(sentences)
    WordpieceEmbeddingsSentence.pack(withEmbeddings)
  }

  /** Annotator reference id. Used to identify elements in metadata or to refer to this annotator type */
  override val inputAnnotatorTypes = Array(AnnotatorType.DOCUMENT, AnnotatorType.WORDPIECE)
  override val outputAnnotatorType: AnnotatorType = AnnotatorType.WORD_EMBEDDINGS

  override def onWrite(path: String, spark: SparkSession): Unit = {
    super.onWrite(path, spark)
    writeTensorflowModel(path, spark, tensorflow, "_bert", BertEmbeddingsModel.tfFile)
  }
}

trait PretrainedBertModel {
  def pretrained(name: String = "bert_small_multilang_cased", language: Option[String] = None, remoteLoc: String = ResourceDownloader.publicLoc): BertEmbeddingsModel =
    ResourceDownloader.downloadModel(BertEmbeddingsModel, name, language, remoteLoc)
}

object BertEmbeddingsModel extends ParamsAndFeaturesReadable[BertEmbeddingsModel]
  with PretrainedBertModel
  with ReadTensorflowModel {

  override val tfFile: String = "bert_tensorflow"

  def readTensorflow(instance: BertEmbeddingsModel, path: String, spark: SparkSession): Unit = {
    val tf = readTensorflowModel(path, spark, "_bert_tf")
    instance.setTensorflow(tf)
  }

  addReader(readTensorflow)


  def loadFromPython(folder: String): BertEmbeddingsModel = {
    val f = new File(folder)
    require(f.exists, s"Folder ${folder} not found")
    require(f.isDirectory, s"File ${folder} is not folder")

    val wrapper = TensorflowWrapper.read(folder, zipped = false)
    new BertEmbeddingsModel().setTensorflow(wrapper)
  }
}
