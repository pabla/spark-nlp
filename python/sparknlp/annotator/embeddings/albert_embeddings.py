#  Copyright 2017-2022 John Snow Labs
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

from sparknlp.common import *

class AlbertEmbeddings(AnnotatorModel,
                       HasEmbeddingsProperties,
                       HasCaseSensitiveProperties,
                       HasStorageRef,
                       HasBatchedAnnotate):
    """ALBERT: A Lite Bert For Self-Supervised Learning Of Language
    Representations - Google Research, Toyota Technological Institute at Chicago

    These word embeddings represent the outputs generated by the Albert model.
    All official Albert releases by google in TF-HUB are supported with this
    Albert Wrapper:

    **Ported TF-Hub Models:**

    ============================ ============================================================== =====================================================
    Model Name                   TF-Hub Model                                                   Model Properties
    ============================ ============================================================== =====================================================
    ``"albert_base_uncased"``    `albert_base <https://tfhub.dev/google/albert_base/3>`__       768-embed-dim,   12-layer,  12-heads, 12M parameters
    ``"albert_large_uncased"``   `albert_large <https://tfhub.dev/google/albert_large/3>`__     1024-embed-dim,  24-layer,  16-heads, 18M parameters
    ``"albert_xlarge_uncased"``  `albert_xlarge <https://tfhub.dev/google/albert_xlarge/3>`__   2048-embed-dim,  24-layer,  32-heads, 60M parameters
    ``"albert_xxlarge_uncased"`` `albert_xxlarge <https://tfhub.dev/google/albert_xxlarge/3>`__ 4096-embed-dim,  12-layer,  64-heads, 235M parameters
    ============================ ============================================================== =====================================================

    This model requires input tokenization with SentencePiece model, which is
    provided by Spark-NLP (See tokenizers package).

    Pretrained models can be loaded with :meth:`.pretrained` of the companion
    object:

    >>> embeddings = AlbertEmbeddings.pretrained() \\
    ...    .setInputCols(["sentence", "token"]) \\
    ...    .setOutputCol("embeddings")


    The default model is ``"albert_base_uncased"``, if no name is provided.

    For extended examples of usage, see the `Spark NLP Workshop
    <https://github.com/JohnSnowLabs/spark-nlp-workshop/blob/master/jupyter/training/english/dl-ner/ner_albert.ipynb>`__.
    To see which models are compatible and how to import them see
    `Import Transformers into Spark NLP 🚀
    <https://github.com/JohnSnowLabs/spark-nlp/discussions/5669>`_.

    ====================== ======================
    Input Annotation types Output Annotation type
    ====================== ======================
    ``DOCUMENT, TOKEN``    ``WORD_EMBEDDINGS``
    ====================== ======================

    Parameters
    ----------
    batchSize
        Size of every batch, by default 8
    dimension
        Number of embedding dimensions, by default 768
    caseSensitive
        Whether to ignore case in tokens for embeddings matching, by default
        False
    configProtoBytes
        ConfigProto from tensorflow, serialized into byte array.
    maxSentenceLength
        Max sentence length to process, by default 128

    Notes
    -----
    ALBERT uses repeating layers which results in a small memory footprint,
    however the computational cost remains similar to a BERT-like architecture
    with the same number of hidden layers as it has to iterate through the same
    number of (repeating) layers.

    References
    ----------
    `ALBERT: A LITE BERT FOR SELF-SUPERVISED LEARNING OF LANGUAGE REPRESENTATIONS <https://arxiv.org/pdf/1909.11942.pdf>`__

    https://github.com/google-research/ALBERT

    https://tfhub.dev/s?q=albert

    **Paper abstract:**

    *Increasing model size when pretraining natural language representations
    often results in improved performance on downstream tasks. However, at some
    point further model increases become harder due to GPU/TPU memory
    limitations and longer training times. To address these problems, we present
    two parameter reduction techniques to lower memory consumption and increase
    the training speed of BERT (Devlin et al., 2019). Comprehensive empirical
    evidence shows that our proposed methods lead to models that scale much
    better compared to the original BERT. We also use a self-supervised loss
    that focuses on modeling inter-sentence coherence, and show it consistently
    helps downstream tasks with multi-sentence inputs. As a result, our best
    model establishes new state-of-the-art results on the GLUE, RACE, and SQuAD
    benchmarks while having fewer parameters compared to BERT-large.*

    Examples
    --------
    >>> import sparknlp
    >>> from sparknlp.base import *
    >>> from sparknlp.annotator import *
    >>> from pyspark.ml import Pipeline
    >>> documentAssembler = DocumentAssembler() \\
    ...     .setInputCol("text") \\
    ...     .setOutputCol("document")
    >>> tokenizer = Tokenizer() \\
    ...     .setInputCols(["document"]) \\
    >>> embeddings = AlbertEmbeddings.pretrained() \\
    ...     .setInputCols(["token", "document"]) \\
    ...     .setOutputCol("embeddings")
    >>> embeddingsFinisher = EmbeddingsFinisher() \\
    ...     .setInputCols(["embeddings"]) \\
    ...     .setOutputCols("finished_embeddings") \\
    ...     .setOutputAsVector(True) \\
    ...     .setCleanAnnotations(False)
    >>> pipeline = Pipeline().setStages([
    ...     documentAssembler,
    ...     tokenizer,
    ...     embeddings,
    ...     embeddingsFinisher
    ... ])
    >>> data = spark.createDataFrame([["This is a sentence."]]).toDF("text")
    >>> result = pipeline.fit(data).transform(data)
    >>> result.selectExpr("explode(finished_embeddings) as result").show(5, 80)
    +--------------------------------------------------------------------------------+
    |                                                                          result|
    +--------------------------------------------------------------------------------+
    |[1.1342473030090332,-1.3855540752410889,0.9818322062492371,-0.784737348556518...|
    |[0.847029983997345,-1.047153353691101,-0.1520637571811676,-0.6245765686035156...|
    |[-0.009860038757324219,-0.13450059294700623,2.707749128341675,1.2916892766952...|
    |[-0.04192575812339783,-0.5764210224151611,-0.3196685314178467,-0.527840495109...|
    |[0.15583214163780212,-0.1614152491092682,-0.28423872590065,-0.135491415858268...|
    +--------------------------------------------------------------------------------+

    See Also
    --------
    AlbertForTokenClassification : for  AlbertEmbeddings with a token classification layer on top
    """

    name = "AlbertEmbeddings"

    configProtoBytes = Param(Params._dummy(),
                             "configProtoBytes",
                             "ConfigProto from tensorflow, serialized into byte array. Get with config_proto.SerializeToString()",
                             TypeConverters.toListInt)

    maxSentenceLength = Param(Params._dummy(),
                              "maxSentenceLength",
                              "Max sentence length to process",
                              typeConverter=TypeConverters.toInt)

    def setConfigProtoBytes(self, b):
        """Sets configProto from tensorflow, serialized into byte array.

        Parameters
        ----------
        b : List[int]
            ConfigProto from tensorflow, serialized into byte array
        """
        return self._set(configProtoBytes=b)

    def setMaxSentenceLength(self, value):
        """Sets max sentence length to process.

        Parameters
        ----------
        value : int
            Max sentence length to process
        """
        return self._set(maxSentenceLength=value)

    @keyword_only
    def __init__(self, classname="com.johnsnowlabs.nlp.embeddings.AlbertEmbeddings", java_model=None):
        super(AlbertEmbeddings, self).__init__(
            classname=classname,
            java_model=java_model
        )
        self._setDefault(
            batchSize=8,
            dimension=768,
            maxSentenceLength=128,
            caseSensitive=False
        )

    @staticmethod
    def loadSavedModel(folder, spark_session):
        """Loads a locally saved model.

        Parameters
        ----------
        folder : str
            Folder of the saved model
        spark_session : pyspark.sql.SparkSession
            The current SparkSession

        Returns
        -------
        AlbertEmbeddings
            The restored model
        """
        from sparknlp.internal import _AlbertLoader
        jModel = _AlbertLoader(folder, spark_session._jsparkSession)._java_obj
        return AlbertEmbeddings(java_model=jModel)

    @staticmethod
    def pretrained(name="albert_base_uncased", lang="en", remote_loc=None):
        """Downloads and loads a pretrained model.

        Parameters
        ----------
        name : str, optional
            Name of the pretrained model, by default "albert_base_uncased"
        lang : str, optional
            Language of the pretrained model, by default "en"
        remote_loc : str, optional
            Optional remote address of the resource, by default None. Will use
            Spark NLPs repositories otherwise.

        Returns
        -------
        AlbertEmbeddings
            The restored model
        """
        from sparknlp.pretrained import ResourceDownloader
        return ResourceDownloader.downloadModel(AlbertEmbeddings, name, lang, remote_loc)

