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
from pynestml.utils.type_caster import TypeCaster
from pynestml.meta_model.ast_comparison_operator import ASTComparisonOperator
from pynestml.symbols.type_symbol import TypeSymbol
from pynestml.symbols.void_type_symbol import VoidTypeSymbol
from pynestml.symbols.string_type_symbol import StringTypeSymbol
from pynestml.symbols.integer_type_symbol import IntegerTypeSymbol
from pynestml.symbols.real_type_symbol import RealTypeSymbol
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
        pass

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
