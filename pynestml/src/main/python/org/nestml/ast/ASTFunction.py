"""
/*
 *  ASTFunction.py
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
from pynestml.src.main.python.org.nestml.ast.ASTBlock import ASTBlock
from pynestml.src.main.python.org.nestml.ast.ASTParameters import ASTParameters
from pynestml.src.main.python.org.nestml.ast.ASTReturnStmt import ASTReturnStmt


class ASTFunction(ASTElement):
    """
    This class is used to store a user-defined function.
    ASTFunction a function definition:
      function set_V_m(v mV):
        y3 = v - E_L
      end
    @attribute name Functionname.
    @attribute parameters List with function parameters.
    @attribute returnType Complex return type, e.g. String
    @attribute primitiveType Primitive return type, e.g. int
    @attribute block Implementation of the function.
    Grammar:
    function: 'function' NAME '(' parameters? ')' (returnType=datatype)?
                   BLOCK_OPEN
                     block
                   BLOCK_CLOSE;
    """
    __name = None
    __parameters = None
    __returnType = None
    __block = None

    def __init__(self, _name=None, _parameters=None, _returnType=None, _block=None, _sourcePosition=None):
        """
        Standard constructor.
        :param _name: the name of the defined function.
        :type _name: str 
        :param _parameters: (Optional) Set of parameters.  
        :type _parameters: ASTParameters
        :param _returnType: (Optional) Return type. 
        :type _returnType: ASTDataType
        :param _block: a block of declarations.
        :type _block: ASTBlock
        :param _sourcePosition: the position of this element in the source file.
        :type _sourcePosition: ASTSourcePosition.
        """
        assert (_name is not None and isinstance(_name, str)), '(PyNestML.AST.Function) No name or wrong type provided!'
        assert (_block is not None and isinstance(_block, ASTBlock)), \
            '(PyNestML.AST.Function) No block or wrong type provided!'
        assert (_parameters is None or isinstance(_parameters, ASTParameters)), \
            '(PyNestML.AST.Function) Wrong type of parameters provided!'
        assert (_returnType is None or isinstance(_returnType, ASTReturnStmt)), \
            '(PyNestML.AST.Function) Wrong type of return provided!'
        super(ASTFunction, self).__init__(_sourcePosition)
        self.__block = _block
        self.__returnType = _returnType
        self.__parameters = _parameters
        self.__name = _name

    @classmethod
    def makeASTFunction(cls, _name=None, _parameters=None, _returnType=None, _block=None, _sourcePosition=None):
        """
        Factory method of the ASTFunction class.
        :param _name: the name of the defined function.
        :type _name: str 
        :param _parameters: (Optional) Set of parameters.  
        :type _parameters: ASTParameters
        :param _returnType: (Optional) Return type. 
        :type _returnType: ASTDataType
        :param _block: a block of declarations.
        :type _block: ASTBlock
        :param _sourcePosition: the position of this element in the source file.
        :type _sourcePosition: ASTSourcePosition.
        :return: a new ASTFunction object.
        :rtype: ASTFunction
        """
        return cls(_name, _parameters, _returnType, _block, _sourcePosition)

    def getName(self):
        """
        Returns the name of the function.
        :return: the name of the function.
        :rtype: str
        """
        return self.__name

    def hasParameters(self):
        """
        Returns whether parameters have been defined.
        :return: True if parameters defined, otherwise False.
        :rtype: bool
        """
        return (self.__parameters is not None) and (len(self.__parameters.getParametersList()) > 0)

    def getParameters(self):
        """
        Returns the list of parameters.
        :return: a parameters object containing the list.
        :rtype: ASTParameters
        """
        return self.__parameters

    def hasReturnType(self):
        """
        Returns whether return a type has been defined.
        :return: True if return type defined, otherwise False.
        :rtype: bool
        """
        return self.__returnType is not None

    def getReturnType(self):
        """
        Returns the return type of function.
        :return: the return type 
        :rtype: ASTDataType
        """
        return self.__returnType

    def getBlock(self):
        """
        Returns the block containing the definitions.
        :return: the block of the definitions.
        :rtype: ASTBlock
        """
        return self.__block

    def printAST(self):
        """
        Returns a string representation of the function defintion.
        :return: a string representation.
        :rtype: str
        """
        ret = 'function ' + self.getName() + '('
        if self.hasParameters():
            ret += self.getParameters().printAST()
        ret += ')'
        if self.hasReturnType():
            ret += self.getReturnType().printAST()
        ret += ':\n' + self.getBlock().printAST() + '\nend'
        return ret