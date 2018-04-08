#
# SpiNNackerNamesConverter.py
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
from pynestml.modelprocessor.VariableSymbol import VariableSymbol,BlockType


class SpiNNackerNamesConverter(object):
    """
    TODO
    """

    @classmethod
    def convertToCPPName(cls, variable_name):
        """
        Converts a handed over name to the corresponding nest / c++ naming guideline.
        In concrete terms:
            Converts names of the form g_in'' to a compilable C++ identifier: __DDX_g_in
        :param variable_name: a single name.
        :type variable_name: str
        :return: the corresponding transformed name.
        :rtype: str
        """
        differential_order = variable_name.count('\'')
        if differential_order > 0:
            return '__' + 'D' * differential_order + '_' + variable_name.replace('\'', '')
        else:
            return variable_name

    @classmethod
    def name(cls, obj):
        """
        Returns for the handed over element the corresponding nest processable string.
        :param obj: a single variable symbol or variable
        :type obj: VariableSymbol or ASTVariable
        :return: the corresponding string representation
        :rtype: str
        """
        if isinstance(obj, VariableSymbol):
            # we have to regard the init values als different parameters
            if obj.get_block_type() == BlockType.INITIAL_VALUES:
                return cls.convertToCPPName(obj.get_symbol_name()) + '_init'
            else:
                return cls.convertToCPPName(obj.get_symbol_name())
        else:
            return cls.convertToCPPName(obj.getCompleteName())
