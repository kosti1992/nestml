#
# co_co_constraint_block_correctly_built.py
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


class CoCoConstraintBlockCorrectlyBuilt(CoCo):
    """
    This coco checks whether the constraint block has been correctly constructed. I.e., that boundaries are not
    empty (e.g. 1 < V < 0 shall be detected) and types are equal.

    """

    @classmethod
    def check_co_co(cls, node):
        # type: (ASTNeuron) -> None
        if node.get_constraint_block() is None:
            # no constraints, thus nothing to do
            return
        for const in node.get_constraint_block().constraints:
            # first check whether the types are suitable
            if const.left_bound is not None:
                cls.__bound_typing_check(const.left_bound, const.variable)
            else:
                pass
            if const.right_bound is not None:
                cls.__bound_typing_check(const.right_bound, const.variable)
            # now check if the bounds are sat, i.e. left_bound <= right_bound
            # TODO


    @classmethod
    def __bound_typing_check(cls, bound, var):
        # the most simple case: both are equal
        if bound.type.equals(var.get_type_symbol()):
            #print('ok' + str(bound) + ':' + str(var))
            return
        else:
            #print('not ok:'+ str(bound) + ':' + str(var))
            pass
        #todo

    @classmethod
    def __bound_sat_check(cls, constraint):
        pass