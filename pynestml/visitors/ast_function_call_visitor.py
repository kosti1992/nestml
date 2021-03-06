#
# ast_function_call_visitor.py
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
"""
simpleExpression : functionCall
"""
from pynestml.meta_model.ast_simple_expression import ASTSimpleExpression
from pynestml.symbols.error_type_symbol import ErrorTypeSymbol
from pynestml.symbols.predefined_functions import PredefinedFunctions
from pynestml.symbols.symbol import SymbolKind
from pynestml.symbols.void_type_symbol import VoidTypeSymbol
from pynestml.utils.logger import Logger, LoggingLevel
from pynestml.utils.messages import Messages
from pynestml.visitors.ast_visitor import ASTVisitor


class ASTFunctionCallVisitor(ASTVisitor):
    """
    Visits a single function call and updates its type.
    """

    def visit_simple_expression(self, node):
        """
        Visits a single function call as stored in a simple expression and derives the correct type of all its
        parameters.
        :param node: a simple expression
        :type node: ASTSimpleExpression
        :rtype void
        """
        assert isinstance(node, ASTSimpleExpression), \
            '(PyNestML.Visitor.FunctionCallVisitor) No or wrong type of simple expression provided (%s)!' % type(node)
        assert (node.get_scope() is not None), \
            "(PyNestML.Visitor.FunctionCallVisitor) No scope found, run symboltable creator!"
        scope = node.get_scope()
        function_name = node.get_function_call().get_name()
        method_symbol = scope.resolve_to_symbol(function_name, SymbolKind.FUNCTION)
        # check if this function exists
        if method_symbol is None:
            code, message = Messages.get_could_not_resolve(function_name)
            Logger.log_message(code=code, message=message, error_position=node.get_source_position(),
                               log_level=LoggingLevel.ERROR)
            node.type = ErrorTypeSymbol()
            return
        return_type = method_symbol.get_return_type()
        return_type.referenced_object = node

        # convolve symbol does not have a return type set.
        # returns whatever type the second parameter is.
        if function_name == PredefinedFunctions.CONVOLVE:
            # Deviations from the assumptions made here are handled in the convolveCoco
            buffer_parameter = node.get_function_call().get_args()[1]

            if buffer_parameter.getVariable() is not None:
                buffer_name = buffer_parameter.getVariable().getName()
                buffer_symbol_resolve = scope.resolve_to_symbol(buffer_name, SymbolKind.VARIABLE)
                if buffer_symbol_resolve is not None:
                    node.type = buffer_symbol_resolve.getTypeSymbol()
                    return

            # getting here means there is an error with the parameters to convolve
            code, message = Messages.get_convolve_needs_buffer_parameter()
            Logger.log_message(code=code, message=message, error_position=node.get_source_position(),
                               log_level=LoggingLevel.ERROR)
            node.type = ErrorTypeSymbol()
            return

        if isinstance(method_symbol.get_return_type(), VoidTypeSymbol):
            code, message = Messages.get_void_function_on_rhs(function_name, str(node))
            Logger.log_message(code=code, message=message, error_position=node.get_source_position(),
                               log_level=LoggingLevel.ERROR)
            node.type = ErrorTypeSymbol()
            return

        # if nothing special is handled, just get the expression type from the return type of the function
        node.type = return_type
