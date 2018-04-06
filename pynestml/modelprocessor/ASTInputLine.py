#
# ASTInputLine.py
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
from pynestml.modelprocessor.ASTSignalType import ASTSignalType
from pynestml.modelprocessor.ASTDataType import ASTDataType
from pynestml.modelprocessor.ASTInputType import ASTInputType


class ASTInputLine(ASTNode):
    """
    This class is used to store a declaration of an input line.
    ASTInputLine represents a single line form the input, e.g.:
      spikeBuffer   <- inhibitory excitatory spike

    @attribute sizeParameter Optional parameter representing  multisynapse neuron.
    @attribute sizeParameter Type of the inputchannel: e.g. inhibitory or excitatory (or both).
    @attribute spike true iff the neuron is a spike.
    @attribute current true iff. the neuron is a current.
    Grammar:
          inputLine :
            name=NAME
            ('[' sizeParameter=NAME ']')?
            (datatype)?
            '<-' inputType*
            (is_current = 'current' | is_spike = 'spike');
    """
    __name = None
    __sizeParameter = None
    __dataType = None
    __inputTypes = None
    __signalType = None

    def __init__(self, name=None, size_parameter=None, data_type=None, input_types=list(), signal_type=None,
                 source_position=None):
        """
        Standard constructor.
        :param name: the name of the buffer
        :type name: str
        :param size_parameter: a parameter indicating the index in an array.
        :type size_parameter:  str
        :param data_type: the data type of this buffer
        :type data_type: ASTDataType
        :param input_types: a list of input types specifying the buffer.
        :type input_types: list(ASTInputType)
        :param signal_type: type of signal received, i.e., spikes or currents
        :type signal_type: SignalType
        :param source_position: the position of this element in the source file.
        :type source_position: ASTSourceLocation.
        """
        assert (name is not None and isinstance(name, str)), \
            '(PyNestML.AST.InputLine) No or wrong type of name provided (%s)!' % type(name)
        assert (signal_type is not None and isinstance(signal_type, ASTSignalType)), \
            '(PyNestML.AST.InputLine) No or wrong type of input signal type provided (%s)!' % type(signal_type)
        assert (input_types is not None and isinstance(input_types, list)), \
            '(PyNestML.AST.InputLine) No or wrong type of input types provided (%s)!' % type(input_types)
        for typ in input_types:
            assert (typ is not None and isinstance(typ, ASTInputType)), \
                '(PyNestML.AST.InputLine) No or wrong type of input type provided (%s)!' % type(typ)
        assert (size_parameter is None or isinstance(size_parameter, str)), \
            '(PyNestML.AST.InputLine) Wrong type of index parameter provided (%s)!' % type(size_parameter)
        assert (data_type is None or isinstance(data_type, ASTDataType)), \
            '(PyNestML.AST.InputLine) Wrong type of data-type provided (%s)!' % type(data_type)
        super(ASTInputLine, self).__init__(source_position)
        self.__signalType = signal_type
        self.__inputTypes = input_types
        self.__sizeParameter = size_parameter
        self.__name = name
        self.__dataType = data_type
        return

    def get_name(self):
        """
        Returns the name of the declared buffer.
        :return: the name.
        :rtype: str
        """
        return self.__name

    def has_index_parameter(self):
        """
        Returns whether a index parameter has been defined.
        :return: True if index has been used, otherwise False.
        :rtype: bool
        """
        return self.__sizeParameter is not None

    def get_index_parameter(self):
        """
        Returns the index parameter.
        :return: the index parameter.
        :rtype: str
        """
        return self.__sizeParameter

    def has_input_types(self):
        """
        Returns whether input types have been defined.
        :return: True, if at least one input type has been defined.
        :rtype: bool
        """
        return len(self.__inputTypes) > 0

    def get_input_types(self):
        """
        Returns the list of input types.
        :return: a list of input types.
        :rtype: list(ASTInputType)
        """
        return self.__inputTypes

    def is_spike(self):
        """
        Returns whether this is a spike buffer or not.
        :return: True if spike buffer, False else.
        :rtype: bool 
        """
        return self.__signalType is ASTSignalType.SPIKE

    def is_current(self):
        """
        Returns whether this is a current buffer or not.
        :return: True if current buffer, False else.
        :rtype: bool 
        """
        return self.__signalType is ASTSignalType.CURRENT

    def is_excitatory(self):
        """
        Returns whether this buffer is excitatory or not. For this, it has to be marked explicitly by the 
        excitatory keyword or no keywords at all shall occur (implicitly all types).
        :return: True if excitatory, False otherwise.
        :rtype: bool
        """
        if self.get_input_types() is not None and len(self.get_input_types()) == 0:
            return True
        for typE in self.get_input_types():
            if typE.is_excitatory:
                return True
        return False

    def is_inhibitory(self):
        """
        Returns whether this buffer is inhibitory or not. For this, it has to be marked explicitly by the 
        inhibitory keyword or no keywords at all shall occur (implicitly all types).
        :return: True if inhibitory, False otherwise.
        :rtype: bool
        """
        if self.get_input_types() is not None and len(self.get_input_types()) == 0:
            return True
        for typE in self.get_input_types():
            if typE.is_inhibitory:
                return True
        return False

    def has_datatype(self):
        """
        Returns whether this buffer has a defined data type or not.
        :return: True if it has a datatype, otherwise False.
        :rtype: bool
        """
        return self.__dataType is not None and isinstance(self.__dataType, ASTDataType)

    def get_datatype(self):
        """
        Returns the currently used data type of this buffer.
        :return: a single data type object.
        :rtype: ASTDataType
        """
        return self.__dataType

    def get_parent(self, ast=None):
        """
        Indicates whether a this node contains the handed over node.
        :param ast: an arbitrary ast node.
        :type ast: AST_
        :return: AST if this or one of the child nodes contains the handed over element.
        :rtype: AST_ or None
        """
        if self.has_datatype():
            if self.get_datatype() is ast:
                return self
            elif self.get_datatype().get_parent(ast) is not None:
                return self.get_datatype().get_parent(ast)
        for line in self.get_input_types():
            if line is ast:
                return self
            elif line.get_parent(ast) is not None:
                return line.get_parent(ast)
        return None

    def __str__(self):
        """
        Returns a string representation of the input line.
        :return: a string representing the input line.
        :rtype: str
        """
        ret = self.get_name()
        if self.has_datatype():
            ret += ' ' + str(self.get_datatype()) + ' '
        if self.has_index_parameter():
            ret += '[' + self.get_index_parameter() + ']'
        ret += '<-'
        if self.has_input_types():
            for iType in self.get_input_types():
                ret += str(iType) + ' '
        if self.is_spike():
            ret += 'spike'
        else:
            ret += 'current'
        return ret

    def equals(self, other=None):
        """
        The equals method.
        :param other: a different object.
        :type other: object
        :return: True if equal,otherwise False.
        :rtype: bool
        """
        if not isinstance(other, ASTInputLine):
            return False
        if self.get_name() != other.get_name():
            return False
        if self.has_index_parameter() + other.has_index_parameter() == 1:
            return False
        if (self.has_index_parameter() and other.has_index_parameter() and
                self.get_input_types() != other.get_index_parameter()):
            return False
        if self.has_datatype() + other.has_datatype() == 1:
            return False
        if self.has_datatype() and other.has_datatype() and not self.get_datatype().equals(other.get_datatype()):
            return False
        if len(self.get_input_types()) != len(other.get_input_types()):
            return False
        my_input_types = self.get_input_types()
        your_input_types = other.get_input_types()
        for i in range(0, len(my_input_types)):
            if not my_input_types[i].equals(your_input_types[i]):
                return False
        return self.is_spike() == other.is_spike() and self.is_current() == other.is_current()
