#
# ast_small_stmt.py
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


class ASTSmallStmt(ASTNode):
    """
    This class is used to store small statements, e.g., a declaration.
    Grammar:
        smallStmt : assignment
                 | functionCall
                 | declaration
                 | returnStmt;
    Attributes:
        assignment (ast_assignment): A assignment reference.
        function_call (ast_function_call): A function call reference.
        declaration (ast_declaration): A declaration reference.
        return_stmt (ast_return_stmt): A reference to the returns statement.
    """

    def __init__(self, assignment=None, function_call=None, declaration=None, return_stmt=None, source_position=None):
        """
        Standard constructor.
        :param assignment: an meta_model-assignment object.
        :type assignment: ASTAssignment
        :param function_call: an meta_model-function call object.
        :type function_call: ASTFunctionCall
        :param declaration: an meta_model-declaration object.
        :type declaration: ASTDeclaration
        :param return_stmt: an meta_model-return statement object.
        :type return_stmt: ASTReturnStmt
        :param source_position: the position of this element in the source file.
        :type source_position: ASTSourceLocation.
        """
        super(ASTSmallStmt, self).__init__(source_position)
        self.assignment = assignment
        self.function_call = function_call
        self.declaration = declaration
        self.return_stmt = return_stmt

    def is_assignment(self):
        """
        Returns whether it is an assignment statement or not.
        :return: True if assignment, False else.
        :rtype: bool
        """
        return self.assignment is not None

    def get_assignment(self):
        """
        Returns the assignment.
        :return: the assignment statement.
        :rtype: ASTAssignment
        """
        return self.assignment

    def is_function_call(self):
        """
        Returns whether it is an function call or not.
        :return: True if function call, False else.
        :rtype: bool
        """
        return self.function_call is not None

    def get_function_call(self):
        """
        Returns the function call.
        :return: the function call statement.
        :rtype: ASTFunctionCall
        """
        return self.function_call

    def is_declaration(self):
        """
        Returns whether it is a declaration statement or not.
        :return: True if declaration, False else.
        :rtype: bool
        """
        return self.declaration is not None

    def get_declaration(self):
        """
        Returns the assignment.
        :return: the declaration statement.
        :rtype: ASTDeclaration
        """
        return self.declaration

    def is_return_stmt(self):
        """
        Returns whether it is a return statement or not.
        :return: True if return stmt, False else.
        :rtype: bool
        """
        return self.return_stmt is not None

    def get_return_stmt(self):
        """
        Returns the return statement.
        :return: the return statement.
        :rtype: ASTReturnStmt
        """
        return self.return_stmt

    def equals(self, other):
        """
        The equals method.
        :param other: a different object
        :type other: object
        :return: True if equals, otherwise False.
        :rtype: bool
        """
        if not isinstance(other, ASTSmallStmt):
            return False
        if self.is_function_call() + other.is_function_call() == 1:
            return False
        if self.is_function_call() and other.is_function_call() and \
                not self.get_function_call().equals(other.get_function_call()):
            return False
        if self.is_assignment() + other.is_assignment() == 1:
            return False
        if self.is_assignment() and other.is_assignment() and not self.get_assignment().equals(other.get_assignment()):
            return False
        if self.is_declaration() + other.is_declaration() == 1:
            return False
        if self.is_declaration() and other.is_declaration() and not self.get_declaration().equals(
                other.get_declaration()):
            return False
        if self.is_return_stmt() + other.is_return_stmt() == 1:
            return False
        if self.is_return_stmt() and other.is_return_stmt() and not self.get_return_stmt().equals(
                other.get_return_stmt()):
            return False
        return True
