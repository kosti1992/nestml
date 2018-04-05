#
# ASTNeuron.py
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

from pynestml.modelprocessor.ASTBody import ASTBody
from pynestml.modelprocessor.ASTNode import ASTNode
from pynestml.modelprocessor.VariableSymbol import BlockType
from pynestml.modelprocessor.VariableSymbol import VariableSymbol
from pynestml.utils.ASTUtils import ASTUtils
from pynestml.utils.Logger import LoggingLevel, Logger
from pynestml.utils.Messages import Messages


class ASTNeuron(ASTNode):
    """
    This class is used to store instances of neurons.
    ASTNeuron represents neuron.
    @attribute Name    The name of the neuron
    @attribute Body    The body of the neuron, e.g. internal, state, parameter...
    Grammar:
        neuron : 'neuron' NAME body;
    """
    __name = None
    __body = None
    __artifactName = None

    def __init__(self, name=None, body=None, source_position=None, artifact_name=None):
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
        assert (name is not None and isinstance(name, str)), \
            '(PyNestML.AST.Neuron) No  or wrong type of neuron name provided (%s)!' % type(name)
        assert (body is not None and isinstance(body, ASTBody)), \
            '(PyNestML.AST.Neuron) No or wrong type of neuron body provided (%s)!' % type(body)
        assert (artifact_name is not None and isinstance(artifact_name, str)), \
            '(PyNestML.AST.Neuron) No or wrong type of artifact name provided (%s)!' % type(artifact_name)
        super(ASTNeuron, self).__init__(source_position)
        self.__name = name
        self.__body = body
        self.__artifactName = artifact_name

    def get_name(self):
        """
        Returns the name of the neuron.
        :return: the name of the neuron.
        :rtype: str
        """
        return self.__name

    def get_body(self):
        """
        Return the body of the neuron.
        :return: the body containing the definitions.
        :rtype: ASTBody
        """
        return self.__body

    def get_artifact_name(self):
        """
        Returns the name of the artifact this neuron has been stored in.
        :return: the name of the file
        :rtype: str
        """
        return self.__artifactName

    def get_functions(self):
        """
        Returns a list of all function block declarations in this body.
        :return: a list of function declarations.
        :rtype: list(ASTFunction)
        """
        ret = list()
        from pynestml.modelprocessor.ASTFunction import ASTFunction
        for elem in self.get_body().get_body_elements():
            if isinstance(elem, ASTFunction):
                ret.append(elem)
        return ret

    def get_update_blocks(self):
        """
        Returns a list of all update blocks defined in this body.
        :return: a list of update-block elements.
        :rtype: list(ASTUpdateBlock)
        """
        ret = list()
        from pynestml.modelprocessor.ASTUpdateBlock import ASTUpdateBlock
        for elem in self.get_body().get_body_elements():
            if isinstance(elem, ASTUpdateBlock):
                ret.append(elem)
        if isinstance(ret, list) and len(ret) == 1:
            return ret[0]
        elif isinstance(ret, list) and len(ret) == 0:
            return None
        else:
            return ret

    def get_state_blocks(self):
        """
        Returns a list of all state blocks defined in this body.
        :return: a list of state-blocks.
        :rtype: list(ASTBlockWithVariables)
        """
        ret = list()
        from pynestml.modelprocessor.ASTBlockWithVariables import ASTBlockWithVariables
        for elem in self.get_body().get_body_elements():
            if isinstance(elem, ASTBlockWithVariables) and elem.isState():
                ret.append(elem)
        if isinstance(ret, list) and len(ret) == 1:
            return ret[0]
        elif isinstance(ret, list) and len(ret) == 0:
            return None
        else:
            return ret

    def get_initial_blocks(self):
        """
        Returns a list of all initial blocks defined in this body.
        :return: a list of initial-blocks.
        :rtype: list(ASTBlockWithVariables)
        """
        ret = list()
        from pynestml.modelprocessor.ASTBlockWithVariables import ASTBlockWithVariables
        for elem in self.get_body().get_body_elements():
            if isinstance(elem, ASTBlockWithVariables) and elem.isInitialValues():
                ret.append(elem)
        if isinstance(ret, list) and len(ret) == 1:
            return ret[0]
        elif isinstance(ret, list) and len(ret) == 0:
            return None
        else:
            return ret

    def get_parameter_blocks(self):
        """
        Returns a list of all parameter blocks defined in this body.
        :return: a list of parameters-blocks.
        :rtype: list(ASTBlockWithVariables)
        """
        ret = list()
        from pynestml.modelprocessor.ASTBlockWithVariables import ASTBlockWithVariables
        for elem in self.get_body().get_body_elements():
            if isinstance(elem, ASTBlockWithVariables) and elem.isParameters():
                ret.append(elem)
        if isinstance(ret, list) and len(ret) == 1:
            return ret[0]
        elif isinstance(ret, list) and len(ret) == 0:
            return None
        else:
            return ret

    def get_internals_blocks(self):
        """
        Returns a list of all internals blocks defined in this body.
        :return: a list of internals-blocks.
        :rtype: list(ASTBlockWithVariables)
        """
        ret = list()
        from pynestml.modelprocessor.ASTBlockWithVariables import ASTBlockWithVariables
        for elem in self.get_body().get_body_elements():
            if isinstance(elem, ASTBlockWithVariables) and elem.isInternals():
                ret.append(elem)
        if isinstance(ret, list) and len(ret) == 1:
            return ret[0]
        elif isinstance(ret, list) and len(ret) == 0:
            return None
        else:
            return ret

    def get_equations_blocks(self):
        """
        Returns a list of all equations BLOCKS defined in this body.
        :return: a list of equations-blocks.
        :rtype: list(ASTEquationsBlock)
        """
        ret = list()
        from pynestml.modelprocessor.ASTEquationsBlock import ASTEquationsBlock
        for elem in self.get_body().get_body_elements():
            if isinstance(elem, ASTEquationsBlock):
                ret.append(elem)
        if isinstance(ret, list) and len(ret) == 1:
            return ret[0]
        elif isinstance(ret, list) and len(ret) == 0:
            return None
        else:
            return ret

    def get_initial_values_declarations(self):
        """
        Returns a list of initial values declarations made in this neuron.
        :return: a list of initial values declarations
        :rtype: list(ASTDeclaration)
        """
        initial_values_block = self.get_initial_blocks()
        initial_values_declarations = list()
        if initial_values_block is not None:
            for decl in initial_values_block.getDeclarations():
                initial_values_declarations.append(decl)
        return initial_values_declarations

    def get_equations(self):
        """
        Returns all ode equations as defined in this neuron.
        :return list of ode-equations
        :rtype list(ASTOdeEquation)
        """
        from pynestml.modelprocessor.ASTEquationsBlock import ASTEquationsBlock
        ret = list()
        blocks = self.get_equations_blocks()
        # the get equations block is not deterministic method, it can return a list or a single object.
        if isinstance(blocks, list):
            for block in blocks:
                ret.extend(block.getOdeEquations())
        elif isinstance(blocks, ASTEquationsBlock):
            return blocks.getOdeEquations()
        else:
            return ret

    def get_input_blocks(self):
        """
        Returns a list of all input-blocks defined.
        :return: a list of defined input-blocks.
        :rtype: list(ASTInputBlock)
        """
        ret = list()
        from pynestml.modelprocessor.ASTInputBlock import ASTInputBlock
        for elem in self.get_body().get_body_elements():
            if isinstance(elem, ASTInputBlock):
                ret.append(elem)
        if isinstance(ret, list) and len(ret) == 1:
            return ret[0]
        elif isinstance(ret, list) and len(ret) == 0:
            return None
        else:
            return ret

    def get_input_buffers(self):
        """
        Returns a list of all defined input buffers.
        :return: a list of all input buffers.
        :rtype: list(VariableSymbol)
        """
        from pynestml.modelprocessor.VariableSymbol import BlockType
        symbols = self.get_scope().get_symbols_in_this_scope()
        ret = list()
        for symbol in symbols:
            if isinstance(symbol, VariableSymbol) and (symbol.get_block_type() == BlockType.INPUT_BUFFER_SPIKE or
                                                       symbol.get_block_type() == BlockType.INPUT_BUFFER_CURRENT):
                ret.append(symbol)
        return ret

    def get_spike_buffers(self):
        """
        Returns a list of all spike input buffers defined in the model.
        :return: a list of all spike input buffers.
        :rtype: list(VariableSymbol)
        """
        ret = list()
        for BUFFER in self.get_input_buffers():
            if BUFFER.is_spike_buffer():
                ret.append(BUFFER)
        return ret

    def get_current_buffers(self):
        """
        Returns a list of all current buffers defined in the model.
        :return: a list of all current input buffers.
        :rtype: list(VariableSymbol)
        """
        ret = list()
        for BUFFER in self.get_input_buffers():
            if BUFFER.is_current_buffer():
                ret.append(BUFFER)
        return ret

    def get_parameter_symbols(self):
        """
        Returns a list of all parameter symbol defined in the model.
        :return: a list of parameter symbols.
        :rtype: list(VariableSymbol)
        """
        symbols = self.get_scope().get_symbols_in_this_scope()
        ret = list()
        for symbol in symbols:
            if isinstance(symbol, VariableSymbol) and symbol.get_block_type() == BlockType.PARAMETERS and \
                    not symbol.is_predefined():
                ret.append(symbol)
        return ret

    def get_initial_values_symbols(self):
        """
        Returns a list of all parameter symbol defined in the model.
        :return: a list of parameter symbols.
        :rtype: list(VariableSymbol)
        """
        symbols = self.get_scope().get_symbols_in_this_scope()
        ret = list()
        for symbol in symbols:
            if isinstance(symbol, VariableSymbol) and symbol.get_block_type() == BlockType.INITIAL_VALUES and \
                    not symbol.is_predefined():
                ret.append(symbol)
        return ret

    def get_state_symbols(self):
        """
        Returns a list of all state symbol defined in the model.
        :return: a list of state symbols.
        :rtype: list(VariableSymbol)
        """
        symbols = self.get_scope().get_symbols_in_this_scope()
        ret = list()
        for symbol in symbols:
            if isinstance(symbol, VariableSymbol) and symbol.get_block_type() == BlockType.STATE and \
                    not symbol.is_predefined():
                ret.append(symbol)
        return ret

    def get_internal_symbols(self):
        """
        Returns a list of all internals symbol defined in the model.
        :return: a list of internals symbols.
        :rtype: list(VariableSymbol)
        """
        from pynestml.modelprocessor.VariableSymbol import BlockType
        symbols = self.get_scope().get_symbols_in_this_scope()
        ret = list()
        for symbol in symbols:
            if isinstance(symbol, VariableSymbol) and symbol.get_block_type() == BlockType.INTERNALS and \
                    not symbol.is_predefined():
                ret.append(symbol)
        return ret

    def get_ode_aliases(self):
        """
        Returns a list of all equation function symbols defined in the model.
        :return: a list of equation function  symbols.
        :rtype: list(VariableSymbol)
        """
        from pynestml.modelprocessor.VariableSymbol import BlockType
        symbols = self.get_scope().get_symbols_in_this_scope()
        ret = list()
        for symbol in symbols:
            if isinstance(symbol,
                          VariableSymbol) and symbol.get_block_type() == BlockType.EQUATION and symbol.is_function():
                ret.append(symbol)
        return ret

    def variables_defined_by_ode(self):
        """
        Returns a list of all variables which are defined by an ode.
        :return: a list of variable symbols
        :rtype: list(VariableSymbol)
        """
        symbols = self.get_scope().get_symbols_in_complete_scope()
        ret = list()
        for symbol in symbols:
            if isinstance(symbol, VariableSymbol) and symbol.is_ode_defined():
                ret.append(symbol)
        return ret

    def get_output_blocks(self):
        """
        Returns a list of all output-blocks defined.
        :return: a list of defined output-blocks.
        :rtype: list(ASTOutputBlock)
        """
        ret = list()
        from pynestml.modelprocessor.ASTOutputBlock import ASTOutputBlock
        for elem in self.get_body().get_body_elements():
            if isinstance(elem, ASTOutputBlock):
                ret.append(elem)
        if isinstance(ret, list) and len(ret) == 1:
            return ret[0]
        elif isinstance(ret, list) and len(ret) == 0:
            return None
        else:
            return ret

    def is_multisynapse_spikes(self):
        """
        Returns whether this neuron uses multi-synapse spikes.
        :return: True if multi-synaptic, otherwise False.
        :rtype: bool
        """
        buffers = self.get_spike_buffers()
        for iBuffer in buffers:
            if iBuffer.has_vector_parameter():
                return True
        return False

    def get_multiple_receptors(self):
        """
        Returns a list of all spike buffers which are defined as inhibitory and excitatory.
        :return: a list of spike buffers variable symbols
        :rtype: list(VariableSymbol)
        """
        ret = list()
        for iBuffer in self.get_spike_buffers():
            if iBuffer.is_excitatory() and iBuffer.is_inhibitory():
                if iBuffer is not None:
                    ret.append(iBuffer)
                else:
                    code, message = Messages.getCouldNotResolve(iBuffer.getSymbolName())
                    Logger.log_message(
                        message=message,
                        code=code,
                        error_position=iBuffer.getSourcePosition(),
                        log_level=LoggingLevel.ERROR)
        return ret

    def getParameterNonAliasSymbols(self):
        """
        Returns a list of all variable symbols representing non-function parameter variables.
        :return: a list of variable symbols
        :rtype: list(VariableSymbol)
        """
        ret = list()
        for param in self.get_parameter_symbols():
            if not param.is_function() and not param.is_predefined():
                ret.append(param)
        return ret

    def getStateNonAliasSymbols(self):
        """
        Returns a list of all variable symbols representing non-function state variables.
        :return: a list of variable symbols
        :rtype: list(VariableSymbol)
        """
        ret = list()
        for param in self.get_state_symbols():
            if not param.is_function() and not param.is_predefined():
                ret.append(param)
        return ret

    def getInternalNonAliasSymbols(self):
        """
        Returns a list of all variable symbols representing non-function internal variables.
        :return: a list of variable symbols
        :rtype: list(VariableSymbol)
        """
        ret = list()
        for param in self.get_internal_symbols():
            if not param.is_function() and not param.is_predefined():
                ret.append(param)

        return ret

    def getInitialValuesSymbols(self):
        """
        Returns a list of all initial values symbol defined in the model.
        :return: a list of initial values symbols.
        :rtype: list(VariableSymbol)
        """
        from pynestml.modelprocessor.VariableSymbol import BlockType
        symbols = self.get_scope().get_symbols_in_this_scope()
        ret = list()
        for symbol in symbols:
            if isinstance(symbol, VariableSymbol) and symbol.get_block_type() == BlockType.INITIAL_VALUES and \
                    not symbol.is_predefined():
                ret.append(symbol)
        return ret

    def getFunctionInitialValuesSymbols(self):
        """
        Returns a list of all initial values symbols as defined in the model which are marked as functions.
        :return: a list of symbols
        :rtype: list(VariableSymbol)
        """
        ret = list()
        for symbol in self.getInitialValuesSymbols():
            if symbol.is_function():
                ret.append(symbol)
        return ret

    def getNonFunctionInitialValuesSymbols(self):
        """
        Returns a list of all initial values symbols as defined in the model which are not marked as functions.
        :return: a list of symbols
        :rtype:list(VariableSymbol)
        """
        ret = list()
        for symbol in self.getInitialValuesSymbols():
            if not symbol.is_function():
                ret.append(symbol)
        return ret

    def getOdeDefinedSymbols(self):
        """
        Returns a list of all variable symbols which have been defined in th initial_values blocks
        and are provided with an ode.
        :return: a list of initial value variables with odes
        :rtype: list(VariableSymbol)
        """
        from pynestml.modelprocessor.VariableSymbol import BlockType
        symbols = self.get_scope().get_symbols_in_this_scope()
        ret = list()
        for symbol in symbols:
            if isinstance(symbol, VariableSymbol) and \
                    symbol.get_block_type() == BlockType.INITIAL_VALUES and symbol.is_ode_defined() \
                    and not symbol.is_predefined() and not symbol.is_predefined():
                ret.append(symbol)
        return ret

    def getStateSymbolsWithoutOde(self):
        """
        Returns a list of all elements which have been defined in the state block.
        :return: a list of of state variable symbols.
        :rtype: list(VariableSymbol)
        """
        from pynestml.modelprocessor.VariableSymbol import BlockType
        symbols = self.get_scope().get_symbols_in_this_scope()
        ret = list()
        for symbol in symbols:
            if isinstance(symbol, VariableSymbol) and \
                    symbol.get_block_type() == BlockType.STATE and not symbol.is_ode_defined() \
                    and not symbol.is_predefined() and not symbol.is_predefined():
                ret.append(symbol)
        return ret

    def isArrayBuffer(self):
        """
        This method indicates whether this neuron uses buffers defined vector-wise.
        :return: True if vector buffers defined, otherwise False.
        :rtype: bool
        """
        buffers = self.get_input_buffers()
        for BUFFER in buffers:
            if BUFFER.has_vector_parameter():
                return True
        return False

    def getParameterInvariants(self):
        """
        Returns a list of all invariants of all parameters.
        :return: a list of rhs representing invariants
        :rtype: list(ASTExpression)
        """
        from pynestml.modelprocessor.ASTBlockWithVariables import ASTBlockWithVariables
        ret = list()
        blocks = self.get_parameter_blocks()
        # the get parameters block is not deterministic method, it can return a list or a single object.
        if isinstance(blocks, list):
            for block in blocks:
                for decl in block.getDeclarations():
                    if decl.has_invariant():
                        ret.append(decl.get_invariant())
        elif isinstance(blocks, ASTBlockWithVariables):
            for decl in blocks.getDeclarations():
                if decl.has_invariant():
                    ret.append(decl.get_invariant())
        return ret

    def addToStateBlock(self, _declaration=None):
        """
        Adds the handed over declaration the state block
        :param _declaration: a single declaration
        :type _declaration: ASTDeclaration
        """
        if self.get_state_blocks() is None:
            ASTUtils.create_state_block(self)
        self.get_state_blocks().getDeclarations().append(_declaration)
        return

    def addToInternalBlock(self, _declaration=None):
        """
        Adds the handed over declaration the internal block
        :param _declaration: a single declaration
        :type _declaration: ASTDeclaration
        """
        if self.get_internals_blocks() is None:
            ASTUtils.create_internal_block(self)
        self.get_internals_blocks().getDeclarations().append(_declaration)
        return

    def addToInitialValuesBlock(self, _declaration=None):
        """
        Adds the handed over declaration to the initial values block.
        :param _declaration: a single declaration.
        :type _declaration: ASTDeclaration
        """
        if self.get_initial_blocks() is None:
            ASTUtils.create_initial_values_block(self)
        self.get_initial_blocks().getDeclarations().append(_declaration)
        return

    """
    The following print methods are used by the backend and represent the comments as stored at the corresponding 
    parts of the neuron definition.
    """

    def printDynamicsComment(self, _prefix=None):
        """
        Prints the dynamic block comment.
        :param _prefix: a prefix string
        :type _prefix: str
        :return: the corresponding comment.
        :rtype: str
        """
        block = self.get_update_blocks()
        if block is None:
            return _prefix if _prefix is not None else ''
        return block.print_comment(_prefix)

    def printParameterComment(self, _prefix=None):
        """
        Prints the update block comment.
        :param _prefix: a prefix string
        :type _prefix: str
        :return: the corresponding comment.
        :rtype: str
        """
        block = self.get_parameter_blocks()
        if block is None:
            return _prefix if _prefix is not None else ''
        return block.print_comment(_prefix)

    def printStateComment(self, _prefix=None):
        """
        Prints the state block comment.
        :param _prefix: a prefix string
        :type _prefix: str
        :return: the corresponding comment.
        :rtype: str
        """
        block = self.get_state_blocks()
        if block is None:
            return _prefix if _prefix is not None else ''
        return block.print_comment(_prefix)

    def printInternalComment(self, _prefix=None):
        """
        Prints the internal block comment.
        :param _prefix: a prefix string
        :type _prefix: str
        :return: the corresponding comment.
        :rtype: str
        """
        block = self.get_internals_blocks()
        if block is None:
            return _prefix if _prefix is not None else ''
        return block.print_comment(_prefix)

    def print_comment(self, prefix=None):
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

    """
    Mandatory methods as contained in the super class have to be extended to implement the correct behavior.
    """

    def get_parent(self, ast=None):
        """
        Indicates whether a this node contains the handed over node.
        :param ast: an arbitrary ast node.
        :type ast: AST_
        :return: AST if this or one of the child nodes contains the handed over element.
        :rtype: AST_ or None
        """
        if self.get_body() is ast:
            return self
        elif self.get_body().get_parent(ast) is not None:
            return self.get_body().get_parent(ast)
        return None

    def __str__(self):
        """
        Returns a string representation of the neuron.
        :return: a string representation.
        :rtype: str
        """
        return 'neuron ' + self.get_name() + ':\n' + str(self.get_body()) + '\nend'

    def equals(self, other=None):
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
