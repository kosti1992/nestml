#
# ASTOdeFunction.py
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


from pynestml.modelprocessor.ASTNode import ASTNode
from pynestml.modelprocessor.ASTDataType import ASTDataType
from pynestml.modelprocessor.ASTExpression import ASTExpression
from pynestml.modelprocessor.ASTSimpleExpression import ASTSimpleExpression


class ASTOdeFunction(ASTNode):
    """
    Stores a single declaration of a ode function, e.g., 
        function v_init mV = V_m - 50mV.
    Grammar:    
        odeFunction : (recordable='recordable')? 'function' variableName=NAME datatype '=' rhs;
    """
    is_recordable = False
    variable_name = None
    data_type = None
    expression = None

    def __init__(self, is_recordable=False, variable_name=None, data_type=None, expression=None, source_position=None):
        """
        Standard constructor.
        :param is_recordable: (optional) is this function recordable or not.
        :type is_recordable: bool
        :param variable_name: the name of the variable.
        :type variable_name: str
        :param data_type: the datatype of the function.
        :type data_type: ASTDataType
        :param expression: the computation rhs.
        :type expression: ASTExpression
        :param source_position: the position of this element in the source file.
        :type source_position: ASTSourceLocation.
        """
        super(ASTOdeFunction, self).__init__(source_position)
        self.is_recordable = is_recordable
        self.variable_name = variable_name
        self.data_type = data_type
        self.expression = expression

    def isRecordable(self):
        """
        Returns whether this ode function is recordable or not.
        :return: True if recordable, else False.
        :rtype: bool
        """
        return self.is_recordable

    def get_variable_name(self):
        """
        Returns the variable name.
        :return: the name of the variable.
        :rtype: str
        """
        return self.variable_name

    def get_data_type(self):
        """
        Returns the data type as an object of ASTDatatype.
        :return: the type as an object of ASTDatatype.
        :rtype: ASTDataType
        """
        return self.data_type

    def get_expression(self):
        """
        Returns the rhs as an object of ASTExpression.
        :return: the rhs as an object of ASTExpression.
        :rtype: ASTExpression
        """
        return self.expression

    def get_parent(self, ast=None):
        """
        Indicates whether a this node contains the handed over node.
        :param ast: an arbitrary ast node.
        :type ast: AST_
        :return: AST if this or one of the child nodes contains the handed over element.
        :rtype: AST_ or None
        """
        if self.get_data_type() is ast:
            return self
        elif self.get_data_type().get_parent(ast) is not None:
            return self.get_data_type().get_parent(ast)
        if self.get_expression() is ast:
            return self
        elif self.get_expression().get_parent(ast) is not None:
            return self.get_expression().get_parent(ast)
        return None

    def __str__(self):
        """
        Returns a string representation of the ode function.
        :return: a string representation
        :rtype: str
        """
        ret = ''
        if self.isRecordable():
            ret += 'recordable'
        ret += 'function ' + str(self.get_variable_name()) + ' ' + str(self.get_data_type()) + \
               ' = ' + str(self.get_expression())
        return ret

    def equals(self, other=None):
        """
        The equals method.
        :param other: a different object.
        :type other: object
        :return: True if equal, otherwise False.
        :rtype: bool
        """
        if not isinstance(other, ASTOdeFunction):
            return False
        if self.isRecordable() != other.isRecordable():
            return False
        if self.get_variable_name() != other.get_variable_name():
            return False
        if not self.get_data_type().equals(other.get_data_type()):
            return False
        return self.get_expression().equals(other.get_expression())
