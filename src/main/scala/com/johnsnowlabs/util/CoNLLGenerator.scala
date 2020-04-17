package com.johnsnowlabs.util

import com.johnsnowlabs.nlp.Finisher
import com.johnsnowlabs.nlp.pretrained.PretrainedPipeline
import org.apache.spark.ml.PipelineModel
import org.apache.spark.ml.Pipeline
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

import scala.collection.mutable.ArrayBuffer

object CoNLLGenerator {

  def exportConllFiles(spark: SparkSession, filesPath: String, pipelineModel: PipelineModel, outputPath: String): Unit = {
    import spark.implicits._ //for toDS and toDF

    val data = spark.sparkContext.wholeTextFiles(filesPath).toDS.toDF("filename", "text")

    exportConllFiles(data, pipelineModel, outputPath)
  }

  def exportConllFiles(spark: SparkSession, filesPath: String, pipelinePath: String, outputPath: String): Unit = {
    val model = PipelineModel.load(pipelinePath)
    exportConllFiles(spark, filesPath, model, outputPath)
  }

  def exportConllFiles(data: DataFrame, pipelineModel: PipelineModel, outputPath: String): Unit = {
    import data.sparkSession.implicits._ // for row casting
    val POSdataset = pipelineModel.transform(data)

    val newPOSDataset = POSdataset.select("finished_token", "finished_pos", "finished_token_metadata").
      as[(Array[String], Array[String], Array[(String, String)])]

   val CoNLLDataset = makeConLLFormat(newPOSDataset)

    CoNLLDataset.coalesce(1).write.format("com.databricks.spark.csv").
      option("delimiter", " ").
      save(outputPath)
  }

  def exportConllFiles(data: DataFrame, outputPath: String): Unit = {
    import data.sparkSession.implicits._ // for row casting

    val preModel = PretrainedPipeline("explain_document_ml", lang="en").model
    val finisher = new Finisher()
      .setInputCols("token", "pos")
      .setIncludeMetadata(true)
    val pipelineModel = new Pipeline().setStages(Array(preModel, finisher)).fit(Seq(
      "").toDF("text"))
    val POSdataset = pipelineModel.transform(data)

    val newPOSDataset = POSdataset.select("finished_token", "finished_pos", "finished_token_metadata").
      as[(Array[String], Array[String], Array[(String, String)])]

    val CoNLLDataset = makeConLLFormat(newPOSDataset)
    CoNLLDataset.coalesce(1).write.format("com.databricks.spark.csv").
      option("delimiter", " ").
      save(outputPath)
  }

  def exportConllFiles(data: DataFrame, pipelinePath: String, outputPath: String): Unit = {
    val model = PipelineModel.load(pipelinePath)
    exportConllFiles(data, model, outputPath)
  }

  //helper function
  def makeConLLFormat(newPOSDataset : Dataset[(Array[String], Array[String], Array[(String, String)])]) ={
    import newPOSDataset.sparkSession.implicits._ //for row casting
    newPOSDataset.flatMap(row => {
      val newColumns: ArrayBuffer[(String, String, String, String)] = ArrayBuffer()
      val columns = (row._1 zip row._2 zip row._3.map(_._2.toInt)).map{case (a,b) => (a._1, a._2, b)}
      var sentenceId = 1
      newColumns.append(("", "", "", ""))
      newColumns.append(("-DOCSTART-", "-X-", "-X-", "O"))
      newColumns.append(("", "", "", ""))
      columns.foreach(a => {
        if (a._3 != sentenceId){
          newColumns.append(("", "", "", ""))
          sentenceId = a._3
        }
        newColumns.append((a._1, a._2, a._2, "O"))
      })
      newColumns
    })
  }



}
