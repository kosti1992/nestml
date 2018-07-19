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

    def __init__(self, left_bound, left_bound_type, variable, right_bound_type, right_bound, source_position):
        super(ASTConstraint, self).__init__(source_position)
        self.left_bound = left_bound
        self.left_bound_type = left_bound_type
        self.variable = variable
        self.right_bound_type = right_bound_type
        self.right_bound = right_bound

    def get_parent(self, ast):
        # todo
        raise Exception('implement me in constraint')

    def equals(self, other):
        if not isinstance(other, ASTConstraint):
            return False
        return (self.left_bound == other.left_bound and self.variable.equals(other.variable) and
                self.right_bound == other.right_bound and self.sourcePosition.equals(other.sourcePosition) and
                self.left_bound == other.left_bound and self.right_bound == other.right_bound)

    class Boundary(Enum):
        LESS_THAN = 1
        LESS_EQUAL = 2
        EQUAL = 3
