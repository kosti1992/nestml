#
# gsl_reference_converter.py
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
from pynestml.codegeneration.gsl_names_converter import GSLNamesConverter
from pynestml.codegeneration.i_reference_converter import IReferenceConverter
from pynestml.codegeneration.nest_names_converter import NestNamesConverter
from pynestml.codegeneration.nest_reference_converter import NESTReferenceConverter
from pynestml.codegeneration.unit_converter import UnitConverter
from pynestml.meta_model.ast_function_call import ASTFunctionCall
from pynestml.meta_model.ast_variable import ASTVariable
from pynestml.symbols.predefined_functions import PredefinedFunctions
from pynestml.symbols.predefined_units import PredefinedUnits
from pynestml.symbols.predefined_variables import PredefinedVariables
from pynestml.symbols.symbol import SymbolKind


class GSLReferenceConverter(IReferenceConverter):
    """
    This class is used to convert operators and constant to the GSL (GNU Scientific Library) processable format.
    """
    maximal_exponent = 10.0

    def __init__(self, is_upper_bound = False):
        """
        Standard constructor.
        :param is_upper_bound: Indicates whether an upper bound for the exponent shall be used.
        :type is_upper_bound: bool
        """
        self.is_upper_bound = is_upper_bound

    def convert_name_reference(self, ast_variable):
        """
        Converts a single name reference to a gsl processable format.
        :param ast_variable: a single variable
        :type ast_variable: ASTVariable
        :return: a gsl processable format of the variable
        :rtype: str
        """
        variable_name = NestNamesConverter.convert_to_cpp_name(ast_variable.get_name())
        symbol = ast_variable.get_scope().resolve_to_symbol(ast_variable.get_complete_name(), SymbolKind.VARIABLE)

        if PredefinedUnits.is_unit(ast_variable.get_complete_name()):
            return str(
                    UnitConverter.get_factor(PredefinedUnits.get_unit(ast_variable.get_complete_name()).get_unit()))
        if symbol.is_init_values():
            return GSLNamesConverter.name(symbol)
        elif symbol.is_buffer():
            return 'node.B_.' + NestNamesConverter.buffer_value(symbol)
        elif variable_name == PredefinedVariables.E_CONSTANT:
            return 'numerics::e'
        elif symbol.is_local() or symbol.is_function:
            return variable_name
        elif symbol.has_vector_parameter():
            return 'node.get_' + variable_name + '()[i]'
        else:
            return 'node.get_' + variable_name + '()'

    def convert_function_call(self, function_call):
        """
        Converts a single function call to a gsl processable format.
        :param function_call: a single function call
        :type function_call: ASTFunctionCall
        :return: a string representation
        :rtype: str
        """
        function_name = function_call.get_name()
        if function_name == 'resolution':
            return 'nest::Time::get_resolution().get_ms()'
        if function_name == 'steps':
            return 'nest::Time(nest::Time::ms((double) %s)).get_steps()'
        if function_name == PredefinedFunctions.POW:
            return 'std::pow(%s)'
        if function_name == PredefinedFunctions.LOG:
            return 'std::log(%s)'
        if function_name == PredefinedFunctions.EXPM1:
            return 'numerics::expm1(%s)'
        if function_name == PredefinedFunctions.EXP:
            if self.is_upper_bound:
                return 'std::exp(std::min(%s,' + str(self.maximal_exponent) + '))'
            else:
                return 'std::exp(%s)'
        if function_name == PredefinedFunctions.MAX or function_name == PredefinedFunctions.BOUNDED_MAX:
            return 'std::max(%s)'
        if function_name == PredefinedFunctions.MIN or function_name == PredefinedFunctions.BOUNDED_MIN:
            return 'std::min(%s)'
        if function_name == PredefinedFunctions.EMIT_SPIKE:
            return 'set_spiketime(nest::Time::step(origin.get_steps()+lag+1));\n' \
                   'nest::SpikeEvent se;\n' \
                   'nest::kernel().event_delivery_manager.send(*this, se, lag)'
        raise RuntimeError('Cannot map the function: "' + function_name + '".')

    def convert_constant(self, constant_name):
        """
        No modifications to the constant required.
        :param constant_name: a single constant.
        :type constant_name: str
        :return: the same constant
        :rtype: str
        """
        return constant_name

    def convert_unary_op(self, unary_operator):
        """
        No modifications to the operator required.
        :param unary_operator: a string of a unary operator.
        :type unary_operator: str
        :return: the same operator
        :rtype: str
        """
        return str(unary_operator) + '(%s)'

    def convert_binary_op(self, binary_operator):
        """
        Converts a singe binary operator. Here, we have only to regard the pow operator in a special manner.
        :param binary_operator: a binary operator in string representation.
        :type binary_operator:  str
        :return: a string representing the included binary operator.
        :rtype: str
        """
        from pynestml.meta_model.ast_arithmetic_operator import ASTArithmeticOperator
        if isinstance(binary_operator, ASTArithmeticOperator) and binary_operator.is_pow_op:
            return 'pow(%s, %s)'
        else:
            return '%s' + str(binary_operator) + '%s'

    def convert_logical_not(self):
        return NESTReferenceConverter.convert_logical_not()

    def convert_logical_operator(self, op):
        return NESTReferenceConverter.convert_logical_operator(op)

    def convert_comparison_operator(self, op):
        return NESTReferenceConverter.convert_comparison_operator(op)

    def convert_bit_operator(self, op):
        return NESTReferenceConverter.convert_bit_operator(op)

    def convert_encapsulated(self):
        return NESTReferenceConverter.convert_encapsulated()

    def convert_ternary_operator(self):
        return NESTReferenceConverter.convert_ternary_operator()

    def convert_arithmetic_operator(self, op):
        return NESTReferenceConverter.convert_arithmetic_operator(op)

    def convert_bool(self, value):
        return NESTReferenceConverter.convert_bool(value)

    def convert_numeric(self, value):
        return NESTReferenceConverter.convert_numeric(value)

    def convert_string(self, value):
        return NESTReferenceConverter.convert_string(value)
