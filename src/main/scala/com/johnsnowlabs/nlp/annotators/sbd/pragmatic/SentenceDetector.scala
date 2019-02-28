package com.johnsnowlabs.nlp.annotators.sbd.pragmatic

import com.johnsnowlabs.nlp.annotators.common.{Sentence, SentenceSplit}
import com.johnsnowlabs.nlp.annotators.sbd.SentenceDetectorParams
import com.johnsnowlabs.nlp.{Annotation, AnnotatorModel}
import org.apache.spark.ml.util.{DefaultParamsReadable, Identifiable}
import org.apache.spark.sql.{DataFrame, Dataset}

/**
  * Annotator that detects sentence boundaries using any provided approach
  * @param uid internal constructor requirement for serialization of params
  * @@ model: Model to use for boundaries detection
  */
class SentenceDetector(override val uid: String) extends AnnotatorModel[SentenceDetector] with SentenceDetectorParams {

  import com.johnsnowlabs.nlp.AnnotatorType._

  def this() = this(Identifiable.randomUID("SENTENCE"))

  override val outputAnnotatorType: AnnotatorType = DOCUMENT

  override val inputAnnotatorTypes: Array[AnnotatorType] = Array(DOCUMENT)

  lazy val model: PragmaticMethod =
    if ($(customBounds).nonEmpty && $(useCustomBoundsOnly))
      new CustomPragmaticMethod($(customBounds))
    else if ($(customBounds).nonEmpty)
      new MixedPragmaticMethod($(useAbbrevations), $(customBounds))
    else
      new DefaultPragmaticMethod($(useAbbrevations))

  def tag(document: String): Array[Sentence] = {
    model.extractBounds(
      document
    ).flatMap(sentence => {
      var currentStart = sentence.start
      sentence.content.grouped($(maxLength)).zipWithIndex.map{ case (limitedSentence, index) => {
        val currentEnd = currentStart + limitedSentence.length - 1
        val result = Sentence(limitedSentence, currentStart, currentEnd, index)
        currentStart = currentEnd + 1
        result
      }}
    })
  }

  override def beforeAnnotate(dataset: Dataset[_]): Dataset[_] = {
    /** Preload model */
    model

    dataset
  }

  /**
    * Uses the model interface to prepare the context and extract the boundaries
    * @param annotations Annotations that correspond to inputAnnotationCols generated by previous annotators if any
    * @return One to many annotation relationship depending on how many sentences there are in the document
    */
  override def annotate(annotations: Seq[Annotation]): Seq[Annotation] = {
    val docs = annotations.map(_.result)
    val sentences = docs.flatMap(doc => tag(doc))
    SentenceSplit.pack(sentences)
  }

  override protected def afterAnnotate(dataset: DataFrame): DataFrame = {
    import org.apache.spark.sql.functions.{col, explode, array}
    if ($(explodeSentences)) {
      dataset
        .select(dataset.columns.filterNot(_ == getOutputCol).map(col) :+ explode(col(getOutputCol)).as("_tmp"):_*)
        .withColumn(getOutputCol, array(col("_tmp")).as(getOutputCol, dataset.schema.fields.find(_.name == getOutputCol).get.metadata))
        .drop("_tmp")
    }
    else dataset
  }

}

object SentenceDetector extends DefaultParamsReadable[SentenceDetector]