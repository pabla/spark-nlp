package com.johnsnowlabs.nlp.embeddings

import java.io.File

import com.johnsnowlabs.ml.tensorflow._
import com.johnsnowlabs.nlp._
import com.johnsnowlabs.nlp.annotators.common._
import com.johnsnowlabs.nlp.annotators.tokenizer.wordpiece.{BasicTokenizer, WordpieceEncoder}
import com.johnsnowlabs.nlp.embeddings.BertEmbeddings.{addReader, readTensorflowModel}
import com.johnsnowlabs.nlp.pretrained.ResourceDownloader
import com.johnsnowlabs.nlp.serialization.MapFeature
import com.johnsnowlabs.nlp.util.io.{ExternalResource, ReadAs, ResourceHelper}
import org.apache.spark.ml.param.IntParam
import org.apache.spark.ml.util.Identifiable
import org.apache.spark.sql.{DataFrame, SparkSession}


class BertEmbeddings(override val uid: String) extends
  AnnotatorModel[BertEmbeddings]
  with WriteTensorflowModel
  with HasEmbeddings
{

  def this() = this(Identifiable.randomUID("BERT_EMBEDDINGS"))

  val maxSentenceLength = new IntParam(this, "maxSentenceLength", "Max sentence length to process")
  val batchSize = new IntParam(this, "batchSize", "Batch size. Large values allows faster processing but requires more memory.")

  val vocabulary: MapFeature[String, Int] = new MapFeature(this, "vocabulary")

  def setVocabulary(value: Map[String, Int]): this.type = set(vocabulary, value)

  def sentenceStartTokenId: Int = {
    $$(vocabulary)("[CLS]")
  }

  def sentenceEndTokenId: Int = {
    $$(vocabulary)("[SEP]")
  }

  setDefault(
    dimension -> 768,
    batchSize -> 5,
    maxSentenceLength -> 256
  )

  def setBatchSize(size: Int): this.type = set(batchSize, size)

  def setMaxSentenceLength(value: Int): this.type = set(maxSentenceLength, value)
  def getMaxSentenceLength: Int = $(maxSentenceLength)

  def tokenize(sentences: Seq[Sentence]): Seq[WordpieceTokenizedSentence] = {
    val basicTokenizer = new BasicTokenizer($(caseSensitive))
    val encoder = new WordpieceEncoder($$(vocabulary))

    sentences.map { s =>
      val tokens = basicTokenizer.tokenize(s)
      val wordpieceTokens = tokens.flatMap(token => encoder.encode(token))
      WordpieceTokenizedSentence(wordpieceTokens)
    }
  }

  /**
    * takes a document and annotations and produces new annotations of this annotator's annotation type
    *
    * @param annotations Annotations that correspond to inputAnnotationCols generated by previous annotators if any
    * @return any number of annotations processed for every input annotation. Not necessary one to one relationship
    */
  override def annotate(annotations: Seq[Annotation]): Seq[Annotation] = {
    val sentences = SentenceSplit.unpack(annotations)
    val tokenized = tokenize(sentences)
    val withEmbeddings = BertEmbeddings.getBertTensorflow(this).calculateEmbeddings(tokenized)
    WordpieceEmbeddingsSentence.pack(withEmbeddings)
  }

  override def afterAnnotate(dataset: DataFrame): DataFrame = {
    dataset.withColumn(getOutputCol, wrapEmbeddingsMetadata(dataset.col(getOutputCol), $(dimension)))
  }

  /** Annotator reference id. Used to identify elements in metadata or to refer to this annotator type */
  override val inputAnnotatorTypes = Array(AnnotatorType.DOCUMENT)
  override val outputAnnotatorType: AnnotatorType = AnnotatorType.WORD_EMBEDDINGS

  override def onWrite(path: String, spark: SparkSession): Unit = {
    super.onWrite(path, spark)
    writeTensorflowModel(path, spark, BertEmbeddings.getBertTensorflow(this).tensorflow, "_bert", BertEmbeddings.tfFile)
  }
}

trait PretrainedBertModel {
  def pretrained(name: String = "bert_uncased_base", language: Option[String] = None, remoteLoc: String = ResourceDownloader.publicLoc): BertEmbeddings =
    ResourceDownloader.downloadModel(BertEmbeddings, name, language, remoteLoc)
}

trait ReadBertTensorflowModel extends ReadTensorflowModel {
  override val tfFile: String = "bert_tensorflow"

  def readTensorflow(instance: BertEmbeddings, path: String, spark: SparkSession): Unit = {
    val tf = readTensorflowModel(path, spark, "_bert_tf")
    BertEmbeddings.setBertTensorflow(tf, instance)
  }

  addReader(readTensorflow)

  def loadFromPython(folder: String): BertEmbeddings = {
    val f = new File(folder)
    val vocab = new File(folder, "vocab.txt")
    require(f.exists, s"Folder ${folder} not found")
    require(f.isDirectory, s"File ${folder} is not folder")
    require(vocab.exists(), s"Vocabulary file vocab.txt not found in folder ${folder}")

    val wrapper = TensorflowWrapper.read(folder, zipped = false)

    val vocabResource = new ExternalResource(vocab.getAbsolutePath, ReadAs.LINE_BY_LINE, Map("format" -> "text"))
    val words = ResourceHelper.parseLines(vocabResource).zipWithIndex.toMap

    val bert = new BertEmbeddings()
      .setVocabulary(words)

    BertEmbeddings.setBertTensorflow(wrapper, bert)

    bert
  }
}

object BertEmbeddings extends ParamsAndFeaturesReadable[BertEmbeddings]
  with PretrainedBertModel
  with ReadBertTensorflowModel {

  private val tensorflowInstances = scala.collection.mutable.Map.empty[String, TensorflowBert]

  def setBertTensorflow(tensorflowWrapper: TensorflowWrapper, instance: BertEmbeddings): TensorflowBert = {
    val tensorflowBert = new TensorflowBert(
      tensorflowWrapper,
      instance.sentenceStartTokenId,
      instance.sentenceEndTokenId,
      instance.getMaxSentenceLength)
    tensorflowInstances.update(instance.uid, tensorflowBert)
    tensorflowBert
  }

  def getBertTensorflow(instance: BertEmbeddings): TensorflowBert = {
    tensorflowInstances(instance.uid)
  }

}
