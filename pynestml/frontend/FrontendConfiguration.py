#
# FrontendConfiguration.py
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

from pynestml.frontend.BackendTargets import BackendTargets
from pynestml.utils.Logger import Logger


class FrontendConfiguration(object):
    """
    This class encapsulates all settings as handed over to the frontend at start of the toolchain.
    """
    __argumentParser = None
    __pathsToCompilationUnits = None
    __providedPath = None
    __loggingLevel = None
    __dryRun = None
    __targetPath = None
    __moduleName = None
    __storeLog = False
    __isDebug = False
    __codeGenerationTarget = list()

    @classmethod
    def config(cls, _args=None):
        """
        Standard constructor.
        :param _args: a set of arguments as handed over to the frontend
        :type _args: list(str)
        """
        cls.__argumentParser = argparse.ArgumentParser(
            description='NESTML is a domain specific language that supports the specification of neuron models in a'
                        ' precise and concise syntax, based on the syntax of Python. Model equations can either be '
                        ' given as a simple string of mathematical notation or as an algorithm written in the built-in '
                        ' procedural language. The equations are analyzed by NESTML to compute an exact solution'
                        ' if possible or use an appropriate numeric solver otherwise.'
                        ' Version 0.0.6, beta.')
        cls.__argumentParser.add_argument('-sp', '--sourcepath', type=str, nargs='+',
                                          help='Path to a single file or a directory containing the source models.')
        cls.__argumentParser.add_argument('-tp', '--targetpath', metavar='Target', type=str, nargs='?',
                                          help='Path to a target directory where models should be generated to. '
                                               'Standard is "target".')
        cls.__argumentParser.add_argument('-dr', '--dry', action='store_true',
                                          help='Indicates that a dry run shall be performed, i.e.,'
                                               ' without generating a target model.')
        cls.__argumentParser.add_argument('-ll', '--logging_level', type=str, nargs='+',
                                          help='Indicates which messages shall be logged and printed to the'
                                               'screen. Available = {INFO,WARNING/S,ERROR/S,NO}, Standard is ERRORS.')
        cls.__argumentParser.add_argument('-m', '--module_name', type=str, nargs='+',
                                          help='Indicates the name of the module. Optional. If not indicated,'
                                               'the name of the directory containing the models is used!')
        cls.__argumentParser.add_argument('-s', '--store_log', action='store_true',
                                          help='Indicates whether a log file containing all messages shall'
                                               'be stored. Standard is NO.')
        cls.__argumentParser.add_argument('-dv', '--dev', action='store_true',
                                          help='Indicates whether the dev mode should be active, i.e., the whole '
                                               'toolchain executed even though errors in models are present.'
                                               'This option is designed for debug purpose only!')
        cls.__argumentParser.add_argument('-tt', '--targets', nargs='*', required=False,
                                          help='Indicates for which target platforms code shall be generated. '
                                               'Available = {NEST,SpiNNacker}, Standard is NEST.')
        parsed_args = cls.__argumentParser.parse_args(_args)
        cls.__providedPath = parsed_args.sourcepath
        if cls.__providedPath is None:
            # check if the mandatory path arg has been handed over, just terminate
            raise RuntimeError('Invalid Path')
        cls.__pathsToCompilationUnits = list()
        if parsed_args.sourcepath is None:
            raise RuntimeError('Invalid Path')
        elif os.path.isfile(parsed_args.sourcepath[0]):
            cls.__pathsToCompilationUnits.append(parsed_args.sourcepath[0])
        elif os.path.isdir(parsed_args.sourcepath[0]):
            for filename in os.listdir(parsed_args.sourcepath[0]):
                if filename.endswith(".nestml"):
                    cls.__pathsToCompilationUnits.append(os.path.join(parsed_args.sourcepath[0], filename))
        else:
            cls.__pathsToCompilationUnits = parsed_args.sourcepath[0]
            raise RuntimeError('Invalid Path')
        # initialize the logger

        if parsed_args.logging_level is not None:
            cls.__loggingLevel = parsed_args.logging_level
            Logger.init_logger(Logger.string_to_level(parsed_args.logging_level[0]))
        else:
            cls.__loggingLevel = "ERROR"
            Logger.init_logger(Logger.string_to_level("ERROR"))
        # check if a dry run shall be preformed, i.e. without generating a target model
        cls.__dryRun = parsed_args.dry
        # check if a target has been selected, otherwise set the buildNest as target
        if parsed_args.targetpath is not None:
            cls.__targetPath = str(os.path.realpath(os.path.join('..', parsed_args.targetpath)))
        else:
            if not os.path.isdir(os.path.realpath(os.path.join('..', 'target'))):
                os.makedirs(os.path.realpath(os.path.join('..', 'target')))
            cls.__targetPath = str(os.path.realpath(os.path.join('..', 'target')))
        # now adjust the name of the module, if it is a single file, then it is called just module
        if parsed_args.module_name is not None:
            cls.__moduleName = parsed_args.module_name[0]
        elif os.path.isfile(parsed_args.sourcepath[0]):
            cls.__moduleName = 'module'
        elif os.path.isdir(parsed_args.sourcepath[0]):
            cls.__moduleName = os.path.basename(os.path.normpath(parsed_args.sourcepath[0]))
        else:
            cls.__moduleName = 'module'
        cls.__storeLog = parsed_args.store_log
        cls.__isDebug = parsed_args.dev
        # now check which target for code generation has been selected
        if parsed_args.targets is not None:
            # parse the targets and add them
            for target in parsed_args.targets:
                possibleTarget = BackendTargets.parseTarget(target)
                if possibleTarget is not None:
                    cls.__codeGenerationTarget.append(possibleTarget)
        else:
            # otherwise only NEST is generated
            cls.__codeGenerationTarget.append(BackendTargets.NEST)
        return

    @classmethod
    def get_path(cls):
        """
        Returns the path to the handed over directory or file.
        :return: a single path
        :rtype: str
        """
        return cls.__providedPath

    @classmethod
    def get_files(cls):
        """
        Returns a list of all files to process.
        :return: a list of paths to files as str.
        :rtype: list(str)
        """
        return cls.__pathsToCompilationUnits

    @classmethod
    def is_dry_run(cls):
        """
        Indicates whether it is a dry run, i.e., no modell shall be generated
        :return: True if dry run, otherwise false.
        :rtype: bool
        """
        return cls.__dryRun

    @classmethod
    def get_logging_level(cls):
        """
        Returns the set logging level.
        :return: the logging level
        :rtype: LOGGING_LEVEL
        """
        return cls.__loggingLevel

    @classmethod
    def get_target_path(cls):
        """
        Returns the path to which models shall be generated to.
        :return: the target path.
        :rtype: str
        """
        return cls.__targetPath

    @classmethod
    def get_module_name(cls):
        """
        Returns the name of the module.
        :return: the name of the module.
        :rtype: str
        """
        return cls.__moduleName

    @classmethod
    def store_log(cls):
        """
        Returns whether the log shall be stored.
        :return: True if shall be stored, otherwise False.
        :rtype: bool
        """
        return cls.__storeLog

    @classmethod
    def is_dev(cls):
        """
        Returns whether the dev mode have benn set as active.
        :return: True if dev mode is active, otherwise False.
        :rtype: bool
        """
        return cls.__isDebug

    @classmethod
    def get_targets(cls):
        """
        Returns the currently set target for code generation.
        :return: BackendTargets
        """
        return cls.__codeGenerationTarget
