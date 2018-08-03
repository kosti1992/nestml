#
# ast_parent_collector_visitor.py
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
from pynestml.visitors.ast_parent_aware_visitor import ASTParentAwareVisitor


class ASTParentCollectorVisitor(ASTParentAwareVisitor):
    """
    This visitor stores the parent of the node as handed over on initialization. It hereby starts searching on the
    node on which accept has been called.
    """

    def __init__(self, to_find):
        super(ASTParentCollectorVisitor, self).__init__()
        self.to_find = to_find
        self.parent = None

    def visit(self, node):
        if node == self.to_find:
            self.parent = self.parents.pop()
