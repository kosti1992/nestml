#
# ast_simple_expression.py
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

from pynestml.meta_model.ast_expression_node import ASTExpressionNode
from pynestml.meta_model.ast_function_call import ASTFunctionCall
from pynestml.meta_model.ast_variable import ASTVariable


class ASTSimpleExpression(ASTExpressionNode):
    """
    This class is used to store a simple rhs, e.g. +42mV.
    ASTSimpleExpression, consisting of a single element without combining operator, e.g.,10mV, inf, V_m.
    Grammar:
    simpleExpression : functionCall
                   | BOOLEAN_LITERAL // true & false ;
                   | (INTEGER|FLOAT) (variable)?
                   | isInf='inf'
                   | STRING_LITERAL
                   | variable;
    Attributes:
        function_call: A function call reference.
        numeric_literal: A numeric literal.
        variable: A variable reference.
        is_boolean_true (bool): True if this is a boolean true literal.
        is_boolean_false (bool): True if this is a boolean false literal.
        is_inf_literal (bool): True if this is a infinity literal.
        string (str): A string literal.

    """

    def __init__(self, function_call = None, boolean_literal = None, numeric_literal = None, is_inf = False,
                 variable = None, string = None, source_position = None):
        """
        Standard constructor.
        :param function_call: a function call.
        :type function_call: ASTFunctionCall
        :param boolean_literal: a boolean value.
        :type boolean_literal: bool
        :param numeric_literal: a numeric value.
        :type numeric_literal: float/int
        :param is_inf: is inf symbol.
        :type is_inf: bool
        :param variable: a variable object.
        :type variable: ASTVariable
        :param string: a single string literal
        :type string: str
        :param source_position: the position of this element in the source file.
        :type source_position: ASTSourceLocation.
        """
        assert (function_call is None or isinstance(function_call, ASTFunctionCall)), \
            '(PyNestML.AST.SimpleExpression) Not a function call provided (%s)!' % type(function_call)
        assert (boolean_literal is None or isinstance(boolean_literal, bool)), \
            '(PyNestML.AST.SimpleExpression) Not a bool provided (%s)!' % type(boolean_literal)
        assert (is_inf is None or isinstance(is_inf, bool)), \
            '(PyNestML.AST.SimpleExpression) Not a bool provided (%s)!' % type(is_inf)
        assert (variable is None or isinstance(variable, ASTVariable)), \
            '(PyNestML.AST.SimpleExpression) Not a variable provided (%s)!' % type(variable)
        assert (numeric_literal is None or isinstance(numeric_literal, int) or isinstance(numeric_literal, float)), \
            '(PyNestML.AST.SimpleExpression) Not a number provided (%s)!' % type(numeric_literal)
        assert (string is None or isinstance(string, str)), \
            '(PyNestML.AST.SimpleExpression) Not a string provided (%s)!' % type(string)
        super(ASTSimpleExpression, self).__init__(source_position)
        self.function_call = function_call
        self.is_boolean_true = False
        self.is_boolean_false = False
        if boolean_literal is not None:
            if boolean_literal:
                self.is_boolean_true = True
            else:
                self.is_boolean_false = True
        self.numeric_literal = numeric_literal
        self.is_inf_literal = is_inf
        self.variable = variable
        self.string = string
        return

    def is_function_call(self):
        """
        Returns whether it is a function call or not.
        :return: True if function call, otherwise False.
        :rtype: bool
        """
        return self.function_call is not None

    def get_function_call(self):
        """
        Returns the function call object.
        :return: the function call object.
        :rtype: ASTFunctionCall
        """
        return self.function_call

    def is_numeric_literal(self):
        """
        Returns whether it is a numeric literal or not.
        :return: True if numeric literal, otherwise False.
        :rtype: bool
        """
        return self.numeric_literal is not None

    def get_numeric_literal(self):
        """
        Returns the value of the numeric literal.
        :return: the value of the numeric literal.
        :rtype: int/float
        """
        return self.numeric_literal

    def is_variable(self):
        """
        Returns whether it is a variable or not.
        :return: True if has a variable, otherwise False.
        :rtype: bool
        """
        return self.variable is not None and self.numeric_literal is None

    def has_unit(self):
        """
        Returns whether this is a numeric literal with a defined unit.
        :return: True if numeric literal with unit, otherwise False. 
        :rtype: bool
        """
        return self.variable is not None and self.numeric_literal is not None

    def get_variable(self):
        """
        Returns the variable.
        :return: the variable object.
        :rtype: ASTVariable
        """
        return self.variable

    def is_string(self):
        """
        Returns whether this simple rhs is a string.
        :return: True if string, False otherwise.
        :rtype: bool
        """
        return self.string is not None and isinstance(self.string, str)

    def get_string(self):
        """
        Returns the string as stored in this simple rhs.
        :return: a string as stored in this rhs.
        :rtype: str
        """
        return self.string

    def set_variable(self, variable):
        """
        Updates the variable of this node.
        :param variable: a single variable
        :type variable: ASTVariable
        """
        assert (variable is None or isinstance(variable, ASTVariable)), \
            '(PyNestML.AST.SimpleExpression) No or wrong type of variable provided (%s)!' % type(variable)
        self.variable = variable
        return

    def set_function_call(self, function_call):
        """
        Updates the function call of this node.
        :param function_call: a single function call
        :type function_call: Union(ASTFunctionCall,None)
        """
        assert (function_call is None or isinstance(function_call, ASTVariable)), \
            '(PyNestML.AST.SimpleExpression) No or wrong type of function call provided (%s)!' % type(function_call)
        self.function_call = function_call
        return

    def equals(self, other):
        """
        The equals method.
        :param other: a different object.
        :type other: object
        :return:True if equal, otherwise False.
        :rtype: bool
        """
        if not isinstance(other, ASTSimpleExpression):
            return False
        if self.is_function_call() + other.is_function_call() == 1:
            return False
        if self.is_function_call() and other.is_function_call() and not self.get_function_call().equals(
                other.get_function_call()):
            return False
        if self.get_numeric_literal() != other.get_numeric_literal():
            return False
        if self.is_boolean_false != other.is_boolean_false or self.is_boolean_true != other.is_boolean_true:
            return False
        if self.is_variable() + other.is_variable() == 1:
            return False
        if self.is_variable() and other.is_variable() and not self.get_variable().equals(other.get_variable()):
            return False
        if self.is_inf_literal != other.is_inf_literal:
            return False
        if self.is_string() + other.is_string() == 1:
            return False
        if self.get_string() != other.get_string():
            return False
        return True
