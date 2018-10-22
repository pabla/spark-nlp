package com.johnsnowlabs.nlp.annotators.parser.dep

import com.johnsnowlabs.nlp.AnnotatorApproach
import com.johnsnowlabs.nlp.AnnotatorType._
import com.johnsnowlabs.nlp.annotators.param.ExternalResourceParam
import com.johnsnowlabs.nlp.annotators.parser.TagDictionary
import com.johnsnowlabs.nlp.annotators.parser.dep.GreedyTransition._
import com.johnsnowlabs.nlp.util.io.{ExternalResource, ReadAs, ResourceHelper}
import org.apache.spark.ml.PipelineModel
import org.apache.spark.ml.util.{DefaultParamsReadable, Identifiable}
import org.apache.spark.sql.Dataset

class DependencyParserApproach(override val uid: String) extends AnnotatorApproach[DependencyParserModel] {
  override val description: String = "Dependency Parser Estimator used to train"

  def this() = this(Identifiable.randomUID(DEPENDENCY))

  val source = new ExternalResourceParam(this, "source", "source file for dependency model")
  val dependencyTreeBank = new ExternalResourceParam(this, "dependencyTreeBank", "dependency treebank source files")

  def setSource(value: ExternalResource): this.type = set(source, value)

  def setDependencyTreeBank(path: String, readAs: ReadAs.Format = ReadAs.LINE_BY_LINE,
                            options: Map[String, String] = Map.empty[String, String]): this.type =
    set(dependencyTreeBank, ExternalResource(path, readAs, options))

  override val annotatorType:String = DEPENDENCY

  override val requiredAnnotatorTypes = Array(DOCUMENT, POS, TOKEN)

  private lazy val filesContent = ResourceHelper.getFilesContentAsArray($(dependencyTreeBank))

  private lazy val trainingSentences = filesContent.flatMap(fileContent => readCONLL(fileContent)).toList

  def readCONLL(filesContent: String): List[Sentence] = {

    val sections = filesContent.split("\\n\\n").toList

    val sentences = sections.map(
      s => {
        val lines = s.split("\\n").toList
        val body  = lines.map( l => {
          val arr = l.split("\\s+")
          val (raw, pos, dep) = (arr(0), arr(1), arr(2).toInt)
          // CONLL dependency layout assumes [root, word1, word2, ..., wordn]  (where n == lines.length)
          // our   dependency layout assumes [word0, word1, ..., word(n-1)] { root }
          val dep_ex = if(dep==0) lines.length+1-1 else dep-1
          WordData(raw, pos, dep_ex)
        })
        body  // Don't pretty up the sentence itself
      }
    )
    sentences
  }

  override def train(dataset: Dataset[_], recursivePipeline: Option[PipelineModel]): DependencyParserModel = {

    val (classes, tagDictionary) = TagDictionary.classesAndTagDictionary(trainingSentences)

    val tagger = new Tagger(classes, tagDictionary)

    val performanceProgress = (0 until 10).map { seed =>
        tagger.train(trainingSentences, seed) //Iterates to increase accuracy
    }
    println(s"Tagger Performance = $performanceProgress")

    val perceptronAsArray = tagger.getPerceptronAsArray

    new DependencyParserModel()
      .setSourcePath($(source))
      .setPerceptronAsArray(perceptronAsArray)
  }

}

object DependencyParserApproach extends DefaultParamsReadable[DependencyParserApproach]