---
layout: model
title: sdsdas
author: John Snow Labs
name: ner_TestAnalyticsPage2
date: 2023-10-16
tags: [en, open_source, tensorflow]
task: Named Entity Recognition
language: en
edition: Spark NLP 5.1.0
spark_version: 3.2
supported: true
engine: tensorflow
annotator: NerDLModel
article_header:
  type: cover
use_language_switcher: "Python-Scala-Java"
---

## Description

dfsfdsf

## Predicted Entities

`Pathogen`, `MedicalCondition`, `Medicine`

{:.btn-box}
<button class="button button-orange" disabled>Live Demo</button>
<button class="button button-orange" disabled>Open in Colab</button>
[Download](https://s3.amazonaws.com/models-hub-auxdata/public/models/ner_TestAnalyticsPage2_en_5.1.0_3.2_1697490820871.zip){:.button.button-orange.button-orange-trans.arr.button-icon}
[Copy S3 URI](s3://models-hub-auxdata/public/models/ner_TestAnalyticsPage2_en_5.1.0_3.2_1697490820871.zip){:.button.button-orange.button-orange-trans.button-icon.button-copy-s3}

## How to use



<div class="tabs-box" markdown="1">
{% include programmingLanguageSelectScalaPythonNLU.html %}
```python
document_assembler = DocumentAssembler()
			.setInputCol("text")
			.setOutputCol("document")

sentence_detector = SentenceDetector()
			.setInputCols(["document"])
			.setOutputCol("sentence")
			.setCustomBounds([""])

tokenizer = Tokenizer()
		.setInputCols(["sentence"])
		.setOutputCol(\"token\")
		.setSplitChars(['-'])"

word_embeddings = WordEmbeddingsModel()
			.pretrained("glove_100d", "en", "clinical/models")
			.setInputCols(["sentence", "token"])
			.setOutputCol("embeddings")

ner = NerDLModel().pretrained("ner_TestAnalyticsPage2", "en","clinical/models")
		.setInputCols(["sentence", "token", "embeddings"])
		.setOutputCol("ner")

ner_converter = NerConverter()
			.setInputCols(["sentence", "token", "ner"])
			.setOutputCol("ner_chunk")

pipeline = Pipeline(stages=[document_assembler,
			    sentence_detector,
			    tokenizer,
			    word_embeddings,
			    ner,
			    ner_converter])

data = spark.createDataFrame([["All medical applications known so far involve not pure adamantane, but its derivatives. The first adamantane derivative used as a drug was amantadine – first (1967) as an antiviral drug against various strains of flu[50] and then to treat Parkinson's disease.[51][52] Other drugs among adamantane derivatives include adapalene, adapromine, bromantane, carmantadine, chlodantane, dopamantine, memantine, rimantadine, saxagliptin, tromantadine, and vildagliptin. Polymers of adamantane have been patented as antiviral agents against HIV.[53]"]]).toDF("text")
result = pipeline.fit(data).transform(data)
```

</div>

## Results

```bash
         chunk ner_label
0   amantadine  Medicine
1  dopamantine  Medicine
2    memantine  Medicine
3  rimantadine  Medicine
4  saxagliptin  Medicine
```

{:.model-param}
## Model Information

{:.table-model}
|---|---|
|Model Name:|ner_TestAnalyticsPage2|
|Type:|ner|
|Compatibility:|Spark NLP 5.1.0+|
|License:|Open Source|
|Edition:|Official|
|Input Labels:|[sentence, token, embeddings]|
|Output Labels:|[ner]|
|Language:|en|
|Size:|2.6 MB|
|Dependencies:|glove_100d|