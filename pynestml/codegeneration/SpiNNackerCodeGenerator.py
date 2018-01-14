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
from jinja2 import Environment,FileSystemLoader
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
        env = Environment(loader=FileSystemLoader(os.path.join(os.path.dirname(__file__), 'resourcesNEST')))
        # setup the neuron header template
        self.__templateNeuronHeader = 1
        # setup the header implementation template
        self.__templateNeuronImplementation = 1
        # setup the neuron integration template
        self.__templateIntegrationFile = 1







