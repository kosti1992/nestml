#
# ast_function.py
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
from pynestml.meta_model.i_typeable import ITypeable


class ASTFunction(ASTNode, ITypeable):
    """
    This class is used to store a user-defined function.
    ASTFunction a function definition:
      function set_V_m(v mV):
        y3 = v - E_L
      end
    @attribute name Functionname.
    @attribute parameter A single parameter.
    @attribute returnType Complex return type, e.g. String
    @attribute primitiveType Primitive return type, e.g. int
    @attribute block Implementation of the function.
    Grammar:
    function: 'function' NAME '(' (parameter (',' parameter)*)? ')' (returnType=datatype)?
           BLOCK_OPEN
             block
           BLOCK_CLOSE;
    Attributes:
        name = None
        parameters = None
        return_type = None
        block = None
        # the corresponding type symbol
        type_symbol = None
    """

    def __init__(self, name, parameters, return_type, block, source_position):
        """
        Standard constructor.
        :param name: the name of the defined function.
        :type name: str
        :param parameters: (Optional) Set of parameters.
        :type parameters: list(ASTParameter)
        :param return_type: (Optional) Return type.
        :type return_type: ast_data_type
        :param block: a block of declarations.
        :type block: ASTBlock
        :param source_position: the position of this element in the source file.
        :type source_position: ASTSourceLocation.
        """
        super(ASTFunction, self).__init__(source_position)
        self.block = block
        self.return_type = return_type
        self.parameters = parameters
        self.name = name

    def get_name(self):
        """
        Returns the name of the function.
        :return: the name of the function.
        :rtype: str
        """
        return self.name

    def has_parameters(self):
        """
        Returns whether parameters have been defined.
        :return: True if parameters defined, otherwise False.
        :rtype: bool
        """
        return (self.parameters is not None) and (len(self.parameters) > 0)

    def get_parameters(self):
        """
        Returns the list of parameters.
        :return: a parameters object containing the list.
        :rtype: list(ASTParameter)
        """
        return self.parameters

    def has_return_type(self):
        """
        Returns whether return a type has been defined.
        :return: True if return type defined, otherwise False.
        :rtype: bool
        """
        return self.return_type is not None

    def get_return_type(self):
        """
        Returns the return type of function.
        :return: the return type 
        :rtype: ASTDataType
        """
        return self.return_type

    def get_block(self):
        """
        Returns the block containing the definitions.
        :return: the block of the definitions.
        :rtype: ASTBlock
        """
        return self.block

    def equals(self, other):
        """
        The equals method.
        :param other: a different object.
        :type other: object
        :return: True if equal, otherwise False.
        :rtype: bool
        """
        if not isinstance(other, ASTFunction):
            return False
        if self.get_name() != other.get_name():
            return False
        if len(self.get_parameters()) != len(other.get_parameters()):
            return False
        my_parameters = self.get_parameters()
        your_parameters = other.get_parameters()
        for i in range(0, len(my_parameters)):
            if not my_parameters[i].equals(your_parameters[i]):
                return False
        if self.has_return_type() + other.has_return_type() == 1:
            return False
        if (self.has_return_type() and other.has_return_type() and
                not self.get_return_type().equals(other.get_return_type())):
            return False
        return self.get_block().equals(other.get_block())
