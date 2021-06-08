/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.johnsnowlabs.nlp.annotators.ner.crf

import com.johnsnowlabs.ml.crf.{FbCalculator, LinearChainCrfModel}
import com.johnsnowlabs.nlp.AnnotatorType._
import com.johnsnowlabs.nlp._
import com.johnsnowlabs.nlp.annotators.common.Annotated.{NerTaggedSentence, PosTaggedSentence}
import com.johnsnowlabs.nlp.annotators.common._
import com.johnsnowlabs.nlp.serialization.{MapFeature, StructFeature}
import com.johnsnowlabs.storage.HasStorageRef
import org.apache.spark.ml.param.{BooleanParam, StringArrayParam}
import org.apache.spark.ml.util._
import org.apache.spark.sql.Dataset

import scala.collection.Map


/**
 * Algorithm for training Named Entity Recognition Model
 *
 * This Named Entity recognition annotator allows for a generic model to be trained by utilizing a CRF machine learning algorithm. Its train data (train_ner) is either a labeled or an external CoNLL 2003 IOB based spark dataset with Annotations columns. Also the user has to provide word embeddings annotation column.
 * Optionally the user can provide an entity dictionary file for better accuracy
 *
 * See [[https://github.com/JohnSnowLabs/spark-nlp/tree/master/src/test/scala/com/johnsnowlabs/nlp/annotators/ner/crf]] for further reference on this API.
 */
class NerCrfModel(override val uid: String) extends AnnotatorModel[NerCrfModel] with HasSimpleAnnotate[NerCrfModel] with HasStorageRef {

  def this() = this(Identifiable.randomUID("NER"))

  /** List of Entities to recognize
   *
   * @group param
   **/
  val entities = new StringArrayParam(this, "entities", "List of Entities to recognize")
  /** crfModel
   *
   * @group param
   **/
  val model: StructFeature[LinearChainCrfModel] = new StructFeature[LinearChainCrfModel](this, "crfModel")
  /** dictionaryFeatures
   *
   * @group param
   **/
  val dictionaryFeatures: MapFeature[String, String] = new MapFeature[String, String](this, "dictionaryFeatures")
  /** whether or not to calculate prediction confidence by token, includes in metadata
   *
   * @group param
   **/
  val includeConfidence = new BooleanParam(this, "includeConfidence", "whether or not to calculate prediction confidence by token, includes in metadata")

  /** A  LinearChainCrfModel
   *
   * @group setParam
   **/
  def setModel(crf: LinearChainCrfModel): NerCrfModel = set(model, crf)

  /** DictionaryFeatures
   *
   * @group setParam
   **/
  def setDictionaryFeatures(dictFeatures: DictionaryFeatures): this.type = set(dictionaryFeatures, dictFeatures.dict)

  /** Entities to detect
   *
   * @group setParam
   **/
  def setEntities(toExtract: Array[String]): NerCrfModel = set(entities, toExtract)

  /** Whether or not to calculate prediction confidence by token, includes in metadata
   *
   * @group setParam
   **/
  def setIncludeConfidence(c: Boolean): this.type = set(includeConfidence, c)

  /** Whether or not to calculate prediction confidence by token, includes in metadata
   *
   * @group getParam
   **/
  def getIncludeConfidence: Boolean = $(includeConfidence)

  setDefault(dictionaryFeatures, () => Map.empty[String, String])
  setDefault(includeConfidence, false)

  /**
   * Predicts Named Entities in input sentences
   *
   * @param sentences POS tagged and WordpieceEmbeddings sentences
   * @return sentences with recognized Named Entities
   */
  def tag(sentences: Seq[(PosTaggedSentence, WordpieceEmbeddingsSentence)]): Seq[NerTaggedSentence] = {
    require(model.isSet, "model must be set before tagging")

    val crf = $$(model)

    val fg = FeatureGenerator(new DictionaryFeatures($$(dictionaryFeatures)))
    sentences.map{case (sentence, withEmbeddings) =>
      val instance = fg.generate(sentence, withEmbeddings, crf.metadata)

      lazy val confidenceValues = {
        val fb = new FbCalculator(instance.items.length, crf.metadata)
        fb.calculate(instance, $$(model).weights, 1)
        fb.alpha
      }

      val labelIds = crf.predict(instance)

      val words = sentence.indexedTaggedWords
        .zip(labelIds.labels)
        .zipWithIndex
        .flatMap{case ((word, labelId), idx) =>
          val label = crf.metadata.labels(labelId)

          val alpha = if ($(includeConfidence)) {
            val scores = Some(confidenceValues.apply(idx))
            Some(crf.metadata.labels
              .zipWithIndex
              .filter(x=> x._2 != 0)
              .map {case (t, i) => Map(t -> scores.getOrElse(Array.empty[String]).lift(i).getOrElse(0.0f).toString)})
          } else None

          if (!isDefined(entities) || $(entities).isEmpty || $(entities).contains(label))
            Some(IndexedTaggedWord(word.word, label, word.begin, word.end, alpha))
          else
            None
        }

      TaggedSentence(words)
    }
  }

  override protected def beforeAnnotate(dataset: Dataset[_]): Dataset[_] = {
    validateStorageRef(dataset, $(inputCols), AnnotatorType.WORD_EMBEDDINGS)
    dataset
  }

  override def annotate(annotations: Seq[Annotation]): Seq[Annotation] = {
    val sourceSentences = PosTagged.unpack(annotations)
    val withEmbeddings = WordpieceEmbeddingsSentence.unpack(annotations)
    val taggedSentences = tag(sourceSentences.zip(withEmbeddings))
    NerTagged.pack(taggedSentences)
  }

  def shrink(minW: Float): NerCrfModel = set(model, $$(model).shrink(minW))

  override val inputAnnotatorTypes: Array[String] = Array(DOCUMENT, TOKEN, POS, WORD_EMBEDDINGS)

  override val outputAnnotatorType: AnnotatorType = NAMED_ENTITY

}

trait ReadablePretrainedNerCrf extends ParamsAndFeaturesReadable[NerCrfModel] with HasPretrained[NerCrfModel] {
  override val defaultModelName: Option[String] = Some("ner_crf")
  /** Java compliant-overrides */
  override def pretrained(): NerCrfModel = super.pretrained()
  override def pretrained(name: String): NerCrfModel = super.pretrained(name)
  override def pretrained(name: String, lang: String): NerCrfModel = super.pretrained(name, lang)
  override def pretrained(name: String, lang: String, remoteLoc: String): NerCrfModel = super.pretrained(name, lang, remoteLoc)
}

/**
 * This is the companion object of [[NerCrfModel]]. Please refer to that class for the documentation.
 */
object NerCrfModel extends ReadablePretrainedNerCrf
