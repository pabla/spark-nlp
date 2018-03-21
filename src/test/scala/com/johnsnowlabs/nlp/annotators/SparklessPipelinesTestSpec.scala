package com.johnsnowlabs.nlp.annotators

import com.johnsnowlabs.nlp.annotators.sbd.pragmatic.SentenceDetector
import com.johnsnowlabs.nlp.annotators.sda.vivekn.ViveknSentimentApproach
import com.johnsnowlabs.nlp.annotators.spell.norvig.NorvigSweetingApproach
import com.johnsnowlabs.nlp.util.io.{ExternalResource, ReadAs}
import com.johnsnowlabs.nlp.{Annotation, ContentProvider, DocumentAssembler, SparklessPipeline}
import com.johnsnowlabs.util.Benchmark
import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark.sql.{Dataset, Row}
import org.scalatest._

import scala.language.reflectiveCalls

class SparklessPipelinesTestSpec extends FlatSpec {
  def fixture = new {
    val data: Dataset[Row] = ContentProvider.parquetData.limit(1000)

    val documentAssembler: DocumentAssembler = new DocumentAssembler()
      .setInputCol("text")
      .setOutputCol("document")

    val sentenceDetector: SentenceDetector = new SentenceDetector()
      .setInputCols(Array("document"))
      .setOutputCol("sentence")

    val tokenizer: Tokenizer = new Tokenizer()
      .setInputCols(Array("sentence"))
      .setOutputCol("token")

    val normalizer: Normalizer = new Normalizer()
      .setInputCols(Array("token"))
      .setOutputCol("normalized")

    val spellChecker: NorvigSweetingApproach = new NorvigSweetingApproach()
      .setInputCols(Array("normalized"))
      .setOutputCol("spell")

    val sentimentDetector: ViveknSentimentApproach = new ViveknSentimentApproach()
      .setInputCols(Array("spell", "sentence"))
      .setOutputCol("vivekn")
      .setPositiveSource(ExternalResource("/vivekn/positive/1.txt", ReadAs.LINE_BY_LINE, Map("tokenPattern" -> "\\S+")))
      .setNegativeSource(ExternalResource("/vivekn/negative/1.txt", ReadAs.LINE_BY_LINE, Map("tokenPattern" -> "\\S+")))
      .setCorpusPrune(0)

    val pipeline: Pipeline = new Pipeline()
      .setStages(Array(
        documentAssembler,
        sentenceDetector,
        tokenizer,
        normalizer,
        spellChecker,
        sentimentDetector
      ))

    val model: PipelineModel = pipeline.fit(data)

    val textDF: Dataset[Row] = ContentProvider.parquetData.limit(10000)

    import textDF.sparkSession.implicits._
    val textArray: Array[String] = ContentProvider.parquetData.limit(10000).select("text").as[String].collect
  }

  "A SparklessPipeline" should "annotate for each annotator" in {
    val f = fixture
    val annotations = new SparklessPipeline(f.model).annotate(f.textArray)
    annotations.foreach { mapAnnotations =>
      mapAnnotations.values.foreach { annotations =>
        assert(annotations.nonEmpty)
        annotations.foreach { annotation =>
          assert(annotation.isInstanceOf[Annotation])
          assert(annotation.start >= 0)
        }
      }
    }
  }

  it should "annotate for each string in the text array" in {
    val f = fixture
    val annotations = new SparklessPipeline(f.model).annotate(f.textArray)
    assert(f.textArray.length == annotations.length)
  }

  it should "run faster than a tranditional pipeline" in {
    val f = fixture

    val t1: Double = Benchmark.measure("Time to collect all pipeline results") {
      f.model.transform(f.textDF).collect
    }

    val t2: Double = Benchmark.measure("Time to collect sparkless results") {
      new SparklessPipeline(f.model).annotate(f.textArray)
    }

    val t3: Double = Benchmark.measure("Time to collect sparkless results in parallel") {
      new SparklessPipeline(f.model).parAnnotate(f.textArray)
    }

    assert(t1 > t2)
    assert(t1 > t3)
  }
}
