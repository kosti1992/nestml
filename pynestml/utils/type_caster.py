#
# type_caster.py
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

from pynestml.symbols.unit_type_symbol import UnitTypeSymbol
from pynestml.utils.logger import Logger, LoggingLevel
from pynestml.utils.messages import Messages


class TypeCaster(object):
    @staticmethod
    def do_magnitude_conversion_rhs_to_lhs(rhs_type_symbol, lhs_type_symbol, containing_expression):
        """
        determine conversion factor from rhs to lhs, register it with the relevant expression, drop warning
        """
        containing_expression.set_implicit_conversion_factor(
                UnitTypeSymbol.get_conversion_factor_from_to(rhs_type_symbol.astropy_unit,
                                                             lhs_type_symbol.astropy_unit))
        containing_expression.type = lhs_type_symbol

        code, message = Messages.get_implicit_magnitude_conversion(lhs_type_symbol, rhs_type_symbol,
                                                                   containing_expression.
                                                                   get_implicit_conversion_factor())
        Logger.log_message(code=code, message=message,
                           error_position=containing_expression.get_source_position(),
                           log_level=LoggingLevel.WARNING)

    @staticmethod
    def try_to_recover_or_error(lhs_type_symbol, rhs_type_symbol, containing_expression):
        if rhs_type_symbol.differs_only_in_magnitude_or_is_equal_to(lhs_type_symbol):
            TypeCaster.do_magnitude_conversion_rhs_to_lhs(rhs_type_symbol, lhs_type_symbol, containing_expression)
        elif rhs_type_symbol.is_castable_to(lhs_type_symbol):
            code, message = Messages.get_implicit_cast_rhs_to_lhs(rhs_type_symbol,
                                                                  lhs_type_symbol)
            Logger.log_message(error_position=containing_expression.get_source_position(),
                               code=code, message=message, log_level=LoggingLevel.WARNING)

        else:
            code, message = Messages.get_type_different_from_expected(lhs_type_symbol, rhs_type_symbol)
            Logger.log_message(error_position=containing_expression.get_source_position(),
                               code=code, message=message, log_level=LoggingLevel.ERROR)
