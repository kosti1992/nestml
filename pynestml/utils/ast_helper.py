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
from pynestml.meta_model.ast_block_with_variables import ASTBlockWithVariables
from pynestml.meta_model.ast_equations_block import ASTEquationsBlock
from pynestml.meta_model.ast_function import ASTFunction
from pynestml.meta_model.ast_input_block import ASTInputBlock
from pynestml.meta_model.ast_node_factory import ASTNodeFactory
from pynestml.meta_model.ast_ode_equation import ASTOdeEquation
from pynestml.meta_model.ast_ode_function import ASTOdeFunction
from pynestml.meta_model.ast_ode_shape import ASTOdeShape
from pynestml.meta_model.ast_output_block import ASTOutputBlock
from pynestml.meta_model.ast_update_block import ASTUpdateBlock
from pynestml.meta_model.ast_variable import ASTVariable
from pynestml.symbols.symbol import SymbolKind
from pynestml.symbols.variable_symbol import VariableSymbol


class ASTHelper(object):
    # todo: update comments
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
        from pynestml.meta_model.ast_node_factory import ASTNodeFactory
        result = ASTNodeFactory.create_ast_compound_expression(lhs=lhs_variable, binary_operator=operator,
                                                               rhs=rhs_in_brackets,
                                                               source_position=assignment.get_source_position())
        result.update_scope(assignment.get_scope())
        return result

    @classmethod
    def get_bracketed_rhs_expression(cls, assignment):
        from pynestml.meta_model.ast_node_factory import ASTNodeFactory
        result = ASTNodeFactory.create_ast_expression(is_encapsulated=True,
                                                      expression=assignment.get_expression(),
                                                      source_position=assignment.get_expression().get_source_position())
        result.update_scope(assignment.get_scope())
        return result

    @classmethod
    def get_lhs_variable_as_expression(cls, assignment):
        from pynestml.meta_model.ast_node_factory import ASTNodeFactory
        # TODO: maybe calculate new source positions exactly?
        result = ASTNodeFactory.create_ast_simple_expression(variable=assignment.get_variable(),
                                                             source_position=assignment.get_variable().
                                                             get_source_position())
        result.update_scope(assignment.get_scope())
        return result

    @classmethod
    def extract_operator_from_compound_assignment(cls, assignment):
        assert not assignment.is_direct_assignment
        # TODO: maybe calculate new source positions exactly?
        result = None
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
        # TODO: get rid of this through polymorphism?
        assert not assignment.is_direct_assignment, "Can only be invoked on a compound assignment."

        operator = cls.extract_operator_from_compound_assignment(assignment)
        lhs_variable = cls.get_lhs_variable_as_expression(assignment)
        rhs_in_brackets = cls.get_bracketed_rhs_expression(assignment)
        result = cls.construct_equivalent_direct_assignment_rhs(assignment, operator, lhs_variable, rhs_in_brackets)
        # create symbols for the new Expression:
        visitor = ASTSymbolTableVisitor()
        result.accept(visitor)
        return result

    @classmethod
    def get_functions_from_body(cls, body):
        """
        Returns a list of all function block declarations in this body.
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
        Returns a list of all update blocks defined in this body.
        :return: a list of update-block elements.
        :rtype: list(ASTUpdateBlock)
        """
        ret = list()

        for elem in body.get_body_elements():
            if isinstance(elem, ASTUpdateBlock):
                ret.append(elem)
        return ret

    @classmethod
    def get_state_block_from_body(cls, body):
        """
        Returns a list of all state blocks defined in this body.
        :return: a list of state-blocks.
        :rtype: list(ASTBlockWithVariables)
        """
        ret = list()
        for elem in body.get_body_elements():
            if isinstance(elem, ASTBlockWithVariables) and elem.is_state:
                ret.append(elem)
        return ret

    @classmethod
    def get_parameter_block_from_body(cls, body):
        """
        Returns a list of all parameter blocks defined in this body.
        :return: a list of parameters-blocks.
        :rtype: list(ASTBlockWithVariables)
        """
        ret = list()
        from pynestml.meta_model.ast_block_with_variables import ASTBlockWithVariables
        for elem in body.get_body_elements():
            if isinstance(elem, ASTBlockWithVariables) and elem.is_parameters:
                ret.append(elem)
        return ret

    @classmethod
    def get_internals_block_from_body(cls, body):
        """
        Returns a list of all internals blocks defined in this body.
        :return: a list of internals-blocks.
        :rtype: list(ASTBlockWithVariables)
        """
        ret = list()
        for elem in body.get_body_elements():
            if isinstance(elem, ASTBlockWithVariables) and elem.is_internals:
                ret.append(elem)
        return ret

    @classmethod
    def get_equations_block_from_body(cls, body):
        """
        Returns a list of all equations blocks defined in this body.
        :return: a list of equations-blocks.
        :rtype: list(ASTEquationsBlock)
        """
        ret = list()
        for elem in body.get_body_elements():
            if isinstance(elem, ASTEquationsBlock):
                ret.append(elem)
        return ret

    @classmethod
    def get_input_block_from_body(cls, body):
        """
        Returns a list of all input-blocks defined.
        :return: a list of defined input-blocks.
        :rtype: list(ASTInputBlock)
        """
        ret = list()
        for elem in body.get_body_elements():
            if isinstance(elem, ASTInputBlock):
                ret.append(elem)
        return ret

    @classmethod
    def get_output_block_from_body(cls, body):
        """
        Returns a list of all output-blocks defined.
        :return: a list of defined output-blocks.
        :rtype: list(ASTOutputBlock)
        """
        ret = list()
        for elem in body.get_body_elements():
            if isinstance(elem, ASTOutputBlock):
                ret.append(elem)
        return ret

    @classmethod
    def get_spike_buffers_from_body(cls, body):
        """
        Returns a list of all spike input buffers defined in the model.
        :return: a list of all spike input buffers
        :rtype: list(ASTInputLine)
        """
        ret = list()
        blocks = cls.get_input_block_from_body(body)
        if isinstance(blocks, list):
            for block in blocks:
                for line in block.get_input_lines():
                    if line.is_spike():
                        ret.append(line)
            return ret
        else:
            return ret

    @classmethod
    def get_ode_equations_from_equations_block(cls, equations_block):
        """
        Returns a list of all ode equations in this block.
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
        :return: a list of variables.
        :rtype: list(ASTVariable)
        """
        from pynestml.meta_model.ast_simple_expression import ASTSimpleExpression
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
        :return: a list of all used units.
        :rtype: list(ASTVariable)
        """
        from pynestml.meta_model.ast_simple_expression import ASTSimpleExpression
        from pynestml.meta_model.ast_expression import ASTExpression
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
        Returns a list of all function calls as used in this rhs
        :return: a list of all function calls in this rhs.
        :rtype: list(ASTFunctionCall)
        """
        from pynestml.meta_model.ast_simple_expression import ASTSimpleExpression
        from pynestml.meta_model.ast_expression import ASTExpression
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
        Returns a list of all update blocks defined in this body.
        :return: a list of update-block elements.
        :rtype: list(ASTUpdateBlock)
        """
        ret = list()
        from pynestml.meta_model.ast_update_block import ASTUpdateBlock
        for elem in neuron.get_body().get_body_elements():
            if isinstance(elem, ASTUpdateBlock):
                ret.append(elem)
        if isinstance(ret, list) and len(ret) == 1:
            return ret[0]
        elif isinstance(ret, list) and len(ret) == 0:
            return None
        else:
            return ret

    @classmethod
    def get_state_block_from_neuron(cls, neuron):
        """
        Returns a list of all state blocks defined in this body.
        :return: a list of state-blocks.
        :rtype: list(ASTBlockWithVariables)
        """
        ret = None
        for elem in neuron.get_body().get_body_elements():
            if isinstance(elem, ASTBlockWithVariables) and elem.is_state:
                ret = elem
                break
        return ret

    @classmethod
    def get_initial_block_from_neuron(cls, neuron):
        """
        Returns a list of all initial blocks defined in this body.
        :return: a list of initial-blocks.
        :rtype: list(ASTBlockWithVariables)
        """
        ret = None
        for elem in neuron.get_body().get_body_elements():
            if isinstance(elem, ASTBlockWithVariables) and elem.is_initial_values:
                ret = elem
                break
        return ret

    @classmethod
    def get_parameter_block_from_neuron(cls, neuron):
        """
        Returns a list of all parameter blocks defined in this body.
        :return: a list of parameters-blocks.
        :rtype: list(ASTBlockWithVariables)
        """
        ret = None
        for elem in neuron.get_body().get_body_elements():
            if isinstance(elem, ASTBlockWithVariables) and elem.is_parameters:
                ret = elem
                break
        return ret

    @classmethod
    def get_internals_block_from_neuron(cls, neuron):
        """
        Returns a list of all internals blocks defined in this body.
        :return: a list of internals-blocks.
        :rtype: list(ASTBlockWithVariables)
        """
        ret = None
        for elem in neuron.get_body().get_body_elements():
            if isinstance(elem, ASTBlockWithVariables) and elem.is_internals:
                ret = elem
                break
        return ret

    @classmethod
    def get_equations_block_from_neuron(cls, neuron):
        """
        Returns a list of all equations BLOCKS defined in this body.
        :return: a list of equations-blocks.
        :rtype: list(ASTEquationsBlock)
        """
        from pynestml.meta_model.ast_equations_block import ASTEquationsBlock
        ret = None
        for elem in neuron.get_body().get_body_elements():
            if isinstance(elem, ASTEquationsBlock):
                ret = elem
                break
        return ret
