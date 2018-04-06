#
# CoCoParametersAssignedOnlyInParameterBlock.py
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
from pynestml.modelprocessor.ASTNeuron import ASTNeuron
from pynestml.modelprocessor.ASTVisitor import ASTVisitor
from pynestml.modelprocessor.CoCo import CoCo
from pynestml.modelprocessor.Scope import ScopeType
from pynestml.modelprocessor.Symbol import SymbolKind
from pynestml.modelprocessor.VariableSymbol import BlockType
from pynestml.utils.Logger import LoggingLevel, Logger
from pynestml.utils.Messages import Messages


class CoCoParametersAssignedOnlyInParameterBlock(CoCo):
    """
    This coco checks that no parameters are assigned outside the parameters block.
    Allowed:
        parameters:
            par mV = 10mV
        end
    Not allowed:
        parameters:
            par mV = 10mV
        end
        ...
        update:
           par = 20mV
        end    
    """

    @classmethod
    def check_co_co(cls, node=None):
        """
        Ensures the coco for the handed over neuron.
        :param node: a single neuron instance.
        :type node: ASTNeuron
        """
        assert (node is not None and isinstance(node, ASTNeuron)), \
            '(PyNestML.CoCo.BufferNotAssigned) No or wrong type of neuron provided (%s)!' % type(node)
        node.accept(ParametersAssignmentVisitor())
        return


class ParametersAssignmentVisitor(ASTVisitor):
    """
    This visitor checks that no parameters have been assigned outside the parameters block.
    """

    def visit_assignment(self, node):
        """
        Checks the coco on the current node.
        :param node: a single node.
        :type node: ASTAssignment
        """
        symbol = node.get_scope().resolve_to_symbol(node.get_variable().get_name(), SymbolKind.VARIABLE)
        if (symbol is not None and symbol.get_block_type() == BlockType.PARAMETERS and
                node.get_scope().get_scope_type() != ScopeType.GLOBAL):
            code, message = Messages.getAssignmentNotAllowed(node.get_variable().get_complete_name())
            Logger.log_message(error_position=node.get_source_position(),
                               code=code, message=message,
                               log_level=LoggingLevel.ERROR)
        return
