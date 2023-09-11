---
layout: model
title: This is the title
author: John Snow Labs
name: ner_NER_TEST_TRAINING
date: 2023-09-11
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

This is description

## Predicted Entities

`MedicalCondition`, `Pathogen`, `Medicine`

{:.btn-box}
[Live Demo](https://annotationlab.johnsnowlabs.com/){:.button.button-orange}
[Open in Colab](https://annotationlab.johnsnowlabs.com/){:.button.button-orange.button-orange-trans.co.button-icon}
[Download](https://s3.amazonaws.com/models-hub-auxdata/public/models/ner_NER_TEST_TRAINING_es_4.3.1_3.2_1694460456199.zip){:.button.button-orange.button-orange-trans.arr.button-icon}
[Copy S3 URI](s3://models-hub-auxdata/public/models/ner_NER_TEST_TRAINING_es_4.3.1_3.2_1694460456199.zip){:.button.button-orange.button-orange-trans.button-icon.button-copy-s3}

## How to use

This is how to use

<div class="tabs-box" markdown="1">
{% include programmingLanguageSelectScalaPythonNLU.html %}
```python
This is Python Code
```
```scala
This is scala Code
```

{:.nlu-block}
```python
This is NLU Code
```
</div>

## Results

```bash
This is Results
```

{:.model-param}
## Model Information

{:.table-model}
|---|---|
|Model Name:|ner_NER_TEST_TRAINING|
|Type:|ner|
|Compatibility:|Spark NLP 4.3.1+|
|License:|Open Source|
|Edition:|Official|
|Input Labels:|[sentence, token, embeddings]|
|Output Labels:|[ner]|
|Language:|es|
|Size:|2.5 MB|
|Dependencies:|glove_100d|

## References

This is Dataset used for training

## Benchmarking

```bash
This is Benchmarking
```