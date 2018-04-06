#
# ASTCompoundStmt.py
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
from pynestml.modelprocessor.ASTIfStmt import ASTIfStmt
from pynestml.modelprocessor.ASTWhileStmt import ASTWhileStmt
from pynestml.modelprocessor.ASTForStmt import ASTForStmt


class ASTCompoundStmt(ASTNode):
    """
    This class is used to store compound statements.
    Grammar:
        compoundStmt : ifStmt
                | forStmt
                | whileStmt;
    """
    __ifStmt = None
    __whileStmt = None
    __forStmt = None

    def __init__(self, _ifStmt=None, _whileStmt=None, _forStmt=None, source_position=None):
        """
        Standard constructor.
        :param _ifStmt: a if statement object
        :type _ifStmt: ASTIfStmt
        :param _whileStmt: a while statement object
        :type _whileStmt: ASTWhileStmt
        :param _forStmt: a for statement object
        :type _forStmt: ASTForStmt
        :param source_position: The source position of the assignment
        :type _sourcePosition: ASTSourceLocation
        """
        assert (_ifStmt is None or isinstance(_ifStmt, ASTIfStmt)), \
            '(PyNestML.AST.CompoundStmt) Wrong type of if-statement provided (%s)!' % type(_ifStmt)
        assert (_whileStmt is None or isinstance(_whileStmt, ASTWhileStmt)), \
            '(PyNestML.AST.CompoundStmt) Wrong type of while-statement provided (%s)!' % type(_whileStmt)
        assert (_forStmt is None or isinstance(_forStmt, ASTForStmt)), \
            '(PyNestML.AST.CompoundStmt) Wrong type of for-statement provided (%s)!' % type(_forStmt)
        super(ASTCompoundStmt, self).__init__(source_position)
        self.__ifStmt = _ifStmt
        self.__whileStmt = _whileStmt
        self.__forStmt = _forStmt
        return

    def isIfStmt(self):
        """
        Returns whether it is an "if" statement or not.
        :return: True if if stmt, False else.
        :rtype: bool
        """
        return self.__ifStmt is not None and isinstance(self.__ifStmt, ASTIfStmt)

    def getIfStmt(self):
        """
        Returns the "if" statement.
        :return: the "if" statement.
        :rtype: ASTIfStmt
        """
        return self.__ifStmt

    def isWhileStmt(self):
        """
        Returns whether it is an "while" statement or not.
        :return: True if "while" stmt, False else.
        :rtype: bool
        """
        return self.__whileStmt is not None and isinstance(self.__whileStmt, ASTWhileStmt)

    def getWhileStmt(self):
        """
        Returns the while statement.
        :return: the while statement.
        :rtype: ASTWhileStmt
        """
        return self.__whileStmt

    def isForStmt(self):
        """
        Returns whether it is an "for" statement or not.
        :return: True if "for" stmt, False else.
        :rtype: bool
        """
        return self.__forStmt is not None and isinstance(self.__forStmt, ASTForStmt)

    def getForStmt(self):
        """
        Returns the for statement.
        :return: the for statement.
        :rtype: ASTForStmt
        """
        return self.__forStmt

    def get_parent(self, ast=None):
        """
        Indicates whether a this node contains the handed over node.
        :param ast: an arbitrary ast node.
        :type ast: AST_
        :return: AST if this or one of the child nodes contains the handed over element.
        :rtype: AST_ or None
        """
        if self.isIfStmt():
            if self.getIfStmt() is ast:
                return self
            elif self.getIfStmt().get_parent(ast) is not None:
                return self.getIfStmt().get_parent(ast)
        if self.isWhileStmt():
            if self.getWhileStmt() is ast:
                return self
            elif self.getWhileStmt().get_parent(ast) is not None:
                return self.getWhileStmt().get_parent(ast)
        if self.isForStmt():
            if self.isForStmt() is ast:
                return self
            elif self.getForStmt().get_parent(ast) is not None:
                return self.getForStmt().get_parent(ast)
        return None

    def equals(self, other=None):
        """
        The equals method.
        :param other: a different object.
        :type other: object
        :return: True if equal, otherwise False.
        :rtype: bool
        """
        if not isinstance(other, ASTCompoundStmt):
            return False
        if self.getForStmt() is not None and other.getForStmt() is not None and \
                not self.getForStmt().equals(other.getForStmt()):
            return False
        if self.getWhileStmt() is not None and other.getWhileStmt() is not None and \
                not self.getWhileStmt().equals(other.getWhileStmt()):
            return False
        if self.getIfStmt() is not None and other.getIfStmt() is not None and \
                not self.getIfStmt().equals(other.getIfStmt()):
            return False
        return True

    def __str__(self):
        """
        Returns a string representation of the compound statement.
        :return: a string representing the compound statement.
        :rtype: str
        """
        if self.isIfStmt():
            return str(self.getIfStmt())
        elif self.isForStmt():
            return str(self.getForStmt())
        elif self.isWhileStmt():
            return str(self.getWhileStmt())
        else:
            raise RuntimeError('Type of compound statement not specified!')
