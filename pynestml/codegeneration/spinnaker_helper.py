#
# spinnaker_helper.py
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

from pynestml.meta_model.ast_neuron import ASTNeuron
from pynestml.symbols.predefined_types import PredefinedTypes
from pynestml.symbols.unit_type_symbol import UnitTypeSymbol


class SpiNNakerHelper(object):

    @classmethod
    def get_defined_elements(cls, neuron):
        ret = list()
        assert isinstance(neuron, ASTNeuron)
        ret.extend(neuron.get_state_symbols())
        ret.extend(neuron.get_initial_values_symbols())
        ret.extend(neuron.get_parameter_symbols())
        ret.extend(neuron.get_internal_symbols())
        return ret

    @classmethod
    def get_spinnaker_type(cls, type_symbol):
        if type_symbol.equals(PredefinedTypes.get_integer_type()):
            return 'DataType.INT32'
        elif type_symbol.equals(PredefinedTypes.get_real_type()):
            return 'DataType.S1615'
        elif type_symbol.equals(PredefinedTypes.get_boolean_type()):
            return ''
        elif isinstance(type_symbol, UnitTypeSymbol):
            return 'DataType.S1615'
        else:
            raise Exception('SpiNNaker: Void and String currently not supported!')

