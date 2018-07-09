#
# spinnaker_helper.py
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

from pynestml.meta_model.ast_neuron import ASTNeuron
from pynestml.symbols.predefined_types import PredefinedTypes
from pynestml.symbols.unit_type_symbol import UnitTypeSymbol
from pynestml.visitors.ast_parent_aware_visitor import ASTParentAwareVisitor
from pynestml.symbols.predefined_functions import PredefinedFunctions
from pynestml.meta_model.ast_update_block import ASTUpdateBlock
from pynestml.meta_model.ast_compound_stmt import ASTCompoundStmt
from pynestml.meta_model.ast_if_stmt import ASTIfStmt
from pynestml.utils.ast_utils import ASTUtils
from pynestml.meta_model.ast_expression import ASTExpression
from copy import deepcopy


class SpiNNakerHelper(object):

    @classmethod
    def get_defined_elements(cls, neuron):
        ret = list()
        assert isinstance(neuron, ASTNeuron)
        ret.extend(neuron.get_state_symbols())
        ret.extend(neuron.get_initial_values_symbols())
        ret.extend(neuron.get_parameter_symbols())
        ret.extend(neuron.get_internal_symbols())
        return ret

    @classmethod
    def get_spinnaker_type(cls, type_symbol):
        if type_symbol.equals(PredefinedTypes.get_integer_type()):
            return 'DataType.INT32'
        elif type_symbol.equals(PredefinedTypes.get_real_type()):
            return 'DataType.S1615'
        elif type_symbol.equals(PredefinedTypes.get_boolean_type()):
            return ''
        elif isinstance(type_symbol, UnitTypeSymbol):
            return 'DataType.S1615'
        else:
            raise Exception('SpiNNaker: Void and String currently not supported!')

    @classmethod
    def get_membrane_variable(cls, ast):
        assert isinstance(ast, ASTNeuron)
        v = None
        v_star = None
        # list of all checked vars
        var_list = list()
        var_list.extend(ast.get_initial_values_symbols())
        var_list.extend(ast.get_state_symbols())
        for var in var_list:
            if var.name == 'V_m':
                return var  # return, since V_m is always the membrane potential
            if var.name == 'V':
                v = var
                continue  # continue, since V_m can still be there
            if var.name.startswith('V'):
                v_star = var  # in worst take anything which starts with V, e.g., V_mn
        if v is not None:
            return v
        elif v_star is not None:
            return v_star
        else:
            return None

    @classmethod
    def is_update_block(cls, ast):
        return isinstance(ast, ASTUpdateBlock)

    @classmethod
    def is_compound_stmt(cls, ast):
        return isinstance(ast, ASTCompoundStmt)

    @classmethod
    def has_threshold_block(cls, ast):
        # now check if the handed over block contains an emit spike
        visitor = ContainsEmitSpikeVisitor()
        ast.accept(visitor)
        return visitor.contains_emit_spike

    @classmethod
    def get_threshold_block(cls, ast):
        visitor = ContainsEmitSpikeVisitor()
        ast.accept(visitor)
        top = visitor.trace.pop()
        while (not isinstance(top, ASTCompoundStmt)) and (not isinstance(top, ASTUpdateBlock)) and \
                not visitor.trace.is_empty():
            top = visitor.trace.pop()
        if isinstance(top, ASTCompoundStmt) or isinstance(top, ASTUpdateBlock):
            return top
        raise Exception('spinnaker:not implemented yet')

    @classmethod
    def get_emit_spike_blocks_from_compound_stmt(cls, ast):
        assert isinstance(ast, ASTCompoundStmt)
        if ast.is_if_stmt():
            node = ast.get_if_stmt()
        elif ast.is_for_stmt():
            node = ast.get_for_stmt()
        else:
            node = ast.get_while_stmt()
        return cls.__extract_block_with_emit_spike_from_stmt(node)

    @classmethod
    def __extract_block_with_emit_spike_from_stmt(cls, ast):
        from pynestml.meta_model.ast_if_stmt import ASTIfStmt
        from pynestml.meta_model.ast_while_stmt import ASTWhileStmt
        from pynestml.meta_model.ast_for_stmt import ASTForStmt
        """
        This function extracts all blocks which contain an emit_spike call.
        """
        ret = list()
        if isinstance(ast, ASTIfStmt):
            visitor = ContainsEmitSpikeVisitor()
            ast.get_if_clause().accept(visitor)
            if visitor.contains_emit_spike:
                ret.append(ast.get_if_clause().get_block())
            for el_if in ast.get_elif_clauses():
                visitor = ContainsEmitSpikeVisitor()
                el_if.accept(visitor)
                if visitor.contains_emit_spike:
                    ret.append(el_if.get_block())
            if ast.has_else_clause():
                visitor = ContainsEmitSpikeVisitor()
                ast.get_else_clause().accept(visitor)
                if visitor.contains_emit_spike:
                    ret.append(ast.get_else_clause().get_block())

        elif isinstance(ast, ASTWhileStmt):
            ret.append(ast.get_block())
        elif isinstance(ast, ASTForStmt):
            ret.append(ast.get_block())
        return ret


class ContainsEmitSpikeVisitor(ASTParentAwareVisitor):
    def __init__(self):
        super(ContainsEmitSpikeVisitor, self).__init__()
        self.contains_emit_spike = False
        self.trace = None

    def visit_function_call(self, node):
        if node.get_name() == PredefinedFunctions.EMIT_SPIKE:
            self.contains_emit_spike = True
            self.trace = deepcopy(self.parents)
