class RecursivePipeline(Pipeline, JavaEstimator):
    """Recursive pipelines are Spark NLP specific pipelines that allow a Spark
    ML Pipeline to know about itself on every Pipeline Stage task.

    This allows annotators to utilize this same pipeline against external
    resources to process them in the same way the user decides.

    Only some of the annotators take advantage of this. RecursivePipeline
    behaves exactly the same as normal Spark ML pipelines, so they can be used
    with the same intention.

    Examples
    --------
    >>> from sparknlp.annotator import *
    >>> from sparknlp.base import *
    >>> recursivePipeline = RecursivePipeline(stages=[
    ...     documentAssembler,
    ...     sentenceDetector,
    ...     tokenizer,
    ...     lemmatizer,
    ...     finisher
    ... ])
    """
    @keyword_only
    def __init__(self, *args, **kwargs):
        super(RecursivePipeline, self).__init__(*args, **kwargs)
        self._java_obj = self._new_java_obj("com.johnsnowlabs.nlp.RecursivePipeline", self.uid)
        kwargs = self._input_kwargs
        self.setParams(**kwargs)

    def _fit(self, dataset):
        stages = self.getStages()
        for stage in stages:
            if not (isinstance(stage, Estimator) or isinstance(stage, Transformer)):
                raise TypeError(
                    "Cannot recognize a pipeline stage of type %s." % type(stage))
        indexOfLastEstimator = -1
        for i, stage in enumerate(stages):
            if isinstance(stage, Estimator):
                indexOfLastEstimator = i
        transformers = []
        for i, stage in enumerate(stages):
            if i <= indexOfLastEstimator:
                if isinstance(stage, Transformer):
                    transformers.append(stage)
                    dataset = stage.transform(dataset)
                elif isinstance(stage, RecursiveEstimator):
                    model = stage.fit(dataset, pipeline=PipelineModel(transformers))
                    transformers.append(model)
                    if i < indexOfLastEstimator:
                        dataset = model.transform(dataset)
                else:
                    model = stage.fit(dataset)
                    transformers.append(model)
                    if i < indexOfLastEstimator:
                        dataset = model.transform(dataset)
            else:
                transformers.append(stage)
        return PipelineModel(transformers)

class RecursivePipelineModel(PipelineModel):
    """Fitted RecursivePipeline.

    Behaves the same as a Spark PipelineModel does. Not intended to be
    initialized by itself. To create a RecursivePipelineModel please fit data to
    a :class:`.RecursivePipeline`.
    """
    def __init__(self, pipeline_model):
        super(PipelineModel, self).__init__()
        self.stages = pipeline_model.stages

    def _transform(self, dataset):
        for t in self.stages:
            if isinstance(t, HasRecursiveTransform):
                # drops current stage from the recursive pipeline within
                dataset = t.transform_recursive(dataset, PipelineModel(self.stages[:-1]))
            elif isinstance(t, AnnotatorProperties) and t.getLazyAnnotator():
                pass
            else:
                dataset = t.transform(dataset)
        return dataset

