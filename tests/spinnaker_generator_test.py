#
# spinnaker_generator_test.py
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
import unittest

from pynestml.meta_model.ast_source_location import ASTSourceLocation
from pynestml.frontend.pynestml_frontend import main
from pynestml.symbol_table.symbol_table import SymbolTable
from pynestml.symbols.predefined_functions import PredefinedFunctions
from pynestml.symbols.predefined_types import PredefinedTypes
from pynestml.symbols.predefined_units import PredefinedUnits
from pynestml.symbols.predefined_variables import PredefinedVariables
from pynestml.utils.logger import Logger, LoggingLevel
from pynestml.frontend.frontend_configuration import FrontendConfiguration

# setups the infrastructure
PredefinedUnits.register_units()
PredefinedTypes.register_types()
PredefinedFunctions.register_functions()
PredefinedVariables.register_variables()
SymbolTable.initialize_symbol_table(ASTSourceLocation(start_line=0, start_column=0, end_line=0, end_column=0))
Logger.init_logger(LoggingLevel.NO)


class SpiNNakerCodeGeneratorTest(unittest.TestCase):
    """
    TODO: test whether spinnaker code is generated correctly
    """

    def test_generate_spinnaker(self):
        from pynestml.frontend.frontend_configuration import Targets,FrontendConfiguration
        FrontendConfiguration.targets.append(Targets.SpiNNaker)

        path = str(os.path.realpath(os.path.join(os.path.dirname(__file__), os.path.join('..', 'models',
                                                                                         'izhikevich.nestml'))))
        params = list()
        params.append('-path')
        params.append(path)
        # params.append('-dry')
        params.append('-logging_level')
        params.append('NO')
        params.append('-target')
        params.append('target')
        params.append('-store_log')
        params.append('-dev')
        # try:
        main(params)
        self.assertTrue(True)  # the goal is to reach this point without exceptions
        # except Exception:
        #    self.assertTrue(False)

    def tearDown(self):
        # clean up
        import shutil
        shutil.rmtree(FrontendConfiguration.target_path)


if __name__ == '__main__':
    unittest.main()
