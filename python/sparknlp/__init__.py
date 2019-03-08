import sys
from pyspark.sql import SparkSession
from sparknlp import annotator
from sparknlp.base import DocumentAssembler, Finisher, TokenAssembler, Chunk2Doc, Doc2Chunk

sys.modules['com.johnsnowlabs.nlp.annotators'] = annotator
sys.modules['com.johnsnowlabs.nlp.annotators.tokenizer'] = annotator
sys.modules['com.johnsnowlabs.nlp.annotators.tokenizer.wordpiece'] = annotator
sys.modules['com.johnsnowlabs.nlp.annotators.ner'] = annotator
sys.modules['com.johnsnowlabs.nlp.annotators.ner.regex'] = annotator
sys.modules['com.johnsnowlabs.nlp.annotators.ner.crf'] = annotator
sys.modules['com.johnsnowlabs.nlp.annotators.ner.dl'] = annotator
sys.modules['com.johnsnowlabs.nlp.annotators.pos'] = annotator
sys.modules['com.johnsnowlabs.nlp.annotators.pos.perceptron'] = annotator
sys.modules['com.johnsnowlabs.nlp.annotators.sbd'] = annotator
sys.modules['com.johnsnowlabs.nlp.annotators.sbd.pragmatic'] = annotator
sys.modules['com.johnsnowlabs.nlp.annotators.sbd.deep'] = annotator
sys.modules['com.johnsnowlabs.nlp.annotators.sda'] = annotator
sys.modules['com.johnsnowlabs.nlp.annotators.sda.pragmatic'] = annotator
sys.modules['com.johnsnowlabs.nlp.annotators.sda.vivekn'] = annotator
sys.modules['com.johnsnowlabs.nlp.annotators.spell'] = annotator
sys.modules['com.johnsnowlabs.nlp.annotators.spell.norvig'] = annotator
sys.modules['com.johnsnowlabs.nlp.annotators.spell.context'] = annotator
sys.modules['com.johnsnowlabs.nlp.annotators.embeddings'] = annotator

annotators = annotator


def session():
    return SparkSession.builder \
        .appName("spark-nlp") \
        .master("local[*]") \
        .config("spark.driver.memory", "4G") \
        .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer") \
        .config("spark.jars.packages", "JohnSnowLabs:spark-nlp:1.8.2") \
        .getOrCreate()
