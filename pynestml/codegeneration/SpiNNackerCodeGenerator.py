#
# SpiNNackerCodeGenerator.py
#
# This file is part of NEST.
#
# Copyright (C) 2004 The NEST Initiative
#
# NEST is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 2 of the License, or
# (at your option) any later version.
#
# NEST is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with NEST.  If not, see <http://www.gnu.org/licenses/>.
import os

from jinja2 import Environment, FileSystemLoader

from pynestml.codegeneration.NestAssignmentsHelper import NestAssignmentsHelper
from pynestml.codegeneration.SpiNNackerNamesConverter import SpiNNackerNamesConverter
from pynestml.codegeneration.SpiNNakerHelper import SpiNNakerHelper
from pynestml.codegeneration.SpiNNakerReferenceConverter import SpiNNakerReferenceConverter
from pynestml.codegeneration.LegacyExpressionPrinter import LegacyExpressionPrinter
from pynestml.codegeneration.NestPrinter import NestPrinter


from pynestml.frontend.FrontendConfiguration import FrontendConfiguration
from pynestml.modelprocessor.ASTNeuron import ASTNeuron
from pynestml.utils.ASTUtils import ASTUtils
from pynestml.utils.Logger import Logger, LoggingLevel
from pynestml.utils.Messages import Messages


class SpiNNackerCodeGenerator(object):
    """
    This class is responsible for the generation of compilable neuron code on the SpiNNacker platform.
    For more details regarding SpiNNacker, visit: http://www.artificialbrains.com/spinnaker
    """

    __templateNeuronHeader = None
    __templateNeuronImplementation = None
    __templateIntegrationFile = None
    __path = None

    def __init__(self):
        """
        Standard constructor to initiate the generator.
        """
        # setup the environment
        env = Environment(loader=FileSystemLoader(os.path.join(os.path.dirname(__file__), 'resourcesSpiNNacker')))
        # setup the neuron header template
        self.__templateNeuronHeader = env.get_template('NeuronHeader.jinja2')
        # setup the header implementation template
        self.__templateNeuronImplementation = env.get_template('NeuronImplementation.jinja2')
        # setup the neuron integration template
        self.__templateIntegrationFile = env.get_template('NeuronIntegration.jinja2')
        # setup the path
        self.__path = os.path.join(FrontendConfiguration.get_target_path(), 'SpiNNacker')

        return

    def analyse_and_generate_neuron(self, _neuron=None):
        """
        Generates the code for the handed over neuron model.
        :param _neuron: a single neuron instance
        :type _neuron: ASTNeuron
        """
        # first create a sub-dir for SpiNNacker, in order to avoid overwritten NEST models
        if not os.path.isdir(self.__path):
            os.makedirs(self.__path)
        self.generate_model_header(_neuron)
        self.generate_model_implementation(_neuron)
        self.generate_model_integration(_neuron)
        code, message = Messages.getSpiNNackerCodeGenerated(_neuron.get_name(), self.__path)
        Logger.log_message(neuron=_neuron, error_position=_neuron.get_source_position(), code=code, message=message,
                           log_level=LoggingLevel.INFO)
        return

    def analyse_and_generate_neurons(self, _neurons=None):
        """
        Generates a set of neuron implementations from a handed over list.
        :param _neurons: a list of neuron models
        :return: list(ASTNeuron)
        """
        for neuron in _neurons:
            self.analyse_and_generate_neuron(neuron)
        return

    def generate_model_header(self, _neuron=None):
        """
        For a handed over neuron, this method generates the corresponding header file.
        :param _neuron: a single neuron object.
        :type _neuron:  ASTNeuron
        """
        assert (_neuron is not None and isinstance(_neuron, ASTNeuron)), \
            '(PyNestML.CodeGenerator.NEST) No or wrong type of neuron provided (%s)!' % type(_neuron)
        input_neuron_header = self.setup_standard_namespace(_neuron)
        output_neuron_header = self.__templateNeuronHeader.render(input_neuron_header)
        with open(str(os.path.join(self.__path, _neuron.get_name())) + '.h',
                  'w+') as f:
            f.write(str(output_neuron_header))
        return

    def generate_model_implementation(self, _neuron=None):
        """
        For a handed over neuron, this method generates the corresponding implementation file.
        :param _neuron: a single neuron object.
        :type _neuron: ASTNeuron
        """
        assert (_neuron is not None and isinstance(_neuron, ASTNeuron)), \
            '(PyNestML.CodeGenerator.NEST) No or wrong type of neuron provided (%s)!' % type(_neuron)
        input_neuron_implementation = self.setup_standard_namespace(_neuron)
        output_neuron_implementation = self.__templateNeuronImplementation.render(input_neuron_implementation)
        with open(str(os.path.join(self.__path, _neuron.get_name())) + '.c', 'w+') as f:
            f.write(str(output_neuron_implementation))
        return

    def generate_model_integration(self, _neuron=None):
        """
        For a handed over neuron, this method generates the corresponding simulation integration file.
        :param _neuron: a single neuron instance
        :type _neuron: ASTNeuron
        """
        assert (_neuron is not None and isinstance(_neuron, ASTNeuron)), \
            '(PyNestML.CodeGenerator.NEST) No or wrong type of neuron provided (%s)!' % type(_neuron)
        input_neuron_integration = self.setup_standard_namespace(_neuron)
        output_neuron_integration = self.__templateIntegrationFile.render(input_neuron_integration)
        with open(
                str(os.path.join(self.__path, _neuron.get_name())) + '.py',
                'w+') as f:
            f.write(str(output_neuron_integration))
        return

    def setup_standard_namespace(self, _neuron=None):
        """
        Returns a standard namespace with often required functionality.
        :param _neuron: a single neuron instance
        :type _neuron: ASTNeuron
        :return: a map from name to functionality.
        :rtype: dict
        """
        namespace = {'neuronName': _neuron.get_name(), 'neuron': _neuron,
                     'moduleName': FrontendConfiguration.get_module_name(), 'names': SpiNNackerNamesConverter(),
                     'helper': SpiNNakerHelper, 'utils': ASTUtils}
        namespace['assignments'] = NestAssignmentsHelper()
        converter = SpiNNakerReferenceConverter()
        legacy_pretty_printer = LegacyExpressionPrinter(_referenceConverter=converter)
        namespace['printer'] = NestPrinter(_expressionPrettyPrinter=legacy_pretty_printer)
        return namespace
