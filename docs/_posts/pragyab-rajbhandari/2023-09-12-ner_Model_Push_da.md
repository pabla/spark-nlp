---
layout: model
title: This should be the title
author: John Snow Labs
name: ner_Model_Push
date: 2023-09-12
tags: [tag1, tag2, da, open_source, tensorflow]
task: Named Entity Recognition
language: da
edition: Spark NLP 4.0.0
spark_version: 3.0
supported: true
engine: tensorflow
annotator: NerDLModel
article_header:
  type: cover
use_language_switcher: "Python-Scala-Java"
---

## Description

This is the Description

## Predicted Entities

`Medicine`, `MedicalCondition`, `Pathogen`, `Edited`

{:.btn-box}
<button class="button button-orange" disabled>Live Demo</button>
<button class="button button-orange" disabled>Open in Colab</button>
[Download](https://s3.amazonaws.com/models-hub-auxdata/public/models/ner_Model_Push_da_4.0.0_3.0_1694542049441.zip){:.button.button-orange.button-orange-trans.arr.button-icon}
[Copy S3 URI](s3://models-hub-auxdata/public/models/ner_Model_Push_da_4.0.0_3.0_1694542049441.zip){:.button.button-orange.button-orange-trans.button-icon.button-copy-s3}

## How to use



<div class="tabs-box" markdown="1">
{% include programmingLanguageSelectScalaPythonNLU.html %}
```python
# Unpack the artifact file
rm -r artifacts/
tar xofp annotationlab-${version}.tar.gz
cd artifacts/

#unpack charts and change theme in values.yaml
tar xofp annotationlab-${version}.tgz
sed -i "s/johnsnowlabs\/annotationlab:auth-theme-${version}/registry.johnsnowlabs.com\/nlp-lab\/annotationlab:auth-theme-${version}/g" annotationlab/values.yaml
helm package annotationlab
rm -r annotationlab

```

</div>

{:.model-param}
## Model Information

{:.table-model}
|---|---|
|Model Name:|ner_Model_Push|
|Type:|ner|
|Compatibility:|Spark NLP 4.0.0+|
|License:|Open Source|
|Edition:|Official|
|Input Labels:|[sentence, token, embeddings]|
|Output Labels:|[ner]|
|Language:|da|
|Size:|2.5 MB|
|Dependencies:|glove_100dedited|