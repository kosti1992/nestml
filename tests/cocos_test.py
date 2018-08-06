#
# cocos_test.py
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

from __future__ import print_function

import os
import unittest

from pynestml.meta_model.ast_source_location import ASTSourceLocation
from pynestml.symbol_table.symbol_table import SymbolTable
from pynestml.symbols.predefined_functions import PredefinedFunctions
from pynestml.symbols.predefined_types import PredefinedTypes
from pynestml.symbols.predefined_units import PredefinedUnits
from pynestml.symbols.predefined_variables import PredefinedVariables
from pynestml.utils.logger import Logger, LoggingLevel
from pynestml.utils.model_parser import ModelParser

# minor setup steps required
Logger.init_logger(LoggingLevel.INFO)
SymbolTable.initialize_symbol_table(ASTSourceLocation(start_line=0, start_column=0, end_line=0, end_column=0))
PredefinedUnits.register_units()
PredefinedTypes.register_types()
PredefinedVariables.register_variables()
PredefinedFunctions.register_functions()


class ElementDefinedAfterUsage(unittest.TestCase):
    def test_invalid(self):
        Logger.set_logging_level(LoggingLevel.NO)
        model = ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'invalid')),
                             'CoCoVariableDefinedAfterUsage.nestml'))
        self.assertEqual(len(
                Logger.get_all_messages_of_level_and_or_neuron(model.get_neuron_list()[0], LoggingLevel.ERROR)), 2)

    def test_valid(self):
        Logger.set_logging_level(LoggingLevel.NO)
        model = ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'valid')),
                             'CoCoVariableDefinedAfterUsage.nestml'))
        self.assertEqual(len(
                Logger.get_all_messages_of_level_and_or_neuron(model.get_neuron_list()[0], LoggingLevel.ERROR)), 0)


class ElementInSameLine(unittest.TestCase):
    def test_invalid(self):
        Logger.set_logging_level(LoggingLevel.NO)
        model = ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'invalid')),
                             'CoCoElementInSameLine.nestml'))
        self.assertEqual(len(
                Logger.get_all_messages_of_level_and_or_neuron(model.get_neuron_list()[0], LoggingLevel.ERROR)), 1)

    def test_valid(self):
        Logger.set_logging_level(LoggingLevel.NO)
        model = ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'valid')),
                             'CoCoElementInSameLine.nestml'))
        self.assertEqual(len(
                Logger.get_all_messages_of_level_and_or_neuron(model.get_neuron_list()[0], LoggingLevel.ERROR)), 0)


class ElementNotDefinedInScope(unittest.TestCase):
    def test_invalid(self):
        Logger.set_logging_level(LoggingLevel.NO)
        model = ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'invalid')),
                             'CoCoVariableNotDefined.nestml'))
        self.assertEqual(len(Logger.get_all_messages_of_level_and_or_neuron(model.get_neuron_list()[0],
                                                                            LoggingLevel.ERROR)), 4)

    def test_valid(self):
        Logger.set_logging_level(LoggingLevel.NO)
        model = ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'valid')),
                             'CoCoVariableNotDefined.nestml'))
        self.assertEqual(
                len(Logger.get_all_messages_of_level_and_or_neuron(model.get_neuron_list()[0], LoggingLevel.ERROR)),
                0)


class VariableRedeclaration(unittest.TestCase):
    def test_invalid(self):
        Logger.set_logging_level(LoggingLevel.NO)
        model = ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'invalid')),
                             'CoCoVariableRedeclared.nestml'))
        self.assertEqual(len(
                Logger.get_all_messages_of_level_and_or_neuron(model.get_neuron_list()[0], LoggingLevel.ERROR)), 2)

    def test_valid(self):
        Logger.set_logging_level(LoggingLevel.NO)
        model = ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'valid')),
                             'CoCoVariableRedeclared.nestml'))
        self.assertEqual(len(
                Logger.get_all_messages_of_level_and_or_neuron(model.get_neuron_list()[0], LoggingLevel.ERROR)), 0)


class EachBlockUnique(unittest.TestCase):
    def test_invalid(self):
        Logger.set_logging_level(LoggingLevel.NO)
        model = ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'invalid')),
                             'CoCoEachBlockUnique.nestml'))
        self.assertEqual(len(
                Logger.get_all_messages_of_level_and_or_neuron(model.get_neuron_list()[0], LoggingLevel.ERROR)), 2)

    def test_valid(self):
        Logger.set_logging_level(LoggingLevel.NO)
        model = ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'valid')),
                             'CoCoEachBlockUnique.nestml'))
        self.assertEqual(len(
                Logger.get_all_messages_of_level_and_or_neuron(model.get_neuron_list()[0], LoggingLevel.ERROR)), 0)


class FunctionUniqueAndDefined(unittest.TestCase):
    def test_invalid(self):
        Logger.set_logging_level(LoggingLevel.NO)
        model = ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'invalid')),
                             'CoCoFunctionNotUnique.nestml'))
        self.assertEqual(
                len(Logger.get_all_messages_of_level_and_or_neuron(model.get_neuron_list()[0], LoggingLevel.ERROR)), 4)

    def test_valid(self):
        Logger.set_logging_level(LoggingLevel.NO)
        model = ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'valid')),
                             'CoCoFunctionNotUnique.nestml'))
        self.assertEqual(
                len(Logger.get_all_messages_of_level_and_or_neuron(model.get_neuron_list()[0], LoggingLevel.ERROR)), 0)


class FunctionsHaveRhs(unittest.TestCase):
    def test_invalid(self):
        Logger.set_logging_level(LoggingLevel.NO)
        model = ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'invalid')),
                             'CoCoFunctionHasNoRhs.nestml'))
        self.assertEqual(len(
                Logger.get_all_messages_of_level_and_or_neuron(model.get_neuron_list()[0], LoggingLevel.ERROR)), 1)

    def test_valid(self):
        Logger.set_logging_level(LoggingLevel.NO)
        model = ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'valid')),
                             'CoCoFunctionHasNoRhs.nestml'))
        self.assertEqual(len(
                Logger.get_all_messages_of_level_and_or_neuron(model.get_neuron_list()[0], LoggingLevel.ERROR)), 0)


class FunctionHasSeveralLhs(unittest.TestCase):
    def test_invalid(self):
        Logger.set_logging_level(LoggingLevel.NO)
        model = ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'invalid')),
                             'CoCoFunctionWithSeveralLhs.nestml'))
        self.assertEqual(len(
                Logger.get_all_messages_of_level_and_or_neuron(model.get_neuron_list()[0], LoggingLevel.ERROR)), 1)

    def test_valid(self):
        Logger.set_logging_level(LoggingLevel.NO)
        model = ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'valid')),
                             'CoCoFunctionWithSeveralLhs.nestml'))
        self.assertEqual(len(
                Logger.get_all_messages_of_level_and_or_neuron(model.get_neuron_list()[0], LoggingLevel.ERROR)), 0)


class NoValuesAssignedToBuffers(unittest.TestCase):
    def test_invalid(self):
        Logger.set_logging_level(LoggingLevel.NO)
        model = ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'invalid')),
                             'CoCoValueAssignedToBuffer.nestml'))
        self.assertEqual(len(
                Logger.get_all_messages_of_level_and_or_neuron(model.get_neuron_list()[0], LoggingLevel.ERROR)), 2)

    def test_valid(self):
        Logger.set_logging_level(LoggingLevel.NO)
        model = ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'valid')),
                             'CoCoValueAssignedToBuffer.nestml'))
        self.assertEqual(len(
                Logger.get_all_messages_of_level_and_or_neuron(model.get_neuron_list()[0], LoggingLevel.ERROR)), 0)


class OrderOfEquationsCorrect(unittest.TestCase):
    def test_invalid(self):
        Logger.set_logging_level(LoggingLevel.NO)
        model = ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'invalid')),
                             'CoCoNoOrderOfEquations.nestml'))
        self.assertEqual(len(
                Logger.get_all_messages_of_level_and_or_neuron(model.get_neuron_list()[0], LoggingLevel.ERROR)), 1)

    def test_valid(self):
        Logger.set_logging_level(LoggingLevel.NO)
        model = ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'valid')),
                             'CoCoNoOrderOfEquations.nestml'))
        self.assertEqual(len(
                Logger.get_all_messages_of_level_and_or_neuron(model.get_neuron_list()[0], LoggingLevel.ERROR)), 0)


class NumeratorOfUnitOne(unittest.TestCase):
    def test_invalid(self):
        Logger.set_logging_level(LoggingLevel.NO)
        model = ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'invalid')),
                             'CoCoUnitNumeratorNotOne.nestml'))
        self.assertEqual(len(Logger.get_all_messages_of_level_and_or_neuron(model.get_neuron_list()[0],
                                                                            LoggingLevel.ERROR)), 2)

    def test_valid(self):
        Logger.set_logging_level(LoggingLevel.NO)
        model = ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'valid')),
                             'CoCoUnitNumeratorNotOne.nestml'))
        self.assertEqual(len(
                Logger.get_all_messages_of_level_and_or_neuron(model.get_neuron_list()[0], LoggingLevel.ERROR)), 0)


class NamesOfNeuronsUnique(unittest.TestCase):
    def test_invalid(self):
        Logger.init_logger(LoggingLevel.NO)
        ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'invalid')),
                             'CoCoMultipleNeuronsWithEqualName.nestml'))
        self.assertEqual(len(Logger.get_all_messages_of_level_and_or_neuron(None, LoggingLevel.ERROR)), 1)

    def test_valid(self):
        Logger.init_logger(LoggingLevel.NO)
        ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'valid')),
                             'CoCoMultipleNeuronsWithEqualName.nestml'))
        self.assertEqual(len(Logger.get_all_messages_of_level_and_or_neuron(None, LoggingLevel.ERROR)), 0)


class NoNestCollision(unittest.TestCase):
    def test_invalid(self):
        Logger.set_logging_level(LoggingLevel.NO)
        model = ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'invalid')),
                             'CoCoNestNamespaceCollision.nestml'))
        self.assertEqual(len(
                Logger.get_all_messages_of_level_and_or_neuron(model.get_neuron_list()[0], LoggingLevel.ERROR)), 1)

    def test_valid(self):
        Logger.set_logging_level(LoggingLevel.NO)
        model = ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'valid')),
                             'CoCoNestNamespaceCollision.nestml'))
        self.assertEqual(len(
                Logger.get_all_messages_of_level_and_or_neuron(model.get_neuron_list()[0], LoggingLevel.ERROR)), 0)


class RedundantBufferKeywordsDetected(unittest.TestCase):
    def test_invalid(self):
        Logger.set_logging_level(LoggingLevel.NO)
        model = ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'invalid')),
                             'CoCoBufferWithRedundantTypes.nestml'))
        self.assertEqual(len(
                Logger.get_all_messages_of_level_and_or_neuron(model.get_neuron_list()[0], LoggingLevel.ERROR)), 1)

    def test_valid(self):
        Logger.set_logging_level(LoggingLevel.NO)
        model = ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'valid')),
                             'CoCoBufferWithRedundantTypes.nestml'))
        self.assertEqual(len(
                Logger.get_all_messages_of_level_and_or_neuron(model.get_neuron_list()[0], LoggingLevel.ERROR)), 0)


class ParametersAssignedOnlyInParametersBlock(unittest.TestCase):
    def test_invalid(self):
        Logger.set_logging_level(LoggingLevel.NO)
        model = ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'invalid')),
                             'CoCoParameterAssignedOutsideBlock.nestml'))
        self.assertEqual(len(
                Logger.get_all_messages_of_level_and_or_neuron(model.get_neuron_list()[0], LoggingLevel.ERROR)), 1)

    def test_valid(self):
        Logger.set_logging_level(LoggingLevel.NO)
        model = ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'valid')),
                             'CoCoParameterAssignedOutsideBlock.nestml'))
        self.assertEqual(len(
                Logger.get_all_messages_of_level_and_or_neuron(model.get_neuron_list()[0], LoggingLevel.ERROR)), 0)


class CurrentBuffersNotSpecifiedWithKeywords(unittest.TestCase):
    def test_invalid(self):
        Logger.set_logging_level(LoggingLevel.NO)
        model = ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'invalid')),
                             'CoCoCurrentBufferTypeSpecified.nestml'))
        self.assertEqual(len(
                Logger.get_all_messages_of_level_and_or_neuron(model.get_neuron_list()[0], LoggingLevel.ERROR)), 1)

    def test_valid(self):
        Logger.set_logging_level(LoggingLevel.NO)
        model = ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'valid')),
                             'CoCoCurrentBufferTypeSpecified.nestml'))
        self.assertEqual(len(
                Logger.get_all_messages_of_level_and_or_neuron(model.get_neuron_list()[0], LoggingLevel.ERROR)), 0)


class SpikeBufferWithoutDatatype(unittest.TestCase):
    def test_invalid(self):
        Logger.set_logging_level(LoggingLevel.NO)
        model = ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'invalid')),
                             'CoCoSpikeBufferWithoutType.nestml'))
        self.assertEqual(len(
                Logger.get_all_messages_of_level_and_or_neuron(model.get_neuron_list()[0], LoggingLevel.ERROR)), 1)

    def test_valid(self):
        Logger.set_logging_level(LoggingLevel.NO)
        model = ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'valid')),
                             'CoCoSpikeBufferWithoutType.nestml'))
        self.assertEqual(len(
                Logger.get_all_messages_of_level_and_or_neuron(model.get_neuron_list()[0], LoggingLevel.ERROR)), 0)


class FunctionWithWrongArgNumberDetected(unittest.TestCase):
    def test_invalid(self):
        Logger.set_logging_level(LoggingLevel.NO)
        model = ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'invalid')),
                             'CoCoFunctionCallNotConsistentWrongArgNumber.nestml'))
        self.assertEqual(len(
                Logger.get_all_messages_of_level_and_or_neuron(model.get_neuron_list()[0], LoggingLevel.ERROR)), 1)

    def test_valid(self):
        Logger.set_logging_level(LoggingLevel.NO)
        model = ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'valid')),
                             'CoCoFunctionCallNotConsistentWrongArgNumber.nestml'))
        self.assertEqual(len(
                Logger.get_all_messages_of_level_and_or_neuron(model.get_neuron_list()[0], LoggingLevel.ERROR)), 0)


class InitValuesHaveRhsAndOde(unittest.TestCase):
    def test_invalid(self):
        Logger.set_logging_level(LoggingLevel.NO)
        model = ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'invalid')),
                             'CoCoInitValuesWithoutOde.nestml'))
        self.assertEqual(len(
                Logger.get_all_messages_of_level_and_or_neuron(model.get_neuron_list()[0], LoggingLevel.ERROR)), 3)

    def test_valid(self):
        Logger.set_logging_level(LoggingLevel.NO)
        model = ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'valid')),
                             'CoCoInitValuesWithoutOde.nestml'))
        self.assertEqual(len(
                Logger.get_all_messages_of_level_and_or_neuron(model.get_neuron_list()[0], LoggingLevel.ERROR)), 0)


class IncorrectReturnStmtDetected(unittest.TestCase):
    def test_invalid(self):
        Logger.set_logging_level(LoggingLevel.NO)
        model = ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'invalid')),
                             'CoCoIncorrectReturnStatement.nestml'))
        self.assertEqual(len(
                Logger.get_all_messages_of_level_and_or_neuron(model.get_neuron_list()[0], LoggingLevel.ERROR)), 4)

    def test_valid(self):
        Logger.set_logging_level(LoggingLevel.NO)
        model = ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'valid')),
                             'CoCoIncorrectReturnStatement.nestml'))
        self.assertEqual(len(
                Logger.get_all_messages_of_level_and_or_neuron(model.get_neuron_list()[0], LoggingLevel.ERROR)), 0)


class OdeVarsOutsideInitBlockDetected(unittest.TestCase):
    def test_invalid(self):
        Logger.set_logging_level(LoggingLevel.NO)
        model = ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'invalid')),
                             'CoCoOdeVarNotInInitialValues.nestml'))
        self.assertEqual(len(
                Logger.get_all_messages_of_level_and_or_neuron(model.get_neuron_list()[0], LoggingLevel.ERROR)), 1)

    def test_valid(self):
        Logger.set_logging_level(LoggingLevel.NO)
        model = ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'valid')),
                             'CoCoOdeVarNotInInitialValues.nestml'))
        self.assertEqual(len(
                Logger.get_all_messages_of_level_and_or_neuron(model.get_neuron_list()[0], LoggingLevel.ERROR)), 0)


class ConvolveCorrectlyDefined(unittest.TestCase):
    def test_invalid(self):
        Logger.set_logging_level(LoggingLevel.NO)
        model = ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'invalid')),
                             'CoCoConvolveNotCorrectlyProvided.nestml'))
        self.assertEqual(len(Logger.get_all_messages_of_level_and_or_neuron(model.get_neuron_list()[0],
                                                                            LoggingLevel.ERROR)), 3)

    def test_valid(self):
        Logger.set_logging_level(LoggingLevel.NO)
        model = ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'valid')),
                             'CoCoConvolveNotCorrectlyProvided.nestml'))
        self.assertEqual(len(
                Logger.get_all_messages_of_level_and_or_neuron(model.get_neuron_list()[0], LoggingLevel.ERROR)), 0)


class VectorInNonVectorDeclarationDetected(unittest.TestCase):
    def test_invalid(self):
        Logger.set_logging_level(LoggingLevel.NO)
        model = ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'invalid')),
                             'CoCoVectorInNonVectorDeclaration.nestml'))
        self.assertEqual(len(
                Logger.get_all_messages_of_level_and_or_neuron(model.get_neuron_list()[0], LoggingLevel.ERROR)), 1)

    def test_valid(self):
        Logger.set_logging_level(LoggingLevel.NO)
        model = ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'valid')),
                             'CoCoVectorInNonVectorDeclaration.nestml'))
        self.assertEqual(len(
                Logger.get_all_messages_of_level_and_or_neuron(model.get_neuron_list()[0], LoggingLevel.ERROR)), 0)


class SumCorrectlyParametrized(unittest.TestCase):
    def test_invalid(self):
        Logger.set_logging_level(LoggingLevel.NO)
        model = ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'invalid')),
                             'CoCoSumNotCorrectlyParametrized.nestml'))
        self.assertEqual(len(
                Logger.get_all_messages_of_level_and_or_neuron(model.get_neuron_list()[0], LoggingLevel.ERROR)), 2)

    def test_valid(self):
        Logger.set_logging_level(LoggingLevel.NO)
        model = ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'valid')),
                             'CoCoSumNotCorrectlyParametrized.nestml'))
        self.assertEqual(len(Logger.get_all_messages_of_level_and_or_neuron(model.get_neuron_list()[0],
                                                                            LoggingLevel.ERROR)), 0)


class InvariantCorrectlyTyped(unittest.TestCase):
    def test_invalid(self):
        Logger.set_logging_level(LoggingLevel.NO)
        model = ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'invalid')),
                             'CoCoInvariantNotBool.nestml'))
        self.assertEqual(len(
                Logger.get_all_messages_of_level_and_or_neuron(model.get_neuron_list()[0], LoggingLevel.ERROR)), 1)

    def test_valid(self):
        Logger.set_logging_level(LoggingLevel.NO)
        model = ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'valid')),
                             'CoCoInvariantNotBool.nestml'))
        self.assertEqual(len(
                Logger.get_all_messages_of_level_and_or_neuron(model.get_neuron_list()[0], LoggingLevel.ERROR)), 0)


class ExpressionCorrectlyTyped(unittest.TestCase):
    def test_invalid(self):
        Logger.set_logging_level(LoggingLevel.NO)
        model = ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'invalid')),
                             'CoCoIllegalExpression.nestml'))
        self.assertEqual(len(Logger.get_all_messages_of_level_and_or_neuron(model.get_neuron_list()[0],
                                                                            LoggingLevel.ERROR)), 6)

    def test_valid(self):
        Logger.set_logging_level(LoggingLevel.NO)
        model = ModelParser.parse_model(
                os.path.join(os.path.realpath(os.path.join(os.path.dirname(__file__), 'valid')),
                             'CoCoIllegalExpression.nestml'))
        self.assertEqual(len(
                Logger.get_all_messages_of_level_and_or_neuron(model.get_neuron_list()[0], LoggingLevel.ERROR)), 0)


if __name__ == '__main__':
    unittest.main()
