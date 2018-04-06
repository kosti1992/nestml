#
# ASTIfStmt.py
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
from pynestml.modelprocessor.ASTIfClause import ASTIfClause
from pynestml.modelprocessor.ASTElseClause import ASTElseClause
from pynestml.modelprocessor.ASTElifClause import ASTElifClause


class ASTIfStmt(ASTNode):
    """
    This class is used to store a single if block.
    Grammar:
        ifStmt : ifClause
                    elifClause*
                    (elseClause)?
                    BLOCK_CLOSE;
    """
    __ifClause = None
    __elifClauses = None
    __elseClause = None

    def __init__(self, _ifClause=None, _elifClauses=list(), _elseClause=None, source_position=None):
        """
        Standard construcotr.
        :param _ifClause: the if-clause
        :type _ifClause: ASTIfClause
        :param _elifClauses: (optional) list of elif clauses
        :type _elifClauses: ASTElifClause
        :param _elseClause: (optional) else clause
        :type _elseClause: ASTElseClause
        :param source_position: the position of this element in the source file.
        :type _sourcePosition: ASTSourceLocation.
        """
        assert (_ifClause is not None and isinstance(_ifClause, ASTIfClause)), \
            '(PyNestML.AST.IfStmt) No or wrong type of if-clause provided (%s)!' % type(_ifClause)
        assert (_elifClauses is None or isinstance(_elifClauses, list)), \
            '(PyNestML.AST.IfStmt) Wrong type of elif-clauses provided (%s)!' % type(_elifClauses)
        for elifClause in _elifClauses:
            assert (elifClause is not None and isinstance(elifClause, ASTElifClause)), \
                '(PyNestML.AST.IfStmt) Wrong type of elif-clause provided (%s)!' % type(elifClause)
        assert (_elseClause is None or isinstance(_elseClause, ASTElseClause)), \
            '(PyNestML.AST.IfStmt) Wrong type of else-clauses provided (%s)!' % type(_elseClause)
        super(ASTIfStmt, self).__init__(source_position)
        self.__elseClause = _elseClause
        self.__ifClause = _ifClause
        self.__elifClauses = _elifClauses
        return

    def getIfClause(self):
        """
        Returns the if-clause.
        :return: the if clause
        :rtype: ASTfClause
        """
        return self.__ifClause

    def hasElifClauses(self):
        """
        Returns whether object contains elif clauses.
        :return: True if at leas one elif clause, False else.
        :rtype: bool
        """
        return len(self.__elifClauses) > 0

    def getElifClauses(self):
        """
        Returns a list of elif-clauses.
        :return: a list of elif-clauses.
        :rtype: list(ASTElifClause)
        """
        return self.__elifClauses

    def hasElseClause(self):
        """
        Returns whether object contains elif clauses.
        :return: True if object contains an else-clause, False else.
        :rtype: bool
        """
        return self.__elseClause is not None

    def getElseClause(self):
        """
        Returns the else-clause.
        :return: the else-clause.
        :rtype: ASTElseClause
        """
        return self.__elseClause

    def get_parent(self, ast=None):
        """
        Indicates whether a this node contains the handed over node.
        :param ast: an arbitrary ast node.
        :type ast: AST_
        :return: AST if this or one of the child nodes contains the handed over element.
        :rtype: AST_ or None
        """
        if self.getIfClause() is ast:
            return self
        elif self.getIfClause().get_parent(ast) is not None:
            return self.getIfClause().get_parent(ast)
        for elifClause in self.getElifClauses():
            if elifClause is ast:
                return self
            elif elifClause.get_parent(ast) is not None:
                return elifClause.get_parent(ast)
        if self.hasElseClause():
            if self.getElseClause() is ast:
                return self
            elif self.getElseClause().get_parent(ast) is not None:
                return self.getElseClause().get_parent(ast)
        return None

    def __str__(self):
        """
        Returns a string representation of the if-statement.
        :return: a string representation
        :rtype: str
        """
        ret = str(self.getIfClause())
        if self.getElifClauses() is not None:
            for clause in self.getElifClauses():
                ret += str(clause)
        if self.getElseClause() is not None:
            ret += str(self.getElseClause())
        ret += 'end'
        return ret

    def equals(self, other=None):
        """
        The equals method.
        :param other: a different object.
        :type other: object
        :return: True if equals, otherwise False.
        :rtype: bool
        """
        if not isinstance(other, ASTIfStmt):
            return False
        if not self.getIfClause().equals(other.getIfClause()):
            return False
        if len(self.getElifClauses()) != len(other.getElifClauses()):
            return False
        myElifClauses = self.getElifClauses()
        yourElifClauses = other.getElifClauses()
        for i in range(0, len(myElifClauses)):
            if not myElifClauses[i].equals(yourElifClauses[i]):
                return False
        if self.hasElseClause() + other.hasElseClause() == 1:
            return False
        if self.hasElseClause() and other.hasElseClause() and not self.getElseClause().equals(other.getElseClause()):
            return False
        return True
