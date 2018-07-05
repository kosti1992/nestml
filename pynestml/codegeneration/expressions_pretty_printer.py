#
# expressions_pretty_printer.py
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
from pynestml.codegeneration.i_reference_converter import IReferenceConverter
from pynestml.codegeneration.idempotent_reference_converter import IdempotentReferenceConverter
from pynestml.meta_model.ast_expression import ASTExpression
from pynestml.meta_model.ast_expression_node import ASTExpressionNode
from pynestml.meta_model.ast_function_call import ASTFunctionCall
from pynestml.meta_model.ast_simple_expression import ASTSimpleExpression
from pynestml.utils.ast_utils import ASTUtils


class ExpressionsPrettyPrinter(object):
    """
    Converts expressions to the executable platform dependent code. By using different
    referenceConverters for the handling of variables, names, and functions can be adapted. For this,
    implement own IReferenceConverter specialisation.
    This class is used to transform only parts of the procedural language and not nestml in whole.
    """

    def __init__(self, reference_converter = None):
        # type: (IReferenceConverter) -> None
        # todo by kp: this should expect a ITypesPrinter as the second arg
        if reference_converter is not None:
            self.reference_converter = reference_converter
        else:
            self.reference_converter = IdempotentReferenceConverter()

    def print_expression(self, node):
        # type: (ASTExpressionNode) -> str
        if node.get_implicit_conversion_factor() is not None:
            return str(node.get_implicit_conversion_factor()) + ' * (' + self.__do_print(node) + ')'
        else:
            return self.__do_print(node)

    def __do_print(self, node):
        # type: (ASTExpressionNode) -> str
        if isinstance(node, ASTSimpleExpression):
            if node.has_unit():
                # todo by kp: this should not be done in the typesPrinter, obsolete
                return self.reference_converter.convert_numeric(node.get_numeric_literal()) + '*' + \
                       self.reference_converter.convert_name_reference(node.get_variable())
            elif node.is_numeric_literal():
                return str(node.get_numeric_literal())
            elif node.is_inf_literal:
                return self.reference_converter.convert_constant('inf')
            elif node.is_string():
                return self.reference_converter.convert_string(node.get_string())
            elif node.is_boolean_true:
                return self.reference_converter.convert_bool(True)
            elif node.is_boolean_false:
                return self.reference_converter.convert_bool(False)
            elif node.is_variable():
                return self.reference_converter.convert_name_reference(node.get_variable())
            elif node.is_function_call():
                return self.print_function_call(node.get_function_call())
        elif isinstance(node, ASTExpression):
            # a unary operator
            if node.is_unary_operator():
                op = self.reference_converter.convert_unary_op(node.get_unary_operator())
                rhs = self.print_expression(node.get_expression())
                return op % rhs
            # encapsulated in brackets
            elif node.is_encapsulated:
                return self.reference_converter.convert_encapsulated() % self.print_expression(node.get_expression())
            # logical not
            elif node.is_logical_not:
                op = self.reference_converter.convert_logical_not()
                rhs = self.print_expression(node.get_expression())
                return op % rhs
            # compound rhs with lhs + rhs
            elif node.is_compound_expression():
                lhs = self.print_expression(node.get_lhs())
                op = self.reference_converter.convert_binary_op(node.get_binary_operator())
                rhs = self.print_expression(node.get_rhs())
                return op % (lhs, rhs)
            elif node.is_ternary_operator():
                condition = self.print_expression(node.get_condition())
                if_true = self.print_expression(node.get_if_true())
                if_not = self.print_expression(node.if_not)
                return self.reference_converter.convert_ternary_operator() % (condition, if_true, if_not)
        else:
            raise RuntimeError('Unsupported rhs in rhs pretty printer!')

    def print_function_call(self, function_call):
        # type: (ASTFunctionCall) -> str
        function_name = self.reference_converter.convert_function_call(function_call)
        if ASTUtils.needs_arguments(function_call):
            return function_name % self.print_function_call_arguments(function_call)
        else:
            return function_name

    def print_function_call_arguments(self, function_call):
        # type: (ASTFunctionCall) -> str
        ret = ''
        for arg in function_call.get_args():
            ret += self.print_expression(arg)
            if function_call.get_args().index(arg) < len(function_call.get_args()) - 1:
                ret += ', '
        return ret
