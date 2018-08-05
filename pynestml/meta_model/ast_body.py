#
# ast_body.py
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
from pynestml.meta_model.ast_node import ASTNode


class ASTBody(ASTNode):
    """
    This class is used to store the body of a neuron, an object containing all the definitions.
    ASTBody The body of the neuron, e.g. internal, state, parameter...
    Grammar:
        body : BLOCK_OPEN
               (NEWLINE | blockWithVariables | updateBlock | equationsBlock | inputBlock | outputBlock | function)*
               BLOCK_CLOSE;        
    Attributes:
        bodyElements = None
    """

    def __init__(self, body_elements, source_position):
        """
        Standard constructor.
        :param body_elements: a list of elements, e.g. variable blocks.
        :type body_elements: list()
        :param source_position: the position of the element in the source model
        :rtype source_location: ASTSourceLocation
        """
        super(ASTBody, self).__init__(source_position)
        self.body_elements = body_elements

    def get_body_elements(self):
        """
        Returns the list of body elements.
        :return: a list of body elements.
        :rtype: list()
        """
        return self.body_elements

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
