

class SpiNNackerNamesConvert(object):
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
