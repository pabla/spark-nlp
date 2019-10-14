package com.johnsnowlabs.nlp.annotators.sda.vivekn

import com.johnsnowlabs.nlp.annotators.common.{TokenizedSentence, TokenizedWithSentence}
import com.johnsnowlabs.nlp.serialization.{MapFeature, SetFeature}
import com.johnsnowlabs.nlp.{Annotation, AnnotatorModel, HasPretrained, ParamsAndFeaturesReadable}
import org.apache.spark.ml.param.{DoubleParam, IntParam, LongParam}
import org.apache.spark.ml.util.Identifiable

class ViveknSentimentModel(override val uid: String) extends AnnotatorModel[ViveknSentimentModel] with ViveknSentimentUtils {

  import com.johnsnowlabs.nlp.AnnotatorType._

  override val outputAnnotatorType: AnnotatorType = SENTIMENT

  override val inputAnnotatorTypes: Array[AnnotatorType] = Array(TOKEN, DOCUMENT)

  protected val positive: MapFeature[String, Long] = new MapFeature(this, "positive_sentences")
  protected val negative: MapFeature[String, Long] = new MapFeature(this, "negative_sentences")
  protected val words: SetFeature[String] = new SetFeature[String](this, "words")

  protected val positiveTotals: LongParam = new LongParam(this, "positive_totals", "count of positive words")
  protected val negativeTotals: LongParam = new LongParam(this, "negative_totals", "count of negative words")

  protected val importantFeatureRatio = new DoubleParam(this, "importantFeatureRatio", "proportion of feature content to be considered relevant. Defaults to 0.5")
  protected val unimportantFeatureStep = new DoubleParam(this, "unimportantFeatureStep", "proportion to lookahead in unimportant features. Defaults to 0.025")
  protected val featureLimit = new IntParam(this, "featureLimit", "content feature limit, to boost performance in very dirt text. Default disabled with -1")

  def this() = this(Identifiable.randomUID("VIVEKN"))

  def setImportantFeatureRatio(v: Double): this.type = set(importantFeatureRatio, v)
  def setUnimportantFeatureStep(v: Double): this.type = set(unimportantFeatureStep, v)
  def setFeatureLimit(v: Int): this.type = set(featureLimit, v)

  def getImportantFeatureRatio(v: Double): Double = $(importantFeatureRatio)
  def getUnimportantFeatureStep(v: Double): Double = $(unimportantFeatureStep)
  def getFeatureLimit(v: Int): Int = $(featureLimit)

  def getPositive: Map[String, Long] = $$(positive)
  def getNegative: Map[String, Long] = $$(negative)
  def getFeatures: Set[String] = $$(words)

  private[vivekn] def setPositive(value: Map[String, Long]): this.type = set(positive, value)
  private[vivekn] def setNegative(value: Map[String, Long]): this.type = set(negative, value)
  private[vivekn] def setPositiveTotals(value: Long): this.type = set(positiveTotals, value)
  private[vivekn] def setNegativeTotals(value: Long): this.type = set(negativeTotals, value)
  private[vivekn] def setWords(value: Array[String]): this.type = {
    require(value.nonEmpty, "Word analysis for features cannot be empty. Set prune to false if training is small")
    val currentFeatures = scala.collection.mutable.Set.empty[String]
    val start = (value.length * $(importantFeatureRatio)).ceil.toInt
    val afterStart = {
      if ($(featureLimit) == -1) value.length
      else $(featureLimit)
    }
    val step = (afterStart * $(unimportantFeatureStep)).ceil.toInt
    value.take(start).foreach(currentFeatures.add)
    Range(start, afterStart, step).foreach(k => {
      value.slice(k, k+step).foreach(currentFeatures.add)
    })

    set(words, currentFeatures.toSet)
  }

  /** Positive: 0, Negative: 1, NA: 2*/
  def classify(sentence: TokenizedSentence): (Short, Double) = {
    val wordFeatures = negateSequence(sentence.tokens).intersect($$(words)).toList
    if (wordFeatures.isEmpty) return (2, 0.0)
    val positiveScore = wordFeatures.map(word => scala.math.log(($$(positive).getOrElse(word, 0L) + 1.0) / (2.0 * $(positiveTotals)))).sum
    val negativeScore = wordFeatures.map(word => scala.math.log(($$(negative).getOrElse(word, 0L) + 1.0) / (2.0 * $(negativeTotals)))).sum
    val positiveSum = wordFeatures.map(word => $$(positive).getOrElse(word, 0L)).sum.toDouble
    val negativeSum = wordFeatures.map(word => $$(negative).getOrElse(word, 0L)).sum.toDouble
    lazy val positiveConfidence = positiveSum / (positiveSum + negativeSum)
    lazy val negativeConfidence = negativeSum / (positiveSum + negativeSum)
    if (positiveScore > negativeScore) (0, positiveConfidence) else (1, negativeConfidence)
  }

  /**
    * Tokens are needed to identify each word in a sentence boundary
    * POS tags are optionally submitted to the model in case they are needed
    * Lemmas are another optional annotator for some models
    * Bounds of sentiment are hardcoded to 0 as they render useless
    * @param annotations Annotations that correspond to inputAnnotationCols generated by previous annotators if any
    * @return any number of annotations processed for every input annotation. Not necessary one to one relationship
    */
  override def annotate(annotations: Seq[Annotation]): Seq[Annotation] = {
    val sentences = TokenizedWithSentence.unpack(annotations)

    sentences.filter(s => s.indexedTokens.nonEmpty).map(sentence => {
      val (result, confidence) = classify(sentence)
      Annotation(
        outputAnnotatorType,
        sentence.indexedTokens.map(t => t.begin).min,
        sentence.indexedTokens.map(t => t.end).max,
        if (result == 0) "positive" else if (result == 1) "negative" else "na",
        Map("confidence" -> confidence.toString.take(6))
      )
    })
  }

}

trait ReadablePretrainedVivekn extends ParamsAndFeaturesReadable[ViveknSentimentModel] with HasPretrained[ViveknSentimentModel] {
  override protected val defaultModelName: String = "sentiment_vivekn"
}

object ViveknSentimentModel extends ReadablePretrainedVivekn