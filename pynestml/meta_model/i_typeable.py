#
# i_typeable.py
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
from copy import copy

from pynestml.utils.either import Either


class ITypeable(object):
    """
    This interface indicates that an AST element can store a corresponding type symbol, e.g., a variable
    with a respective type.
    """

    def __init__(self):
        self.type_symbol = None

    def get_type_symbol(self):
        return copy(self.type_symbol)

    def set_type_symbol(self, type_symbol):
        """
        Updates the current type symbol to the handed over one.
        :param type_symbol: a single type symbol object.
        :type type_symbol: type_symbol
        """
        assert (type_symbol is not None and isinstance(type_symbol, Either)), \
            '(PyNestML.AST.Variable) No or wrong type of type symbol provided (%s)!' % type(type_symbol)
        self.type_symbol = type_symbol
        return
