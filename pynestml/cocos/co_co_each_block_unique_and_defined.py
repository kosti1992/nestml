#
# CoCoEachBlockUnique.py
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
from pynestml.cocos.co_co import CoCo
from pynestml.meta_model.ast_neuron import ASTNeuron
from pynestml.utils.ast_helper import ASTHelper
from pynestml.utils.logger import Logger, LoggingLevel
from pynestml.utils.messages import Messages
from pynestml.visitors.ast_visitor import ASTVisitor


class CoCoEachBlockUniqueAndDefined(CoCo):
    """
    This context  condition ensures that each block is defined at most once.
    Not allowed:
        state:
            ...
        end
        ...
        state:
            ...
        end
    """

    def check_co_co(self, node):
        """
        Checks whether each block is define at most once.
        :param node: a single neuron.
        :type node: ASTNeuron
        """
        assert (node is not None and isinstance(node, ASTNeuron)), \
            '(PyNestML.CoCo.BlocksUniques) No or wrong type of neuron provided (%s)!' % type(node)
        visitor = BlockCounterChecker()
        node.accept(visitor)
        if visitor.report['state'] > 1:
            code, message = Messages.get_block_not_defined_correctly('State', False)
            Logger.log_message(code=code, message=message, neuron=node, error_position=node.get_source_position(),
                               log_level=LoggingLevel.ERROR)
        # check that update block is defined exactly once
        if visitor.report['update'] > 1:
            code, message = Messages.get_block_not_defined_correctly('Update', False)
            Logger.log_message(code=code, message=message, neuron=node, error_position=node.get_source_position(),
                               log_level=LoggingLevel.ERROR)
        if visitor.report['update'] == 0:
            code, message = Messages.get_block_not_defined_correctly('Update', True)
            Logger.log_message(code=code, message=message, neuron=node, error_position=node.get_source_position(),
                               log_level=LoggingLevel.ERROR)
        # check that parameters block is defined at most once
        if visitor.report['parameters'] > 1:
            code, message = Messages.get_block_not_defined_correctly('Parameters', False)
            Logger.log_message(code=code, message=message, neuron=node, error_position=node.get_source_position(),
                               log_level=LoggingLevel.ERROR)
        # check that internals block is defined at most once
        if visitor.report['internals'] > 1:
            code, message = Messages.get_block_not_defined_correctly('Internals', False)
            Logger.log_message(code=code, message=message, neuron=node, error_position=node.get_source_position(),
                               log_level=LoggingLevel.ERROR)
        # check that equations block is defined at most once
        if visitor.report['equations'] > 1:
            code, message = Messages.get_block_not_defined_correctly('Equations', False)
            Logger.log_message(code=code, message=message, neuron=node, error_position=node.get_source_position(),
                               log_level=LoggingLevel.ERROR)
        # check that input block is defined exactly once
        if visitor.report['input'] > 1:
            code, message = Messages.get_block_not_defined_correctly('Input', False)
            Logger.log_message(code=code, message=message, neuron=node, error_position=node.get_source_position(),
                               log_level=LoggingLevel.ERROR)
        if visitor.report['input'] == 0:
            code, message = Messages.get_block_not_defined_correctly('Input', True)
            Logger.log_message(code=code, message=message, neuron=node, error_position=node.get_source_position(),
                               log_level=LoggingLevel.ERROR)
        # check that output block is defined exactly once
        if visitor.report['output'] > 1:
            code, message = Messages.get_block_not_defined_correctly('Output', False)
            Logger.log_message(code=code, message=message, neuron=node, error_position=node.get_source_position(),
                               log_level=LoggingLevel.ERROR)
        if visitor.report['output'] == 0:
            code, message = Messages.get_block_not_defined_correctly('Output', True)
            Logger.log_message(code=code, message=message, neuron=node, error_position=node.get_source_position(),
                               log_level=LoggingLevel.ERROR)
        # check the initial values block
        if visitor.report['init_values'] > 1:
            code, message = Messages.get_block_not_defined_correctly('Initial Values', False)
            Logger.log_message(code=code, message=message, neuron=node, error_position=node.get_source_position(),
                               log_level=LoggingLevel.ERROR)
        # check the constraints block
        if visitor.report['constraints'] > 1:
            code, message = Messages.get_block_not_defined_correctly('Constraints', False)
            Logger.log_message(code=code, message=message, neuron=node, error_position=node.get_source_position(),
                               log_level=LoggingLevel.ERROR)

        return


class BlockCounterChecker(ASTVisitor):

    def __init__(self):
        super(BlockCounterChecker, self).__init__()
        self.report = {
            'state': 0, 'update': 0, 'input': 0, 'output': 0, 'constraints': 0,
            'parameters': 0, 'internals': 0, 'equations': 0, 'init_values': 0
        }

    def visit_block_with_variables(self, node):
        if node.is_initial_values:
            self.report['init_values'] += 1
        if node.is_internals:
            self.report['internals'] += 1
        if node.is_parameters:
            self.report['parameters'] += 1
        if node.is_state:
            self.report['state'] += 1

    def visit_equations_block(self, node):
        self.report['equations'] += 1

    def visit_update_block(self, node):
        self.report['update'] += 1

    def visit_input_block(self, node):
        self.report['input'] += 1

    def visit_output_block(self, node):
        self.report['output'] += 1

    def visit_constraints_block(self, node):
        self.report['constraints'] += 1
