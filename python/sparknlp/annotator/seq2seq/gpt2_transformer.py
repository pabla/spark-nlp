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
"""Contains classes for the GPT2Transformer."""

from sparknlp.common import *


class GPT2Transformer(AnnotatorModel, HasBatchedAnnotate, HasEngine):
    """GPT2: the OpenAI Text-To-Text Transformer

    GPT-2 is a large transformer-based language model with 1.5 billion parameters, trained on a dataset of 8 million
    web pages. GPT-2 is trained with a simple objective: predict the next word, given all of the previous words within
    some text. The diversity of the dataset causes this simple goal to contain naturally occurring demonstrations of
    many tasks across diverse domains. GPT-2 is a direct scale-up of GPT, with more than 10X the parameters and trained
    on more than 10X the amount of data.

    GPT-2 displays a broad set of capabilities, including the ability to generate conditional synthetic text samples of
    unprecedented quality, where we prime the model with an input and have it generate a lengthy continuation. In
    addition, GPT-2 outperforms other language models trained on specific domains (like Wikipedia, news, or books)
    without needing to use these domain-specific training datasets. On language tasks like question answering, reading
    comprehension, summarization, and translation, GPT-2 begins to learn these tasks from the raw text, using no
    task-specific training data. While scores on these downstream tasks are far from state-of-the-art, they suggest
    that the tasks can benefit from unsupervised techniques, given sufficient (unlabeled) data and compute.

    Pretrained models can be loaded with :meth:`.pretrained` of the companion
    object:

    >>> gpt2 = GPT2Transformer.pretrained() \\
    ...     .setInputCols(["document"]) \\
    ...     .setOutputCol("generation")


    The default model is ``"gpt2"``, if no name is provided. For available
    pretrained models please see the `Models Hub
    <https://nlp.johnsnowlabs.com/models?q=gpt2>`__.

    ====================== ======================
    Input Annotation types Output Annotation type
    ====================== ======================
    ``DOCUMENT``           ``DOCUMENT``
    ====================== ======================

    Parameters
    ----------
    task
        Transformer's task, e.g. ``summarize:`` , by default ""
    configProtoBytes
        ConfigProto from tensorflow, serialized into byte array.
    minOutputLength
        Minimum length of the sequence to be generated, by default 0
    maxOutputLength
        Maximum length of output text, by default 20
    doSample
        Whether or not to use sampling; use greedy decoding otherwise, by default False
    temperature
        The value used to module the next token probabilities, by default 1.0
    topK
        The number of highest probability vocabulary tokens to keep for
        top-k-filtering, by default 50
    topP
        Top cumulative probability for vocabulary tokens, by default 1.0

        If set to float < 1, only the most probable tokens with probabilities
        that add up to ``topP`` or higher are kept for generation.
    repetitionPenalty
        The parameter for repetition penalty, 1.0 means no penalty. , by default
        1.0
    noRepeatNgramSize
        If set to int > 0, all ngrams of that size can only occur once, by
        default 0
    ignoreTokenIds
        A list of token ids which are ignored in the decoder's output, by
        default []

    Notes
    -----
    This is a very computationally expensive module especially on larger
    sequence. The use of an accelerator such as GPU is recommended.

    References
    ----------
    - `Language Models are Unsupervised Multitask Learners
      <https://d4mucfpksywv.cloudfront.net/better-language-models/language_models_are_unsupervised_multitask_learners.pdf>`__
    - https://github.com/openai/gpt-2

    **Paper Abstract:**

    *Natural language processing tasks, such as question answering, machine translation, reading comprehension, and
    summarization, are typically approached with supervised learning on taskspecific datasets. We demonstrate that
    language models begin to learn these tasks without any explicit supervision when trained on a new dataset
    of millions of webpages called WebText. When conditioned on a document plus questions, the answers generated by
    the language model reach F1 on the CoQA dataset - matching or exceeding the performance of 3 out of 4 baseline
    systems without using the 127,000+ training examples. The capacity of the language model is essential to the
    success of zero-shot task transfer and increasing it improves performance in a log-linear fashion across tasks.
    Our largest model, GPT-2, is a 1.5B parameter Transformer that achieves state of the art results on 7 out of 8
    tested language modeling datasets in a zero-shot setting but still underfits WebText. Samples from the model
    reflect these improvements and contain coherent paragraphs of text. These findings suggest a promising path
    towards building language processing systems which learn to perform tasks from their naturally occurring
    demonstrations.*

    Examples
    --------
    >>> import sparknlp
    >>> from sparknlp.base import *
    >>> from sparknlp.annotator import *
    >>> from pyspark.ml import Pipeline
    >>> documentAssembler = DocumentAssembler() \\
    ...     .setInputCol("text") \\
    ...     .setOutputCol("documents")
    >>> gpt2 = GPT2Transformer.pretrained("gpt2") \\
    ...     .setInputCols(["documents"]) \\
    ...     .setMaxOutputLength(50) \\
    ...     .setOutputCol("generation")
    >>> pipeline = Pipeline().setStages([documentAssembler, gpt2])
    >>> data = spark.createDataFrame([["My name is Leonardo."]]).toDF("text")
    >>> result = pipeline.fit(data).transform(data)
    >>> result.select("summaries.generation").show(truncate=False)
    +----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
    |result                                                                                                                                                                                              |
    +----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
    |[My name is Leonardo. I am a man of letters. I have been a man for many years. I was born in the year 1776. I came to the United States in 1776, and I have lived in the United Kingdom since 1776.]|
    -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
    """

    name = "GPT2Transformer"

    inputAnnotatorTypes = [AnnotatorType.DOCUMENT]

    task = Param(Params._dummy(), "task", "Transformer's task, e.g. 'is it true that'>",
                 typeConverter=TypeConverters.toString)

    configProtoBytes = Param(Params._dummy(),
                             "configProtoBytes",
                             "ConfigProto from tensorflow, serialized into byte array. Get with config_proto.SerializeToString()",
                             TypeConverters.toListInt)

    minOutputLength = Param(Params._dummy(), "minOutputLength", "Minimum length of the sequence to be generated",
                            typeConverter=TypeConverters.toInt)

    maxOutputLength = Param(Params._dummy(), "maxOutputLength", "Maximum length of output text",
                            typeConverter=TypeConverters.toInt)

    doSample = Param(Params._dummy(), "doSample", "Whether or not to use sampling; use greedy decoding otherwise",
                     typeConverter=TypeConverters.toBoolean)

    temperature = Param(Params._dummy(), "temperature", "The value used to module the next token probabilities",
                        typeConverter=TypeConverters.toFloat)

    topK = Param(Params._dummy(), "topK",
                 "The number of highest probability vocabulary tokens to keep for top-k-filtering",
                 typeConverter=TypeConverters.toInt)

    topP = Param(Params._dummy(), "topP",
                 "If set to float < 1, only the most probable tokens with probabilities that add up to ``top_p`` or higher are kept for generation",
                 typeConverter=TypeConverters.toFloat)

    repetitionPenalty = Param(Params._dummy(), "repetitionPenalty",
                              "The parameter for repetition penalty. 1.0 means no penalty. See `this paper <https://arxiv.org/pdf/1909.05858.pdf>`__ for more details",
                              typeConverter=TypeConverters.toFloat)

    noRepeatNgramSize = Param(Params._dummy(), "noRepeatNgramSize",
                              "If set to int > 0, all ngrams of that size can only occur once",
                              typeConverter=TypeConverters.toInt)

    ignoreTokenIds = Param(Params._dummy(), "ignoreTokenIds",
                           "A list of token ids which are ignored in the decoder's output",
                           typeConverter=TypeConverters.toListInt)

    def setTask(self, value):
        """Sets the transformer's task, e.g. ``summarize:``.

        Parameters
        ----------
        value : str
            The transformer's task
        """
        return self._set(task=value)

    def setIgnoreTokenIds(self, value):
        """A list of token ids which are ignored in the decoder's output.

        Parameters
        ----------
        value : List[int]
            The words to be filtered out
        """
        return self._set(ignoreTokenIds=value)

    def setConfigProtoBytes(self, b):
        """Sets configProto from tensorflow, serialized into byte array.

        Parameters
        ----------
        b : List[int]
            ConfigProto from tensorflow, serialized into byte array
        """
        return self._set(configProtoBytes=b)

    def setMinOutputLength(self, value):
        """Sets minimum length of the sequence to be generated.

        Parameters
        ----------
        value : int
            Minimum length of the sequence to be generated
        """
        return self._set(minOutputLength=value)

    def setMaxOutputLength(self, value):
        """Sets maximum length of output text.

        Parameters
        ----------
        value : int
            Maximum length of output text
        """
        return self._set(maxOutputLength=value)

    def setDoSample(self, value):
        """Sets whether or not to use sampling, use greedy decoding otherwise.

        Parameters
        ----------
        value : bool
            Whether or not to use sampling; use greedy decoding otherwise
        """
        return self._set(doSample=value)

    def setTemperature(self, value):
        """Sets the value used to module the next token probabilities.

        Parameters
        ----------
        value : float
            The value used to module the next token probabilities
        """
        return self._set(temperature=value)

    def setTopK(self, value):
        """Sets the number of highest probability vocabulary tokens to keep for
        top-k-filtering.

        Parameters
        ----------
        value : int
            Number of highest probability vocabulary tokens to keep
        """
        return self._set(topK=value)

    def setTopP(self, value):
        """Sets the top cumulative probability for vocabulary tokens.

        If set to float < 1, only the most probable tokens with probabilities
        that add up to ``topP`` or higher are kept for generation.

        Parameters
        ----------
        value : float
            Cumulative probability for vocabulary tokens
        """
        return self._set(topP=value)

    def setRepetitionPenalty(self, value):
        """Sets the parameter for repetition penalty. 1.0 means no penalty.

        Parameters
        ----------
        value : float
            The repetition penalty

        References
        ----------
        See `Ctrl: A Conditional Transformer Language Model For Controllable
        Generation <https://arxiv.org/pdf/1909.05858.pdf>`__ for more details.
        """
        return self._set(repetitionPenalty=value)

    def setNoRepeatNgramSize(self, value):
        """Sets size of n-grams that can only occur once.

        If set to int > 0, all ngrams of that size can only occur once.

        Parameters
        ----------
        value : int
            N-gram size can only occur once
        """
        return self._set(noRepeatNgramSize=value)

    @keyword_only
    def __init__(self, classname="com.johnsnowlabs.nlp.annotators.seq2seq.GPT2Transformer", java_model=None):
        super(GPT2Transformer, self).__init__(
            classname=classname,
            java_model=java_model
        )
        self._setDefault(
            task="",
            minOutputLength=0,
            maxOutputLength=20,
            doSample=False,
            temperature=1.0,
            topK=50,
            topP=1.0,
            repetitionPenalty=1.0,
            noRepeatNgramSize=0,
            ignoreTokenIds=[],
            batchSize=4
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
        GPT2Transformer
            The restored model
        """
        from sparknlp.internal import _GPT2Loader
        jModel = _GPT2Loader(folder, spark_session._jsparkSession)._java_obj
        return GPT2Transformer(java_model=jModel)

    @staticmethod
    def pretrained(name="gpt2", lang="en", remote_loc=None):
        """Downloads and loads a pretrained model.

        Parameters
        ----------
        name : str, optional
            Name of the pretrained model, by default "gpt2"
        lang : str, optional
            Language of the pretrained model, by default "en"
        remote_loc : str, optional
            Optional remote address of the resource, by default None. Will use
            Spark NLPs repositories otherwise.

        Returns
        -------
        GPT2Transformer
            The restored model
        """
        from sparknlp.pretrained import ResourceDownloader
        return ResourceDownloader.downloadModel(GPT2Transformer, name, lang, remote_loc)
