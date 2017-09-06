"""
/*
 *  ASTNESTMLCompilationUnit.py
 *
 *  This file is part of NEST.
 *
 *  Copyright (C) 2004 The NEST Initiative
 *
 *  NEST is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  NEST is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with NEST.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
@author kperun
"""
from pynestml.src.main.python.org.nestml.ast.ASTNeuron import ASTNeuron
from pynestml.src.main.python.org.nestml.ast.ASTElement import ASTElement


class ASTNESTMLCompilationUnit(ASTElement):
    """
    The ASTNESTMLCompilationUnit class as used to store a collection of processed ASTNeurons.
    """
    # a list of all processed neurons
    __neuron_list = None

    def __init__(self, _sourcePosition=None):
        """
        Standard constructor of ASTNESTMLCompilationUnit.
        :param _sourcePosition: the position of this element in the source file.
        :type _sourcePosition: ASTSourcePosition.
        """
        super(ASTNESTMLCompilationUnit, self).__init__(_sourcePosition)
        self.__neuron_list = list()

    @classmethod
    def makeASTNESTMLCompilationUnit(cls, _listOfNeurons=list(), _sourcePosition=None):
        """
        A factory method used to generate new ASTNESTMLCompilationUnits.
        :param _listOfNeurons: a list of ASTNeurons
        :type _listOfNeurons: list
        :param _sourcePosition: the position of this element in the source file.
        :type _sourcePosition: ASTSourcePosition.
        :return: a new object of type ASTNESTMLCompilationUnits.
        :rtype: ASTNESTMLCompilationUnits
        """
        assert (_listOfNeurons is not None), '(PyNestML.AST.NESTMLCompilationUnit) Handed over element not a list!'
        instance = cls(_sourcePosition)
        for i in _listOfNeurons:
            # ensure that only object of type Neuron are added
            if isinstance(i, ASTNeuron):
                instance.addNeuron(i)
            else:
                raise NotANeuronException('(PyNestML.AST.NESTMLCompilationUnit) Not a neuron handed over!')
        return instance

    def addNeuron(self, _neuron):
        """
        Expects an instance of neuron element which is added to the collection.
        :param _neuron: an instance of a neuron 
        :type _neuron: ASTNeuron
        :return: no returned value
        :rtype: void
        """
        assert (_neuron is not None), '(NESTML) "None" handed over as neuron to add.'
        self.__neuron_list.append(_neuron)

    def deleteNeuron(self, _neuron=None):
        """
        Expects an instance of neuron element which is deleted from the collection.
        :param _neuron: an instance of a ASTNeuron
        :type _neuron:ASTNeuron
        :return: True if element deleted from list, False else.
        :rtype: bool
        """
        if self.__neuron_list.__contains__(_neuron):
            self.__neuron_list.remove(_neuron)
            return True
        else:
            return False

    def getNeuronList(self):
        """
        :return: a list of neuron elements as stored in the unit
        :rtype: list(ASTNeuron)
        """
        return self.__neuron_list

    def printAST(self):
        """
        Returns a string representation of the compilation unit.
        :return: a string representation.
        :rtype: str
        """
        ret = ''
        if self.getNeuronList() is not None:
            for neuron in self.getNeuronList():
                ret += neuron.printAST() + '\n'
        return ret


class NotANeuronException(Exception):
    pass