#
# co_co_constraint_block_correctly_built.py
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
from pynestml.cocos.co_co import CoCo
from pynestml.meta_model.ast_neuron import ASTNeuron
from pynestml.meta_model.ast_constraint import ASTConstraint
from pynestml.utils.type_caster import TypeCaster
from pynestml.meta_model.ast_comparison_operator import ASTComparisonOperator
from pynestml.symbols.symbol import SymbolKind
from pynestml.symbols.type_symbol import TypeSymbol
from pynestml.symbols.void_type_symbol import VoidTypeSymbol
from pynestml.symbols.string_type_symbol import StringTypeSymbol
from pynestml.symbols.integer_type_symbol import IntegerTypeSymbol
from pynestml.symbols.real_type_symbol import RealTypeSymbol
from pynestml.meta_model.ast_simple_expression import ASTSimpleExpression
from pynestml.symbols.boolean_type_symbol import BooleanTypeSymbol
from pynestml.symbols.unit_type_symbol import UnitTypeSymbol
from pynestml.utils.messages import Messages
from pynestml.utils.logger import LoggingLevel, Logger
from pynestml.utils.ast_utils import ASTUtils


class CoCoConstraintBlockCorrectlyBuilt(CoCo):
    """
    This coco checks whether the constraint block has been correctly constructed. I.e., that boundaries are not
    empty (e.g. 1 < V < 0 shall be detected) and types are equal.

    """
    __current_neuron = None  # TODO: we have to make cocos objects, not classes

    @classmethod
    def check_co_co(cls, node):
        # type: (ASTNeuron) -> None
        cls.__current_neuron = node
        if node.get_constraint_block() is None:
            # no constraints, thus nothing to do
            return
        for const in node.get_constraint_block().constraints:
            if const.left_bound is not None:
                # first check whether the types are suitable
                cls.__bound_typing_check(const.left_bound, const.variable)
                # now check if the comparison operators are even allowed for selected types,
                # e.g. True < False -> invalid, while 1 < 2 -> valid
                cls.__operator_valid(const.variable.get_type_symbol(), const.left_bound_type)
            # now the other side
            if const.right_bound is not None:
                cls.__bound_typing_check(const.right_bound, const.variable)
                cls.__operator_valid(const.variable.get_type_symbol(), const.right_bound_type)
            # finally check whether the bounds are sat
            cls.__bound_sat_check(const)

    @classmethod
    def __bound_typing_check(cls, bound, var):
        # the most simple case: both are equal
        if bound.type.equals(var.get_type_symbol()):
            return
        else:
            # not equal, check if differ in magnitude or castable and drop otherwise an error
            TypeCaster.try_to_recover_or_error(var.get_type_symbol(), bound.type, bound)

    @classmethod
    def __bound_sat_check(cls, constraint):
        # type: (ASTConstraint) -> None
        from pynestml.meta_model.ast_expression import ASTExpression
        if constraint.left_bound is not None and constraint.right_bound is not None:
            # the bound sat checks are only available for simple expression
            if isinstance(constraint.left_bound, ASTExpression) and not constraint.left_bound.is_unary_operator():
                # report
                code, message = Messages.get_sat_check_only_for_simple_expressions(constraint, True)
                Logger.log_message(code=code, message=message,
                                   error_position=constraint.left_bound.get_source_position(),
                                   log_level=LoggingLevel.WARNING, neuron=cls.__current_neuron)
                return
            if isinstance(constraint.left_bound, ASTExpression) and not constraint.left_bound.is_unary_operator():
                code, message = Messages.get_sat_check_only_for_simple_expressions(constraint, False)
                Logger.log_message(code=code, message=message,
                                   error_position=constraint.right_bound.get_source_position(),
                                   log_level=LoggingLevel.WARNING, neuron=cls.__current_neuron)
                return

            lower = ASTUtils.get_lower_bound_of_constraint(constraint)
            upper = ASTUtils.get_upper_bound_of_constraint(constraint)
            if len(lower) == 0 or len(upper) == 0:
                # it is not bound in one direction, ignore the next steps
                return
            lower[0] = get_simple_expression(lower[0])
            upper[0] = get_simple_expression(upper[0])
            symbol = constraint.get_scope().resolve_to_symbol(constraint.variable.get_complete_name(),
                                                              SymbolKind.VARIABLE)

            # only if it is bound from both sides, we have to check if it is sat
            if len(lower) == 1 and len(upper) == 1 and lower[0].type.is_numeric() and upper[0].type.is_numeric():

                lower_val = get_simple_expression(lower[0]).get_numeric_literal() \
                            * get_signum(ASTUtils.get_lower_bound_of_constraint(constraint)[0])
                upper_val = get_simple_expression(upper[0]).get_numeric_literal() \
                            * get_signum(ASTUtils.get_upper_bound_of_constraint(constraint)[0])
                symbol = constraint.get_scope().resolve_to_symbol(constraint.variable.get_complete_name(),
                                                                  SymbolKind.VARIABLE)
                if symbol is None:
                    return
                if isinstance(constraint.variable.type_symbol, UnitTypeSymbol):
                    if lower[0].has_unit() and not constraint.variable.type_symbol.equals(lower[0].type):
                        lower_val *= lower[0].type.get_conversion_factor(constraint.variable.type_symbol.astropy_unit,
                                                                         lower[0].type.astropy_unit)
                if upper[0].has_unit() and not constraint.variable.type_symbol.equals(upper[0].type):
                    upper_val *= upper[0].type.get_conversion_factor(constraint.variable.type_symbol.astropy_unit,
                                                                     upper[0].type.astropy_unit)
                if not (lower_val <= upper_val):
                    code, message = Messages.get_bounds_not_sat(constraint)
                    Logger.log_message(neuron=cls.__current_neuron, message=message,
                                       error_position=constraint.get_source_position(),
                                       code=code, log_level=LoggingLevel.ERROR)
                if symbol.declaring_expression is not None and isinstance(symbol.declaring_expression,
                                                                          ASTSimpleExpression):
                    if symbol.declaring_expression.is_numeric_literal() and \
                            symbol.declaring_expression.get_numeric_literal() < lower_val:
                        code, message = Messages.get_start_value_out_of_bounds(str(constraint.variable),
                                                                               str(symbol.declaring_expression),
                                                                               str(constraint))
                        Logger.log_message(neuron=cls.__current_neuron, message=message,
                                           error_position=constraint.get_source_position(),
                                           code=code, log_level=LoggingLevel.WARNING)
                    if symbol.declaring_expression.is_numeric_literal() and \
                            symbol.declaring_expression.get_numeric_literal() > upper_val:
                        code, message = Messages.get_start_value_out_of_bounds(str(constraint.variable),
                                                                               str(symbol.declaring_expression),
                                                                               str(constraint), True)
                        Logger.log_message(neuron=cls.__current_neuron, message=message,
                                           error_position=constraint.get_source_position(),
                                           code=code, log_level=LoggingLevel.WARNING)

                if symbol.initial_value is not None and isinstance(symbol.initial_value,
                                                                   ASTSimpleExpression):
                    if symbol.initial_value.is_numeric_literal() and \
                            symbol.initial_value.get_numeric_literal() < lower_val:
                        code, message = Messages.get_start_value_out_of_bounds(str(constraint.variable),
                                                                               str(symbol.initial_value),
                                                                               str(constraint))
                        Logger.log_message(neuron=cls.__current_neuron, message=message,
                                           error_position=constraint.get_source_position(),
                                           code=code, log_level=LoggingLevel.WARNING)
                    if symbol.initial_value.is_numeric_literal() and \
                            symbol.initial_value.get_numeric_literal() > upper_val:
                        code, message = Messages.get_start_value_out_of_bounds(str(constraint.variable),
                                                                               str(symbol.initial_value),
                                                                               str(constraint), True)
                        Logger.log_message(neuron=cls.__current_neuron, message=message,
                                           error_position=constraint.get_source_position(),
                                           code=code, log_level=LoggingLevel.WARNING)

    @classmethod
    def __operator_valid(cls, type_symbol, operator):
        # type: (TypeSymbol,ASTComparisonOperator) -> None
        if isinstance(type_symbol, UnitTypeSymbol):
            # physical unit types support all operators
            return
        elif isinstance(type_symbol, VoidTypeSymbol):
            # void does not support any operators at all
            code, message = Messages.get_not_supported_op_in_constraint(operator, type_symbol)
            Logger.log_message(neuron=cls.__current_neuron, message=message,
                               error_position=operator.get_source_position(),
                               code=code, log_level=LoggingLevel.ERROR)
        elif isinstance(type_symbol, StringTypeSymbol):
            if not (operator.is_eq or operator.is_ne or operator.is_ne2):
                # string only supports ==,!= and <>
                code, message = Messages.get_not_supported_op_in_constraint(operator, type_symbol)
                Logger.log_message(neuron=cls.__current_neuron, message=message,
                                   error_position=operator.get_source_position(),
                                   code=code, log_level=LoggingLevel.ERROR)
        elif isinstance(type_symbol, IntegerTypeSymbol):
            # int supports all ops
            return
        elif isinstance(type_symbol, RealTypeSymbol):
            # real supports all ops
            return
        elif isinstance(type_symbol, BooleanTypeSymbol):
            # bool supports the same set as string
            if not (operator.is_eq or operator.is_ne or operator.is_ne2):
                # string only supports ==,!= and <>
                code, message = Messages.get_not_supported_op_in_constraint(operator, type_symbol)
                Logger.log_message(neuron=cls.__current_neuron, message=message,
                                   error_position=operator.get_source_position(),
                                   code=code, log_level=LoggingLevel.ERROR)


def get_simple_expression(expr):
    from pynestml.meta_model.ast_expression import ASTExpression
    if isinstance(expr, ASTSimpleExpression):
        return expr
    elif isinstance(expr, ASTExpression) and expr.is_unary_operator():
        return expr.get_expression()
    else:
        return None


def get_signum(node):
    # type: (ASTExpressionNode) -> int
    from pynestml.meta_model.ast_expression_node import ASTExpressionNode
    from pynestml.meta_model.ast_expression import ASTExpression
    if not isinstance(node, ASTExpression) or (isinstance(node, ASTExpression) and not node.is_unary_operator()):
        return 1
    if node.get_unary_operator().is_unary_minus:
        return -1
    else:
        return 1
