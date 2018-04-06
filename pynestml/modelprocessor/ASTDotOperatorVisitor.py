#
# ASTDotOperatorVisitortor.py
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

"""
rhs : left=rhs (timesOp='*' | divOp='/' | moduloOp='%') right=rhs
"""
from pynestml.modelprocessor.ASTArithmeticOperator import ASTArithmeticOperator
from pynestml.modelprocessor.PredefinedTypes import PredefinedTypes
from pynestml.modelprocessor.ErrorStrings import ErrorStrings
from pynestml.modelprocessor.ASTVisitor import ASTVisitor
from pynestml.modelprocessor.Either import Either
from pynestml.modelprocessor.ASTExpression import ASTExpression
from pynestml.utils.Logger import Logger, LoggingLevel
from pynestml.utils.Messages import MessageCode


class ASTDotOperatorVisitor(ASTVisitor):
    """
    This visitor is used to derive the correct type of expressions which use a binary dot operator.
    """

    def visit_expression(self, node):
        """
        Visits a single rhs and updates the type.
        :param node: a single rhs
        :type node: ASTExpression
        """
        lhs_type_e = node.get_lhs().get_type_either()
        rhs_type_e = node.get_rhs().get_type_either()

        if lhs_type_e.is_error():
            node.set_type_either(lhs_type_e)
            return

        if rhs_type_e.is_error():
            node.set_type_either(rhs_type_e)
            return

        lhs_type = lhs_type_e.get_value()
        rhs_type = rhs_type_e.get_value()

        arith_op = node.get_binary_operator()
        # arithOp exists if we get into this visitor, but make sure:
        assert arith_op is not None and isinstance(arith_op, ASTArithmeticOperator)

        if arith_op.is_modulo_op:
            if lhs_type.is_integer() and rhs_type.is_integer():
                node.set_type_either(Either.value(PredefinedTypes.get_integer_type()))
                return
            else:
                error_msg = ErrorStrings.messageExpectedInt(self, node.get_source_position())
                node.set_type_either(Either.error(error_msg))
                Logger.log_message(code=MessageCode.TYPE_DIFFERENT_FROM_EXPECTED,
                                   message=error_msg,
                                   error_position=node.get_source_position(),
                                   log_level=LoggingLevel.ERROR)
                return
        if arith_op.is_div_op or arith_op.is_times_op:
            if lhs_type.is_numeric() and rhs_type.is_numeric():
                # If both are units, calculate resulting Type
                if lhs_type.is_unit() and rhs_type.is_unit():
                    left_unit = lhs_type.get_encapsulated_unit()
                    right_unit = rhs_type.get_encapsulated_unit()
                    if arith_op.is_times_op:
                        return_type = PredefinedTypes.get_type(left_unit * right_unit)
                        node.set_type_either(Either.value(return_type))
                        return
                    elif arith_op.is_div_op:
                        return_type = PredefinedTypes.get_type(left_unit / right_unit)
                        node.set_type_either(Either.value(return_type))
                        return
                # if lhs is Unit, and rhs real or integer, return same Unit
                if lhs_type.is_unit():
                    node.set_type_either(Either.value(lhs_type))
                    return
                # if lhs is real or integer and rhs a unit, return unit for timesOP and inverse(unit) for divOp
                if rhs_type.is_unit():
                    if arith_op.is_times_op:
                        node.set_type_either(Either.value(rhs_type))
                        return
                    elif arith_op.is_div_op:
                        right_unit = rhs_type.get_encapsulated_unit()
                        return_type = PredefinedTypes.get_type(1 / right_unit)
                        node.set_type_either(Either.value(return_type))
                        return
                # if no Units are involved, Real takes priority
                if lhs_type.is_real() or rhs_type.is_real():
                    node.set_type_either(Either.value(PredefinedTypes.get_real_type()))
                    return
                # here, both are integers, but check to be sure
                if lhs_type.is_integer() and rhs_type.is_integer():
                    node.set_type_either(Either.value(PredefinedTypes.get_integer_type()))
                    return
        # Catch-all if no case has matched
        type_mismatch = lhs_type.print_symbol() + " / " if arith_op.is_div_op else " * " + rhs_type.print_symbol()
        error_msg = ErrorStrings.messageTypeMismatch(self, type_mismatch, node.get_source_position())
        node.set_type_either(Either.error(error_msg))
        Logger.log_message(message=error_msg,
                           code=MessageCode.TYPE_DIFFERENT_FROM_EXPECTED,
                           error_position=node.get_source_position(),
                           log_level=LoggingLevel.ERROR)
