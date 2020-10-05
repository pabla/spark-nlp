package com.johnsnowlabs.nlp.annotators.sbd.pragmatic

import com.johnsnowlabs.nlp.annotators.common.{Sentence, SentenceSplit}
import com.johnsnowlabs.nlp.annotators.sbd.SentenceDetectorParams
import com.johnsnowlabs.nlp.{Annotation, AnnotatorModel, HasSimpleAnnotate}
import org.apache.spark.ml.util.{DefaultParamsReadable, Identifiable}
import org.apache.spark.sql.{DataFrame, Dataset}

/**
  * Annotator that detects sentence boundaries using any provided approach
  *
  * See [[https://github.com/JohnSnowLabs/spark-nlp/tree/master/src/test/scala/com/johnsnowlabs/nlp/annotators/sbd/pragmatic]] for further reference on how to use this API
  *
  * @param uid internal constructor requirement for serialization of params
  * @@ model: Model to use for boundaries detection
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
class SentenceDetector(override val uid: String) extends AnnotatorModel[SentenceDetector] with HasSimpleAnnotate[SentenceDetector] with SentenceDetectorParams {

  import com.johnsnowlabs.nlp.AnnotatorType._

  def this() = this(Identifiable.randomUID("SENTENCE"))

  /** Output annotator type : DOCUMENT
    *
    * @group anno
    **/
  override val outputAnnotatorType: AnnotatorType = DOCUMENT
  /** Input annotator type : DOCUMENT
    *
    * @group anno
    **/
  override val inputAnnotatorTypes: Array[AnnotatorType] = Array(DOCUMENT)

  lazy val model: PragmaticMethod =
    if ($(customBounds).nonEmpty && $(useCustomBoundsOnly))
      new CustomPragmaticMethod($(customBounds))
    else if ($(customBounds).nonEmpty)
      new MixedPragmaticMethod($(useAbbrevations), $(detectLists), $(customBounds))
    else
      new DefaultPragmaticMethod($(useAbbrevations), $(detectLists))

  def tag(document: String): Array[Sentence] = {
    model.extractBounds(
      document
    ).flatMap(sentence => {
      var currentStart = sentence.start
      get(splitLength).map(splitLength => truncateSentence(sentence.content, splitLength)).getOrElse(Array(sentence.content))
        .zipWithIndex.map{ case (truncatedSentence, index) =>
        val currentEnd = currentStart + truncatedSentence.length - 1
        val result = Sentence(truncatedSentence, currentStart, currentEnd, index)
        /** +1 because of shifting to the next token begin. +1 because of a whitespace jump to next token. */
        currentStart = currentEnd + 2
        result
      }
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
      .filter(t => t.content.nonEmpty && t.content.length >= $(minLength) && get(maxLength).forall(m => t.content.length <= m))
    SentenceSplit.pack(sentences)
  }

  override protected def afterAnnotate(dataset: DataFrame): DataFrame = {
    import org.apache.spark.sql.functions.{array, col, explode}
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