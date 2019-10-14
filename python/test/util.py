from pyspark.sql import SparkSession

import os


class SparkContextForTest:
    spark = SparkSession.builder \
        .master("local[4]") \
        .config("spark.jars", 'lib/sparknlp.jar,lib/sparknlp-ocr.jar') \
        .config("spark.driver.memory", "6500M") \
        .getOrCreate()
    spark.sparkContext.setLogLevel("WARN")
    data = spark. \
        read \
        .parquet("file:///" + os.getcwd() + "/../src/test/resources/sentiment.parquet") \
        .limit(100)
    data.cache()
    data.count()


class SparkSessionWithoutOCRForTest:
    spark = SparkSession.builder \
        .master("local[4]") \
        .config("spark.driver.memory", "6500M") \
        .config("spark.jars", 'lib/sparknlp.jar') \
        .getOrCreate()
    spark.sparkContext.setLogLevel("WARN")


class SparkSessionWithOCRForTest:
    spark = SparkSession.builder \
        .master("local[4]") \
        .config("spark.driver.memory", "6500M") \
        .config("spark.jars", 'lib/sparknlp.jar,lib/sparknlp-ocr.jar') \
        .getOrCreate()
    spark.sparkContext.setLogLevel("WARN")
