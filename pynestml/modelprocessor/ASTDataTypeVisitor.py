#
# ASTDataTypeVisitor.py
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
from astropy import units

from pynestml.modelprocessor.ASTUnitType import ASTUnitType
from pynestml.modelprocessor.ASTVisitor import ASTVisitor
from pynestml.modelprocessor.PredefinedTypes import PredefinedTypes
from pynestml.modelprocessor.PredefinedUnits import PredefinedUnits
from pynestml.modelprocessor.TypeSymbol import TypeSymbol
from pynestml.modelprocessor.UnitType import UnitType


class ASTDataTypeVisitor(ASTVisitor):
    """
    This class represents a visitor which inspects a handed over data type, checks if correct typing has been used
    (e.g., no computation between primitive and non primitive data types etc.) and finally updates the type symbols
    of the datatype ast.
    """

    def __init__(self):
        super(ASTDataTypeVisitor, self).__init__()
        self.symbol = None
        self.result = None

    def visit_data_type(self, node):
        """
        Visits a single data type ast node and updates, checks correctness and updates its type symbol.
        This visitor can also be used to derive the original name of the unit.
        :param node: a single datatype node.
        :type node: ASTDataType
        """
        if node.is_integer():
            self.symbol = PredefinedTypes.get_integer_type()
            node.set_type_symbol(self.symbol)
        elif node.is_real():
            self.symbol = PredefinedTypes.get_real_type()
            node.set_type_symbol(self.symbol)
        elif node.is_string():
            self.symbol = PredefinedTypes.get_string_type()
            node.set_type_symbol(self.symbol)
        elif node.is_boolean():
            self.symbol = PredefinedTypes.get_boolean_type()
            node.set_type_symbol(self.symbol)
        elif node.is_void():
            self.symbol = PredefinedTypes.get_void_type()
            node.set_type_symbol(self.symbol)

    def endvisit_data_type(self, node):
        if node.is_unit_type():
            node.set_type_symbol(node.get_unit_type().get_type_symbol())
        if self.symbol is not None:
            self.result = self.symbol.get_symbol_name()
        else:
            raise RuntimeError('ASTDataType type symbol could not be derived!')

    def visit_unit_type(self, node):
        """
        Visits a single unit type element, checks for correct usage of units and builds the corresponding combined 
        unit.
        :param node: a single unit type ast.
        :type node: ASTUnitType
        :return: a new type symbol representing this unit type.
        :rtype: TypeSymbol
        """
        if node.is_simple_unit():
            type_s = PredefinedTypes.get_type(node.unit)
            if type_s is None:
                raise RuntimeError('Unknown atomic unit %s.' % node.unit)
            else:
                node.set_type_symbol(type_s)
                self.symbol = type_s

    def endvisit_unit_type(self, node):
        if node.is_encapsulated:
            node.set_type_symbol(node.compound_unit.get_type_symbol())
        elif node.is_pow:
            base_symbol = node.base.get_type_symbol()
            assert isinstance(base_symbol, TypeSymbol)
            exponent = node.exponent
            sympy_unit = base_symbol.get_encapsulated_unit() ** exponent
            res = handle_unit(sympy_unit)
            node.set_type_symbol(res)
            self.symbol = res
        elif node.is_div:
            if isinstance(node.get_lhs(), ASTUnitType):  # regard that lhs can be a numeric or a unit-type
                lhs = node.get_lhs().get_type_symbol().get_encapsulated_unit()
            else:
                lhs = node.get_lhs()
            rhs = node.get_rhs().get_type_symbol().get_encapsulated_unit()
            res = lhs / rhs
            res = handle_unit(res)
            node.set_type_symbol(res)
            self.symbol = res
        elif node.is_times:
            if isinstance(node.get_lhs(), ASTUnitType):  # regard that lhs can be a numeric or a unit-type
                lhs = node.get_lhs().get_type_symbol().get_encapsulated_unit()
            else:
                lhs = node.get_lhs()
            rhs = node.get_rhs().get_type_symbol().get_encapsulated_unit()
            res = lhs * rhs
            res = handle_unit(res)
            node.set_type_symbol(res)
            self.symbol = res
        return


def handle_unit(unit_type):
    """
    Handles a handed over unit by creating the corresponding unit-type, storing it in the list of predefined
    units, creating a type symbol and returning it.
    :param unit_type: a single sympy unit symbol
    :type unit_type: Symbol (sympy)
    :return: a new type symbol
    :rtype: TypeSymbol
    """
    # first clean up the unit of not required components, here it is the 1.0 in front of the unit
    # e.g., 1.0 * 1 / ms. This step is not mandatory for correctness, but makes  reporting easier
    if isinstance(unit_type, units.Quantity) and unit_type.value == 1.0:
        to_process = unit_type.unit
    else:
        to_process = unit_type
    if str(to_process) not in PredefinedUnits.get_units().keys():
        unit_type = UnitType(_name=str(to_process), _unit=to_process)
        PredefinedUnits.register_unit(unit_type)
    # now create the corresponding type symbol if it does not exists
    if PredefinedTypes.get_type(str(to_process)) is None:
        type_symbol = TypeSymbol(name=str(to_process),
                                 unit=PredefinedUnits.get_unit(str(to_process)),
                                 is_integer=False, is_real=False, is_void=False,
                                 is_boolean=False, is_string=False, is_buffer=False)
        PredefinedTypes.register_type(type_symbol)
    return PredefinedTypes.get_type(name=str(to_process))
