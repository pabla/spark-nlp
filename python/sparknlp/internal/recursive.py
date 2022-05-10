class RecursiveEstimator(JavaEstimator, ABC):

    def _fit_java(self, dataset, pipeline=None):
        self._transfer_params_to_java()
        if pipeline:
            return self._java_obj.recursiveFit(dataset._jdf, pipeline._to_java())
        else:
            return self._java_obj.fit(dataset._jdf)

    def _fit(self, dataset, pipeline=None):
        java_model = self._fit_java(dataset, pipeline)
        model = self._create_model(java_model)
        return self._copyValues(model)

    def fit(self, dataset, params=None, pipeline=None):
        if params is None:
            params = dict()
        if isinstance(params, (list, tuple)):
            models = [None] * len(params)
            for index, model in self.fitMultiple(dataset, params):
                models[index] = model
            return models
        elif isinstance(params, dict):
            if params:
                return self.copy(params)._fit(dataset, pipeline=pipeline)
            else:
                return self._fit(dataset, pipeline=pipeline)
        else:
            raise ValueError("Params must be either a param map or a list/tuple of param maps, "
                             "but got %s." % type(params))

class RecursiveTransformer(JavaModel):

    def _transform_recursive(self, dataset, recursive_pipeline):
        self._transfer_params_to_java()
        return DataFrame(self._java_obj.recursiveTransform(dataset._jdf, recursive_pipeline._to_java()),
                         dataset.sql_ctx)

    def transform_recursive(self, dataset, recursive_pipeline, params=None):
        if params is None:
            params = dict()
        if isinstance(params, dict):
            if params:
                return self.copy(params)._transform_recursive(dataset, recursive_pipeline)
            else:
                return self._transform_recursive(dataset, recursive_pipeline)
        else:
            raise ValueError("Params must be a param map but got %s." % type(params))

