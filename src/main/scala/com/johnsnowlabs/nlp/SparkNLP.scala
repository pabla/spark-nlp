package com.johnsnowlabs.nlp

import org.apache.spark.sql.SparkSession

object SparkNLP {

  val currentVersion = "2.1.0-rc3"

  def start(includeOcr: Boolean = false): SparkSession = {
    val build = SparkSession.builder()
      .appName("Spark NLP")
      .master("local[*]")
      .config("spark.driver.memory", "6G")
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")

    if (includeOcr) {
      build
        .config("spark.jars.packages", "JohnSnowLabs:spark-nlp:2.1.0-rc3,com.johnsnowlabs.nlp:spark-nlp-ocr_2.11:2.1.0-rc3,javax.media.jai:com.springsource.javax.media.jai.core:1.1.3")
        .config("spark.jars.repositories", "http://repo.spring.io/plugins-release")
    } else {
      build
        .config("spark.jars.packages", "JohnSnowLabs:spark-nlp:2.1.0-rc3")
    }

    build.getOrCreate()
  }

  def version(): Unit = {
    println(currentVersion)
  }

}
