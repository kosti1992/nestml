#
# ast_constraints_block.py
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


class ASTConstraintsBlock(ASTNode):
    """
    This class represents a constraints block, i.e., a set of constraints which have to apply during runtime.
    Grammar:
        constraintsBlock: 'constraints' BLOCK_OPEN constraint* BLOCK_CLOSE;
    """

    def __init__(self, constraints, source_position):
        super(ASTConstraintsBlock, self).__init__(source_position)
        self.constraints = constraints

    def equals(self, other):
        if not isinstance(other, ASTConstraintsBlock):
            return False
        if len(self.constraints) != len(other.constraints()):
            return False
        for i in range(0, len(self.constraints)):
            if not self.constraints[i].equals(other.constraints[i]):
                return False
        return True
