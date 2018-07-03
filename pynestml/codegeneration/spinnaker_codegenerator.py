#
# SpiNNakerCodeGenerator.py
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

from pynestml.codegeneration.expressions_pretty_printer import ExpressionsPrettyPrinter
from pynestml.codegeneration.spinnaker_reference_converter import SpiNNakerReferenceConverter
from pynestml.codegeneration.nest_assignments_helper import NestAssignmentsHelper
from pynestml.codegeneration.spinnaker_names_converter import SpiNNakerNamesConverter
from pynestml.codegeneration.spinnaker_helper import SpiNNakerHelper
from pynestml.codegeneration.spinnaker_reference_converter import SpiNNakerReferenceConverter
from pynestml.codegeneration.legacy_expression_printer import LegacyExpressionPrinter
from pynestml.codegeneration.nest_printer import NestPrinter

from pynestml.codegeneration.spinnaker_printer import SpiNNakerPrinter
from pynestml.frontend.frontend_configuration import FrontendConfiguration
from pynestml.meta_model.ast_neuron import ASTNeuron
from pynestml.utils.ast_utils import ASTUtils
from pynestml.utils.logger import Logger, LoggingLevel
from pynestml.utils.messages import Messages


class SpiNNakerCodeGenerator(object):
    """
    This class is responsible for the generation of compilable neuron code on the SpiNNaker platform.
    For more details regarding SpiNNaker, visit: http://www.artificialbrains.com/spinnaker
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
        env = Environment(loader=FileSystemLoader(os.path.join(os.path.dirname(__file__), 'resources_spinnaker')))
        # setup the neuron header template
        self.__templateNeuronHeader = env.get_template('NeuronHeader.jinja2')
        # setup the header implementation template
        self.__templateNeuronImplementation = env.get_template('NeuronImplementation.jinja2')
        # setup the neuron integration template
        self.__templateIntegrationFile = env.get_template('NeuronIntegration.jinja2')
        # setup the path
        self.__path = os.path.join(FrontendConfiguration.get_target_path(), 'SpiNNaker')

        return

    def analyse_and_generate_neuron(self, _neuron = None):
        """
        Generates the code for the handed over neuron model.
        :param _neuron: a single neuron instance
        :type _neuron: ASTNeuron
        """
        # first create a sub-dir for SpiNNaker, in order to avoid overwritten NEST models
        if not os.path.isdir(self.__path):
            os.makedirs(self.__path)
        self.generate_model_header(_neuron)
        self.generate_model_implementation(_neuron)
        self.generate_model_integration(_neuron)
        # TODO: correct reporting
        # code, message = Messages.getSpiNNakerCodeGenerated(_neuron.get_name(), self.__path)
        # Logger.log_message(neuron=_neuron, error_position=_neuron.get_source_position(), code=code, message=message,
        #                   log_level=LoggingLevel.INFO)
        return

    def analyse_and_generate_neurons(self, neurons):
        """
        Generates a set of neuron implementations from a handed over list.
        :param neurons: a list of neuron models
        :return: list(ASTNeuron)
        """
        for neuron in neurons:
            self.analyse_and_generate_neuron(neuron)
        return

    def generate_model_header(self, neuron):
        """
        For a handed over neuron, this method generates the corresponding header file.
        :param neuron: a single neuron object.
        :type neuron:  ASTNeuron
        """
        assert (neuron is not None and isinstance(neuron, ASTNeuron)), \
            '(PyNestML.CodeGenerator.NEST) No or wrong type of neuron provided (%s)!' % type(neuron)
        input_neuron_header = self.setup_standard_namespace(neuron)
        output_neuron_header = self.__templateNeuronHeader.render(input_neuron_header)
        with open(str(os.path.join(self.__path, neuron.get_name())) + '.h',
                  'w+') as f:
            f.write(str(output_neuron_header))
        return

    def generate_model_implementation(self, neuron):
        """
        For a handed over neuron, this method generates the corresponding implementation file.
        :param neuron: a single neuron object.
        :type neuron: ASTNeuron
        """
        assert (neuron is not None and isinstance(neuron, ASTNeuron)), \
            '(PyNestML.CodeGenerator.NEST) No or wrong type of neuron provided (%s)!' % type(neuron)
        input_neuron_implementation = self.setup_standard_namespace(neuron)
        output_neuron_implementation = self.__templateNeuronImplementation.render(input_neuron_implementation)
        with open(str(os.path.join(self.__path, neuron.get_name())) + '.c', 'w+') as f:
            f.write(str(output_neuron_implementation))
        return

    def generate_model_integration(self, _neuron = None):
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

    def setup_standard_namespace(self, neuron):
        """
        Returns a standard namespace with often required functionality.
        :param neuron: a single neuron instance
        :type neuron: ASTNeuron
        :return: a map from name to functionality.
        :rtype: dict
        """
        namespace = {'neuronName': neuron.get_name(), 'neuron': neuron,
                     'moduleName': FrontendConfiguration.get_module_name(), 'names': SpiNNakerNamesConverter(),
                     'helper': SpiNNakerHelper, 'utils': ASTUtils, 'assignments': NestAssignmentsHelper()}
        ref_converter = SpiNNakerReferenceConverter()
        printer = ExpressionsPrettyPrinter(reference_converter=ref_converter)
        namespace['printer'] = SpiNNakerPrinter(expression_pretty_printer=printer)
        return namespace
