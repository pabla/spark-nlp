package com.johnsnowlabs.nlp.annotators

import com.johnsnowlabs.nlp.{Annotation, AnnotatorModel, ParamsAndFeaturesReadable}
import com.johnsnowlabs.nlp.AnnotatorType.TOKEN
import com.johnsnowlabs.nlp.serialization.MapFeature
import org.apache.spark.ml.param.{BooleanParam, StringArrayParam}
import org.apache.spark.ml.util.Identifiable

class NormalizerModel(override val uid: String) extends AnnotatorModel[NormalizerModel] {

  override val outputAnnotatorType: AnnotatorType = TOKEN

  override val inputAnnotatorTypes: Array[AnnotatorType] = Array(TOKEN)

  val cleanupPatterns = new StringArrayParam(this, "cleanupPatterns",
    "normalization regex patterns which match will be removed from token")

  val lowercase = new BooleanParam(this, "lowercase", "whether to convert strings to lowercase")

  protected val slangDict: MapFeature[String, String] = new MapFeature(this, "slangDict")

  val slangMatchCase = new BooleanParam(this, "slangMatchCase", "whether or not to be case sensitive to match slangs. Defaults to false.")

  def this() = this(Identifiable.randomUID("NORMALIZER"))

  def setCleanupPatterns(value: Array[String]): this.type = set(cleanupPatterns, value)

  def getCleanupPatterns: Array[String] = $(cleanupPatterns)

  def setLowercase(value: Boolean): this.type = set(lowercase, value)

  def getLowercase: Boolean = $(lowercase)

  def setSlangDict(value: Map[String, String]): this.type = set(slangDict, value)

  def setSlangMatchCase(value: Boolean): this.type = set(slangMatchCase, value)

  def getSlangMatchCase: Boolean = $(slangMatchCase)

  def applyRegexPatterns(word: String): String = {

    val nToken = {
      get(cleanupPatterns).map(_.foldLeft(word)((currentText, compositeToken) => {
        currentText.replaceAll(compositeToken, "")
      })).getOrElse(word)
    }
    nToken
  }

  protected def getSlangDict: Map[String, String] = $$(slangDict)

  /** ToDo: Review implementation, Current implementation generates spaces between non-words, potentially breaking tokens */
  override def annotate(annotations: Seq[Annotation]): Seq[Annotation] = {

    val normalizedAnnotations = annotations.flatMap { originalToken =>

      /** slang dictionary keys should have been lowercased if slangMatchCase is false */
      val unslanged = $$(slangDict).get(
        if ($(slangMatchCase)) originalToken.result
        else originalToken.result.toLowerCase
      )

      /** simple-tokenize the unslanged slag phrase */
      val tokenizedUnslang = {
        unslanged.map(unslang => {
          unslang.split(" ")
        }).getOrElse(Array(originalToken.result))
      }

      val cleaned = tokenizedUnslang.map(word => applyRegexPatterns(word))

      val cased = if ($(lowercase)) cleaned.map(_.toLowerCase) else cleaned

      cased.filter(_.nonEmpty).map { finalToken => {
        Annotation(
          outputAnnotatorType,
          originalToken.begin,
          originalToken.begin + finalToken.length - 1,
          finalToken,
          originalToken.metadata
        )
      }}

    }
    resetIndexAnnotations(normalizedAnnotations)
  }

  private def resetIndexAnnotations(annotations: Seq[Annotation]): Seq[Annotation] = {
    val wrongBeginIndex = getFirstAnnotationIndexWithWrongTokenIndexesValues(annotations)
    if (wrongBeginIndex == -1) {
      annotations
    } else {
      val rightAnnotations = annotations.slice(0, wrongBeginIndex)
      val wrongAnnotations = annotations.slice(wrongBeginIndex, annotations.length)
      var priorEnd = 0
      val resetAnnotations = wrongAnnotations.zipWithIndex.map{ case (normalizedToken, index) =>
        val begin = if (index == 0) annotations(wrongBeginIndex - 1).end + 2 else priorEnd + 2
        priorEnd = begin + normalizedToken.result.length - 1
        Annotation(normalizedToken.annotatorType, begin, priorEnd, normalizedToken.result, normalizedToken.metadata)
      }
      rightAnnotations ++ resetAnnotations
    }
  }

  private def getFirstAnnotationIndexWithWrongTokenIndexesValues(annotations: Seq[Annotation]): Int = {
    val wrongIndex = annotations.zipWithIndex.flatMap { case (normalizedToken, index) =>
      val verifiedBegin = if (index > 0) annotations(index - 1).end + 2 else annotations(index).begin
      if (normalizedToken.begin != verifiedBegin) Some(index) else None
    }
    if (wrongIndex.isEmpty) -1 else wrongIndex.head
  }

}

object NormalizerModel extends ParamsAndFeaturesReadable[NormalizerModel]