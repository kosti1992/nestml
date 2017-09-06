"""
 /*
 *  ASTIF_Stmt.py
 *
 *  This file is part of NEST.
 *
 *  Copyright (C) 2004 The NEST Initiative
 *
 *  NEST is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  NEST is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with NEST.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
@author kperun
"""
from pynestml.src.main.python.org.nestml.ast.ASTElement import ASTElement
from pynestml.src.main.python.org.nestml.ast.ASTIF_Clause import ASTIF_Clause
from pynestml.src.main.python.org.nestml.ast.ASTELSE_Clause import ASTELSE_Clause


class ASTIF_Stmt(ASTElement):
    """
    This class is used to store a single if block.
    Grammar:
        if_Stmt : if_Clause
                    elif_Clause*
                    (else_Clause)?
                    BLOCK_CLOSE;
    """
    __ifClause = None
    __elifClauses = None
    __elseClause = None

    def __init__(self, _ifClause=None, _elifClauses=list(), _elseClause=None, _sourcePosition=None):
        """
        Standard construcotr.
        :param _ifClause: the if clause
        :type _ifClause: ASTIF_Clause
        :param _elifClauses: (optional) list of elif clauses
        :type _elifClauses: ASTELIF_Clause
        :param _elseClause: (optional) else clause
        :type _elseClause: ASTELSE_Clause
        :param _sourcePosition: the position of this element in the source file.
        :type _sourcePosition: ASTSourcePosition.
        """
        assert (_ifClause is not None and isinstance(_ifClause, ASTIF_Clause)), \
            '(PyNestML.AST.If_Stmt) No or wrong type of if-clause provided!'
        assert (_elifClauses is None or isinstance(_elifClauses, list)), \
            '(PyNestML.AST.If_Stmt) Wrong type of elif-clauses provided!'
        assert (_elseClause is None or isinstance(_elseClause, ASTELSE_Clause)), \
            '(PyNestML.AST.If_Stmt) Wrong type of else-clauses provided!'
        super(ASTIF_Stmt, self).__init__(_sourcePosition)
        self.__elseClause = _elseClause
        self.__ifClause = _ifClause
        self.__elifClauses = _elifClauses

    @classmethod
    def makeASTIF_Stmt(cls, _ifClause=None, _elifClauses=list(), _elseClause=None, _sourcePosition=None):
        """
        The factory method of the ASTIF_Stmt class.
        :param _ifClause: the if clause
        :type _ifClause: ASTIF_Clause
        :param _elifClauses: (optional) list of elif clauses
        :type _elifClauses: ASTELIF_Clause
        :param _elseClause: (optional) else clause
        :type _elseClause: ASTELSE_Clause
        :param _sourcePosition: the position of this element in the source file.
        :type _sourcePosition: ASTSourcePosition.
        :return: a new ASTIF_Stmt object
        :rtype: ASTIF_Stmt
        """
        return cls(_ifClause, _elifClauses, _elseClause, _sourcePosition)

    def getIfClause(self):
        """
        Returns the if-clause.
        :return: the if clause
        :rtype: ASTIF_Clause
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
        :rtype: list(ASTELIF_Clause)
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
        :rtype: ASTELSE_Clause
        """
        return self.__elseClause

    def printAST(self):
        """
        Returns a string representation of the if-statement.
        :return: a string representation
        :rtype: str
        """
        ret = self.getIfClause().printAST()
        if self.getElifClauses() is not None:
            for clause in self.getElifClauses():
                ret += clause.printAST()
        if self.getElseClause() is not None:
            ret += self.getElseClause().printAST()
        ret += 'end'
        return ret