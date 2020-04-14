package com.johnsnowlabs.nlp.annotators

import com.johnsnowlabs.nlp.annotators.common.{InfixToken, PrefixedToken, SuffixedToken}
import com.johnsnowlabs.nlp.serialization.{ArrayFeature, SetFeature}
import com.johnsnowlabs.nlp.{Annotation, AnnotatorModel, AnnotatorType, ParamsAndFeaturesWritable}
import org.apache.spark.ml.util.Identifiable

class SimpleTokenizerModel(override val uid: String) extends
  AnnotatorModel[SimpleTokenizerModel] with ParamsAndFeaturesWritable {

  val prefixes: ArrayFeature[String] = new ArrayFeature[String](this, "prefixes")
  def setPrefixes(p: Array[String]):this.type = set(prefixes, p.sortBy(_.size).reverse)

  val suffixes: ArrayFeature[String] = new ArrayFeature[String](this, "suffixes")
  def setSuffixes(s: Array[String]):this.type = set(suffixes, s.sortBy(_.size).reverse)

  val infixes: ArrayFeature[String] = new ArrayFeature[String](this, "infixes")
  def setInfixes(s: Array[String]):this.type = set(infixes, s.sortBy(_.size).reverse)

  val whitelist: SetFeature[String] = new SetFeature[String](this, "whitelist")
  def setWhitelist(wlist: Set[String]):this.type = set(whitelist, wlist)


  /**
    * takes a document and annotations and produces new annotations of this annotator's annotation type
    *
    * @param annotations Annotations that correspond to inputAnnotationCols generated by previous annotators if any
    * @return any number of annotations processed for every input annotation. Not necessary one to one relationship
    */

  /**
    * takes a document and annotations and produces new annotations of this annotator's annotation type
    *
    * @param annotations Annotations that correspond to inputAnnotationCols generated by previous annotators if any
    * @return any number of annotations processed for every input annotation. Not necessary one to one relationship
    */
  override def annotate(annotations: Seq[Annotation]): Seq[Annotation] =
    annotations.flatMap { annotation =>
      tokenize(annotation.result).map(token => annotation.
        copy(annotatorType = AnnotatorType.TOKEN, result = token, metadata = annotation.metadata.updated("sentence",
          annotation.metadata.getOrElse("sentence", "0"))))
    }

  // hardcoded at this time
  @transient
  private lazy val firstPass = Seq(InfixToken($$(infixes)))


  @transient
  private lazy val secondPass = Seq(SuffixedToken($$(suffixes)),
    PrefixedToken($$(prefixes)))

  private def tokenize(text: String):Seq[String] =
    text.split(" ").filter(_!=" ").flatMap{ token =>
      var tmp = Seq(token)

      firstPass.foreach{ parser =>
        tmp = tmp.flatMap{ t =>
          if(whitelist.getOrDefault.contains(t))
            Seq(t)
          else
            parser.separate(t).split(" ")
        }
      }

      secondPass.foreach{ parser =>
        tmp = tmp.flatMap{ t =>
          if(whitelist.getOrDefault.contains(t))
            Seq(t)
          else
            parser.separate(t).split(" ")
        }
      }
      tmp
    }.filter(!_.equals(""))

  def this() = this(Identifiable.randomUID("SimpleTokenizerModel"))


  override val outputAnnotatorType: AnnotatorType = AnnotatorType.TOKEN
  /** Annotator reference id. Used to identify elements in metadata or to refer to this annotator type */
  override val inputAnnotatorTypes: Array[String] = Array(AnnotatorType.DOCUMENT)
}
