#
# ast_ode_shape.py
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

from pynestml.meta_model.ast_node import ASTNode


class ASTOdeShape(ASTNode):
    """
    This class is used to store shapes. 
    Grammar:
        odeShape : 'shape' lhs=variable '=' rhs=expr;
    Attributes:
        lhs = None
        rhs = None
    """

    def __init__(self, lhs, rhs, source_position):
        """
        Standard constructor of ASTOdeShape.
        :param lhs: the variable corresponding to the shape
        :type lhs: ASTVariable
        :param rhs: the right-hand side rhs
        :type rhs: ASTExpressionNode
        :param source_position: the position of this element in the source file.
        :type source_position: ASTSourceLocation.
        """
        super(ASTOdeShape, self).__init__(source_position)
        self.lhs = lhs
        self.rhs = rhs
        return

    def get_variable(self):
        """
        Returns the variable of the left-hand side.
        :return: the variable
        :rtype: ASTVariable
        """
        return self.lhs

    def get_expression(self):
        """
        Returns the right-hand side rhs.
        :return: the rhs
        :rtype: ASTExpression
        """
        return self.rhs

    def equals(self, other):
        """
        The equals method.
        :param other: a different object.
        :type other: object
        :return: True if equal, otherwise False.
        :rtype: bool
        """
        if not isinstance(other, ASTOdeShape):
            return False
        return self.get_variable().equals(other.get_variable()) and self.get_expression().equals(other.get_expression())
