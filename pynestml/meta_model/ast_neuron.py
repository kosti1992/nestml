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


class ASTNeuron(ASTNode):
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
