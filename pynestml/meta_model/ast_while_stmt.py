#
# ast_while_stmt.py
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
from pynestml.meta_model.ast_block import ASTBlock
from pynestml.meta_model.ast_node import ASTNode


class ASTWhileStmt(ASTNode):
    """
    This class is used to store a new while-block.
    Grammar:
        whileStmt : 'while' expr BLOCK_OPEN block BLOCK_CLOSE;
    Attributes:
        condition = None
        block = None
    """

    def __init__(self, condition, block, source_position):
        """
        Standard constructor.
        :param condition: the condition of the block.
        :type condition: ast_expression
        :param block: a block of statements.
        :type block: ASTBlock
        :param source_position: the position of this element in the source file.
        :type source_position: ASTSourceLocation.
        """
        super(ASTWhileStmt, self).__init__(source_position)
        self.block = block
        self.condition = condition

    def get_condition(self):
        """
        Returns the condition of the block.
        :return: the condition.
        :rtype: ASTExpression
        """
        return self.condition

    def get_block(self):
        """
        Returns the block of statements.
        :return: the block of statements.
        :rtype: ASTBlock
        """
        return self.block

    def equals(self, other):
        """
        The equals method.
        :param other: a different object.
        :type other: object
        :return: True if equals, otherwise False.
        :rtype: bool
        """
        if not isinstance(other, ASTWhileStmt):
            return False
        return self.get_condition().equals(other.get_condition()) and self.get_block().equals(other.get_block())
