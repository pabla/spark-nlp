class RegexTokenizer(AnnotatorModel):
    """A tokenizer that splits text by a regex pattern.

    The pattern needs to be set with :meth:`.setPattern` and this sets the
    delimiting pattern or how the tokens should be split. By default this
    pattern is ``\\s+`` which means that tokens should be split by 1 or more
    whitespace characters.

    ====================== ======================
    Input Annotation types Output Annotation type
    ====================== ======================
    ``DOCUMENT``           ``TOKEN``
    ====================== ======================

    Parameters
    ----------
    minLength
        Set the minimum allowed legth for each token, by default 1
    maxLength
        Set the maximum allowed legth for each token
    toLowercase
        Indicates whether to convert all characters to lowercase before
        tokenizing, by default False
    pattern
        Regex pattern used for tokenizing, by default ``\\s+``
    positionalMask
        Using a positional mask to guarantee the incremental progression of the
        tokenization, by default False
    trimWhitespace
        Using a trimWhitespace flag to remove whitespaces from identified tokens,
        by default False
    preservePosition
        Using a preservePosition flag to preserve initial indexes before eventual whitespaces removal in tokens,
        by default True

    Examples
    --------
    >>> import sparknlp
    >>> from sparknlp.base import *
    >>> from sparknlp.annotator import *
    >>> from pyspark.ml import Pipeline
    >>> documentAssembler = DocumentAssembler() \\
    ...     .setInputCol("text") \\
    ...     .setOutputCol("document")
    >>> regexTokenizer = RegexTokenizer() \\
    ...     .setInputCols(["document"]) \\
    ...     .setOutputCol("regexToken") \\
    ...     .setToLowercase(True) \\
    >>> pipeline = Pipeline().setStages([
    ...       documentAssembler,
    ...       regexTokenizer
    ...     ])
    >>> data = spark.createDataFrame([["This is my first sentence.\\nThis is my second."]]).toDF("text")
    >>> result = pipeline.fit(data).transform(data)
    >>> result.selectExpr("regexToken.result").show(truncate=False)
    +-------------------------------------------------------+
    |result                                                 |
    +-------------------------------------------------------+
    |[this, is, my, first, sentence., this, is, my, second.]|
    +-------------------------------------------------------+
    """

    name = "RegexTokenizer"

    @keyword_only
    def __init__(self):
        super(RegexTokenizer, self).__init__(classname="com.johnsnowlabs.nlp.annotators.RegexTokenizer")
        self._setDefault(
            inputCols=["document"],
            outputCol="regexToken",
            toLowercase=False,
            minLength=1,
            pattern="\\s+",
            positionalMask=False,
            trimWhitespace=False,
            preservePosition=True
        )

    minLength = Param(Params._dummy(),
                      "minLength",
                      "Set the minimum allowed legth for each token",
                      typeConverter=TypeConverters.toInt)

    maxLength = Param(Params._dummy(),
                      "maxLength",
                      "Set the maximum allowed legth for each token",
                      typeConverter=TypeConverters.toInt)

    toLowercase = Param(Params._dummy(),
                        "toLowercase",
                        "Indicates whether to convert all characters to lowercase before tokenizing.",
                        typeConverter=TypeConverters.toBoolean)

    pattern = Param(Params._dummy(),
                    "pattern",
                    "regex pattern used for tokenizing. Defaults \S+",
                    typeConverter=TypeConverters.toString)

    positionalMask = Param(Params._dummy(),
                           "positionalMask",
                           "Using a positional mask to guarantee the incremental progression of the tokenization.",
                           typeConverter=TypeConverters.toBoolean)

    trimWhitespace = Param(Params._dummy(),
                           "trimWhitespace",
                           "Indicates whether to use a trimWhitespaces flag to remove whitespaces from identified tokens.",
                           typeConverter=TypeConverters.toBoolean)

    preservePosition = Param(Params._dummy(),
                             "preservePosition",
                             "Indicates whether to use a preserve initial indexes before eventual whitespaces removal in tokens.",
                             typeConverter=TypeConverters.toBoolean)

    def setMinLength(self, value):
        """Sets the minimum allowed legth for each token, by default 1.

        Parameters
        ----------
        value : int
            Minimum allowed legth for each token
        """
        return self._set(minLength=value)

    def setMaxLength(self, value):
        """Sets the maximum allowed legth for each token.

        Parameters
        ----------
        value : int
            Maximum allowed legth for each token
        """
        return self._set(maxLength=value)

    def setToLowercase(self, value):
        """Sets whether to convert all characters to lowercase before
        tokenizing, by default False.

        Parameters
        ----------
        value : bool
            Whether to convert all characters to lowercase before tokenizing
        """
        return self._set(toLowercase=value)

    def setPattern(self, value):
        """Sets the regex pattern used for tokenizing, by default ``\\s+``.

        Parameters
        ----------
        value : str
            Regex pattern used for tokenizing
        """
        return self._set(pattern=value)

    def setPositionalMask(self, value):
        """Sets whether to use a positional mask to guarantee the incremental
        progression of the tokenization, by default False.

        Parameters
        ----------
        value : bool
            Whether to use a positional mask
        """
        return self._set(positionalMask=value)

    def setTrimWhitespace(self, value):
        """Indicates whether to use a trimWhitespaces flag to remove whitespaces from identified tokens.

        Parameters
        ----------
        value : bool
            Indicates whether to use a trimWhitespaces flag, by default False.
        """
        return self._set(trimWhitespace=value)

    def setPreservePosition(self, value):
        """Indicates whether to use a preserve initial indexes before eventual whitespaces removal in tokens.

        Parameters
        ----------
        value : bool
            Indicates whether to use a preserve initial indexes, by default True.
        """
        return self._set(preservePosition=value)

