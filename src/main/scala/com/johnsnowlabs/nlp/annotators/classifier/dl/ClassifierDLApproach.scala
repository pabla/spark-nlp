package com.johnsnowlabs.nlp.annotators.classifier.dl

import com.johnsnowlabs.ml.tensorflow.{ClassifierDatasetEncoder, ClassifierDatasetEncoderParams, TensorflowClassifier, TensorflowWrapper}
import com.johnsnowlabs.nlp.AnnotatorType.{CATEGORY, SENTENCE_EMBEDDINGS}
import com.johnsnowlabs.nlp.annotators.ner.Verbose
import com.johnsnowlabs.nlp.{AnnotatorApproach, AnnotatorType, ParamsAndFeaturesWritable}
import com.johnsnowlabs.storage.HasStorageRef
import org.apache.spark.ml.PipelineModel
import org.apache.spark.ml.param._
import org.apache.spark.ml.util.{DefaultParamsReadable, Identifiable}
import org.apache.spark.sql.types.{DoubleType, FloatType, IntegerType, StringType}
import org.apache.spark.sql.{Dataset, SparkSession}

import scala.util.Random

/**
  * ClassifierDL is a generic Multi-class Text Classification. ClassifierDL uses the state-of-the-art Universal Sentence Encoder as an input for text classifications. The ClassifierDL annotator uses a deep learning model (DNNs) we have built inside TensorFlow and supports up to 50 classes
  *
  * NOTE: This annotator accepts a label column of a single item in either type of String, Int, Float, or Double.
  *
  * NOTE: UniversalSentenceEncoder and SentenceEmbeddings can be used for the inputCol
  *
  * See [[https://github.com/JohnSnowLabs/spark-nlp/blob/master/src/test/scala/com/johnsnowlabs/nlp/annotators/classifier/dl/ClassifierDLTestSpec.scala]] for further reference on how to use this API
  **/
class ClassifierDLApproach(override val uid: String)
  extends AnnotatorApproach[ClassifierDLModel]
    with ParamsAndFeaturesWritable {

  def this() = this(Identifiable.randomUID("ClassifierDL"))

  /** Trains TensorFlow model for multi-class text classification */
  override val description = "Trains TensorFlow model for multi-class text classification"
  /** Output annotator type : SENTENCE_EMBEDDINGS */
  override val inputAnnotatorTypes: Array[AnnotatorType] = Array(SENTENCE_EMBEDDINGS)
  /** Output annotator type : CATEGORY */
  override val outputAnnotatorType: String = CATEGORY

  /** Random seed */
  val randomSeed = new IntParam(this, "randomSeed", "Random seed")
  /** Column with label per each document */
  val labelColumn = new Param[String](this, "labelColumn", "Column with label per each document")
  /** Learning Rate */
  val lr = new FloatParam(this, "lr", "Learning Rate")
  /** Batch size */
  val batchSize = new IntParam(this, "batchSize", "Batch size")
  /** Dropout coefficient */
  val dropout = new FloatParam(this, "dropout", "Dropout coefficient")
  /** Maximum number of epochs to train */
  val maxEpochs = new IntParam(this, "maxEpochs", "Maximum number of epochs to train")
  /** Whether to output to annotators log folder */
  val enableOutputLogs = new BooleanParam(this, "enableOutputLogs", "Whether to output to annotators log folder")
  /** Choose the proportion of training dataset to be validated against the model on each Epoch. The value should be between 0.0 and 1.0 and by default it is 0.0 and off. */
  val validationSplit = new FloatParam(this, "validationSplit", "Choose the proportion of training dataset to be validated against the model on each Epoch. The value should be between 0.0 and 1.0 and by default it is 0.0 and off.")
  /** Level of verbosity during training */
  val verbose = new IntParam(this, "verbose", "Level of verbosity during training")
  /** ConfigProto from tensorflow, serialized into byte array. Get with config_proto.SerializeToString() */
  val configProtoBytes = new IntArrayParam(this, "configProtoBytes", "ConfigProto from tensorflow, serialized into byte array. Get with config_proto.SerializeToString()")

  /** Column with label per each document */
  def setLabelColumn(column: String): ClassifierDLApproach.this.type = set(labelColumn, column)

  /** Learning Rate */
  def setLr(lr: Float): ClassifierDLApproach.this.type = set(this.lr, lr)

  /** Batch size */
  def setBatchSize(batch: Int): ClassifierDLApproach.this.type = set(this.batchSize, batch)

  /** Dropout coefficient */
  def setDropout(dropout: Float): ClassifierDLApproach.this.type = set(this.dropout, dropout)

  /** Maximum number of epochs to train  */
  def setMaxEpochs(epochs: Int): ClassifierDLApproach.this.type = set(maxEpochs, epochs)

  /** Tensorflow config Protobytes passed to the TF session */
  def setConfigProtoBytes(bytes: Array[Int]): ClassifierDLApproach.this.type = set(this.configProtoBytes, bytes)

  /** Whether to output to annotators log folder */
  def setEnableOutputLogs(enableOutputLogs: Boolean): ClassifierDLApproach.this.type = set(this.enableOutputLogs, enableOutputLogs)

  /** Choose the proportion of training dataset to be validated against the model on each Epoch. The value should be between 0.0 and 1.0 and by default it is 0.0 and off.  */
  def setValidationSplit(validationSplit: Float):ClassifierDLApproach.this.type = set(this.validationSplit, validationSplit)

  /** Level of verbosity during training */
  def setVerbose(verbose: Int): ClassifierDLApproach.this.type = set(this.verbose, verbose)

  /** Level of verbosity during training */
  def setVerbose(verbose: Verbose.Level): ClassifierDLApproach.this.type = set(this.verbose, verbose.id)

  /** Column with label per each document */
  def getLabelColumn: String = $(this.labelColumn)

  /** Learning Rate */
  def getLr: Float = $(this.lr)

  /** Batch size */
  def getBatchSize: Int = $(this.batchSize)

  /** Dropout coefficient */
  def getDropout: Float = $(this.dropout)

  /** Whether to output to annotators log folder */
  def getEnableOutputLogs: Boolean = $(enableOutputLogs)

  /** Choose the proportion of training dataset to be validated against the model on each Epoch. The value should be between 0.0 and 1.0 and by default it is 0.0 and off.  */
  def getValidationSplit: Float = $(this.validationSplit)

  /** Maximum number of epochs to train  */
  def getMaxEpochs: Int = $(maxEpochs)

  /** Tensorflow config Protobytes passed to the TF session */
  def getConfigProtoBytes: Option[Array[Byte]] = get(this.configProtoBytes).map(_.map(_.toByte))

  setDefault(
    maxEpochs -> 10,
    lr -> 5e-3f,
    dropout -> 0.5f,
    batchSize -> 64,
    enableOutputLogs -> false,
    verbose -> Verbose.Silent.id,
    validationSplit -> 0.0f
  )

  override def beforeTraining(spark: SparkSession): Unit = {}

  override def train(dataset: Dataset[_], recursivePipeline: Option[PipelineModel]): ClassifierDLModel = {

    val labelColType = dataset.schema($(labelColumn)).dataType
    require(
      labelColType != StringType | labelColType != IntegerType | labelColType != DoubleType | labelColType != FloatType,
      s"The label column $labelColumn type is $labelColType and it's not compatible. " +
        s"Compatible types are StringType, IntegerType, DoubleType, or FloatType. "
    )

    val embeddingsRef = HasStorageRef.getStorageRefFromInput(dataset, $(inputCols), AnnotatorType.SENTENCE_EMBEDDINGS)

    val embeddingsField: String = ".embeddings"
    val inputColumns = getInputCols(0) + embeddingsField
    val train = dataset.select(dataset.col($(labelColumn)).cast("string"), dataset.col(inputColumns))
    val labels = train.select($(labelColumn)).distinct.collect.map(x => x(0).toString)

    require(
      labels.length < 50,
      s"The total unique number of classes must be less than 50. Currently is ${labels.length}"
    )

    val tf = loadSavedModel()

    val settings = ClassifierDatasetEncoderParams(
      tags = labels
    )

    val encoder = new ClassifierDatasetEncoder(
      settings
    )

    val trainDataset = encoder.collectTrainingInstances(train, getLabelColumn)
    val inputEmbeddings = encoder.extractSentenceEmbeddings(trainDataset)
    val inputLabels = encoder.extractLabels(trainDataset)

    val classifier = try {
      val model = new TensorflowClassifier(
        tensorflow = tf,
        encoder,
        Verbose($(verbose))
      )
      if (isDefined(randomSeed)) {
        Random.setSeed($(randomSeed))
      }

      model.train(
        inputEmbeddings,
        inputLabels,
        lr = $(lr),
        batchSize = $(batchSize),
        dropout = $(dropout),
        endEpoch = $(maxEpochs),
        configProtoBytes = getConfigProtoBytes,
        validationSplit = $(validationSplit),
        enableOutputLogs=$(enableOutputLogs),
        uuid = this.uid
      )
      model
    } catch {
      case e: Exception =>
        throw e
    }

    val model = new ClassifierDLModel()
      .setDatasetParams(classifier.encoder.params)
      .setModelIfNotSet(dataset.sparkSession, tf)
      .setStorageRef(embeddingsRef)

    if (get(configProtoBytes).isDefined)
      model.setConfigProtoBytes($(configProtoBytes))

    model
  }

  def loadSavedModel(): TensorflowWrapper = {

    val wrapper =
      TensorflowWrapper.readZippedSavedModel("/classifier-dl", tags = Array("serve"), initAllTables = true)
    wrapper
  }
}

object NerDLApproach extends DefaultParamsReadable[ClassifierDLApproach]
