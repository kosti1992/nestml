#
# ast_helper.py
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
from pynestml.meta_model.ast_arithmetic_operator import ASTArithmeticOperator
from pynestml.meta_model.ast_assignment import ASTAssignment
from pynestml.meta_model.ast_block_with_variables import ASTBlockWithVariables
from pynestml.meta_model.ast_constraints_block import ASTConstraintsBlock
from pynestml.meta_model.ast_equations_block import ASTEquationsBlock
from pynestml.meta_model.ast_expression import ASTExpression
from pynestml.meta_model.ast_function import ASTFunction
from pynestml.meta_model.ast_input_block import ASTInputBlock
from pynestml.meta_model.ast_neuron import ASTNeuron
from pynestml.meta_model.ast_node import ASTNode
from pynestml.meta_model.ast_node_factory import ASTNodeFactory
from pynestml.meta_model.ast_ode_equation import ASTOdeEquation
from pynestml.meta_model.ast_ode_function import ASTOdeFunction
from pynestml.meta_model.ast_ode_shape import ASTOdeShape
from pynestml.meta_model.ast_output_block import ASTOutputBlock
from pynestml.meta_model.ast_simple_expression import ASTSimpleExpression
from pynestml.meta_model.ast_update_block import ASTUpdateBlock
from pynestml.meta_model.ast_variable import ASTVariable
from pynestml.symbols.symbol import SymbolKind
from pynestml.symbols.variable_symbol import BlockType, VariableSymbol
from pynestml.utils.logger import Logger, LoggingLevel
from pynestml.utils.messages import Messages


class ASTHelper(object):
    """
    Contains a set of methods as used to interact with AST classes.
    """

    @classmethod
    def resolve_ast_variable_to_variable_symbol(cls, variable):
        # type: (ASTVariable) -> VariableSymbol
        assert variable.get_scope() is not None
        return variable.get_scope().resolve_to_symbol(variable.get_complete_name(), SymbolKind.VARIABLE)

    @classmethod
    def construct_equivalent_direct_assignment_rhs(cls, assignment, operator, lhs_variable, rhs_in_brackets):
        # type: (ASTAssignment,ASTNode,ASTSimpleExpression,ASTExpression) -> ASTExpression
        result = ASTNodeFactory.create_ast_compound_expression(lhs=lhs_variable, binary_operator=operator,
                                                               rhs=rhs_in_brackets,
                                                               source_position=assignment.get_source_position())
        result.update_scope(assignment.get_scope())
        return result

    @classmethod
    def get_bracketed_expression(cls, expression):
        # type: (ASTExpression) -> ASTExpression
        result = ASTNodeFactory.create_ast_expression(is_encapsulated=True,
                                                      expression=expression.get_expression(),
                                                      source_position=expression.get_expression().get_source_position())
        result.update_scope(expression.get_scope())
        return result

    @classmethod
    def get_lhs_variable_as_expression(cls, assignment):
        # type: (ASTAssignment) -> ASTSimpleExpression
        # TODO: maybe calculate new source positions exactly?
        result = ASTNodeFactory.create_ast_simple_expression(variable=assignment.get_variable(),
                                                             source_position=assignment.get_variable().
                                                             get_source_position())
        result.update_scope(assignment.get_scope())
        return result

    @classmethod
    def extract_operator_from_compound_assignment(cls, assignment):
        # type: (ASTAssignment) -> ASTArithmeticOperator
        assert not assignment.is_direct_assignment
        # TODO: maybe calculate new source positions exactly?
        if assignment.is_compound_minus:
            result = ASTNodeFactory.create_ast_arithmetic_operator(is_minus_op=True,
                                                                   source_position=assignment.get_source_position())
        elif assignment.is_compound_product:
            result = ASTNodeFactory.create_ast_arithmetic_operator(is_times_op=True,
                                                                   source_position=assignment.get_source_position())
        elif assignment.is_compound_quotient:
            result = ASTNodeFactory.create_ast_arithmetic_operator(is_div_op=True,
                                                                   source_position=assignment.get_source_position())
        elif assignment.is_compound_sum:
            result = ASTNodeFactory.create_ast_arithmetic_operator(is_plus_op=True,
                                                                   source_position=assignment.get_source_position())
        else:
            raise RuntimeError('Type of compound operator not recognized!')
        result.update_scope(assignment.get_scope())
        return result

    @classmethod
    def deconstruct_compound_assignment(cls, assignment):
        """
        From lhs and rhs it constructs a new expression which corresponds to direct assignment.
        E.g.: a += b*c -> a = a + b*c
        :return: the rhs for an equivalent direct assignment.
        :rtype: ast_expression
        """
        from pynestml.visitors.ast_symbol_table_visitor import ASTSymbolTableVisitor
        # it is already a direct assignment, thus nothing to do
        if assignment.is_direct_assignment:
            return assignment
        operator = cls.extract_operator_from_compound_assignment(assignment)
        lhs_variable = cls.get_lhs_variable_as_expression(assignment)
        rhs_in_brackets = cls.get_bracketed_expression(assignment)
        result = cls.construct_equivalent_direct_assignment_rhs(assignment, operator, lhs_variable, rhs_in_brackets)
        # create symbols for the new Expression:
        result.accept(ASTSymbolTableVisitor())
        return result

    @classmethod
    def get_functions_from_body(cls, body):
        """
        Returns a list of all function block declarations in this body.
        :param body: a single neuron body instance
        :type body: ASTBody
        :return: a list of function declarations.
        :rtype: list(ASTFunction)
        """
        ret = list()
        for elem in body.get_body_elements():
            if isinstance(elem, ASTFunction):
                ret.append(elem)
        return ret

    @classmethod
    def get_update_block_from_body(cls, body):
        """
        Returns the update block defined in the handed over body.
        :param body: a single neuron body instance
        :type body: ASTBody
        :return: an update-block
        :rtype: ASTUpdateBlock
        """
        for elem in body.get_body_elements():
            if isinstance(elem, ASTUpdateBlock):
                return elem

    @classmethod
    def get_state_block_from_body(cls, body):
        """
        Returns the state block defined in the handed over body.
        :param body: a single neuron body instance
        :type body: ASTBody
        :return: a state-block
        :rtype: ASTBlockWithVariables
        """
        for elem in body.get_body_elements():
            if isinstance(elem, ASTBlockWithVariables) and elem.is_state:
                return elem

    @classmethod
    def get_parameter_block_from_body(cls, body):
        """
        Returns a list of all parameter blocks defined in this body.
        :param body: a single neuron body instance
        :type body: ASTBody
        :return: a list of parameters-blocks.
        :rtype: ASTBlockWithVariables
        """
        for elem in body.get_body_elements():
            if isinstance(elem, ASTBlockWithVariables) and elem.is_parameters:
                return elem

    @classmethod
    def get_internals_block_from_body(cls, body):
        """
        Returns the internals block defined in the handed over body.
        :param body: a single neuron body instance
        :type body: ASTBody
        :return: an internals-block
        :rtype: ASTBlockWithVariables
        """
        for elem in body.get_body_elements():
            if isinstance(elem, ASTBlockWithVariables) and elem.is_internals:
                return elem

    @classmethod
    def get_equations_block_from_body(cls, body):
        """
        Returns the equations block defined in the handed over body.
        :param body: a single neuron body instance
        :type body: ASTBody
        :return: an equations-block
        :rtype: ASTEquationsBlock
        """
        for elem in body.get_body_elements():
            if isinstance(elem, ASTEquationsBlock):
                return elem

    @classmethod
    def get_input_block_from_body(cls, body):
        """
        Returns the input-block defined in the handed over body.
        :param body: a single neuron body instance
        :type body: ASTBody
        :return: an input-block
        :rtype: ASTInputBlock
        """
        for elem in body.get_body_elements():
            if isinstance(elem, ASTInputBlock):
                return elem

    @classmethod
    def get_output_block_from_body(cls, body):
        """
        Returns the output-block defined in the handed over body.
        :param body: a single neuron body instance
        :type body: ASTBody
        :return: an output-block
        :rtype: ASTOutputBlock
        """
        for elem in body.get_body_elements():
            if isinstance(elem, ASTOutputBlock):
                return elem

    @classmethod
    def get_spike_buffers_from_body(cls, body):
        """
        Returns a list of all spike input buffers defined in the model.
        :param body: a single neuron body instance
        :type body: ASTBody
        :return: a list of all spike input buffers
        :rtype: list(ASTInputLine)
        """
        ret = list()
        for line in ASTHelper.get_input_block_from_body(body).get_input_lines():
            if line.is_spike():
                ret.append(line)
        return ret

    @classmethod
    def get_ode_equations_from_equations_block(cls, equations_block):
        """
        Returns a list of all ode equations in this block.
        :param equations_block: a single equations block
        :type equations_block: ASTEquationsBlock
        :return: a list of all ode equations.
        :rtype: list(ASTOdeEquations)
        """
        ret = list()
        for decl in equations_block.get_declarations():
            if isinstance(decl, ASTOdeEquation):
                ret.append(decl)
        return ret

    @classmethod
    def get_ode_functions_from_equations_block(cls, equations_block):
        """
        Returns a list of all ode functions in this block.
        :param equations_block: a single equations block
        :type equations_block: ASTEquationsBlock
        :return: a list of all ode shapes.
        :rtype: list(ASTOdeShape)
        """
        ret = list()
        for decl in equations_block.get_declarations():
            if isinstance(decl, ASTOdeFunction):
                ret.append(decl)
        return ret

    @classmethod
    def get_ode_shapes_from_equations_block(cls, equations_block):
        """
        Returns a list of all ode shapes in this block.
        :param equations_block: a single equations block
        :type equations_block: ASTEquationsBlock
        :return: a list of all ode shapes.
        :rtype: list(ASTOdeShape)
        """
        ret = list()
        for decl in equations_block.get_declarations():
            if isinstance(decl, ASTOdeShape):
                ret.append(decl)
        return ret

    @classmethod
    def get_variables_from_expression(cls, expression):
        """
        Returns a list of all variables as used in this rhs.
        :param expression: a single expression
        :type expression: ASTExpression or ASTSimpleExpression
        :return: a list of variables.
        :rtype: list(ASTVariable)
        """
        # todo: refactor me, use higher order visitor instead
        ret = list()
        if isinstance(expression, ASTSimpleExpression):
            if expression.is_variable():
                ret.append(expression.get_variable())
        else:
            if expression.is_expression():
                ret.extend(cls.get_variables_from_expression(expression.get_expression()))
            elif expression.is_compound_expression():
                ret.extend(cls.get_variables_from_expression(expression.get_lhs()))
                ret.extend(cls.get_variables_from_expression(expression.get_rhs()))
            elif expression.is_ternary_operator():
                ret.extend(cls.get_variables_from_expression(expression.get_condition()))
                ret.extend(cls.get_variables_from_expression(expression.get_if_true()))
                ret.extend(cls.get_variables_from_expression(expression.get_if_not()))
        return ret

    @classmethod
    def get_units_from_expression(cls, expression):
        """
        Returns a list of all units as use in this rhs.
        :param expression: a single expression
        :type expression: ASTExpression or ASTSimpleExpression
        :return: a list of all used units.
        :rtype: list(ASTVariable)
        """
        # todo: refactor me, use higher order visitor instead
        ret = list()
        if isinstance(expression, ASTSimpleExpression):
            if expression.has_unit():
                ret.append(expression.get_variable())
        elif isinstance(expression, ASTExpression):
            if expression.is_expression():
                ret.extend(cls.get_units_from_expression(expression.get_expression()))
            elif expression.is_compound_expression():
                ret.extend(cls.get_units_from_expression((expression.get_lhs())))
                ret.extend(cls.get_units_from_expression((expression.get_rhs())))
            elif expression.is_ternary_operator():
                ret.extend(cls.get_units_from_expression(expression.get_condition()))
                ret.extend(cls.get_units_from_expression(expression.get_if_true()))
                ret.extend(cls.get_units_from_expression(expression.get_if_not()))
        return ret

    @classmethod
    def get_function_calls_from_expression(cls, expression):
        """
        Returns a list of all function calls as used in this rhs.
        :param expression: a single expression
        :type expression: ASTExpression or ASTSimpleExpression
        :return: a list of all function calls in this rhs.
        :rtype: list(ASTFunctionCall)
        """
        # todo: refactor me, use higher order visitor instead
        ret = list()
        if isinstance(expression, ASTSimpleExpression):
            if expression.is_function_call():
                ret.append(expression.get_function_call())
        elif isinstance(expression, ASTExpression):
            if expression.is_expression():
                ret.extend(cls.get_function_calls_from_expression(expression.get_expression()))
            elif expression.is_compound_expression():
                ret.extend(cls.get_function_calls_from_expression(expression.get_lhs()))
                ret.extend(cls.get_function_calls_from_expression(expression.get_rhs()))
            elif expression.is_ternary_operator():
                ret.extend(cls.get_function_calls_from_expression(expression.get_condition()))
                ret.extend(cls.get_function_calls_from_expression(expression.get_if_true()))
                ret.extend(cls.get_function_calls_from_expression(expression.get_if_not()))
        return ret

    @classmethod
    def get_functions_from_neuron(cls, neuron):
        """
        Returns a list of all function block declarations in this body.
        :param neuron: a single neuron instance
        :type neuron: ASTNeuron
        :return: a list of function declarations.
        :rtype: list(ASTFunction)
        """
        ret = list()
        from pynestml.meta_model.ast_function import ASTFunction
        for elem in neuron.get_body().get_body_elements():
            if isinstance(elem, ASTFunction):
                ret.append(elem)
        return ret

    @classmethod
    def get_update_block_from_neuron(cls, neuron):
        """
        Returns the update block defined in the handed over neuron.
        :param neuron: a single neuron instance
        :type neuron: ASTNeuron
        :return: an update-block
        :rtype: ASTUpdateBlock
        """
        for elem in neuron.get_body().get_body_elements():
            if isinstance(elem, ASTUpdateBlock):
                return elem

    @classmethod
    def get_state_block_from_neuron(cls, neuron):
        """
        Returns the state blocks defined in the handed over neuron.
        :param neuron: a single neuron instance
        :type neuron: ASTNeuron
        :return: a state-block
        :rtype: ASTBlockWithVariables
        """
        for elem in neuron.get_body().get_body_elements():
            if isinstance(elem, ASTBlockWithVariables) and elem.is_state:
                return elem

    @classmethod
    def get_initial_block_from_neuron(cls, neuron):
        """
        Returns the initial block defined in the handed over neuron.
        :param neuron: a single neuron instance
        :type neuron: ASTNeuron
        :return: an initial-block
        :rtype: ASTBlockWithVariables
        """
        for elem in neuron.get_body().get_body_elements():
            if isinstance(elem, ASTBlockWithVariables) and elem.is_initial_values:
                return elem

    @classmethod
    def get_parameter_block_from_neuron(cls, neuron):
        """
        Returns the parameter block defined in the handed over neuron.
        :param neuron: a single neuron instance
        :type neuron: ASTNeuron
        :return: a parameters-block.
        :rtype: ASTBlockWithVariables
        """
        for elem in neuron.get_body().get_body_elements():
            if isinstance(elem, ASTBlockWithVariables) and elem.is_parameters:
                return elem

    @classmethod
    def get_internals_block_from_neuron(cls, neuron):
        """
        Returns the internals block defined in the handed over neuron.
        :param neuron: a single neuron instance
        :type neuron: ASTNeuron
        :return: an internals-block
        :rtype: ASTBlockWithVariables
        """
        for elem in neuron.get_body().get_body_elements():
            if isinstance(elem, ASTBlockWithVariables) and elem.is_internals:
                return elem

    @classmethod
    def get_equations_block_from_neuron(cls, neuron):
        """
        Returns the equations block defined in the handed over neuron.
        :param neuron: a single neuron instance
        :type neuron: ASTNeuron
        :return: an equations-block
        :rtype: ASTEquationsBlock
        """
        for elem in neuron.get_body().get_body_elements():
            if isinstance(elem, ASTEquationsBlock):
                return elem

    @classmethod
    def remove_equations_block_from_neuron(cls, neuron):
        """
        Deletes all equations blocks. By construction as checked through cocos there is only one there.
        :param neuron: a single neuron instance
        :type neuron: ASTNeuron
        """
        for elem in neuron.get_body().get_body_elements():
            if isinstance(elem, ASTEquationsBlock):
                neuron.get_body().get_body_elements().remove(elem)

    @classmethod
    def get_initial_values_declarations_from_neuron(cls, neuron):
        """
        Returns a list of initial values declarations made in this neuron.
        :param neuron: a single neuron instance
        :type neuron: ASTNeuron
        :return: a list of initial values declarations
        :rtype: list(ASTDeclaration)
        """
        initial_values_block = ASTHelper.get_initial_block_from_neuron(neuron)
        initial_values_declarations = list()
        if initial_values_block is not None:
            for decl in initial_values_block.get_declarations():
                initial_values_declarations.append(decl)
        return initial_values_declarations

    @classmethod
    def get_equations_from_neuron(cls, neuron):
        """
        Returns all ode equations as defined in this neuron.
        :param neuron: a single neuron instance
        :type neuron: ASTNeuron
        :return list of ode-equations
        :rtype list(ASTOdeEquation)
        """
        block = ASTHelper.get_equations_block_from_neuron(neuron)
        return ASTHelper.get_ode_equations_from_equations_block(block)

    @classmethod
    def get_input_block_from_neuron(cls, neuron):
        """
        Returns the input-block defined in the handed over neuron.
        :param neuron: a single neuron instance
        :type neuron: ASTNeuron
        :return: an input-block
        :rtype: ASTInputBlock
        """
        for elem in neuron.get_body().get_body_elements():
            if isinstance(elem, ASTInputBlock):
                return elem

    @classmethod
    def get_input_buffers_from_neuron(cls, neuron):
        """
        Returns a list of all defined input buffers.
        :param neuron: a single neuron instance
        :type neuron: ASTNeuron
        :return: a list of all input buffers.
        :rtype: list(VariableSymbol)
        """
        from pynestml.symbols.variable_symbol import BlockType
        symbols = neuron.get_scope().get_symbols_in_this_scope()
        ret = list()
        for symbol in symbols:
            if isinstance(symbol, VariableSymbol) and (symbol.block_type == BlockType.INPUT_BUFFER_SPIKE or
                                                       symbol.block_type == BlockType.INPUT_BUFFER_CURRENT):
                ret.append(symbol)
        return ret

    @classmethod
    def get_spike_buffers_from_neuron(cls, neuron):
        """
        Returns a list of all spike input buffers defined in the model.
        :param neuron: a single neuron instance
        :type neuron: ASTNeuron
        :return: a list of all spike input buffers.
        :rtype: list(VariableSymbol)
        """
        ret = list()
        for BUFFER in ASTHelper.get_input_buffers_from_neuron(neuron):
            if BUFFER.is_spike_buffer():
                ret.append(BUFFER)
        return ret

    @classmethod
    def get_current_buffers_from_neuron(cls, neuron):
        """
        Returns a list of all current buffers defined in the model.
        :param neuron: a single neuron instance
        :type neuron: ASTNeuron
        :return: a list of all current input buffers.
        :rtype: list(VariableSymbol)
        """
        ret = list()
        for BUFFER in ASTHelper.get_input_buffers_from_neuron(neuron):
            if BUFFER.is_current_buffer():
                ret.append(BUFFER)
        return ret

    @classmethod
    def get_parameter_symbols_from_neuron(cls, neuron):
        """
        Returns a list of all parameter symbol defined in the model.
        :param neuron: a single neuron instance
        :type neuron: ASTNeuron
        :return: a list of parameter symbols.
        :rtype: list(VariableSymbol)
        """
        symbols = neuron.get_scope().get_symbols_in_this_scope()
        ret = list()
        for symbol in symbols:
            if isinstance(symbol, VariableSymbol) and symbol.block_type == BlockType.PARAMETERS and \
                    not symbol.is_predefined:
                ret.append(symbol)
        return ret

    @classmethod
    def get_state_symbols_from_neuron(cls, neuron):
        """
        Returns a list of all state symbol defined in the model.
        :param neuron: a single neuron instance
        :type neuron: ASTNeuron
        :return: a list of state symbols.
        :rtype: list(VariableSymbol)
        """
        symbols = neuron.get_scope().get_symbols_in_this_scope()
        ret = list()
        for symbol in symbols:
            if isinstance(symbol, VariableSymbol) and symbol.block_type == BlockType.STATE and \
                    not symbol.is_predefined:
                ret.append(symbol)
        return ret

    @classmethod
    def get_internal_symbols_from_neuron(cls, neuron):
        """
        Returns a list of all internals symbol defined in the model.
        :param neuron: a single neuron instance
        :type neuron: ASTNeuron
        :return: a list of internals symbols.
        :rtype: list(VariableSymbol)
        """
        symbols = neuron.get_scope().get_symbols_in_this_scope()
        ret = list()
        for symbol in symbols:
            if isinstance(symbol, VariableSymbol) and symbol.block_type == BlockType.INTERNALS and \
                    not symbol.is_predefined:
                ret.append(symbol)
        return ret

    @classmethod
    def get_ode_aliases_from_neuron(cls, neuron):
        """
        Returns a list of all equation function symbols defined in the model.
        :param neuron: a single neuron instance
        :type neuron: ASTNeuron
        :return: a list of equation function  symbols.
        :rtype: list(VariableSymbol)
        """
        from pynestml.symbols.variable_symbol import BlockType
        symbols = neuron.get_scope().get_symbols_in_this_scope()
        ret = list()
        for symbol in symbols:
            if isinstance(symbol, VariableSymbol) and \
                    symbol.block_type == BlockType.EQUATION and symbol.is_function:
                ret.append(symbol)
        return ret

    @classmethod
    def get_variables_defined_by_ode_from_neuron(cls, neuron):
        """
        Returns a list of all variables which are defined by an ode.
        :param neuron: a single neuron instance
        :type neuron: ASTNeuron
        :return: a list of variable symbols
        :rtype: list(VariableSymbol)
        """
        symbols = neuron.get_scope().get_symbols_in_complete_scope()
        ret = list()
        for symbol in symbols:
            if isinstance(symbol, VariableSymbol) and symbol.is_ode_defined():
                ret.append(symbol)
        return ret

    @classmethod
    def get_output_block_from_neuron(cls, neuron):
        """
        Returns a list of all output-blocks defined.
        :param neuron: a single neuron instance
        :type neuron: ASTNeuron
        :return: a list of defined output-blocks.
        :rtype: list(ASTOutputBlock)
        """
        for elem in neuron.get_body().get_body_elements():
            if isinstance(elem, ASTOutputBlock):
                return elem

    @classmethod
    def neuron_is_multisynapse_spikes(cls, neuron):
        """
        Returns whether this neuron uses multi-synapse spikes.
        :param neuron: a single neuron instance
        :type neuron: ASTNeuron
        :return: True if multi-synaptic, otherwise False.
        :rtype: bool
        """
        for iBuffer in ASTHelper.get_spike_buffers_from_neuron(neuron):
            if iBuffer.has_vector_parameter():
                return True
        return False

    @classmethod
    def get_multiple_receptors_from_neuron(cls, neuron):
        """
        Returns a list of all spike buffers which are defined as inhibitory and excitatory.
        :param neuron: a single neuron instance
        :type neuron: ASTNeuron
        :return: a list of spike buffers variable symbols
        :rtype: list(VariableSymbol)
        """
        ret = list()
        for iBuffer in ASTHelper.get_spike_buffers_from_neuron(neuron):
            if iBuffer.is_excitatory() and iBuffer.is_inhibitory():
                if iBuffer is not None:
                    ret.append(iBuffer)
                else:
                    code, message = Messages.get_could_not_resolve(iBuffer.getSymbolName())
                    Logger.log_message(
                            message=message,
                            code=code,
                            error_position=iBuffer.getSourcePosition(),
                            log_level=LoggingLevel.ERROR)
        return ret

    @classmethod
    def get_constraint_block_from_neuron(cls, neuron):
        """
        Returns the constraint block of the model, if any defined.
        :param neuron: a single neuron instance
        :type neuron: ASTNeuron
        :return: a single constraint block
        :rtype: ASTConstraintBlock
        """
        for block in neuron.get_body().get_body_elements():
            if isinstance(block, ASTConstraintsBlock):
                return block

    @classmethod
    def get_parameter_non_alias_symbols_from_neuron(cls, neuron):
        """
        Returns a list of all variable symbols representing non-function parameter variables.
        :param neuron: a single neuron instance
        :type neuron: ASTNeuron
        :return: a list of variable symbols
        :rtype: list(VariableSymbol)
        """
        ret = list()
        for param in ASTHelper.get_parameter_symbols_from_neuron(neuron):
            if not param.is_function and not param.is_predefined:
                ret.append(param)
        return ret

    @classmethod
    def get_state_non_alias_symbols_from_neuron(cls, neuron):
        """
        Returns a list of all variable symbols representing non-function state variables.
        :param neuron: a single neuron instance
        :type neuron: ASTNeuron
        :return: a list of variable symbols
        :rtype: list(VariableSymbol)
        """
        ret = list()
        for param in ASTHelper.get_state_symbols_from_neuron(neuron):
            if not param.is_function and not param.is_predefined:
                ret.append(param)
        return ret

    @classmethod
    def get_initial_values_non_alias_symbols_from_neuron(cls, neuron):
        """
        Returns a list of all variable symbols representing non-function initial value variables.
        :param neuron: a single neuron instance
        :type neuron: ASTNeuron
        :return: a list of variable symbols
        :rtype: list(VariableSymbol)
        """
        ret = list()
        for init in ASTHelper.get_initial_values_symbols_from_neuron(neuron):
            if not init.is_function and not init.is_predefined:
                ret.append(init)
        return ret

    @classmethod
    def get_internal_non_alias_symbols_from_neuron(cls, neuron):
        """
        Returns a list of all variable symbols representing non-function internal variables.
        :param neuron: a single neuron instance
        :type neuron: ASTNeuron
        :return: a list of variable symbols
        :rtype: list(VariableSymbol)
        """
        ret = list()
        for param in ASTHelper.get_internal_symbols_from_neuron(neuron):
            if not param.is_function and not param.is_predefined:
                ret.append(param)
        return ret

    @classmethod
    def get_initial_values_symbols_from_neuron(cls, neuron):
        """
        Returns a list of all initial values symbol defined in the model.
        :param neuron: a single neuron instance
        :type neuron: ASTNeuron
        :return: a list of initial values symbols.
        :rtype: list(VariableSymbol)
        """
        symbols = neuron.get_scope().get_symbols_in_this_scope()
        ret = list()
        for symbol in symbols:
            if isinstance(symbol, VariableSymbol) and symbol.block_type == BlockType.INITIAL_VALUES and \
                    not symbol.is_predefined:
                ret.append(symbol)
        return ret

    @classmethod
    def get_initial_values_block_from_neuron(cls, neuron):
        """
        Returns the initial values block defined in the handed over neuron.
        :param neuron: a single neuron instance
        :type neuron: ASTNeuron
        :return: a list of initial-blocks.
        :rtype: ASTBlockWithVariables
        """
        for elem in neuron.get_body().get_body_elements():
            if isinstance(elem, ASTBlockWithVariables) and elem.is_initial_values:
                return elem

    @classmethod
    def remove_initial_blocks_from_neuron(cls, neuron):
        """
        Removes all equation blocks from the neuron instance.
        :param neuron: a single neuron instance
        :type neuron: ASTNeuron
        """
        from pynestml.meta_model.ast_block_with_variables import ASTBlockWithVariables
        for elem in neuron.get_body().get_body_elements():
            if isinstance(elem, ASTBlockWithVariables) and elem.is_initial_values:
                neuron.get_body().get_body_elements().remove(elem)

    @classmethod
    def get_function_initial_values_symbols_from_neuron(cls, neuron):
        """
        Returns a list of all initial values symbols as defined in the model which are marked as functions.
        :param neuron: a single neuron instance
        :type neuron: ASTNeuron
        :return: a list of symbols
        :rtype: list(VariableSymbol)
        """
        ret = list()
        for symbol in ASTHelper.get_initial_values_symbols_from_neuron(neuron):
            if symbol.is_function:
                ret.append(symbol)
        return ret

    @classmethod
    def get_non_function_initial_values_symbols_from_neuron(cls, neuron):
        """
        Returns a list of all initial values symbols as defined in the model which are not marked as functions.
        :param neuron: a single neuron instance
        :type neuron: ASTNeuron
        :return: a list of symbols
        :rtype:list(VariableSymbol)
        """
        ret = list()
        for symbol in ASTHelper.get_initial_values_symbols_from_neuron(neuron):
            if not symbol.is_function:
                ret.append(symbol)
        return ret

    @classmethod
    def get_ode_defined_symbols_from_neuron(cls, neuron):
        """
        Returns a list of all variable symbols which have been defined in th initial_values blocks
        and are provided with an ode.
        :param neuron: a single neuron instance
        :type neuron: ASTNeuron
        :return: a list of initial value variables with odes
        :rtype: list(VariableSymbol)
        """
        symbols = neuron.get_scope().get_symbols_in_this_scope()
        ret = list()
        for symbol in symbols:
            if isinstance(symbol, VariableSymbol) and \
                    symbol.block_type == BlockType.INITIAL_VALUES and symbol.is_ode_defined() \
                    and not symbol.is_predefined:
                ret.append(symbol)
        return ret

    @classmethod
    def get_state_symbols_without_ode_from_neuron(cls, neuron):
        """
        Returns a list of all elements which have been defined in the state block.
        :param neuron: a single neuron
        :type neuron: ASTNeuron
        :return: a list of of state variable symbols.
        :rtype: list(VariableSymbol)
        """
        symbols = neuron.get_scope().get_symbols_in_this_scope()
        ret = list()
        for symbol in symbols:
            if isinstance(symbol, VariableSymbol) and \
                    symbol.block_type == BlockType.STATE and not symbol.is_ode_defined() \
                    and not symbol.is_predefined:
                ret.append(symbol)
        return ret

    @classmethod
    def neuron_has_array_buffer(cls, neuron):
        """
        This method indicates whether this neuron uses buffers defined vector-wise.
        :param neuron: A single neuron instance
        :type neuron: ASTNeuron
        :return: True if vector buffers defined, otherwise False.
        :rtype: bool
        """
        for BUFFER in ASTHelper.get_input_buffers_from_neuron(neuron):
            if BUFFER.has_vector_parameter():
                return True
        return False

    @classmethod
    def get_parameter_invariants_from_neuron(cls, neuron):
        """
        Returns a list of all invariants of all parameters.
        :return: a list of rhs representing invariants
        :rtype: list(ASTExpression)
        """
        ret = list()
        block = ASTHelper.get_parameter_block_from_neuron(neuron)
        if block is not None:
            for decl in block.get_declarations():
                if decl.has_invariant():
                    ret.append(decl.get_invariant())
        return ret

    @classmethod
    def add_to_internal_block(cls, neuron, declaration):
        """
        Adds the handed over declaration the internal block
        :param neuron: a single neuron instance
        :type neuron: ASTNeuron
        :param declaration: a single declaration
        :type declaration: ASTDeclaration
        """
        from pynestml.utils.ast_utils import ASTUtils
        if ASTHelper.get_internals_block_from_neuron(neuron) is None:
            ASTUtils.create_internal_block(neuron)
            ASTHelper.get_internals_block_from_neuron(neuron).get_declarations().append(declaration)
        return

    @classmethod
    def add_to_initial_values_block(cls, neuron, declaration):
        """
        Adds the handed over declaration to the initial values block.
        :param neuron: a single neuron instance
        :type neuron: ASTNeuron
        :param declaration: a single declaration.
        :type declaration: ASTDeclaration
        """
        from pynestml.utils.ast_utils import ASTUtils
        if ASTHelper.get_initial_block_from_neuron(neuron) is None:
            ASTUtils.create_initial_values_block(neuron)
        ASTHelper.get_initial_block_from_neuron(neuron).get_declarations().append(declaration)
        return

    @classmethod
    def add_shape(cls, neuron, shape):
        """
        Adds the handed over declaration to the initial values block.
        :param neuron: a single neuron instance
        :type neuron: ASTNeuron
        :param shape: a single declaration.
        :type shape: ASTOdeShape
        """
        assert ASTHelper.get_equations_block_from_neuron(neuron) is not None
        ASTHelper.get_equations_block_from_neuron(neuron).get_declarations().append(shape)

    """
    The following print methods are used by the backend and represent the comments as stored at the corresponding 
    parts of the neuron definition.
    """

    @classmethod
    def print_update_comment_from_neuron(cls, neuron, prefix = None):
        """
        Prints the update block comment.
        :param neuron: a single neuron instance
        :type neuron: ASTNeuron
        :param prefix: a prefix string
        :type prefix: str
        :return: the corresponding comment.
        :rtype: str
        """
        block = ASTHelper.get_update_block_from_neuron(neuron)
        if block is None:
            return prefix if prefix is not None else ''
        return block.print_comment(prefix)

    @classmethod
    def print_parameter_comment_from_neuron(cls, neuron, prefix = None):
        """
        Prints the update block comment.
        :param neuron: a single neuron instance
        :type neuron: ASTNeuron
        :param prefix: a prefix string
        :type prefix: str
        :return: the corresponding comment.
        :rtype: str
        """
        block = ASTHelper.get_parameter_block_from_neuron(neuron)
        if block is None:
            return prefix if prefix is not None else ''
        return block.print_comment(prefix)

    @classmethod
    def print_state_comment_from_neuron(cls, neuron, prefix = None):
        """
        Prints the state block comment.
        :param neuron: a single neuron instance
        :type neuron: ASTNeuron
        :param prefix: a prefix string
        :type prefix: str
        :return: the corresponding comment.
        :rtype: str
        """
        block = ASTHelper.get_state_block_from_neuron(neuron)
        if block is None:
            return prefix if prefix is not None else ''
        return block.print_comment(prefix)

    @classmethod
    def print_internal_comment_from_neuron(cls, neuron, prefix = None):
        """
        Prints the internal block comment.
        :param neuron: a single neuron instance
        :type neuron: ASTNeuron
        :param prefix: a prefix string
        :type prefix: str
        :return: the corresponding comment.
        :rtype: str
        """
        block = ASTHelper.get_internals_block_from_neuron(neuron)
        if block is None:
            return prefix if prefix is not None else ''
        return block.print_comment(prefix)

    @classmethod
    def print_comment_from_neuron(cls, neuron, prefix = None):
        """
        Prints the header information of this neuron.
        :param neuron: a single neuron instance
        :type neuron: ASTNeuron
        :param prefix: a prefix string
        :type prefix: str
        :return: the comment.
        :rtype: str
        """
        ret = ''
        if neuron.get_comment() is None or len(neuron.get_comment()) == 0:
            return prefix if prefix is not None else ''
        for comment in neuron.get_comment():
            ret += (prefix if prefix is not None else '') + comment + '\n'
        return ret
