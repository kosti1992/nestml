from pynestml.modelprocessor.ASTNeuron import ASTNeuron


class SpiNNakerHelper(object):

    @classmethod
    def get_parameters_and_inits(cls, neuron):
        ret = list()
        assert isinstance(neuron, ASTNeuron)
        ret.extend(neuron.get_parameter_symbols())
        ret.extend(neuron.get_initial_values_symbols())
        return ret
