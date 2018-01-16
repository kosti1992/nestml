#
# BackendTargets.py
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


class BackendTargets(enumerate):
    """
    Currently Supported targets for code generation.
    """
    NEST = 0
    SpiNNacker = 1

    @classmethod
    def parseTarget(cls, _targetAsString=None):
        """
        Returns an enum object for a given string representation.
        :param _targetAsString: a string
        :return: BackendTargets
        """
        assert (_targetAsString is not None and isinstance(_targetAsString, str)), \
            '(PyNestML.FrontEnd.BackendTargets) No or wrong type of argument to parse handed over (%s)' % type(
                _targetAsString)
        if _targetAsString == 'NEST':
            return BackendTargets.NEST
        elif _targetAsString == 'SpiNNacker':
            return BackendTargets.SpiNNacker
        else:
            return None
