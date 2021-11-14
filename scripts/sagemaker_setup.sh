#!/bin/bash

# Java setup on SageMaker
echo "Setup SageMaker for PySpark $PYSPARK and Spark NLP $SPARKNLP"
JAVA_8=$(alternatives --display java | grep 'jre-1.8.0-openjdk.x86_64/bin/java' | cut -d' ' -f1)
sudo alternatives --set java $JAVA_8

# Default values for pyspark, spark-nlp, and SPARK_HOME
SPARKNLP="3.3.2"
PYSPARK="3.1.2"

SPARK_FOLDER_NAME="spark-$PYSPARK-bin-hadoop2.7"
LOCAL_ROOT_DIR="/home/ec2-user/SageMaker"
SPARKHOME="$LOCAL_ROOT_DIR/$SPARK_FOLDER_NAME"
APACHE_DL_URL="https://downloads.apache.org/spark"

TARGZ_URL_TO_DOWNLOAD="$APACHE_DL_URL/spark-$PYSPARK/$SPARK_FOLDER_NAME.tgz"

echo "Beginning download of Spark"
wget -q $TARGZ_URL_TO_DOWNLOAD >/dev/null
echo "Download done, beginning extraction."
tar -xvf "$SPARK_FOLDER_NAME.tgz" -C $LOCAL_ROOT_DIR >/dev/null
echo "Spark has been downloaded and extracted, removing .tgz file now."
rm "$SPARK_FOLDER_NAME.tgz"

export SPARK_HOME=$SPARKHOME

# Install pyspark spark-nlp
! pip install --upgrade -q pyspark==$PYSPARK spark-nlp==$SPARKNLP findspark
