#
# CoCoVectorVariableInNonVectorDeclaration.py
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
from pynestml.modelprocessor.CoCo import CoCo
from pynestml.modelprocessor.ASTNeuron import ASTNeuron
from pynestml.modelprocessor.ASTVisitor import ASTVisitor
from pynestml.modelprocessor.Symbol import SymbolKind
from pynestml.utils.Logger import Logger, LoggingLevel
from pynestml.utils.Messages import Messages


class CoCoVectorVariableInNonVectorDeclaration(CoCo):
    """
    This coco ensures that vector variables are not used in non vector declarations.
    Not allowed:
        function three integer[n] = 3
        threePlusFour integer = three + 4 <- error: threePlusFour is not a vector
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
        node.accept(VectorInDeclarationVisitor())
        return


class VectorInDeclarationVisitor(ASTVisitor):
    """
    This visitor checks if somewhere in a declaration of a non-vector value, a vector is used.
    """

    def visit_declaration(self, node):
        """
        Checks the coco.
        :param node: a single declaration.
        :type node: ASTDeclaration
        """
        if node.has_expression():
            variables = node.get_expression().get_variables()
            for variable in variables:
                if variable is not None:
                    symbol = node.get_scope().resolve_to_symbol(variable.get_complete_name(), SymbolKind.VARIABLE)
                    if symbol is not None and symbol.has_vector_parameter() and not node.has_size_parameter():
                        code, message = Messages.getVectorInNonVector(_vector=symbol.get_symbol_name(),
                                                                      _nonVector=list(var.get_complete_name() for
                                                                                      var in
                                                                                      node.get_variables()))

                        Logger.log_message(error_position=node.get_source_position(),
                                           code=code, message=message,
                                           log_level=LoggingLevel.ERROR)
        return
