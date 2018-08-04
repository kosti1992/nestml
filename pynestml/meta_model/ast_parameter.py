#
# ast_parameter.py
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


from pynestml.meta_model.ast_data_type import ASTDataType
from pynestml.meta_model.ast_node import ASTNode


class ASTParameter(ASTNode):
    """
    This class is used to store a single function parameter definition.
    ASTParameter represents singe:
      output: spike
    @attribute compartments Lists with compartments.
    Grammar:
        parameter : NAME datatype;
    Attributes:
        name (str): The name of the parameter.
        data_type (ASTDataType): The data type of the parameter.
    """

    def __init__(self, name=None, data_type=None, source_position=None):
        """
        Standard constructor.
        :param name: the name of the parameter.
        :type name: str
        :param data_type: the type of the parameter.
        :type data_type: ASTDataType
        :param source_position: the position of this element in the source file.
        :type source_position: ASTSourceLocation.
        """
        assert (name is not None and isinstance(name, str)), \
            '(PyNestML.AST.Parameter) No or wrong type of name provided (%s)!' % type(name)
        assert (data_type is not None and isinstance(data_type, ASTDataType)), \
            '(PyNestML.AST.Parameter) No or wrong type of datatype provided (%s)!' % type(data_type)
        super(ASTParameter, self).__init__(source_position)
        self.data_type = data_type
        self.name = name

    def get_name(self):
        """
        Returns the name of the parameter.
        :return: the name of the parameter.
        :rtype: str
        """
        return self.name

    def get_data_type(self):
        """
        Returns the data type of the parameter.
        :return: the data type of the parameter.
        :rtype: ASTDataType
        """
        return self.data_type

    def equals(self, other):
        """
        The equals method.
        :param other: a different object.
        :type other: object
        :return: True if equal, otherwise False.
        :rtype: bool
        """
        if not isinstance(other, ASTParameter):
            return False
        return self.get_name() == other.get_name() and self.get_data_type().equals(other.get_data_type())
