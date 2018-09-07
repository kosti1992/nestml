#
# co_cos_manager.py
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
from pynestml.cocos.co_co_all_variables_defined import CoCoAllVariablesDefined
from pynestml.cocos.co_co_buffer_not_assigned import CoCoBufferNotAssigned
from pynestml.cocos.co_co_constraint_block_correctly_built import CoCoConstraintBlockCorrectlyBuilt
from pynestml.cocos.co_co_convolve_cond_correctly_built import CoCoConvolveCondCorrectlyBuilt
from pynestml.cocos.co_co_correct_numerator_of_unit import CoCoCorrectNumeratorOfUnit
from pynestml.cocos.co_co_correct_order_in_equation import CoCoCorrectOrderInEquation
from pynestml.cocos.co_co_current_buffers_not_specified import CoCoCurrentBuffersNotSpecified
from pynestml.cocos.co_co_each_block_unique_and_defined import CoCoEachBlockUniqueAndDefined
from pynestml.cocos.co_co_equations_only_for_init_values import CoCoEquationsOnlyForInitValues
from pynestml.cocos.co_co_function_calls_consistent import CoCoFunctionCallsConsistent
from pynestml.cocos.co_co_function_have_rhs import CoCoFunctionHaveRhs
from pynestml.cocos.co_co_function_max_one_lhs import CoCoFunctionMaxOneLhs
from pynestml.cocos.co_co_function_unique import CoCoFunctionUnique
from pynestml.cocos.co_co_illegal_expression import CoCoIllegalExpression
from pynestml.cocos.co_co_init_vars_with_odes_provided import CoCoInitVarsWithOdesProvided
from pynestml.cocos.co_co_invariant_is_boolean import CoCoInvariantIsBoolean
from pynestml.cocos.co_co_neuron_name_unique import CoCoNeuronNameUnique
from pynestml.cocos.co_co_no_nest_name_space_collision import CoCoNoNestNameSpaceCollision
from pynestml.cocos.co_co_no_shapes_except_in_convolve import CoCoNoShapesExceptInConvolve
from pynestml.cocos.co_co_no_two_neurons_in_set_of_compilation_units import CoCoNoTwoNeuronsInSetOfCompilationUnits
from pynestml.cocos.co_co_only_spike_buffer_data_types import CoCoOnlySpikeBufferDataTypes
from pynestml.cocos.co_co_parameters_assigned_only_in_parameter_block import \
    CoCoParametersAssignedOnlyInParameterBlock
from pynestml.cocos.co_co_sum_has_correct_parameter import CoCoSumHasCorrectParameter
from pynestml.cocos.co_co_type_of_buffer_unique import CoCoTypeOfBufferUnique
from pynestml.cocos.co_co_user_defined_function_correctly_defined import CoCoUserDefinedFunctionCorrectlyDefined
from pynestml.cocos.co_co_variable_once_per_scope import CoCoVariableOncePerScope
from pynestml.cocos.co_co_vector_variable_in_non_vector_declaration import CoCoVectorVariableInNonVectorDeclaration


class CoCosManager(object):
    """
    This class provides a set of context conditions which have to hold for each neuron instance.
    """

    @classmethod
    def check_function_defined(cls, neuron):
        """
        Checks for the handed over neuron that each used function it is defined.
        """
        CoCoFunctionUnique().check_co_co(neuron)

    @classmethod
    def check_each_block_unique_and_defined(cls, neuron):
        """
        Checks if in the handed over neuron each block ist defined at most once and mandatory blocks are defined.
        :param neuron: a single neuron instance
        :type neuron: ASTNeuron
        """
        CoCoEachBlockUniqueAndDefined().check_co_co(neuron)

    @classmethod
    def check_function_declared_and_correctly_typed(cls, neuron):
        """
        Checks if in the handed over neuron all function calls use existing functions and the arguments are
        correctly typed.
        :param neuron: a single neuron instance
        :type neuron: ASTNeuron
        """
        CoCoFunctionCallsConsistent().check_co_co(neuron)

    @classmethod
    def check_variables_unique_in_scope(cls, neuron):
        """
        Checks that all variables have been declared at most once per scope.
        :param neuron: a single neuron instance
        :type neuron: ASTNeuron
        """
        CoCoVariableOncePerScope().check_co_co(neuron)

    @classmethod
    def check_variables_defined_before_usage(cls, neuron):
        """
        Checks that all variables are defined before being used.
        :param neuron: a single neuron.
        :type neuron: ASTNeuron
        """
        CoCoAllVariablesDefined().check_co_co(neuron)

    @classmethod
    def check_functions_have_rhs(cls, neuron):
        """
        Checks that all functions have a right-hand side, e.g., function V_reset mV = V_m - 55mV 
        :param neuron: a single neuron object
        :type neuron: ASTNeuron
        """
        CoCoFunctionHaveRhs().check_co_co(neuron)

    @classmethod
    def check_function_has_max_one_lhs(cls, neuron):
        """
        Checks that all functions have exactly one left-hand side, e.g., function V_reset mV = V_m - 55mV 
        :param neuron: a single neuron object.
        :type neuron: ASTNeuron
        """
        CoCoFunctionMaxOneLhs().check_co_co(neuron)

    @classmethod
    def check_no_values_assigned_to_buffers(cls, neuron):
        """
        Checks that no values are assigned to buffers.
        :param neuron: a single neuron object.
        :type neuron: ASTNeuron
        """
        CoCoBufferNotAssigned().check_co_co(neuron)

    @classmethod
    def check_order_of_equations_correct(cls, neuron):
        """
        Checks that all equations specify the order of the variable.
        :param neuron: a single neuron object.
        :type neuron: ASTNeuron
        """
        CoCoCorrectOrderInEquation().check_co_co(neuron)

    @classmethod
    def check_numerator_of_unit_is_one_if_numeric(cls, neuron):
        """
        Checks that all units which have a numeric numerator use 1.
        :param neuron: a single neuron object.
        :type neuron: ASTNeuron
        """
        CoCoCorrectNumeratorOfUnit().check_co_co(neuron)

    @classmethod
    def check_neuron_names_unique(cls, compilation_unit):
        """
        Checks that all declared neurons in a compilation unit have a unique name.
        :param compilation_unit: a single compilation unit.
        :type compilation_unit: ASTCompilationUnit
        """
        CoCoNeuronNameUnique().check_co_co(compilation_unit)

    @classmethod
    def check_no_nest_namespace_collisions(cls, neuron):
        """
        Checks that all units which have a numeric numerator use 1.
        :param neuron: a single neuron object.
        :type neuron: ASTNeuron
        """
        CoCoNoNestNameSpaceCollision().check_co_co(neuron)

    @classmethod
    def check_type_of_buffer_unique(cls, neuron):
        """
        Checks that all spike buffers have a unique type, i.e., no buffer is defined with redundant keywords.
        :param neuron: a single neuron object.
        :type neuron: ASTNeuron
        """
        CoCoTypeOfBufferUnique().check_co_co(neuron)

    @classmethod
    def check_parameters_not_assigned_outside_parameters_block(cls, neuron):
        """
        Checks that parameters are not assigned outside the parameters block.
        :param neuron: a single neuron object.
        :type neuron: ASTNeuron
        """
        CoCoParametersAssignedOnlyInParameterBlock().check_co_co(neuron)

    @classmethod
    def check_current_buffers_no_keywords(cls, neuron):
        """
        Checks that input current buffers have not been specified with keywords, e.g., inhibitory.
        :param neuron: a single neuron object.
        :type neuron: ASTNeuron
        """
        CoCoCurrentBuffersNotSpecified().check_co_co(neuron)

    @classmethod
    def check_buffer_types_are_correct(cls, neuron):
        """
        Checks that input buffers have specified the data type if required an no data type if not allowed.
        :param neuron: a single neuron object.
        :type neuron: ASTNeuron
        """
        CoCoOnlySpikeBufferDataTypes().check_co_co(neuron)

    @classmethod
    def check_init_vars_with_odes_provided(cls, neuron):
        """
        Checks that all initial variables have a rhs and are provided with the corresponding ode declaration.
        :param neuron: a single neuron object.
        :type neuron: ASTNeuron
        """
        CoCoInitVarsWithOdesProvided().check_co_co(neuron)

    @classmethod
    def check_user_defined_function_correctly_built(cls, neuron):
        """
        Checks that all user defined functions are correctly constructed, i.e., have a return statement if declared
        and that the type corresponds to the declared one.
        :param neuron: a single neuron object.
        :type neuron: ASTNeuron
        """
        CoCoUserDefinedFunctionCorrectlyDefined().check_co_co(neuron)

    @classmethod
    def check_initial_ode_initial_values(cls, neuron):
        """
        Checks if variables of odes are declared in the initial_values block.
        :param neuron: a single neuron object.
        :type neuron: ASTNeuron
        """
        CoCoEquationsOnlyForInitValues().check_co_co(neuron)

    @classmethod
    def check_convolve_cond_curr_is_correct(cls, neuron):
        """
        Checks if all convolve/curr_sum/cond_sum rhs are correctly provided with arguments.
        :param neuron: a single neuron object.
        :type neuron: ASTNeuron
        """
        CoCoConvolveCondCorrectlyBuilt().check_co_co(neuron)

    @classmethod
    def check_correct_usage_of_shapes(cls, neuron):
        """
        Checks if all shapes are only used in cond_sum, cur_sum, convolve.
        :param neuron: a single neuron object.
        :type neuron: ASTNeuron
        """
        CoCoNoShapesExceptInConvolve().check_co_co(neuron)

    @classmethod
    def check_not_two_neurons_across_units(cls, compilation_units):
        """
        Checks if in a set of compilation units, two neurons have the same name.
        :param compilation_units: a  list of compilation units
        :type compilation_units: list(ASTNestMLCompilationUnit)
        """
        CoCoNoTwoNeuronsInSetOfCompilationUnits().check_co_co(compilation_units)

    @classmethod
    def check_invariant_type_correct(cls, neuron):
        """
        Checks if all invariants are of type boolean.
        :param neuron: a single neuron object.
        :type neuron: ASTNeuron
        """
        CoCoInvariantIsBoolean().check_co_co(neuron)

    @classmethod
    def check_vector_in_non_vector_declaration_detected(cls, neuron):
        """
        Checks if no declaration a vector value is added to a non vector one.
        :param neuron: a single neuron object.
        :type neuron: ASTNeuron
        """
        CoCoVectorVariableInNonVectorDeclaration().check_co_co(neuron)

    @classmethod
    def check_sum_has_correct_parameter(cls, neuron):
        """
        Checks that all cond_sum,cur_sum and convolve have variables as arguments.
        :param neuron: a single neuron object.
        :type neuron: ASTNeuron
        """
        CoCoSumHasCorrectParameter().check_co_co(neuron)

    @classmethod
    def check_expression_correct(cls, neuron):
        """
        Checks that all rhs in the model are correctly constructed, e.g. type(lhs)==type(rhs).
        :param neuron: a single neuron
        :type neuron: ASTNeuron
        """
        CoCoIllegalExpression().check_co_co(neuron)

    @classmethod
    def check_constraint_block_correctly_built(cls, neuron):
        """
        Checks that the constraint block is correctly constructed.
        :param neuron: a single neuron ast.
        :return: ASTNeuron
        """
        CoCoConstraintBlockCorrectlyBuilt().check_co_co(neuron)

    @classmethod
    def post_symbol_table_builder_checks(cls, neuron):
        """
        Checks the following constraints:
            cls.check_function_defined(_neuron)
            cls.check_function_declared_and_correctly_typed(_neuron)
            cls.check_variables_unique_in_scope(_neuron)
            cls.check_variables_defined_before_usage(_neuron)
            cls.check_functions_have_rhs(_neuron)
            cls.check_function_has_max_one_lhs(_neuron)
            cls.check_no_values_assigned_to_buffers(_neuron)
            cls.check_order_of_equations_correct(_neuron)
            cls.check_numerator_of_unit_is_one_if_numeric(_neuron)
            cls.check_no_nest_namespace_collisions(_neuron)
            cls.check_type_of_buffer_unique(_neuron)
            cls.check_parameters_not_assigned_outside_parameters_block(_neuron)
            cls.check_current_buffers_no_keywords(_neuron)
            cls.check_buffer_types_are_correct(_neuron)
            cls.checkUsedDefinedFunctionCorrectlyBuilt(_neuron)
            cls.check_initial_ode_initial_values(_neuron)
            cls.check_invariant_type_correct(_neuron)
            cls.check_vector_in_non_vector_declaration_detected(_neuron)
            cls.check_sum_has_correct_parameter(_neuron)
            cls.check_expression_correct(_neuron)
            cls.check_constraint_block_correctly_built(_neuron)
        :param neuron: a single neuron object.
        :type neuron: ASTNeuron
        """
        cls.check_function_defined(neuron)
        cls.check_function_declared_and_correctly_typed(neuron)
        cls.check_variables_unique_in_scope(neuron)
        cls.check_variables_defined_before_usage(neuron)
        cls.check_functions_have_rhs(neuron)
        cls.check_function_has_max_one_lhs(neuron)
        cls.check_no_values_assigned_to_buffers(neuron)
        cls.check_order_of_equations_correct(neuron)
        cls.check_numerator_of_unit_is_one_if_numeric(neuron)
        cls.check_no_nest_namespace_collisions(neuron)
        cls.check_type_of_buffer_unique(neuron)
        cls.check_parameters_not_assigned_outside_parameters_block(neuron)
        cls.check_current_buffers_no_keywords(neuron)
        cls.check_buffer_types_are_correct(neuron)
        cls.check_user_defined_function_correctly_built(neuron)
        cls.check_initial_ode_initial_values(neuron)
        cls.check_convolve_cond_curr_is_correct(neuron)
        cls.check_correct_usage_of_shapes(neuron)
        cls.check_invariant_type_correct(neuron)
        cls.check_vector_in_non_vector_declaration_detected(neuron)
        cls.check_sum_has_correct_parameter(neuron)
        cls.check_expression_correct(neuron)
        cls.check_constraint_block_correctly_built(neuron)
        return

    @classmethod
    def post_ode_specification_checks(cls, neuron):
        """
        Checks the following constraints:
            cls.check_init_vars_with_odes_provided
        :param neuron: a single neuron object.
        :type neuron: ASTNeuron
        """
        cls.check_init_vars_with_odes_provided(neuron)

    @classmethod
    def get_per_neuron_cocos(cls):
        """
        This method returns all cocos which are only valid for a single neuron, i.e., those which check across
        neurons are not regarded.
        :return: a list of coco object
        """
        ret = list()
        ret.append(CoCoFunctionUnique())
        ret.append(CoCoEachBlockUniqueAndDefined())
        ret.append(CoCoFunctionCallsConsistent())
        ret.append(CoCoVariableOncePerScope())
        ret.append(CoCoAllVariablesDefined())
        ret.append(CoCoFunctionHaveRhs())
        ret.append(CoCoFunctionMaxOneLhs())
        ret.append(CoCoBufferNotAssigned())
        ret.append(CoCoCorrectOrderInEquation())
        ret.append(CoCoCorrectNumeratorOfUnit())
        ret.append(CoCoNeuronNameUnique())
        ret.append(CoCoNoNestNameSpaceCollision())
        ret.append(CoCoTypeOfBufferUnique())
        ret.append(CoCoParametersAssignedOnlyInParameterBlock())
        ret.append(CoCoCurrentBuffersNotSpecified())
        ret.append(CoCoOnlySpikeBufferDataTypes())
        ret.append(CoCoInitVarsWithOdesProvided())
        ret.append(CoCoUserDefinedFunctionCorrectlyDefined())
        ret.append(CoCoEquationsOnlyForInitValues())
        ret.append(CoCoConvolveCondCorrectlyBuilt())
        ret.append(CoCoNoShapesExceptInConvolve())
        ret.append(CoCoInvariantIsBoolean())
        ret.append(CoCoVectorVariableInNonVectorDeclaration())
        ret.append(CoCoSumHasCorrectParameter())
        ret.append(CoCoIllegalExpression())
        ret.append(CoCoConstraintBlockCorrectlyBuilt())
        return ret
