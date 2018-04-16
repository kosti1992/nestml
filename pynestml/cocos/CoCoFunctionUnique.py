#
# CoCoFunctionUnique.py
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
from pynestml.cocos.CoCo import CoCo
from pynestml.symbols.Symbol import SymbolKind
from pynestml.utils.Logger import LoggingLevel, Logger
from pynestml.utils.Messages import Messages


class CoCoFunctionUnique(CoCo):
    """
    This Coco ensures that each function is defined exactly once (thus no redeclaration occurs).
    """

    @classmethod
    def check_co_co(cls, node):
        """
        Checks if each function is defined uniquely.
        :param node: a single neuron
        :type node: ASTNeuron
        """
        checked_funcs_names = list()
        for func in node.get_functions():
            if func.get_name() not in checked_funcs_names:
                symbols = func.get_scope().resolve_to_all_symbols(func.get_name(), SymbolKind.FUNCTION)
                if isinstance(symbols, list) and len(symbols) > 1:
                    checked = list()
                    for funcA in symbols:
                        for funcB in symbols:
                            if funcA is not funcB and funcB not in checked:
                                if funcA.is_predefined:
                                    code, message = Messages.getFunctionRedeclared(funcA.get_symbol_name(), True)
                                    Logger.log_message(
                                        error_position=funcB.get_referenced_object().get_source_position(),
                                        log_level=LoggingLevel.ERROR,
                                        message=message, code=code)
                                elif funcB.is_predefined:
                                    code, message = Messages.getFunctionRedeclared(funcA.get_symbol_name(), True)
                                    Logger.log_message(
                                        error_position=funcA.get_referenced_object().get_source_position(),
                                        log_level=LoggingLevel.ERROR,
                                        message=message, code=code)
                                else:
                                    code, message = Messages.getFunctionRedeclared(funcA.get_symbol_name(), False)
                                    Logger.log_message(
                                        error_position=funcB.get_referenced_object().get_source_position(),
                                        log_level=LoggingLevel.ERROR,
                                        message=message, code=code)
                        checked.append(funcA)
            checked_funcs_names.append(func.get_name())
        return