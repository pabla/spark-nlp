---
layout: model
title: test
author: John Snow Labs
name: ner_TEST_PROJECT_550
date: 2023-09-24
tags: [abc, es, open_source, tensorflow]
task: Named Entity Recognition
language: es
edition: Spark NLP 5.0.2
spark_version: 3.2
supported: true
engine: tensorflow
annotator: NerDLModel
article_header:
  type: cover
use_language_switcher: "Python-Scala-Java"
---

## Description

test

## Predicted Entities

`MedicalCondition`, `Medicine`, `Pathogen`

{:.btn-box}
<button class="button button-orange" disabled>Live Demo</button>
<button class="button button-orange" disabled>Open in Colab</button>
[Download](https://s3.amazonaws.com/models-hub-auxdata/public/models/ner_TEST_PROJECT_550_es_5.0.2_3.2_1695534068408.zip){:.button.button-orange.button-orange-trans.arr.button-icon}
[Copy S3 URI](s3://models-hub-auxdata/public/models/ner_TEST_PROJECT_550_es_5.0.2_3.2_1695534068408.zip){:.button.button-orange.button-orange-trans.button-icon.button-copy-s3}

## How to use



<div class="tabs-box" markdown="1">
{% include programmingLanguageSelectScalaPythonNLU.html %}
```python
print("test")
```

</div>

{:.model-param}
## Model Information

{:.table-model}
|---|---|
|Model Name:|ner_TEST_PROJECT_550|
|Type:|ner|
|Compatibility:|Spark NLP 5.0.2+|
|License:|Open Source|
|Edition:|Official|
|Input Labels:|[sentence, token, embeddings]|
|Output Labels:|[ner]|
|Language:|es|
|Size:|2.5 MB|
|Dependencies:|glove_100d|