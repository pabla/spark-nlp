package com.johnsnowlabs.nlp.annotators.seq2seq

import java.io.File
import com.johnsnowlabs.ml.tensorflow._
import com.johnsnowlabs.ml.tensorflow.sentencepiece._
import com.johnsnowlabs.nlp._
import com.johnsnowlabs.nlp.annotators.common._
import com.johnsnowlabs.nlp.serialization.MapFeature
import com.johnsnowlabs.nlp.util.io.{ExternalResource, ReadAs, ResourceHelper}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.ml.param.{IntArrayParam, IntParam, Param}
import org.apache.spark.ml.util.Identifiable
import org.apache.spark.sql.SparkSession

import scala.collection.immutable

/** MarianTransformer: Fast Neural Machine Translation
  *
  * MarianTransformer uses models trained by MarianNMT.
  *
  * Marian is an efficient, free Neural Machine Translation framework written in pure C++ with minimal dependencies.
  * It is mainly being developed by the Microsoft Translator team. Many academic (most notably the University of Edinburgh and in the past the Adam Mickiewicz University in Poznań) and commercial contributors help with its development.
  *
  * It is currently the engine behind the Microsoft Translator Neural Machine Translation services and being deployed by many companies, organizations and research projects (see below for an incomplete list).
  *
  * '''Sources''' :
  * MarianNMT [[https://marian-nmt.github.io/]]
  * Marian: Fast Neural Machine Translation in C++ [[https://www.aclweb.org/anthology/P18-4020/]]
  *
  * @param uid required internal uid for saving annotator
  *
  * @groupname anno Annotator types
  * @groupdesc anno Required input and expected output annotator types
  * @groupname Ungrouped Members
  * @groupname param Parameters
  * @groupname setParam Parameter setters
  * @groupname getParam Parameter getters
  * @groupname Ungrouped Members
  * @groupprio param  1
  * @groupprio anno  2
  * @groupprio Ungrouped 3
  * @groupprio setParam  4
  * @groupprio getParam  5
  * @groupdesc Parameters A list of (hyper-)parameter keys this annotator can take. Users can set and get the parameter values through setters and getters, respectively.
  */
class MarianTransformer(override val uid: String) extends
  AnnotatorModel[MarianTransformer]
  with WriteTensorflowModel
  with WriteSentencePieceModel {

  /** Annotator reference id. Used to identify elements in metadata or to refer to this annotator type */
  def this() = this(Identifiable.randomUID("MARIAN_TRANSFORMER"))

  /** Input Annotator Type : TOKEN DOCUMENT
    *
    * @group anno
    **/
  override val inputAnnotatorTypes: Array[String] = Array(AnnotatorType.DOCUMENT)

  /** Output Annotator Type : WORD_EMBEDDINGS
    *
    * @group anno
    **/
  override val outputAnnotatorType: AnnotatorType = AnnotatorType.DOCUMENT

  /** Batch size. Large values allows faster processing but requires more memory.
    *
    * @group param
    **/
  val batchSize = new IntParam(this, "batchSize", "Batch size. Large values allows faster processing but requires more memory.")

  /** Vocabulary used to encode and decode piece words generated by SentencePiece
    *
    * @group param
    **/
  val vocabulary: MapFeature[String, Int] = new MapFeature(this, "vocabulary")

  /** Max sentence length to process
    *
    * @group param
    **/
  val maxSentenceLength = new IntParam(this, "maxSentenceLength", "Max sentence length to process")

  /** Sentence initial language token is required in the form of >>id<<
    * (id = valid target language ID)
    *
    * @group param
    **/
  var prefix = new Param[String](this, "prefix", "a sentence initial language token is required in the form of >>id<< (id = valid target language ID)")

  /** ConfigProto from tensorflow, serialized into byte array. Get with config_proto.SerializeToString()
    *
    * @group param
    **/
  val configProtoBytes = new IntArrayParam(this, "configProtoBytes", "ConfigProto from tensorflow, serialized into byte array. Get with config_proto.SerializeToString()")

  /** The Tensorflow Marian Model
    *
    * @group param
    **/
  private var _model: Option[Broadcast[TensorflowMarian]] = None

  /** setVocabulary
    *
    * @group setParam
    * */
  def setVocabulary(value: Map[String, Int]): this.type = {
    if (get(vocabulary).isEmpty)
      set(vocabulary, value)
    this
  }

  /** Batch size. Large values allows faster processing but requires more memory.
    *
    * @group setParam
    **/
  def setBatchSize(size: Int): this.type = {
    if (get(batchSize).isEmpty)
      set(batchSize, size)
    this
  }

  /** Max sentence length to process
    *
    * @group setParam
    **/
  def setMaxSentenceLength(value: Int): this.type = {
    require(value <= 512, "MarianTransformer model does not support sequences longer than 512.")
    set(maxSentenceLength, value)
    this
  }

  /** Max sentence length to process
    *
    * @group getParam
    **/
  def getMaxSentenceLength: Int = $(maxSentenceLength)

  /** prefix
    *
    * @group setParam
    **/
  def setPrefix(taskPrefix: String): MarianTransformer.this.type = {
    set(prefix, taskPrefix)
  }

  /** prefix
    *
    * @group getParam
    **/
  def getPrefix: String = $(prefix)

  /** ConfigProto from tensorflow, serialized into byte array. Get with config_proto.SerializeToString()
    *
    * @group getSaram
    **/
  def setConfigProtoBytes(bytes: Array[Int]): MarianTransformer.this.type = set(this.configProtoBytes, bytes)

  /** ConfigProto from tensorflow, serialized into byte array. Get with config_proto.SerializeToString()
    *
    * @group setGaram
    **/
  def getConfigProtoBytes: Option[Array[Byte]] = get(this.configProtoBytes).map(_.map(_.toByte))

  setDefault(
    batchSize -> 64,
    maxSentenceLength -> 40
  )

  /** Sets Marian tensorflow Model
    *
    * @group setParam
    **/
  def setModelIfNotSet(spark: SparkSession, tensorflow: TensorflowWrapper, sppSrc: SentencePieceWrapper, sppTrg: SentencePieceWrapper): this.type = {
    if (_model.isEmpty) {
      _model = Some(
        spark.sparkContext.broadcast(
          new TensorflowMarian(
            tensorflow,
            sppSrc,
            sppTrg,
            immutable.ListMap($$(vocabulary).toSeq.sortBy(_._2):_*),
            configProtoBytes = getConfigProtoBytes
          )
        )
      )
    }
    this
  }

  /** Gets Marian tensorflow Model
    *
    * @group setParam
    **/
  def getModelIfNotSet: TensorflowMarian = _model.get.value

  /**
    * takes a document and annotations and produces new annotations of this annotator's annotation type
    *
    * @param annotations Annotations that correspond to inputAnnotationCols generated by previous annotators if any
    * @return any number of annotations processed for every input annotation. Not necessary one to one relationship
    */
  override def annotate(annotations: Seq[Annotation]): Seq[Annotation] = {
    val sentences = SentenceSplit.unpack(annotations)
    val nonEmptySentences = sentences.filter(_.content.nonEmpty)

    if (nonEmptySentences.nonEmpty){
      this.getModelIfNotSet.generateSeq2Seq(
        nonEmptySentences,
        $(batchSize),
        $(maxSentenceLength)
      ).zipWithIndex.map(x => {
        new Annotation(
          annotatorType = this.outputAnnotatorType,
          begin = 0,
          end = x._1.length,
          result = x._1,
          metadata = Map("sentence" -> x._2.toString))
      })
    } else {
      Seq.empty[Annotation]
    }

  }

  override def onWrite(path: String, spark: SparkSession): Unit = {
    super.onWrite(path, spark)
    writeTensorflowModel(path, spark, getModelIfNotSet.tensorflow, "_marian", MarianTransformer.tfFile, configProtoBytes = getConfigProtoBytes)
    writeSentencePieceModel(path, spark, getModelIfNotSet.sppSrc, "_src_marian",  MarianTransformer.sppFile+"_src")
    writeSentencePieceModel(path, spark, getModelIfNotSet.sppTrg, "_trg_marian",  MarianTransformer.sppFile+"_trg")

  }

}

trait ReadablePretrainedMarianMTModel extends ParamsAndFeaturesReadable[MarianTransformer] with HasPretrained[MarianTransformer] {
  override val defaultModelName: Some[String] = Some("opus-mt-en-fr")
  /** Java compliant-overrides */
  override def pretrained(): MarianTransformer = super.pretrained()
  override def pretrained(name: String): MarianTransformer = super.pretrained(name)
  override def pretrained(name: String, lang: String): MarianTransformer = super.pretrained(name, lang)
  override def pretrained(name: String, lang: String, remoteLoc: String): MarianTransformer = super.pretrained(name, lang, remoteLoc)
}

trait ReadMarianMTTensorflowModel extends ReadTensorflowModel with ReadSentencePieceModel {
  this: ParamsAndFeaturesReadable[MarianTransformer] =>

  override val tfFile: String = "marian_tensorflow"
  override val sppFile: String = "marian_spp"

  def readTensorflow(instance: MarianTransformer, path: String, spark: SparkSession): Unit = {
    val tf = readTensorflowModel(path, spark, "_marian_tf")
    val sppSrc = readSentencePieceModel(path, spark, "_src_marian", sppFile+"_src")
    val sppTrg = readSentencePieceModel(path, spark, "_trg_marian", sppFile+"_trg")
    instance.setModelIfNotSet(spark, tf, sppSrc, sppTrg)
  }

  addReader(readTensorflow)

  def loadSavedModel(folder: String, spark: SparkSession): MarianTransformer = {

    val f = new File(folder)
    val assetsPath = folder+"/assets"
    val savedModel = new File(folder, "saved_model.pb")
    val sppSrcModel = new File(assetsPath, "source.spm")
    val sppTrgModel = new File(assetsPath, "target.spm")
    val sppVocab = new File(assetsPath, "vocabs.txt")

    require(f.exists, s"Folder $folder not found")
    require(f.isDirectory, s"File $folder is not folder")
    require(
      savedModel.exists(),
      s"savedModel file saved_model.pb not found in folder $folder"
    )
    require(sppSrcModel.exists(), s"SentencePiece model source.spm not found in folder $assetsPath")
    require(sppTrgModel.exists(), s"SentencePiece model target.spm not found in folder $assetsPath")
    require(sppVocab.exists(), s"SentencePiece model source.model not found in folder $assetsPath")

    val vocabResource = new ExternalResource(sppVocab.getAbsolutePath, ReadAs.TEXT, Map("format" -> "text"))
    val words = ResourceHelper.parseLines(vocabResource).zipWithIndex.toMap

    val wrapper = TensorflowWrapper.read(folder, zipped = false, useBundle = true, tags = Array("serve"))
    val sppSrc = SentencePieceWrapper.read(sppSrcModel.toString)
    val sppTrg = SentencePieceWrapper.read(sppTrgModel.toString)

    val marianMT = new MarianTransformer()
      .setVocabulary(words)
      .setModelIfNotSet(spark, wrapper, sppSrc, sppTrg)

     marianMT
  }
}

object MarianTransformer extends ReadablePretrainedMarianMTModel with ReadMarianMTTensorflowModel with ReadSentencePieceModel
