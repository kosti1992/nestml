#
# pynestml_frontend.py
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

from pynestml.frontend.frontend_configuration import Targets
from pynestml.codegeneration.spinnaker_codegenerator import SpiNNakerCodeGenerator
from pynestml.cocos.co_cos_manager import CoCosManager
from pynestml.codegeneration.nest_codegeneration import analyse_and_generate_neurons, generate_nest_module_code
from pynestml.frontend.frontend_configuration import FrontendConfiguration, InvalidPathException, \
    qualifier_store_log_arg, qualifier_module_name_arg, qualifier_logging_level_arg, qualifier_dry_arg, \
    qualifier_target_arg, qualifier_path_arg, qualifier_dev_arg
from pynestml.symbols.predefined_functions import PredefinedFunctions
from pynestml.symbols.predefined_types import PredefinedTypes
from pynestml.symbols.predefined_units import PredefinedUnits
from pynestml.symbols.predefined_variables import PredefinedVariables
from pynestml.utils.logger import Logger, LoggingLevel
from pynestml.utils.messages import Messages
from pynestml.utils.model_parser import ModelParser
from pynestml.utils.model_installer import install_nest as nest_installer


def to_nest(path, target=None, dry=False, logging_level='ERROR', module_name=None, store_log=False, dev=False):
    # if target is not None and not os.path.isabs(target):
    #    print('PyNestML: Please provide absolute target path!')
    #    return
    args = list()
    args.append(qualifier_path_arg)
    args.append(str(path))
    if target is not None:
        args.append(qualifier_target_arg)
        args.append(str(target))
    if dry:
        args.append(qualifier_dry_arg)
    args.append(qualifier_logging_level_arg)
    args.append(str(logging_level))
    if module_name is not None:
        args.append(qualifier_module_name_arg)
        args.append(str(module_name))
    if store_log:
        args.append(qualifier_store_log_arg)
    if dev:
        args.append(qualifier_dev_arg)
    FrontendConfiguration.parse_config(args)
    process()


def install_nest(models_path, nest_path):
    # type: (str,str) -> None
    """
    This procedure can be used to install generate models into the NEST simulator.
    :param models_path: the path to the generated models, should contain the cmake file (automatically generated).
    :param nest_path: the path to the NEST installation, should point to the dir where nest is installed, a.k.a.
            the -Dwith-nest argument of the make command. The postfix /bin/nest-config is automatically attached.
    :return:
    """
    nest_installer(models_path, nest_path)


def main(args):
    try:
        FrontendConfiguration.parse_config(args)
    except InvalidPathException:
        print('Not a valid path to model or directory: "%s"!' % FrontendConfiguration.get_path())
        return
    # after all argument have been collected, start the actual processing
    process()


def process():
    # init log dir
    create_report_dir()
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
                code, message = Messages.get_neuron_contains_errors(neuron.get_name())
                Logger.log_message(neuron=neuron, code=code, message=message,
                                   error_position=neuron.get_source_position(),
                                   log_level=LoggingLevel.INFO)
                neurons.remove(neuron)
    if not FrontendConfiguration.is_dry_run():
        analyse_and_generate_neurons(neurons)
        generate_nest_module_code(neurons)
        # todo: extract this part
        if Targets.SpiNNaker in FrontendConfiguration.targets:
            spin = SpiNNakerCodeGenerator()
            spin.analyse_and_generate_neurons(neurons)
    else:
        code, message = Messages.get_dry_run()
        Logger.log_message(neuron=None, code=code, message=message, log_level=LoggingLevel.INFO)
    if FrontendConfiguration.store_log:
        store_log_to_file()
    return


def init_predefined():
    # initialize the predefined elements
    PredefinedUnits.register_units()
    PredefinedTypes.register_types()
    PredefinedFunctions.register_functions()
    PredefinedVariables.register_variables()


def create_report_dir():
    if not os.path.isdir(os.path.join(FrontendConfiguration.get_target_path(), '..', 'report')):
        os.makedirs(os.path.join(FrontendConfiguration.get_target_path(), '..', 'report'))


def store_log_to_file():
    with open(str(os.path.join(FrontendConfiguration.get_target_path(), '..', 'report',
                               'log')) + '.txt', 'w+') as f:
        f.write(str(Logger.get_json_format()))


if __name__ == '__main__':
    main(sys.argv[1:])
