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
from pynestml.modelprocessor.ASTNeuron import ASTNeuron
from pynestml.frontend.FrontendConfiguration import FrontendConfiguration
from jinja2 import Environment, FileSystemLoader
import os


class SpiNNackerCodeGenerator(object):
    """
    This class is responsible for the generation of compilable neuron code on the SpiNNacker platform.
    For more details regarding SpiNNacker, visit: http://www.artificialbrains.com/spinnaker
    """

    __templateNeuronHeader = None
    __templateNeuronImplementation = None
    __templateIntegrationFile = None

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

    def analyseAndGenerateNeuron(self, _neuron=None):
        """
        Generates the code for the handed over neuron model.
        :param _neuron: a single neuron instance
        :type _neuron: ASTNeuron
        """
        # first create a sub-dir for SpiNNacker, in order to avoid overwritten NEST models
        if not os.path.isdir(os.path.join(FrontendConfiguration.getTargetPath(), 'SpiNNacker')):
            os.makedirs(os.path.join(FrontendConfiguration.getTargetPath(), 'SpiNNacker'))
        self.generateModelHeader(_neuron)
        self.generateModelImplementation(_neuron)
        self.generateModelIntegration(_neuron)
        return

    def analyseAndGenerateNeurons(self, _neurons=None):
        """
        Generates a set of neuron implementations from a handed over list.
        :param _neurons: a list of neuron models
        :return: list(ASTNeuron)
        """
        for neuron in _neurons:
            self.analyseAndGenerateNeuron(neuron)
        return

    def generateModelHeader(self, _neuron=None):
        """
        For a handed over neuron, this method generates the corresponding header file.
        :param _neuron: a single neuron object.
        :type _neuron:  ASTNeuron
        """
        assert (_neuron is not None and isinstance(_neuron, ASTNeuron)), \
            '(PyNestML.CodeGenerator.NEST) No or wrong type of neuron provided (%s)!' % type(_neuron)
        inputNeuronHeader = self.setupStandardNamespace(_neuron)
        outputNeuronHeader = self.__templateNeuronHeader.render(inputNeuronHeader)
        with open(str(os.path.join(FrontendConfiguration.getTargetPath(), _neuron.getName())) + '.h', 'w+') as f:
            f.write(str(outputNeuronHeader))
        return

    def generateModelImplementation(self, _neuron=None):
        """
        For a handed over neuron, this method generates the corresponding implementation file.
        :param _neuron: a single neuron object.
        :type _neuron: ASTNeuron
        """
        assert (_neuron is not None and isinstance(_neuron, ASTNeuron)), \
            '(PyNestML.CodeGenerator.NEST) No or wrong type of neuron provided (%s)!' % type(_neuron)
        inputNeuronImplementation = self.setupStandardNamespace(_neuron)
        outputNeuronImplementation = self.__templateNeuronImplementation.render(inputNeuronImplementation)
        with open(str(os.path.join(FrontendConfiguration.getTargetPath(), _neuron.getName())) + '.cpp', 'w+') as f:
            f.write(str(outputNeuronImplementation))
        return

    def generateModelIntegration(self, _neuron=None):
        """
        For a handed over neuron, this method generates the corresponding simulation integration file.
        :param _neuron: a single neuron instance
        :type _neuron: ASTNeuron
        """
        assert (_neuron is not None and isinstance(_neuron, ASTNeuron)), \
            '(PyNestML.CodeGenerator.NEST) No or wrong type of neuron provided (%s)!' % type(_neuron)
        inputNeuronIntegration = self.setupStandardNamespace(_neuron)
        outputNeuronIntegration = self.__templateIntegrationFile.render(inputNeuronIntegration)
        with open(str(os.path.join(FrontendConfiguration.getTargetPath(), _neuron.getName())) + '.py', 'w+') as f:
            f.write(str(outputNeuronIntegration))
        return

    def setupStandardNamespace(self, _neuron=None):
        """
        Returns a standard namespace with often required functionality.
        :param _neuron: a single neuron instance
        :type _neuron: ASTNeuron
        :return: a map from name to functionality.
        :rtype: dict
        """
        namespace = {'neuronName': _neuron.getName(), 'neuron': _neuron,
                     'moduleName': FrontendConfiguration.getModuleName()}
        return namespace
