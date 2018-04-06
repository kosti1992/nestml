#
# ASTSmallStmt.py
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


from pynestml.modelprocessor.ASTAssignment import ASTAssignment
from pynestml.modelprocessor.ASTFunctionCall import ASTFunctionCall
from pynestml.modelprocessor.ASTReturnStmt import ASTReturnStmt
from pynestml.modelprocessor.ASTDeclaration import ASTDeclaration
from pynestml.modelprocessor.ASTNode import ASTNode


class ASTSmallStmt(ASTNode):
    """
    This class is used to store small statements, e.g., a declaration.
    Grammar:
        smallStmt : assignment
                 | functionCall
                 | declaration
                 | returnStmt;
    Attributes:
        assignment (ASTAssignment): A assignment reference.
        function_call (ASTFunctionCall): A function call reference.
        declaration (ASTDeclaration): A declaration reference.
        return_stmt (ASTReturnStmt): A reference to the returns statement.
    """
    assignment = None
    function_call = None
    declaration = None
    return_stmt = None

    def __init__(self, assignment=None, function_call=None, declaration=None, return_stmt=None, source_position=None):
        """
        Standard constructor.
        :param assignment: an ast-assignment object.
        :type assignment: ASTAssignment
        :param function_call: an ast-function call object.
        :type function_call: ASTFunctionCall
        :param declaration: an ast-declaration object.
        :type declaration: ASTDeclaration
        :param return_stmt: an ast-return statement object.
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

    def get_parent(self, ast=None):
        """
        Indicates whether a this node contains the handed over node.
        :param ast: an arbitrary ast node.
        :type ast: AST_
        :return: AST if this or one of the child nodes contains the handed over element.
        :rtype: AST_ or None
        """
        if self.is_assignment():
            if self.get_assignment() is ast:
                return self
            elif self.get_assignment().get_parent(ast) is not None:
                return self.get_assignment().get_parent(ast)
        if self.is_function_call():
            if self.get_function_call() is ast:
                return self
            elif self.get_function_call().get_parent(ast) is not None:
                return self.get_function_call().get_parent(ast)
        if self.is_declaration():
            if self.get_declaration() is ast:
                return self
            elif self.get_declaration().get_parent(ast) is not None:
                return self.get_declaration().get_parent(ast)
        if self.is_return_stmt():
            if self.get_return_stmt() is ast:
                return self
            elif self.get_return_stmt().get_parent(ast) is not None:
                return self.get_return_stmt().get_parent(ast)
        return None

    def __str__(self):
        """
        Returns a string representation of the small statement.
        :return: a string representation.
        :rtype: str
        """
        if self.is_assignment():
            return str(self.get_assignment())
        elif self.is_function_call():
            return str(self.get_function_call())
        elif self.is_declaration():
            return str(self.get_declaration())
        else:
            return str(self.get_return_stmt())

    def equals(self, other=None):
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
        if self.is_return_stmt() and other.is_return_stmt() and not self.get_return_stmt().equals(other.get_return_stmt()):
            return False
        return True
