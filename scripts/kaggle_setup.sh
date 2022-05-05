#!/bin/bash

#default values for pyspark, spark-nlp, and SPARK_HOME
SPARKNLP="3.4.4"
PYSPARK="3.0.3"

while getopts s:p: option
do
 case "${option}"
 in
 s) SPARKNLP=${OPTARG};;
 p) PYSPARK=${OPTARG};;
 esac
done

echo "setup Kaggle for PySpark $PYSPARK and Spark NLP $SPARKNLP"
export JAVA_HOME="/usr/lib/jvm/java-11-openjdk-amd64"

if [[ "$PYSPARK" == "3.2"* ]]; then
  PYSPARK="3.2.1"
  echo "Installing PySpark $PYSPARK and Spark NLP $SPARKNLP"
  echo "Don't forget to use spark32=True inside sparknlp.start(spark32=True)"
elif [[ "$PYSPARK" == "3.1"* ]]; then
  PYSPARK="3.1.3"
  echo "Installing PySpark $PYSPARK and Spark NLP $SPARKNLP"
elif [[ "$PYSPARK" == "3.0"* ]]; then
  PYSPARK="3.0.3"
  echo "Installing PySpark $PYSPARK and Spark NLP $SPARKNLP"
elif [[ "$PYSPARK" == "2"* ]]; then
  PYSPARK="2.4.8"
  echo "Installing PySpark $PYSPARK and Spark NLP $SPARKNLP"
  echo "Don't forget to use spark24=True inside sparknlp.start(spark24=True)"
  apt-get update
  apt-get purge -y openjdk-11* -qq > /dev/null && sudo apt-get autoremove -y -qq > /dev/null
  apt-get install -y openjdk-8-jdk-headless -qq > /dev/null

  SPARKHOME="/content/spark-2.4.8-bin-hadoop2.7"
  export SPARK_HOME=$SPARKHOME
  export JAVA_HOME="/usr/lib/jvm/java-8-openjdk-amd64"

  wget -q "https://downloads.apache.org/spark/spark-2.4.8/spark-2.4.8-bin-hadoop2.7.tgz" > /dev/null
  tar -xvf spark-2.4.8-bin-hadoop2.8.tgz > /dev/null

else
  PYSPARK="3.0.3"
  echo "Installing PySpark $PYSPARK and Spark NLP $SPARKNLP"
fi

# Install pyspark spark-nlp
! pip install --upgrade -q pyspark==$PYSPARK spark-nlp==$SPARKNLP findspark
