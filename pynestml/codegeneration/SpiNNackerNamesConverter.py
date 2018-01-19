#
# SpiNNackerNamesConverter.py
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

class SpiNNackerNamesConverter(object):
    """
    TODO
    """
    @classmethod
    def convertToCPPName(cls, _variableName=None):
        """
        Converts a handed over name to the corresponding nest / c++ naming guideline.
        In concrete terms:
            Converts names of the form g_in'' to a compilable C++ identifier: __DDX_g_in
        :param _variableName: a single name.
        :type _variableName: str
        :return: the corresponding transformed name.
        :rtype: str
        """
        differentialOrder = _variableName.count('\'')
        if differentialOrder > 0:
            return '__' + 'D' * differentialOrder + '_' + _variableName.replace('\'', '')
        else:
            return _variableName
