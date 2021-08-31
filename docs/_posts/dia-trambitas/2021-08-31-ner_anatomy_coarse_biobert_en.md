---
layout: model
title: Predicts Anatomy Label
author: dia-trambitas
name: ner_anatomy_coarse_biobert
date: 2021-08-31
tags: [en, open_source]
task: Named Entity Recognition
language: en
edition: Spark NLP 2.6.1
spark_version: 2.4
supported: false
article_header:
  type: cover
use_language_switcher: "Python-Scala-Java"
---

## Description

An NER model to extract all types of anatomical references in text using “biobert_pubmed_base_cased” embeddings. It is a single entity model and generalizes all anatomical references to a single entity.

## Predicted Entities

`Anatomy`

{:.btn-box}
<button class="button button-orange" disabled>Live Demo</button>
<button class="button button-orange" disabled>Open in Colab</button>
[Download](https://s3.amazonaws.com/models-hub-community/dia-trambitas/ner_anatomy_coarse_biobert_en_2.6.1_2.4_1630439798075.zip){:.button.button-orange.button-orange-trans.arr.button-icon}

## How to use



<div class="tabs-box" markdown="1">
{% include programmingLanguageSelectScalaPythonNLU.html %}
```python
embeddings = BertEmbeddings.pretrained("biobert_pubmed_base_cased", "en") \
      .setInputCols("sentence", "token") \
      .setOutputCol("embeddings")
clinical_ner = MedicalNerModel.pretrained("ner_anatomy_coarse_biobert", "en", "clinical/models") \
  .setInputCols(["sentence", "token", "embeddings"]) \
  .setOutputCol("ner")
...
nlp_pipeline = Pipeline(stages=[document_assembler, sentence_detector, tokenizer, embeddings, clinical_ner, ner_converter])
model = nlpPipeline.fit(spark.createDataFrame([["content in the lung tissue"]]).toDF("text"))
results = model.transform(data)
```

</div>

## Results

```bash
|    | ner_chunk         | entity    |
|---:|:------------------|:----------|
|  0 | lung tissue       | Anatomy   |
```

{:.model-param}
## Model Information

{:.table-model}
|---|---|
|Model Name:|ner_anatomy_coarse_biobert|
|Type:|ner|
|Compatibility:|Spark NLP 2.6.1+|
|License:|Open Source|
|Edition:|Community|
|Input Labels:|[sentence, token, embeddings]|
|Output Labels:|[ner]|
|Language:|en|
|Dependencies:|biobert_pubmed_base_cased|

## Data Source

Trained on a custom dataset using ‘biobert_pubmed_base_cased’.

## Benchmarking

```bash
|    | label         |    tp |    fp |    fn |     prec |      rec |       f1 |
|---:|--------------:|------:|------:|------:|---------:|---------:|---------:|
|  0 | B-Anatomy     |  2499 |   155 |   162 | 0.941598 | 0.939121 | 0.940357 |
|  1 | I-Anatomy     |  1695 |   116 |   158 | 0.935947 | 0.914733 | 0.925218 |
|  2 | Macro-average | 4194  |  271  |   320 | 0.938772 | 0.926927 | 0.932812 |
|  3 | Micro-average | 4194  |  271  |   320 | 0.939306 | 0.929109 | 0.93418  |
```