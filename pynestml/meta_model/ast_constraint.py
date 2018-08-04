#
# ast_constraint.py
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
from enum import Enum

from pynestml.meta_model.ast_node import ASTNode


class ASTConstraint(ASTNode):
    """
    This class represents a single constraint which has to apply at runtime.
    Grammar:
        constraint: (leftBound=expression leftBoundType=comparisonOperator)?
               variable
              (rightBoundType=comparisonOperator rightBound=expression)?;

    Attributes:
        left_bound: the ASTExpression representing the left bound, e.g., -55mV
        left_bound_type: the comparison operator at the left side, e.g., <
        variable: the constrained variable, e.g., V_m
        right_bound: the ASTExpression representing the right bound, e.g., 60mV
        right_bound_type: the comparison operator at the right side, e.g., <=
    """

    def __init__(self, left_bound, left_bound_type, variable, right_bound_type, right_bound, source_position):
        super(ASTConstraint, self).__init__(source_position)
        self.left_bound = left_bound
        self.left_bound_type = left_bound_type
        self.variable = variable
        self.right_bound_type = right_bound_type
        self.right_bound = right_bound

    def equals(self, other):
        if not isinstance(other, ASTConstraint):
            return False
        return (self.left_bound.equals(other.left_bound) and self.variable.equals(other.variable) and
                self.right_bound.equals(other.right_bound) and self.sourcePosition.equals(other.sourcePosition) and
                self.left_bound_type.equals(other.left_bound) and self.right_bound_type.equals(other.right_bound_type))

    def get_left_bound(self):
        return self.left_bound, self.left_bound_type

    def get_right_bound(self):
        return self.right_bound, self.right_bound_type

    def has_left_bound(self):
        return self.left_bound is not None and self.left_bound_type is not None

    def has_right_bound(self):
        return self.right_bound is not None and self.right_bound_type is not None
