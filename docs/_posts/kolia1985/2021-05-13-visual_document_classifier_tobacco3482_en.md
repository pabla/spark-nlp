---
layout: model
title: Visual Document Classifier
author: John Snow Labs
name: visual_document_classifier_tobacco3482
date: 2021-05-13
tags: [en, licensed]
task: Text Classification
language: en
edition: Spark NLP 3.0.0
spark_version: 2.4
supported: true
article_header:
  type: cover
use_language_switcher: "Python-Scala-Java"
---

## Description

Visual Document Classifier based on LayoutLM

{:.btn-box}
<button class="button button-orange" disabled>Live Demo</button>
<button class="button button-orange" disabled>Open in Colab</button>
[Download](https://s3.amazonaws.com/models-hub-auxdata/clinical/ocr/visual_document_classifier_tobacco3482_en_3.0.0_2.4_1620904318850.zip){:.button.button-orange.button-orange-trans.arr.button-icon}

## How to use



<div class="tabs-box" markdown="1">
{% include programmingLanguageSelectScalaPythonNLU.html %}
```python
img_to_hocr = ImageToHocr()\
    .setInputCol("image")\
    .setOutputCol("hocr")\
    .setIgnoreResolution(False)\
    .setOcrParams(["preserve_interword_spaces=0"])


doc_classifier = VisualDocumentClassifier()\
    .pretrained(*pretrained_model)\
    .setInputCol("hocr")\
    .setLabelCol("label")\
    .setConfidenceCol("conf")

# OCR pipeline
pipeline = PipelineModel(stages=[
    binary_to_image,
    img_to_hocr,
    doc_classifier
])
```

</div>

{:.model-param}
## Model Information

{:.table-model}
|---|---|
|Model Name:|visual_document_classifier_tobacco3482|
|Type:|ocr|
|Compatibility:|Spark NLP 3.0.0+|
|License:|Licensed|
|Edition:|Official|
|Language:|en|
|Case sensitive:|false|
|Max sentense length:|512|