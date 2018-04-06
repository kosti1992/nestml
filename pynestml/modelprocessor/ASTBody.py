#
# ASTBody.py
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


from pynestml.modelprocessor.ASTNode import ASTNode


class ASTBody(ASTNode):
    """
    This class is used to store the body of a neuron, an object containing all the definitions.
    ASTBody The body of the neuron, e.g. internal, state, parameter...
    Grammar:
        body : BLOCK_OPEN
               (NEWLINE | blockWithVariables | updateBlock | equationsBlock | inputBlock | outputBlock | function)*
               BLOCK_CLOSE;        
    """
    bodyElements = None

    def __init__(self, body_elements, source_position):
        """
        Standard constructor.
        :param body_elements: a list of elements, e.g. variable blocks.
        :type body_elements: list()
        :param source_position: the position of the element in the source model
        :rtype source_location: ASTSourceLocation
        """
        super(ASTBody, self).__init__(source_position)
        self.bodyElements = body_elements

    def get_body_elements(self):
        """
        Returns the list of body elements.
        :return: a list of body elements.
        :rtype: list()
        """
        return self.bodyElements

    def get_functions(self):
        """
        Returns a list of all function block declarations in this body.
        :return: a list of function declarations.
        :rtype: list(ASTFunction)
        """
        ret = list()
        from pynestml.modelprocessor.ASTFunction import ASTFunction
        for elem in self.get_body_elements():
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
        for elem in self.get_body_elements():
            if isinstance(elem, ASTUpdateBlock):
                ret.append(elem)
        return ret

    def get_state_blocks(self):
        """
        Returns a list of all state blocks defined in this body.
        :return: a list of state-blocks.
        :rtype: list(ASTBlockWithVariables)
        """
        ret = list()
        from pynestml.modelprocessor.ASTBlockWithVariables import ASTBlockWithVariables
        for elem in self.get_body_elements():
            if isinstance(elem, ASTBlockWithVariables) and elem.isState():
                ret.append(elem)
        return ret

    def get_parameter_blocks(self):
        """
        Returns a list of all parameter blocks defined in this body.
        :return: a list of parameters-blocks.
        :rtype: list(ASTBlockWithVariables)
        """
        ret = list()
        from pynestml.modelprocessor.ASTBlockWithVariables import ASTBlockWithVariables
        for elem in self.get_body_elements():
            if isinstance(elem, ASTBlockWithVariables) and elem.isParameters():
                ret.append(elem)
        return ret

    def get_internals_blocks(self):
        """
        Returns a list of all internals blocks defined in this body.
        :return: a list of internals-blocks.
        :rtype: list(ASTBlockWithVariables)
        """
        ret = list()
        from pynestml.modelprocessor.ASTBlockWithVariables import ASTBlockWithVariables
        for elem in self.get_body_elements():
            if isinstance(elem, ASTBlockWithVariables) and elem.isInternals():
                ret.append(elem)
        return ret

    def get_equations_blocks(self):
        """
        Returns a list of all equations blocks defined in this body.
        :return: a list of equations-blocks.
        :rtype: list(ASTEquationsBlock)
        """
        ret = list()
        from pynestml.modelprocessor.ASTEquationsBlock import ASTEquationsBlock
        for elem in self.get_body_elements():
            if isinstance(elem, ASTEquationsBlock):
                ret.append(elem)
        return ret

    def get_input_blocks(self):
        """
        Returns a list of all input-blocks defined.
        :return: a list of defined input-blocks.
        :rtype: list(ASTInputBlock)
        """
        ret = list()
        from pynestml.modelprocessor.ASTInputBlock import ASTInputBlock
        for elem in self.get_body_elements():
            if isinstance(elem, ASTInputBlock):
                ret.append(elem)
        return ret

    def get_output_blocks(self):
        """
        Returns a list of all output-blocks defined.
        :return: a list of defined output-blocks.
        :rtype: list(ASTOutputBlock)
        """
        ret = list()
        from pynestml.modelprocessor.ASTOutputBlock import ASTOutputBlock
        for elem in self.get_body_elements():
            if isinstance(elem, ASTOutputBlock):
                ret.append(elem)
        return ret

    def get_parent(self, ast=None):
        """
        Indicates whether a this node contains the handed over node.
        :param ast: an arbitrary ast node.
        :type ast: AST_
        :return: AST if this or one of the child nodes contains the handed over element.
        :rtype: AST_ or None
        """
        for stmt in self.get_body_elements():
            if stmt is ast:
                return self
            if stmt.get_parent(ast) is not None:
                return stmt.get_parent(ast)
        return None

    def get_spike_buffers(self):
        """
        Returns a list of all spike input buffers defined in the model.
        :return: a list of all spike input buffers
        :rtype: list(ASTInputLine)
        """
        ret = list()
        blocks = self.get_input_blocks()
        if isinstance(blocks, list):
            for block in blocks:
                for line in block.getInputLines():
                    if line.is_spike():
                        ret.append(line)
            return ret
        else:
            return ret

    def __str__(self):
        """
        Returns a string representation of the body.
        :return: a string representing the body.
        :rtype: str
        """
        ret = ''
        for elem in self.bodyElements:
            ret += str(elem)
            ret += '\n'
        return ret

    def equals(self, other):
        """
        The equals method.
        :param other: a different object.
        :type other: object
        :return: True if equal, otherwise False.
        :rtype: bool
        """
        if not isinstance(other, ASTBody):
            return False
        if len(self.get_body_elements()) != len(other.get_body_elements()):
            return False
        my_body_elements = self.get_body_elements()
        your_body_elements = other.get_body_elements()
        for i in range(0, len(my_body_elements)):
            if not my_body_elements[i].equals(your_body_elements[i]):
                return False
        return True
