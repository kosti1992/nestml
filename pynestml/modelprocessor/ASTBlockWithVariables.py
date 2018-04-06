#
# ASTBlockWithVariables.py
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


class ASTBlockWithVariables(ASTNode):
    """
    This class is used to store a block of variable declarations.
    ASTBlockWithVariables.py represent a block with variables, e.g.:
        state:
          y0, y1, y2, y3 mV [y1 > 0; y2 > 0]
        end

    attribute state true: if the varblock is a state.
    attribute parameter: true if the varblock is a parameter.
    attribute internal: true if the varblock is a state internal.
    attribute AliasDecl: a list with variable declarations
    Grammar:
         blockWithVariables:
            blockType=('state'|'parameters'|'internals'|'initial_values')
            BLOCK_OPEN
              (declaration | NEWLINE)*
            BLOCK_CLOSE;
    """
    __isState = False
    __isParameters = False
    __isInternals = False
    __isInitValues = False
    __declarations = None

    def __init__(self, _isState=False, _isParameters=False, _isInternals=False, _isInitialValues=False,
                 _declarations=list(), source_position=None):
        """
        Standard constructor.
        :param _isState: is a state block.
        :type _isState: bool
        :param _isParameters: is a parameter block.
        :type _isParameters: bool 
        :param _isInternals: is an internals block.
        :type _isInternals: bool
        :param _isInitialValues: is an initial values block.
        :type _isInitialValues: bool
        :param _declarations: a list of declarations.
        :type _declarations: list(ASTDeclaration)
        :param source_position: the position of this element in the source file.
        :type source_position: ASTSourceLocation.
        """
        assert (_isInternals or _isParameters or _isState or _isInitialValues), \
            '(PyNESTML.AST.BlockWithVariables) Type of variable block specified!'
        assert ((_isInternals + _isParameters + _isState + _isInitialValues) == 1), \
            '(PyNestML.AST.BlockWithVariables) Type of block ambiguous!'
        assert (_declarations is None or isinstance(_declarations, list)), \
            '(PyNESTML.AST.BlockWithVariables) Wrong type of declaration provided (%s)!' % type(_declarations)
        super(ASTBlockWithVariables, self).__init__(source_position)
        self.__declarations = _declarations
        self.__isInternals = _isInternals
        self.__isParameters = _isParameters
        self.__isInitValues = _isInitialValues
        self.__isState = _isState
        return

    def isState(self):
        """
        Returns whether it is a state block or not.
        :return: True if state block, otherwise False.
        :rtype: bool
        """
        return self.__isState

    def isParameters(self):
        """
        Returns whether it is a parameters block or not.
        :return: True if parameters block, otherwise False.
        :rtype: bool
        """
        return self.__isParameters

    def isInternals(self):
        """
        Returns whether it is an internals block or not.
        :return: True if internals block, otherwise False.
        :rtype: bool
        """
        return self.__isInternals

    def isInitialValues(self):
        """
        Returns whether it is a initial-values block.
        :return: True if initial values block, otherwise False.
        :rtype: bool
        """
        return self.__isInitValues

    def getDeclarations(self):
        """
        Returns the set of stored declarations.
        :return: set of declarations
        :rtype: set(ASTDeclaration)
        """
        return self.__declarations

    def clear(self):
        """
        Clears the list of declarations in this block.
        """
        del self.__declarations
        self.__declarations = list()
        return

    def get_parent(self, ast=None):
        """
        Indicates whether a this node contains the handed over node.
        :param ast: an arbitrary ast node.
        :type ast: AST_
        :return: AST if this or one of the child nodes contains the handed over element.
        :rtype: AST_ or None
        """
        for stmt in self.getDeclarations():
            if stmt is ast:
                return self
            if stmt.get_parent(ast) is not None:
                return stmt.get_parent(ast)
        return None

    def __str__(self):
        """
        Returns a string representation of the variable block.
        :return: a string representation
        :rtype: str
        """
        ret = ''
        if self.isState():
            ret += 'state'
        elif self.isParameters():
            ret += 'parameters'
        elif self.isInternals():
            ret += 'internals'
        else:
            ret += 'initial_values'
        ret += ':\n'
        if self.getDeclarations() is not None:
            for decl in self.getDeclarations():
                ret += str(decl) + '\n'
        ret += 'end'
        return ret

    def equals(self, other=None):
        """
        The equals method.
        :param other: a different object.
        :type other: object
        :return: True if equal, otherwise False
        :rtype: bool
        """
        if not isinstance(other, ASTBlockWithVariables):
            return False
        if not (self.isInitialValues() == other.isInitialValues() and self.isInternals() == other.isInternals() and
                self.isParameters() == other.isParameters() and self.isState() == other.isState()):
            return False
        if len(self.getDeclarations()) != len(other.getDeclarations()):
            return False
        myDeclarations = self.getDeclarations()
        yourDeclarations = other.getDeclarations()
        for i in range(0, len(myDeclarations)):
            if not myDeclarations[i].equals(yourDeclarations[i]):
                return False
        return True
