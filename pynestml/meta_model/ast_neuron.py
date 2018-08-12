#
# ast_neuron.py
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

from pynestml.meta_model.ast_body import ASTBody
from pynestml.meta_model.ast_node import ASTNode
from pynestml.meta_model.ast_ode_shape import ASTOdeShape
from pynestml.symbols.variable_symbol import VariableSymbol
from pynestml.utils.ast_utils import ASTUtils
from pynestml.utils.logger import Logger, LoggingLevel
from pynestml.utils.messages import Messages


class ASTNeuron(ASTNode):
    # TODO: This class is too large and god-like, refactor it,KP
    """
    This class is used to store instances of neurons.
    ASTNeuron represents neuron.
    @attribute Name    The name of the neuron
    @attribute Body    The body of the neuron, e.g. internal, state, parameter...
    Grammar:
        neuron : 'neuron' NAME body;
    Attributes:
        name = None
        body = None
        artifact_name = None
    """

    def __init__(self, name, body, source_position = None, artifact_name = None):
        """
        Standard constructor.
        :param name: the name of the neuron.
        :type name: str
        :param body: the body containing the definitions.
        :type body: ASTBody
        :param source_position: the position of this element in the source file.
        :type source_position: ASTSourceLocation.
        :param artifact_name: the name of the file this neuron is contained in
        :type artifact_name: str
        """
        assert isinstance(name, str), \
            '(PyNestML.AST.Neuron) No  or wrong type of neuron name provided (%s)!' % type(name)
        assert isinstance(body, ASTBody), \
            '(PyNestML.AST.Neuron) No or wrong type of neuron body provided (%s)!' % type(body)
        assert (artifact_name is not None and isinstance(artifact_name, str)), \
            '(PyNestML.AST.Neuron) No or wrong type of artifact name provided (%s)!' % type(artifact_name)
        super(ASTNeuron, self).__init__(source_position)
        self.name = name
        self.body = body
        self.artifact_name = artifact_name

    def get_name(self):
        """
        Returns the name of the neuron.
        :return: the name of the neuron.
        :rtype: str
        """
        return self.name

    def get_body(self):
        """
        Return the body of the neuron.
        :return: the body containing the definitions.
        :rtype: ASTBody
        """
        return self.body

    def get_artifact_name(self):
        """
        Returns the name of the artifact this neuron has been stored in.
        :return: the name of the file
        :rtype: str
        """
        return self.artifact_name

    def get_multiple_receptors(self):
        """
        Returns a list of all spike buffers which are defined as inhibitory and excitatory.
        :return: a list of spike buffers variable symbols
        :rtype: list(VariableSymbol)
        """
        from pynestml.utils.ast_helper import ASTHelper
        ret = list()
        for iBuffer in ASTHelper.get_spike_buffers_from_neuron(self):
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

    def get_constraint_block(self):
        """
        Returns the constraint block of the model, if any defined.
        :return: a single constraint block
        :rtype: ASTConstraintBlock
        """
        from pynestml.meta_model.ast_constraints_block import ASTConstraintsBlock
        for block in self.get_body().get_body_elements():
            if isinstance(block, ASTConstraintsBlock):
                return block

    def get_parameter_non_alias_symbols(self):
        """
        Returns a list of all variable symbols representing non-function parameter variables.
        :return: a list of variable symbols
        :rtype: list(VariableSymbol)
        """
        from pynestml.utils.ast_helper import ASTHelper
        ret = list()
        for param in ASTHelper.get_parameter_symbols_from_neuron(self):
            if not param.is_function and not param.is_predefined:
                ret.append(param)
        return ret

    def get_state_non_alias_symbols(self):
        """
        Returns a list of all variable symbols representing non-function state variables.
        :return: a list of variable symbols
        :rtype: list(VariableSymbol)
        """
        from pynestml.utils.ast_helper import ASTHelper
        ret = list()
        for param in ASTHelper.get_state_symbols_from_neuron(self):
            if not param.is_function and not param.is_predefined:
                ret.append(param)
        return ret

    def get_initial_values_non_alias_symbols(self):
        ret = list()
        for init in self.get_initial_values_symbols():
            if not init.is_function and not init.is_predefined:
                ret.append(init)
        return ret

    def get_internal_non_alias_symbols(self):
        """
        Returns a list of all variable symbols representing non-function internal variables.
        :return: a list of variable symbols
        :rtype: list(VariableSymbol)
        """
        from pynestml.utils.ast_helper import ASTHelper
        ret = list()
        for param in ASTHelper.get_internal_symbols_from_neuron(self):
            if not param.is_function and not param.is_predefined:
                ret.append(param)

        return ret

    def get_initial_values_symbols(self):
        """
        Returns a list of all initial values symbol defined in the model.
        :return: a list of initial values symbols.
        :rtype: list(VariableSymbol)
        """
        from pynestml.symbols.variable_symbol import BlockType
        symbols = self.get_scope().get_symbols_in_this_scope()
        ret = list()
        for symbol in symbols:
            if isinstance(symbol, VariableSymbol) and symbol.block_type == BlockType.INITIAL_VALUES and \
                    not symbol.is_predefined:
                ret.append(symbol)
        return ret

    def get_initial_values_blocks(self):
        """
        Returns a list of all initial blocks defined in this body.
        :return: a list of initial-blocks.
        :rtype: list(ASTBlockWithVariables)
        """
        ret = list()
        from pynestml.meta_model.ast_block_with_variables import ASTBlockWithVariables
        for elem in self.get_body().get_body_elements():
            if isinstance(elem, ASTBlockWithVariables) and elem.is_initial_values:
                ret.append(elem)
        if isinstance(ret, list) and len(ret) == 1:
            return ret[0]
        elif isinstance(ret, list) and len(ret) == 0:
            return None
        else:
            return ret

    def remove_initial_blocks(self):
        """
        Remove all equations blocks
        """
        from pynestml.meta_model.ast_block_with_variables import ASTBlockWithVariables
        for elem in self.get_body().get_body_elements():
            if isinstance(elem, ASTBlockWithVariables) and elem.is_initial_values:
                self.get_body().get_body_elements().remove(elem)

    def get_function_initial_values_symbols(self):
        """
        Returns a list of all initial values symbols as defined in the model which are marked as functions.
        :return: a list of symbols
        :rtype: list(VariableSymbol)
        """
        ret = list()
        for symbol in self.get_initial_values_symbols():
            if symbol.is_function:
                ret.append(symbol)
        return ret

    def get_non_function_initial_values_symbols(self):
        """
        Returns a list of all initial values symbols as defined in the model which are not marked as functions.
        :return: a list of symbols
        :rtype:list(VariableSymbol)
        """
        ret = list()
        for symbol in self.get_initial_values_symbols():
            if not symbol.is_function:
                ret.append(symbol)
        return ret

    def get_ode_defined_symbols(self):
        """
        Returns a list of all variable symbols which have been defined in th initial_values blocks
        and are provided with an ode.
        :return: a list of initial value variables with odes
        :rtype: list(VariableSymbol)
        """
        from pynestml.symbols.variable_symbol import BlockType
        symbols = self.get_scope().get_symbols_in_this_scope()
        ret = list()
        for symbol in symbols:
            if isinstance(symbol, VariableSymbol) and \
                    symbol.block_type == BlockType.INITIAL_VALUES and symbol.is_ode_defined() \
                    and not symbol.is_predefined:
                ret.append(symbol)
        return ret

    def get_state_symbols_without_ode(self):
        """
        Returns a list of all elements which have been defined in the state block.
        :return: a list of of state variable symbols.
        :rtype: list(VariableSymbol)
        """
        from pynestml.symbols.variable_symbol import BlockType
        symbols = self.get_scope().get_symbols_in_this_scope()
        ret = list()
        for symbol in symbols:
            if isinstance(symbol, VariableSymbol) and \
                    symbol.block_type == BlockType.STATE and not symbol.is_ode_defined() \
                    and not symbol.is_predefined:
                ret.append(symbol)
        return ret

    def is_array_buffer(self):
        """
        This method indicates whether this neuron uses buffers defined vector-wise.
        :return: True if vector buffers defined, otherwise False.
        :rtype: bool
        """
        from pynestml.utils.ast_helper import ASTHelper
        for BUFFER in ASTHelper.get_input_buffers_from_neuron(self):
            if BUFFER.has_vector_parameter():
                return True
        return False

    def get_parameter_invariants(self):
        """
        Returns a list of all invariants of all parameters.
        :return: a list of rhs representing invariants
        :rtype: list(ASTExpression)
        """
        from pynestml.meta_model.ast_block_with_variables import ASTBlockWithVariables
        from pynestml.utils.ast_helper import ASTHelper
        ret = list()
        blocks = ASTHelper.get_parameter_block_from_neuron(self)
        # the get parameters block is not deterministic method, it can return a list or a single object.
        # TODO:refactor this,  we now assure that it is only a single object
        if isinstance(blocks, list):
            for block in blocks:
                for decl in block.get_declarations():
                    if decl.has_invariant():
                        ret.append(decl.get_invariant())
        elif isinstance(blocks, ASTBlockWithVariables):
            for decl in blocks.get_declarations():
                if decl.has_invariant():
                    ret.append(decl.get_invariant())
        return ret

    def add_to_internal_block(self, declaration):
        # todo by KP: factor me out to utils
        """
        Adds the handed over declaration the internal block
        :param declaration: a single declaration
        :type declaration: ASTDeclaration
        """
        from pynestml.utils.ast_helper import ASTHelper
        if ASTHelper.get_internals_block_from_neuron(self) is None:
            ASTUtils.create_internal_block(self)
            ASTHelper.get_internals_block_from_neuron(self).get_declarations().append(declaration)
        return

    def add_to_initial_values_block(self, declaration):
        # todo by KP: factor me out to utils
        """
        Adds the handed over declaration to the initial values block.
        :param declaration: a single declaration.
        :type declaration: ASTDeclaration
        """
        from pynestml.utils.ast_helper import ASTHelper
        if ASTHelper.get_initial_block_from_neuron(self) is None:
            ASTUtils.create_initial_values_block(self)
        ASTHelper.get_initial_block_from_neuron(self).get_declarations().append(declaration)
        return

    def add_shape(self, shape):
        # type: (ASTOdeShape) -> None
        """
        Adds the handed over declaration to the initial values block.
        :param shape: a single declaration.
        """
        from pynestml.utils.ast_helper import ASTHelper
        assert ASTHelper.get_equations_block_from_neuron(self) is not None
        ASTHelper.get_equations_block_from_neuron(self).get_declarations().append(shape)

    """
    The following print methods are used by the backend and represent the comments as stored at the corresponding 
    parts of the neuron definition.
    """

    def print_dynamics_comment(self, prefix = None):
        """
        Prints the dynamic block comment.
        :param prefix: a prefix string
        :type prefix: str
        :return: the corresponding comment.
        :rtype: str
        """
        from pynestml.utils.ast_helper import ASTHelper
        block = ASTHelper.get_update_block_from_neuron(self)
        if block is None:
            return prefix if prefix is not None else ''
        return block.print_comment(prefix)

    def print_parameter_comment(self, prefix = None):
        """
        Prints the update block comment.
        :param prefix: a prefix string
        :type prefix: str
        :return: the corresponding comment.
        :rtype: str
        """
        from pynestml.utils.ast_helper import ASTHelper
        block = ASTHelper.get_parameter_block_from_neuron(self)
        if block is None:
            return prefix if prefix is not None else ''
        return block.print_comment(prefix)

    def print_state_comment(self, prefix = None):
        """
        Prints the state block comment.
        :param prefix: a prefix string
        :type prefix: str
        :return: the corresponding comment.
        :rtype: str
        """
        from pynestml.utils.ast_helper import ASTHelper
        block = ASTHelper.get_state_block_from_neuron(self)
        if block is None:
            return prefix if prefix is not None else ''
        return block.print_comment(prefix)

    def print_internal_comment(self, prefix = None):
        """
        Prints the internal block comment.
        :param prefix: a prefix string
        :type prefix: str
        :return: the corresponding comment.
        :rtype: str
        """
        from pynestml.utils.ast_helper import ASTHelper
        block = ASTHelper.get_internals_block_from_neuron(self)
        if block is None:
            return prefix if prefix is not None else ''
        return block.print_comment(prefix)

    def print_comment(self, prefix = None):
        """
        Prints the header information of this neuron.
        :param prefix: a prefix string
        :type prefix: str
        :return: the comment.
        :rtype: str
        """
        ret = ''
        if self.get_comment() is None or len(self.get_comment()) == 0:
            return prefix if prefix is not None else ''
        for comment in self.get_comment():
            ret += (prefix if prefix is not None else '') + comment + '\n'
        return ret

    def equals(self, other):
        """
        The equals method.
        :param other: a different object.
        :type other: object
        :return: True if equal, otherwise False.
        :rtype: bool
        """
        if not isinstance(other, ASTNeuron):
            return False
        return self.get_name() == other.get_name() and self.get_body().equals(other.get_body())
