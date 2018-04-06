#
# PyNestMLFrontend.py
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
import sys

from pynestml.codegeneration.NestCodeGenerator import NestCodeGenerator
from pynestml.codegeneration.SpiNNackerCodeGenerator import SpiNNackerCodeGenerator
from pynestml.frontend.BackendTargets import BackendTargets
from pynestml.frontend.FrontendConfiguration import FrontendConfiguration
from pynestml.modelprocessor.CoCosManager import CoCosManager
from pynestml.modelprocessor.ModelParser import ModelParser
from pynestml.modelprocessor.PredefinedFunctions import PredefinedFunctions
from pynestml.modelprocessor.PredefinedTypes import PredefinedTypes
from pynestml.modelprocessor.PredefinedUnits import PredefinedUnits
from pynestml.modelprocessor.PredefinedVariables import PredefinedVariables
from pynestml.utils.Logger import Logger, LoggingLevel
from pynestml.utils.Messages import Messages


def main(args):
    try:
        FrontendConfiguration.config(args)
    except RuntimeError:
        print('Not a valid path to model or directory: "%s"!' % FrontendConfiguration.get_path())
        return
    # The handed over parameters seem to be correct, proceed with the main routine
    init_predefined()
    # now proceed to parse all models
    compilation_units = list()
    for file in FrontendConfiguration.get_files():
        parsed_unit = ModelParser.parse_model(file)
        if parsed_unit is not None:
            compilation_units.append(parsed_unit)
    # generate a list of all neurons
    neurons = list()
    for compilationUnit in compilation_units:
        neurons.extend(compilationUnit.get_neuron_list())
    # check if across two files two neurons with same name have been defined
    CoCosManager.check_not_two_neurons_across_units(compilation_units)
    # now exclude those which are broken, i.e. have errors.
    if not FrontendConfiguration.is_dev():
        for neuron in neurons:
            if Logger.has_errors(neuron):
                code, message = Messages.getNeuronContainsErrors(neuron.get_name())
                Logger.log_message(neuron=neuron, code=code, message=message,
                                   error_position=neuron.get_source_position(),
                                   log_level=LoggingLevel.INFO)
                neurons.remove(neuron)

    if not FrontendConfiguration.is_dry_run():
        if BackendTargets.NEST in FrontendConfiguration.get_targets():
            nestGenerator = NestCodeGenerator()
            nestGenerator.analyseAndGenerateNeurons(neurons)
            nestGenerator.generateNESTModuleCode(neurons)
        if BackendTargets.SpiNNacker in FrontendConfiguration.get_targets():
            spinnackerGenerator = SpiNNackerCodeGenerator()
            spinnackerGenerator.analyse_and_generate_neurons(neurons)
    else:
        code, message = Messages.getDryRun()
        Logger.log_message(neuron=None, code=code, message=message, log_level=LoggingLevel.INFO)
    if FrontendConfiguration.store_log():
        store_log_to_file()
    return


def init_predefined():
    # initialize the predefined elements
    PredefinedUnits.register_units()
    PredefinedTypes.register_types()
    PredefinedFunctions.register_predefined_functions()
    PredefinedVariables.register_predefined_variables()


def store_log_to_file():
    with open(str(os.path.join(FrontendConfiguration.get_target_path(),
                               'log')) + '.txt', 'w+') as f:
        f.write(str(Logger.get_json_format()))
    return


if __name__ == '__main__':
    main(sys.argv[1:])
