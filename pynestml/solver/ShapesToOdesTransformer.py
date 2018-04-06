#
# ShapesToOdesTransformer.py
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
from pynestml.modelprocessor.ASTEquationsBlock import ASTEquationsBlock
from pynestml.modelprocessor.ModelParser import ModelParser
from pynestml.solver.SolverOutput import SolverOutput
from pynestml.solver.TransformerBase import TransformerBase


class ShapesToOdesTransformer(object):
    """
    This transformer replaces shapes by the corresponding set of odes.
    """

    @classmethod
    def transformShapesToOdeForm(cls, _neuron=None, _solverOutput=None):
        """
        Takes SymPy result with the implicit form of ODEs (e.g replace shapes through a series of ODES) and replaces
        the shapes by those.
        :param _neuron: a single instance
        :type _neuron: ASTNeuron
        :param _solverOutput: the solver output
        :type _solverOutput: SolverOutput
        :return: a modifier neuron
        :rtype: ASTNeuron
        """
        assert (_solverOutput is not None and isinstance(_solverOutput, SolverOutput)), \
            '(PyNestML.Solver.DeltaSolution) No or wrong type of output provided (%s)!' % _solverOutput
        assert (_neuron is not None and isinstance(_neuron, ASTNeuron)), \
            '(PyNestML.Solver.DeltaSolution) No or wrong type of neuron provided (%s)!' % _neuron
        assert (_neuron.get_equations_blocks() is not None), \
            '(PyNestML.Solver.DeltaSolution) Equations block must not be empty!'
        state_shape_variables_with_initial_values = TransformerBase.computeShapeStateVariablesWithInitialValues(
            _solverOutput)
        working_version = TransformerBase.addVariablesToInitialValues(_neuron, state_shape_variables_with_initial_values)
        # TODO by KP: actually, only shapes that are solved must be reseted, @KP solve this by checking which shapes are now with vars
        cls.__removeShapes(working_version)
        cls.__addStateShapeEquationsToEquationsBlock(_solverOutput.shape_state_odes,
                                                     working_version.get_equations_blocks())
        TransformerBase.applyIncomingSpikes(working_version)
        return working_version

    @classmethod
    def __addStateShapeEquationsToEquationsBlock(cls, _equationsFile=None, _astOdeDeclaration=None):
        """

        :param _equationsFile: a list of tuples
        :type _equationsFile: list(tuple)
        :param _astOdeDeclaration: a single equations block
        :type _astOdeDeclaration: ASTEquationsBlock
        """
        assert (_equationsFile is not None and isinstance(_equationsFile, list)), \
            '(PyNestML.Solver.DeltaSolution) No or wrong type of equations file provided (%s)!' % type(_equationsFile)
        assert (_astOdeDeclaration is not None and isinstance(_astOdeDeclaration, ASTEquationsBlock)), \
            '(PyNestML.Solver.DeltaSolution) No or wrong type of declarations block provided (%s)!' % type(
                _astOdeDeclaration)
        ast_shapes = list()
        for singleDict in _equationsFile:
            for key in singleDict.keys():
                ast_shapes.append(ModelParser.parse_ode_shape('shape ' + key + '\' = ' + singleDict[key]))
        _astOdeDeclaration.getDeclarations().extend(ast_shapes)
        return

    @classmethod
    def __removeShapes(cls, _neuron=None):
        """
        Removes all shapes from a given neuron.
        :param _neuron: a neuron instance
        :type _neuron: ASTNeuron
        :return: a modified version of the neuron
        :rtype: ASTNeuron
        """
        assert (_neuron is not None and isinstance(_neuron, ASTNeuron)), \
            '(PyNestML.Solver.DeltaSolution) No or wrong type of neuron provided (%s)!' % type(_neuron)
        if _neuron.get_equations_blocks() is not None:
            for shape in _neuron.get_equations_blocks().getOdeShapes():
                _neuron.get_equations_blocks().getDeclarations().remove(shape)
        return
