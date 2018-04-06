#
# CoCoTypeOfBufferUnique.py
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
from pynestml.utils.Logger import LoggingLevel, Logger
from pynestml.utils.Messages import Messages


class CoCoTypeOfBufferUnique(CoCo):
    """
    This coco ensures that each spike buffer has at most one type of modifier inhibitory and excitatory.
    Allowed:
        spike <- inhibitory spike
    Not allowed:
        spike <- inhibitory inhibitory spike
    """

    @classmethod
    def check_co_co(cls, node):
        """
        Ensures the coco for the handed over neuron.
        :param node: a single neuron instance.
        :type node: ASTNeuron
        """
        cls.neuronName = node.get_name()
        node.accept(TypeOfBufferUniqueVisitor())


class TypeOfBufferUniqueVisitor(ASTVisitor):
    """
    This visitor ensures that all buffers are specified uniquely by keywords.
    """

    def visit_input_line(self, node):
        """
        Checks the coco on the current node.
        :param node: a single input line.
        :type node: ASTInputLine
        """
        if node.is_spike():
            if node.has_input_types() and len(node.get_input_types()) > 1:
                inh = 0
                ext = 0
                for typ in node.get_input_types():
                    if typ.is_excitatory:
                        ext += 1
                    if typ.is_inhibitory:
                        inh += 1
                if inh > 1:
                    code, message = Messages.getMultipleKeywords('inhibitory')
                    Logger.log_message(error_position=node.get_source_position(), code=code, message=message,
                                       log_level=LoggingLevel.ERROR)
                if ext > 1:
                    code, message = Messages.getMultipleKeywords('excitatory')
                    Logger.log_message(error_position=node.get_source_position(), code=code, message=message,
                                       log_level=LoggingLevel.ERROR)
        return
