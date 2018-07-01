#
# frontend_configuration.py
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
import argparse  # used for parsing of input arguments
import os

from pynestml.exceptions.invalid_path_exception import InvalidPathException
from pynestml.utils.logger import Logger

help_path = 'Path to a single file or a directory containing the source models.'
help_target = 'Path to a target directory where models should be generated to. Standard is "target".'
help_logging = 'Indicates which messages shall be logged and printed to the screen. ' \
               'Available ={INFO,WARNING/S,ERROR/S,NO}, Standard is ERRORS.'
help_dry = 'Indicates that a dry run shall be performed, i.e., without generating a target model.'
help_module = 'Indicates the name of the module. Optional. If not indicated, ' \
              'the name of the directory containing the models is used!'
help_log = 'Indicates whether a log file containing all messages shall be stored. Standard is NO.'
help_dev = 'Indicates whether the dev mode should be active, i.e., ' \
           'the whole toolchain executed even though errors in models are present.' \
           ' This option is designed for debug purpose only!'

qualifier_path_arg = '-path'
qualifier_target_arg = '-target'
qualifier_dry_arg = '-dry'
qualifier_logging_level_arg = '-logging_level'
qualifier_module_name_arg = '-module_name'
qualifier_store_log_arg = '-store_log'
qualifier_dev_arg = '-dev'


class FrontendConfiguration(object):
    """
    This class encapsulates all settings as handed over to the frontend at start of the toolchain.
    """
    argument_parser = None
    paths_to_compilation_units = None
    provided_path = None
    logging_level = None
    dry_run = None
    target_path = None
    module_name = None
    store_log = False
    is_debug = False
    targets = list()

    @classmethod
    def parse_config(cls, args):
        """
        Standard constructor. This method parses the
        :param args: a set of arguments as handed over to the frontend
        :type args: list(str)
        """
        cls.argument_parser = argparse.ArgumentParser(
            description='NESTML is a domain specific language that supports the specification of neuron models in a'
                        ' precise and concise syntax, based on the syntax of Python. Model equations can either be '
                        ' given as a simple string of mathematical notation or as an algorithm written in the built-in '
                        ' procedural language. The equations are analyzed by NESTML to compute an exact solution'
                        ' if possible or use an appropriate numeric solver otherwise.'
                        ' Version 0.0.6, beta.')

        cls.argument_parser.add_argument(qualifier_path_arg, type=str, nargs='+',
                                         help=help_path)
        cls.argument_parser.add_argument(qualifier_target_arg, metavar='Target', type=str, nargs='?',
                                         help=help_target)
        cls.argument_parser.add_argument(qualifier_dry_arg, action='store_true',
                                         help=help_dry)
        cls.argument_parser.add_argument(qualifier_logging_level_arg, type=str, nargs='+',
                                         help=help_logging)
        cls.argument_parser.add_argument(qualifier_module_name_arg, type=str, nargs='+',
                                         help=help_module)
        cls.argument_parser.add_argument(qualifier_store_log_arg, action='store_true',
                                         help=help_log)
        cls.argument_parser.add_argument(qualifier_dev_arg, action='store_true',
                                         help=help_dev)
        parsed_args = cls.argument_parser.parse_args(args)
        cls.provided_path = parsed_args.path
        if cls.provided_path is None:
            # check if the mandatory path arg has been handed over, just terminate
            raise InvalidPathException('Invalid source path!')
        cls.paths_to_compilation_units = list()
        if parsed_args.path is None:
            raise InvalidPathException('Invalid source path!')
        elif os.path.isfile(parsed_args.path[0]):
            cls.paths_to_compilation_units.append(parsed_args.path[0])
        elif os.path.isdir(parsed_args.path[0]):
            for filename in os.listdir(parsed_args.path[0]):
                if filename.endswith(".nestml"):
                    cls.paths_to_compilation_units.append(os.path.join(parsed_args.path[0], filename))
        else:
            cls.paths_to_compilation_units = parsed_args.path[0]
            raise InvalidPathException('Incorrect path provided' + parsed_args.path[0])
        # initialize the logger

        if parsed_args.logging_level is not None:
            cls.logging_level = parsed_args.logging_level
            Logger.init_logger(Logger.string_to_level(parsed_args.logging_level[0]))
        else:
            cls.logging_level = "ERROR"
            Logger.init_logger(Logger.string_to_level("ERROR"))
        # check if a dry run shall be preformed, i.e. without generating a target model
        cls.dry_run = parsed_args.dry
        # now update the target path
        cls.handle_target_path(parsed_args.target)
        # now adjust the name of the module, if it is a single file, then it is called just module
        if parsed_args.module_name is not None:
            cls.module_name = parsed_args.module_name[0]
        elif os.path.isfile(parsed_args.path[0]):
            cls.module_name = 'module'
        elif os.path.isdir(parsed_args.path[0]):
            cls.module_name = os.path.basename(os.path.normpath(parsed_args.path[0]))
        else:
            cls.module_name = 'module'
        cls.store_log = parsed_args.store_log
        cls.is_debug = parsed_args.dev
        return

    @classmethod
    def get_path(cls):
        """
        Returns the path to the handed over directory or file.
        :return: a single path
        :rtype: str
        """
        return cls.provided_path

    @classmethod
    def get_files(cls):
        """
        Returns a list of all files to process.
        :return: a list of paths to files as str.
        :rtype: list(str)
        """
        return cls.paths_to_compilation_units

    @classmethod
    def is_dry_run(cls):
        """
        Indicates whether it is a dry run, i.e., no model shall be generated
        :return: True if dry run, otherwise false.
        :rtype: bool
        """
        return cls.dry_run

    @classmethod
    def get_logging_level(cls):
        """
        Returns the set logging level.
        :return: the logging level
        :rtype: LoggingLevel
        """
        return cls.logging_level

    @classmethod
    def get_target_path(cls):
        """
        Returns the path to which models shall be generated to.
        :return: the target path.
        :rtype: str
        """
        return cls.target_path

    @classmethod
    def get_module_name(cls):
        """
        Returns the name of the module.
        :return: the name of the module.
        :rtype: str
        """
        return cls.module_name

    @classmethod
    def is_dev(cls):
        """
        Returns whether the dev mode have benn set as active.
        :return: True if dev mode is active, otherwise False.
        :rtype: bool
        """
        return cls.is_debug

    @classmethod
    def handle_target_path(cls, path):
        # check if a target has been selected, otherwise set the buildNest as target
        if path is not None:
            if os.path.isabs(path):
                cls.target_path = path
            # a relative path, reconstruct it. get the parent dir where models, pynestml etc. is located
            else:
                pynestml_dir = os.getcwd()
                cls.target_path = os.path.join(pynestml_dir, path)
        else:
            pynestml_dir = os.path.dirname(os.path.dirname(os.path.dirname(os.path.realpath(__file__))))
            cls.target_path = os.path.join(pynestml_dir, 'target')
        # check if the target path dir already exists
        if not os.path.isdir(cls.target_path):
            os.makedirs(cls.target_path)


class Targets(enumerate):
    NEST = 1
    SpiNNaker = 2
