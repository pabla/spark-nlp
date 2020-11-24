package com.johnsnowlabs.nlp.annotators

import java.nio.charset.{Charset, StandardCharsets}

import com.johnsnowlabs.nlp.{Annotation, AnnotatorModel, AnnotatorType}
import com.johnsnowlabs.nlp.AnnotatorType.DOCUMENT
import javax.sound.sampled.AudioFormat.Encoding
import org.apache.spark.ml.param.{BooleanParam, Param, StringArrayParam}
import org.apache.spark.ml.util.{DefaultParamsReadable, Identifiable}


/**
  * Annotator which normalizes raw text from tagged text, e.g. scraped web pages or xml documents, from document type columns into Sentence.
  * Removes all dirty characters from text following one or more input regex patterns.
  * Can apply non wanted character removal which a specific policy.
  * Can apply lower case normalization.
  *
  * See [[https://github.com/JohnSnowLabs/spark-nlp/blob/master/src/test/scala/com/johnsnowlabs/nlp/annotators/DocumentNormalizerTestSpec.scala DocumentNormalizer test class]] for examples examples of usage.
  *
  * @param uid required uid for storing annotator to disk
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
class DocumentNormalizer(override val uid: String) extends AnnotatorModel[DocumentNormalizer] {

  private val EMPTY_STR = ""
  private val BREAK_STR = "|##|"
  private val SPACE_STR = " "
  private val GENERIC_TAGS_REMOVAL_PATTERN = "<[^>]*>"

  /** Input annotator type : DOCUMENT
    *
    * @group anno
    **/
  override val inputAnnotatorTypes: Array[AnnotatorType] = Array[AnnotatorType](DOCUMENT)

  /** Input annotator type : DOCUMENT
    *
    * @group anno
    **/
  override val outputAnnotatorType: AnnotatorType = DOCUMENT

  def this() = this(Identifiable.randomUID("DOCUMENT_NORMALIZER"))

  /** normalization regex patterns which match will be removed from document
    *
    * @group Parameters
    **/
  val cleanupPatterns: StringArrayParam = new StringArrayParam(this, "cleanupPatterns", "normalization regex patterns which match will be removed from document. Defaults is \"<[^>]*>\"")

  /** whether to convert strings to lowercase
    *
    * @group param
    **/
  val lowercase = new BooleanParam(this, "lowercase", "whether to convert strings to lowercase")

  /** removalPolicy to remove patterns from text with a given policy
    *
    * @group param
    **/
  val removalPolicy: Param[String] = new Param(this, "removalPolicy", "removalPolicy to remove pattern from text")

  /** file encoding to apply on normalized documents
    *
    * @group param
    **/
  val encoding: Param[String] = new Param(this, name = "encoding", "file encoding to apply on normalized documents")

  //  Assuming non-html does not contain any < or > and that input string is correctly structured
  setDefault(
    inputCols -> Array(AnnotatorType.DOCUMENT),
    cleanupPatterns -> Array(GENERIC_TAGS_REMOVAL_PATTERN),
    lowercase -> false,
    removalPolicy -> "pretty_all",
    encoding -> "UTF-8"
  )

  /** Regular expressions list for normalization.
    *
    * @group getParam
    **/
  def getCleanupPatterns: Array[String] = $(cleanupPatterns)

  /** Lowercase tokens, default false
    *
    * @group getParam
    **/
  def getLowercase: Boolean = $(lowercase)

  /** Policy to remove patterns from text. Defaults "pretty_all".
    *
    * @group getParam
    **/
  def getRemovalPolicy: String = $(removalPolicy)

  /** Encoding to apply to normalized documents.
    *
    * @group getParam
    **/
  def getEncoding: String = $(encoding)

  /** Regular expressions list for normalization.
    *
    * @group setParam
    **/
  def setCleanupPatterns(value: Array[String]): this.type = set(cleanupPatterns, value)

  /** Lower case tokens, default false
    *
    * @group setParam
    **/
  def setLowercase(value: Boolean): this.type = set(lowercase, value)

  /** Removal policy to apply.
    * Valid policy values are: "all", "pretty_all", "first", "pretty_first"
    *
    * @group setParam
    **/
  def setRemovalPolicy(value: String): this.type = set(removalPolicy, value)

  /** Encoding to apply. Default is UTF-8.
    * Valid encoding are values are: UTF_8, UTF_16, US_ASCII, ISO-8859-1, UTF-16BE, UTF-16LE
    *
    * @group setParam
    **/
  def setEncoding(value: String): this.type = set(encoding, value)

  private def withAllFormatter(text: String, replacement: String = EMPTY_STR): String ={
    val patternsStr: String = $(cleanupPatterns).mkString(BREAK_STR)
    text.replaceAll(patternsStr, replacement)
  }

  /** pattern to grab from text as token candidates. Defaults \\S+
    *
    **/
  private def withPrettyAllFormatter(text: String): String = {
    withAllFormatter(text).split("\\s+").map(_.trim).mkString(SPACE_STR)
  }

  /** pattern to grab from text as token candidates. Defaults \\S+
    *
    **/
  private def withFirstFormatter(text: String, replacement: String = EMPTY_STR): String = {
    val patternsStr = $(cleanupPatterns).mkString(BREAK_STR)
    text.replaceFirst(patternsStr, replacement)
  }

  /** pattern to grab from text as token candidates. Defaults \\S+
    *
    **/
  private def withPrettyFirstFormatter(text: String): String = {
    withFirstFormatter(text).split("\\s+").map(_.trim).mkString(SPACE_STR)
  }

  /** Apply a given encoding to the processed text
    *
    * US-ASCII
    * Seven-bit ASCII, a.k.a. ISO646-US, a.k.a. the Basic Latin block of the Unicode character set
    *
    * ISO-8859-1
    * ISO Latin Alphabet No. 1, a.k.a. ISO-LATIN-1
    *
    * UTF-8
    * Eight-bit UCS Transformation Format
    *
    * UTF-16BE
    * Sixteen-bit UCS Transformation Format, big-endian byte order
    *
    * UTF-16LE
    * Sixteen-bit UCS Transformation Format, little-endian byte order
    *
    * UTF-16
    * Sixteen-bit UCS Transformation Format, byte order identified by an optional byte-order mark
    **/
  private def withEncoding(text: String, encoding: Charset = StandardCharsets.UTF_8): String ={
    val defaultCharset: Charset = Charset.defaultCharset
    if(!Charset.defaultCharset.equals(encoding)){
      log.warn("Requested encoding parameter is different from the default charset.")
    }
    new String(text.getBytes(defaultCharset), encoding)
  }

  /** Apply document normalization on text using patterns, policy, lowercase and encoding  parameters.
    *
    */
  private def applyDocumentNormalization(text: String,
                                         patterns: Array[String],
                                         policy: String,
                                         lowercase: Boolean,
                                         encoding: String): String = {

    require(!text.isEmpty && patterns.length > 0 && !patterns(0).isEmpty && !policy.isEmpty)

    val cleaned: String = policy match {
      case "all" => withAllFormatter(text)
      case "pretty_all" => withPrettyAllFormatter(text)
      case "first" => withFirstFormatter(text)
      case "pretty_first" => withPrettyFirstFormatter(text)
      case _ => throw new Exception("Unknown policy parameter in DocumentNormalizer annotation." +
        "Please select either: all, pretty_all, first, or pretty_first")
    }

    val cased = if (lowercase) cleaned.toLowerCase else cleaned

    encoding match {
      case "UTF-8" => withEncoding(cased, StandardCharsets.UTF_8)
      case "UTF-16" => withEncoding(cased, StandardCharsets.UTF_16)
      case "US-ASCII" => withEncoding(cased, StandardCharsets.US_ASCII)
      case "ISO-8859-1" => withEncoding(cased, StandardCharsets.ISO_8859_1)
      case "UTF-16BE" => withEncoding(cased, StandardCharsets.UTF_16BE)
      case "UTF-16LE" => withEncoding(cased, StandardCharsets.UTF_16LE)
      case _ => throw new Exception("Unknown encoding parameter in DocumentNormalizer annotation." +
        "Please select either: UTF_8, UTF_16, US_ASCII, ISO-8859-1, UTF-16BE, UTF-16LE")
    }
  }

  override def annotate(annotations: Seq[Annotation]): Seq[Annotation] = {
    annotations.
      map { annotation =>
        val cleanedDoc =
          applyDocumentNormalization(annotation.result, getCleanupPatterns, getRemovalPolicy, getLowercase, getEncoding)

        Annotation(
          DOCUMENT,
          annotation.begin,
          cleanedDoc.length - 1,
          cleanedDoc,
          annotation.metadata
        )
      }
  }
}

object DocumentNormalizer extends DefaultParamsReadable[DocumentNormalizer]
