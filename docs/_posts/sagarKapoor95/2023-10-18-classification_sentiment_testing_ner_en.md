---
layout: model
title: sndsnjdns
author: John Snow Labs
name: classification_sentiment_testing_ner
date: 2023-10-18
tags: [en, open_source, tensorflow]
task: Text Classification
language: en
edition: Spark NLP 5.1.0
spark_version: 3.2
supported: true
engine: tensorflow
annotator: MultiClassifierDLModel
article_header:
  type: cover
use_language_switcher: "Python-Scala-Java"
---

## Description

djsadnjkasnd

## Predicted Entities

`Sport`, `Politics`, `Business`

{:.btn-box}
<button class="button button-orange" disabled>Live Demo</button>
<button class="button button-orange" disabled>Open in Colab</button>
[Download](https://s3.amazonaws.com/models-hub-auxdata/public/models/classification_sentiment_testing_ner_en_5.1.0_3.2_1697656598804.zip){:.button.button-orange.button-orange-trans.arr.button-icon}
[Copy S3 URI](s3://models-hub-auxdata/public/models/classification_sentiment_testing_ner_en_5.1.0_3.2_1697656598804.zip){:.button.button-orange.button-orange-trans.button-icon.button-copy-s3}

## How to use



<div class="tabs-box" markdown="1">
{% include programmingLanguageSelectScalaPythonNLU.html %}
```python
document_assembler = DocumentAssembler()
                        .setInputCol("text")
                        .setOutputCol("document")
                        
embeddings = WordEmbeddingsModel().pretrained(glove_100d)
                .setInputCols(["document", 'token'])
                .setOutputCol("word_embeddings")


sentence_embeddings = SentenceEmbeddings()
                        .setInputCols(["document", "word_embeddings"]) 
                        .setOutputCol("sentence_embeddings") 
                        .setPoolingStrategy("AVERAGE")

tokenizer = Tokenizer() 
                    .setInputCols(["sentence"]) 
                    .setOutputCol("token")
                    .setSplitChars(['-'])


classifier = MultiClassifierDLModel().pretrained('classification_sentiment_testing_ner', 'en', 'clinical/models')
                .setInputCols(['document', 'token', 'sentence_embeddings'])
                .setOutputCol('class')

nlp_pipeline = Pipeline(stages=[document_assembler,
                                tokenizer,
embeddings,
                                sentence_embeddings,
classifier])

light_pipeline = LightPipeline(nlp_pipeline.fit(spark.createDataFrame([['']]).toDF(\"text\")))

annotations = light_pipeline.fullAnnotate(["SAMPLE TEXT"])
```

</div>

## Results

```bash
dsadasdasd
```

{:.model-param}
## Model Information

{:.table-model}
|---|---|
|Model Name:|classification_sentiment_testing_ner|
|Compatibility:|Spark NLP 5.1.0+|
|License:|Open Source|
|Edition:|Official|
|Input Labels:|[sentence_embeddings]|
|Output Labels:|[class]|
|Language:|en|
|Size:|87.7 MB|
|Dependencies:|glove_100d|