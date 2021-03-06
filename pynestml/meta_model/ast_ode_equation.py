#
# ast_ode_equation.py
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


class ASTOdeEquation(ASTNode):
    """
    This class is used to store meta_model equations, e.g., V_m' = 10mV + V_m.
    ASTOdeEquation Represents an equation, e.g. "I = exp(t)" or represents an differential equations,
     e.g. "V_m' = V_m+1".
    @attribute lhs      Left hand side, e.g. a Variable.
    @attribute rhs      Expression defining the right hand side.
    Grammar:
        odeEquation : lhs=variable '=' rhs=rhs;
    Attributes:
        lhs = None
        rhs = None
    """

    def __init__(self, lhs, rhs, source_position = None):
        """
        Standard constructor.
        :param lhs: an object of type ASTVariable
        :type lhs: ASTVariable
        :param rhs: an object of type ASTExpression.
        :type rhs: ASTExpression or ast_simple_expression
        :param source_position: the position of this element in the source file.
        :type source_position: ASTSourceLocation.
        """
        super(ASTOdeEquation, self).__init__(source_position)
        self.lhs = lhs
        self.rhs = rhs

    def get_lhs(self):
        """
        Returns the left-hand side of the equation.
        :return: an object of the meta_model-variable class.
        :rtype: ASTVariable
        """
        return self.lhs

    def get_rhs(self):
        """
        Returns the left-hand side of the equation.
        :return: an object of the meta_model-expr class.
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
        if not isinstance(other, ASTOdeEquation):
            return False
        return self.get_lhs().equals(other.get_lhs()) and self.get_rhs().equals(other.get_rhs())
