---
layout: model
title: ssss
author: John Snow Labs
name: ner_once_testing
date: 2023-09-17
tags: [es, open_source, tensorflow]
task: Named Entity Recognition
language: es
edition: Spark NLP 4.3.1
spark_version: 3.2
supported: true
engine: tensorflow
annotator: NerDLModel
article_header:
  type: cover
use_language_switcher: "Python-Scala-Java"
---

## Description

ssss

## Predicted Entities

`MONEY`

{:.btn-box}
[Live Demo](https://johnsnowlabs.atlassian.net/browse/ALAB-4249){:.button.button-orange}
[Open in Colab](https://johnsnowlabs.atlassian.net/browse/ALAB-4249){:.button.button-orange.button-orange-trans.co.button-icon}
[Download](https://s3.amazonaws.com/models-hub-auxdata/public/models/ner_once_testing_es_4.3.1_3.2_1694991088222.zip){:.button.button-orange.button-orange-trans.arr.button-icon}
[Copy S3 URI](s3://models-hub-auxdata/public/models/ner_once_testing_es_4.3.1_3.2_1694991088222.zip){:.button.button-orange.button-orange-trans.button-icon.button-copy-s3}

## How to use



<div class="tabs-box" markdown="1">
{% include programmingLanguageSelectScalaPythonNLU.html %}
```python
ssss
```

</div>

{:.model-param}
## Model Information

{:.table-model}
|---|---|
|Model Name:|ner_once_testing|
|Type:|ner|
|Compatibility:|Spark NLP 4.3.1+|
|License:|Open Source|
|Edition:|Official|
|Input Labels:|[sentence, token, embeddings]|
|Output Labels:|[ner]|
|Language:|es|
|Size:|2.5 MB|
|Dependencies:|glove_100d|