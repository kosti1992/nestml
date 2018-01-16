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

    def generateModel(self, _neuron=None):
        """
        Generates the code for the handed over neuron model.
        :param _neuron:
        :return:
        """
        # first create a sub-dir for SpiNNacker, in order to avoid overwritten NEST models
        if not os.path.isdir(os.path.join(FrontendConfiguration.getTargetPath(), 'spin')):
            os.makedirs(os.path.join(FrontendConfiguration.getTargetPath(), 'spin'))
