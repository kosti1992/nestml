#
# ASTUtils.py
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
from pynestml.utils.Logger import LoggingLevel, Logger

from pynestml.modelprocessor.PredefinedFunctions import PredefinedFunctions
from pynestml.modelprocessor.Symbol import SymbolKind


class ASTUtils(object):
    """
    A collection of helpful methods.
    """

    @classmethod
    def get_all_neurons(cls, list_of_compilation_units):
        """
        For a list of compilation units, it returns a list containing all neurons defined in all compilation
        units.
        :param list_of_compilation_units: a list of compilation units.
        :type list_of_compilation_units: list(ASTNestMLCompilationUnit)
        :return: a list of neurons
        :rtype: list(ASTNeuron)
        """
        ret = list()
        for compilationUnit in list_of_compilation_units:
            ret.extend(compilationUnit.get_neuron_list())
        return ret

    @classmethod
    def is_small_stmt(cls, ast):
        """
        Indicates whether the handed over ast is a small statement. Used in the template.
        :param ast: a single ast object.
        :type ast: AST_
        :return: True if small stmt, otherwise False.
        :rtype: bool
        """
        from pynestml.modelprocessor.ASTSmallStmt import ASTSmallStmt
        return isinstance(ast, ASTSmallStmt)

    @classmethod
    def is_compound_stmt(cls, ast):
        """
        Indicates whether the handed over ast is a compound statement. Used in the template.
        :param ast: a single ast object.
        :type ast: AST_
        :return: True if compound stmt, otherwise False.
        :rtype: bool
        """
        from pynestml.modelprocessor.ASTCompoundStmt import ASTCompoundStmt
        return isinstance(ast, ASTCompoundStmt)

    @classmethod
    def is_integrate(cls, function_call):
        """
        Checks if the handed over function call is a ode integration function call.
        :param function_call: a single function call
        :type function_call: ASTFunctionCall
        :return: True if ode integration call, otherwise False.
        :rtype: bool
        """
        return function_call.get_name() == PredefinedFunctions.INTEGRATE_ODES

    @classmethod
    def is_spike_input(cls, body):
        """
        Checks if the handed over neuron contains a spike input buffer.
        :param body: a single body element.
        :type body: ASTBody
        :return: True if spike buffer is contained, otherwise false.
        :rtype: bool
        """
        inputs = (inputL for block in body.get_input_blocks() for inputL in block.getInputLines())
        for inputL in inputs:
            if inputL.is_spike():
                return True
        return False

    @classmethod
    def is_current_input(cls, body):
        """
        Checks if the handed over neuron contains a current input buffer.
        :param body: a single body element.
        :type body: ASTBody
        :return: True if current buffer is contained, otherwise false.
        :rtype: bool
        """
        inputs = (inputL for block in body.get_input_blocks() for inputL in block.getInputLines())
        for inputL in inputs:
            if inputL.is_current():
                return True
        return False

    @classmethod
    def compute_type_name(cls, data_type):
        """
        Computes the representation of the data type.
        :param data_type: a single data type.
        :type data_type: ASTDataType
        :return: the corresponding representation.
        :rtype: str
        """
        if data_type.is_boolean():
            return 'boolean'
        elif data_type.is_integer():
            return 'integer'
        elif data_type.is_real():
            return 'real'
        elif data_type.is_string():
            return 'string'
        elif data_type.is_void():
            return 'void'
        elif data_type.is_unit_type():
            return str(data_type)
        else:
            Logger.log_message('Type could not be derived!', LoggingLevel.ERROR)
            return ''

    @classmethod
    def deconstruct_assignment(cls, lhs=None, is_plus=False, is_minus=False, is_times=False, is_divide=False,
                               _rhs=None):
        """
        From lhs and rhs it constructs a new rhs which corresponds to direct assignment.
        E.g.: a += b*c -> a = a + b*c
        :param lhs: a lhs rhs
        :type lhs: ASTExpression or ASTSimpleExpression
        :param is_plus: is plus assignment
        :type is_plus: bool
        :param is_minus: is minus assignment
        :type is_minus: bool
        :param is_times: is times assignment
        :type is_times: bool
        :param is_divide: is divide assignment
        :type is_divide: bool
        :param _rhs: a rhs rhs
        :type _rhs: ASTExpression or ASTSimpleExpression
        :return: a new direct assignment rhs.
        :rtype: ASTExpression
        """
        from pynestml.modelprocessor.ASTSymbolTableVisitor import ASTSymbolTableVisitor
        from pynestml.modelprocessor.ASTNodeFactory import ASTNodeFactory
        assert ((is_plus + is_minus + is_times + is_divide) == 1), \
            '(PyNestML.CodeGeneration.Utils) Type of assignment not correctly specified!'
        if is_plus:
            op = ASTNodeFactory.create_ast_arithmetic_operator(is_plus_op=True,
                                                               source_position=_rhs.get_source_position())
        elif is_minus:
            op = ASTNodeFactory.create_ast_arithmetic_operator(is_minus_op=True,
                                                               source_position=_rhs.get_source_position())
        elif is_times:
            op = ASTNodeFactory.create_ast_arithmetic_operator(is_times_op=True,
                                                               source_position=_rhs.get_source_position())
        else:
            op = ASTNodeFactory.create_ast_arithmetic_operator(is_div_op=True,
                                                               source_position=_rhs.get_source_position())
        var_expr = ASTNodeFactory.create_ast_simple_expression(variable=lhs,
                                                               source_position=lhs.get_source_position())
        var_expr.update_scope(lhs.get_scope())
        op.update_scope(lhs.get_scope())
        rhs_in_brackets = ASTNodeFactory.create_ast_expression(is_encapsulated=True, expression=_rhs,
                                                               source_position=_rhs.get_source_position())
        rhs_in_brackets.update_scope(_rhs.get_scope())
        expr = ASTNodeFactory.create_ast_compound_expression(lhs=var_expr, binary_operator=op, rhs=rhs_in_brackets,
                                                             source_position=_rhs.get_source_position())
        expr.update_scope(lhs.get_scope())
        # update the symbols
        expr.accept(ASTSymbolTableVisitor())
        return expr

    @classmethod
    def get_alias_symbols(cls, ast):
        """
        For the handed over ast, this method collects all functions aka. aliases in it.
        :param ast: a single ast node
        :type ast: AST_
        :return: a list of all alias variable symbols
        :rtype: list(VariableSymbol)
        """
        ret = list()
        from pynestml.modelprocessor.ASTHigherOrderVisitor import ASTHigherOrderVisitor
        from pynestml.modelprocessor.ASTVariable import ASTVariable
        res = list()

        def loc_get_vars(node):
            if isinstance(node, ASTVariable):
                res.append(node)

        ast.accept(ASTHigherOrderVisitor(visit_funcs=loc_get_vars))

        for var in res:
            if '\'' not in var.get_complete_name():
                symbol = ast.get_scope().resolve_to_symbol(var.get_complete_name(), SymbolKind.VARIABLE)
                if symbol.is_function():
                    ret.append(symbol)
        return ret

    @classmethod
    def is_castable_to(cls, type_a, type_b):
        """
        Indicates whether typeA can be casted to type b. E.g., in Nest, a unit is always casted down to real, thus
        a unit where unit is expected is allowed.
        :param type_a: a single TypeSymbol
        :type type_a: TypeSymbol
        :param type_b: a single TypeSymbol
        :type type_b: TypeSymbol
        :return: True if castable, otherwise False
        :rtype: bool
        """
        # we can always cast from unit to real
        if type_a.is_unit() and type_b.is_real():
            return True
        elif type_a.is_boolean() and type_b.is_real():
            return True
        elif type_a.is_real() and type_b.is_boolean():
            return True
        elif type_a.is_integer() and type_b.is_real():
            return True
        elif type_a.is_real() and type_b.is_integer():
            return True
        else:
            return False

    @classmethod
    def differs_in_magnitude(cls, type_a, type_b):
        """
        Indicates whether both type represent the same unit but with different magnitudes. This
        case is still valid, e.g., mV can be assigned to volt.
        :param type_a: a type
        :type type_a:  TypeSymbol
        :param type_b: a type
        :type type_b: TypeSymbol
        :return: True if both elements equal or differ in magnitude, otherwise False.
        :rtype: bool
        """
        if type_a.equals(type_b):
            return True
        # in the case that we don't deal with units, there are no magnitudes
        if not (type_a.is_unit() and type_b.is_unit()):
            return False
        # if it represents the same unit, if we disregard the prefix and simplify it
        unit_a = type_a.get_unit().unit
        unit_b = type_b.get_unit().unit
        # if isinstance(unit_a,)
        from astropy import units
        # TODO: consider even more complex cases which can be resolved to the same unit?
        if isinstance(unit_a, units.PrefixUnit) and isinstance(type_b, units.PrefixUnit) \
                and unit_a.physical_type == unit_b.physical_type:
            return True
        return False

    @classmethod
    def get_all(cls, ast, type):
        """
        Finds all ast which are part of the tree as spanned by the handed over ast. The type has to be specified.
        :param ast: a single ast node
        :type ast: AST_
        :param type: the type
        :type type: AST_
        :return: a list of all ast of the specified type
        :rtype: list(AST_)
        """
        from pynestml.modelprocessor.ASTHigherOrderVisitor import ASTHigherOrderVisitor
        ret = list()

        def loc_get_all_of_type(node):
            if isinstance(node, type):
                ret.append(node)

        ast.accept(ASTHigherOrderVisitor(visit_funcs=loc_get_all_of_type))
        return ret

    @classmethod
    def get_vectorized_variable(cls, ast, scope):
        """
        Returns all variable symbols which are contained in the scope and have a size parameter.
        :param ast: a single ast
        :type ast: AST_
        :param scope: a scope object
        :type scope: Scope
        :return: the first element with the size parameter
        :rtype: VariableSymbol
        """
        from pynestml.modelprocessor.ASTVariable import ASTVariable
        from pynestml.modelprocessor.Symbol import SymbolKind
        variables = (var for var in cls.get_all(ast, ASTVariable) if
                     scope.resolve_to_symbol(var.get_complete_name(), SymbolKind.VARIABLE))
        for var in variables:
            symbol = scope.resolve_to_symbol(var.get_complete_name(), SymbolKind.VARIABLE)
            if symbol is not None and symbol.has_vector_parameter():
                return symbol
        return None

    @classmethod
    def get_function_call(cls, ast, function_name):
        """
        Collects for a given name all function calls in a given ast node.
        :param ast: a single node
        :type ast: ASTNode
        :param function_name: the name of the function
        :type function_name: str
        :return: a list of all function calls contained in _ast
        :rtype: list(ASTFunctionCall)
        """
        from pynestml.modelprocessor.ASTHigherOrderVisitor import ASTHigherOrderVisitor
        from pynestml.modelprocessor.ASTFunctionCall import ASTFunctionCall
        ret = list()

        def loc_get_function(node):
            if isinstance(node, ASTFunctionCall) and node.get_name() == function_name:
                ret.append(node)

        ast.accept(ASTHigherOrderVisitor(loc_get_function, list()))
        return ret

    @classmethod
    def get_tuple_from_single_dict_entry(cls, dict_entry):
        """
        For a given dict of length 1, this method returns a tuple consisting of (key,value)
        :param dict_entry: a dict of length 1
        :type dict_entry:  dict
        :return: a single tuple
        :rtype: tuple
        """
        if len(dict_entry.keys()) == 1:
            # key() is not an actual list, thus indexing is not possible.
            for keyIter in dict_entry.keys():
                key = keyIter
                value = dict_entry[key]
                return key, value
        else:
            return None, None

    @classmethod
    def needs_arguments(cls, ast_function_call):
        """
        Indicates whether a given function call has any arguments
        :param ast_function_call: a function call
        :type ast_function_call: ASTFunctionCall
        :return: True if arguments given, otherwise false
        :rtype: bool
        """
        return len(ast_function_call.get_args()) > 0

    @classmethod
    def create_internal_block(cls, neuron):
        """
        Creates a single internal block in the handed over neuron.
        :param neuron: a single neuron
        :type neuron: ASTNeuron
        :return: the modified neuron
        :rtype: ASTNeuron
        """
        from pynestml.modelprocessor.ASTNodeFactory import ASTNodeFactory
        if neuron.get_internals_blocks() is None:
            internal = ASTNodeFactory.create_ast_block_with_variables(False, False, True, False, list(),
                                                                      ASTSourcePosition.get_added_source_position())
            neuron.get_body().get_body_elements().append(internal)
        return neuron

    @classmethod
    def create_state_block(cls, neuron):
        """
        Creates a single internal block in the handed over neuron.
        :param neuron: a single neuron
        :type neuron: ASTNeuron
        :return: the modified neuron
        :rtype: ASTNeuron
        """
        # local import since otherwise circular dependency
        from pynestml.modelprocessor.ASTNodeFactory import ASTNodeFactory
        if neuron.get_internals_blocks() is None:
            state = ASTNodeFactory.create_ast_block_with_variables(True, False, False, False, list(),
                                                                   ASTSourcePosition.get_added_source_position())
            neuron.get_body().get_body_elements().append(state)
        return neuron

    @classmethod
    def create_initial_values_block(cls, neuron):
        """
        Creates a single initial values block in the handed over neuron.
        :param neuron: a single neuron
        :type neuron: ASTNeuron
        :return: the modified neuron
        :rtype: ASTNeuron
        """
        # local import since otherwise circular dependency
        from pynestml.modelprocessor.ASTNodeFactory import ASTNodeFactory
        if neuron.get_initial_blocks() is None:
            inits = ASTNodeFactory.create_ast_block_with_variables(False, False, False, True, list(),
                                                                   ASTSourcePosition.get_added_source_position())
            neuron.get_body().get_body_elements().append(inits)
        return neuron
