package com.johnsnowlabs.nlp.annotators

import com.johnsnowlabs.nlp.annotators.common.{InfixToken, PrefixedToken, SuffixedToken}
import com.johnsnowlabs.nlp.serialization.{ArrayFeature, SetFeature}
import com.johnsnowlabs.nlp.{Annotation, AnnotatorModel, AnnotatorType, ParamsAndFeaturesWritable, HasSimpleAnnotate}
import org.apache.spark.ml.util.Identifiable

/**
 * Instantiated model of the [[RecursiveTokenizer]].
 * For usage and examples see the documentation of the main class.
 *
 * @param uid required internal uid for saving annotator
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
 * @groupdesc param A list of (hyper-)parameter keys this annotator can take. Users can set and get the parameter values through setters and getters, respectively.
 * */
class RecursiveTokenizerModel(override val uid: String)
  extends AnnotatorModel[RecursiveTokenizerModel]
    with HasSimpleAnnotate[RecursiveTokenizerModel]
    with ParamsAndFeaturesWritable {

  /** prefixes
   *
   * @group param
   * */
  val prefixes: ArrayFeature[String] = new ArrayFeature[String](this, "prefixes")

  /** prefixes
   *
   * @group setParam
   * */
  def setPrefixes(p: Array[String]): this.type = set(prefixes, p.sortBy(_.size).reverse)

  /** suffixes
   *
   * @group param
   * */
  val suffixes: ArrayFeature[String] = new ArrayFeature[String](this, "suffixes")

  /** suffixes
   *
   * @group setParam
   * */
  def setSuffixes(s: Array[String]): this.type = set(suffixes, s.sortBy(_.size).reverse)

  /** infixes
   *
   * @group param
   * */
  val infixes: ArrayFeature[String] = new ArrayFeature[String](this, "infixes")

  /** infixes
   *
   * @group setParam
   * */
  def setInfixes(s: Array[String]): this.type = set(infixes, s.sortBy(_.size).reverse)

  /** whitelist
   *
   * @group param
   * */
  val whitelist: SetFeature[String] = new SetFeature[String](this, "whitelist")

  /** whitelist
   *
   * @group setParam
   * */
  def setWhitelist(wlist: Set[String]): this.type = set(whitelist, wlist)

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

  private def tokenize(text: String): Seq[String] =
    text.split(" ").filter(_ != " ").flatMap { token =>
      var tmp = Seq(token)

      firstPass.foreach { parser =>
        tmp = tmp.flatMap { t =>
          if (whitelist.getOrDefault.contains(t))
            Seq(t)
          else
            parser.separate(t).split(" ")
        }
      }

      secondPass.foreach { parser =>
        tmp = tmp.flatMap { t =>
          if (whitelist.getOrDefault.contains(t))
            Seq(t)
          else
            parser.separate(t).split(" ")
        }
      }
      tmp
    }.filter(!_.equals(""))

  /** Annotator reference id. Used to identify elements in metadata or to refer to this annotator type */
  def this() = this(Identifiable.randomUID("SimpleTokenizerModel"))


  /** Output Annotator types: TOKEN
   *
   * @group anno
   */
  override val outputAnnotatorType: AnnotatorType = AnnotatorType.TOKEN
  /** Input Annotator types: DOCUMENT
   *
   * @group anno
   */
  override val inputAnnotatorTypes: Array[String] = Array(AnnotatorType.DOCUMENT)
}
