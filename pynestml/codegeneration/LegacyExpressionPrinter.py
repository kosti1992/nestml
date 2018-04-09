#
# LegacyExpressionPrinter.py
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
from pynestml.codegeneration.ExpressionsPrettyPrinter import ExpressionsPrettyPrinter
from pynestml.utils.Logger import LoggingLevel, Logger
from pynestml.modelprocessor.ASTSimpleExpression import ASTSimpleExpression
from pynestml.modelprocessor.ASTExpression import ASTExpression
from pynestml.modelprocessor.ASTArithmeticOperator import ASTArithmeticOperator
from pynestml.modelprocessor.ASTBitOperator import ASTBitOperator
from pynestml.modelprocessor.ASTComparisonOperator import ASTComparisonOperator
from pynestml.modelprocessor.ASTLogicalOperator import ASTLogicalOperator
from pynestml.modelprocessor.PredefinedUnits import PredefinedUnits
from pynestml.codegeneration.UnitConverter import UnitConverter
from pynestml.codegeneration.IdempotentReferenceConverter import IdempotentReferenceConverter


class LegacyExpressionPrinter(ExpressionsPrettyPrinter):
    """
    An adjusted version of the pretty printer which does not print units with literals.
    """
    __referenceConverter = None
    __typesPrinter = None

    def __init__(self, _referenceConverter=None):
        """
        Standard constructor.
        :param _referenceConverter: a single reference converter object.
        :type _referenceConverter: IReferenceConverter
        """
        from pynestml.codegeneration.ExpressionsPrettyPrinter import TypesPrinter
        super(LegacyExpressionPrinter, self).__init__(_referenceConverter)
        if _referenceConverter is not None:
            self.__referenceConverter = _referenceConverter
        else:
            self.__referenceConverter = IdempotentReferenceConverter()
        self.__typesPrinter = TypesPrinter()

    def doPrint(self, _expr=None):
        """
        Prints a single rhs.
        :param _expr: a single rhs.
        :type _expr: ASTExpression or ASTSimpleExpression.
        :return: string representation of the rhs
        :rtype: str
        """
        if isinstance(_expr, ASTSimpleExpression):
            if _expr.is_numeric_literal():
                return self.__referenceConverter.convertConstant(_expr.get_numeric_literal())
            elif _expr.is_inf_literal():
                return self.__referenceConverter.convertConstant('inf')
            elif _expr.is_string():
                return self.__referenceConverter.convertConstant(_expr.get_string())
            elif _expr.is_boolean_true():
                return self.__referenceConverter.convertConstant('True')
            elif _expr.is_boolean_false():
                return self.__referenceConverter.convertConstant('False')
            elif _expr.is_variable():
                return self.__referenceConverter.convertNameReference(_expr.get_variable())
            elif _expr.is_function_call():
                return self.printFunctionCall(_expr.get_function_call())
        elif isinstance(_expr, ASTExpression):
            if _expr.is_unary_operator():
                if _expr.get_unary_operator().isUnaryPlus():
                    return '(' + self.__referenceConverter.convertUnaryOp('+') + \
                           self.printExpression(_expr.get_expression()) + ')'
                elif _expr.get_unary_operator().isUnaryMinus():
                    return '(' + self.__referenceConverter.convertUnaryOp('-') + \
                           self.printExpression(_expr.get_expression()) + ')'
                elif _expr.get_unary_operator().isUnaryTilde():
                    return '(' + self.__referenceConverter.convertUnaryOp('~') + \
                           self.printExpression(_expr.get_expression()) + ')'
            elif _expr.is_encapsulated:
                return '(' + self.printExpression(_expr.get_expression()) + ')'
            # logical not
            elif _expr.isLogicalNot():
                return self.__referenceConverter.convertUnaryOp('not') + ' ' + \
                       self.printExpression(_expr.get_expression())
            # compound rhs with lhs + rhs
            elif isinstance(_expr, ASTExpression):
                # a unary operator
                if _expr.is_unary_operator():
                    op = self.__referenceConverter.convertUnaryOp(_expr.get_unary_operator())
                    rhs = self.printExpression(_expr.get_expression())
                    return op % rhs
                # encapsulated in brackets
                elif _expr.is_encapsulated:
                    return self.__referenceConverter.convertEncapsulated() % self.printExpression(
                        _expr.get_expression())
                # logical not
                elif _expr.isLogicalNot():
                    op = self.__referenceConverter.convertLogicalNot()
                    rhs = self.printExpression(_expr.get_expression())
                    return op % rhs
                # compound rhs with lhs + rhs
                elif _expr.is_compound_expression():
                    lhs = self.printExpression(_expr.get_lhs())
                    op = self.__referenceConverter.convertBinaryOp(_expr.get_binary_operator())
                    rhs = self.printExpression(_expr.get_rhs())
                    return op % (lhs, rhs)
                elif _expr.is_ternary_operator():
                    condition = self.printExpression(_expr.get_condition())
                    ifTrue = self.printExpression(_expr.get_if_true())
                    ifNot = self.printExpression(_expr.if_not)
                    return self.__referenceConverter.convertTernaryOperator() % (condition, ifTrue, ifNot)
        else:
            raise RuntimeError('Unsupported expression!')
