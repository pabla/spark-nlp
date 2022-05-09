class NerApproach(Params):
    """Base class for Ner*Approach Annotators
    """
    labelColumn = Param(Params._dummy(),
                        "labelColumn",
                        "Column with label per each token",
                        typeConverter=TypeConverters.toString)

    entities = Param(Params._dummy(), "entities", "Entities to recognize", TypeConverters.toListString)

    minEpochs = Param(Params._dummy(), "minEpochs", "Minimum number of epochs to train", TypeConverters.toInt)
    maxEpochs = Param(Params._dummy(), "maxEpochs", "Maximum number of epochs to train", TypeConverters.toInt)

    verbose = Param(Params._dummy(), "verbose", "Level of verbosity during training", TypeConverters.toInt)
    randomSeed = Param(Params._dummy(), "randomSeed", "Random seed", TypeConverters.toInt)

    def setLabelColumn(self, value):
        """Sets name of column for data labels.

        Parameters
        ----------
        value : str
            Column for data labels
        """
        return self._set(labelColumn=value)

    def setEntities(self, tags):
        """Sets entities to recognize.

        Parameters
        ----------
        tags : List[str]
            List of entities
        """
        return self._set(entities=tags)

    def setMinEpochs(self, epochs):
        """Sets minimum number of epochs to train.

        Parameters
        ----------
        epochs : int
            Minimum number of epochs to train
        """
        return self._set(minEpochs=epochs)

    def setMaxEpochs(self, epochs):
        """Sets maximum number of epochs to train.

        Parameters
        ----------
        epochs : int
            Maximum number of epochs to train
        """
        return self._set(maxEpochs=epochs)

    def setVerbose(self, verboseValue):
        """Sets level of verbosity during training.

        Parameters
        ----------
        verboseValue : int
            Level of verbosity
        """
        return self._set(verbose=verboseValue)

    def setRandomSeed(self, seed):
        """Sets random seed for shuffling.

        Parameters
        ----------
        seed : int
            Random seed for shuffling
        """
        return self._set(randomSeed=seed)

    def getLabelColumn(self):
        """Gets column for label per each token.

        Returns
        -------
        str
            Column with label per each token
        """
        return self.getOrDefault(self.labelColumn)

