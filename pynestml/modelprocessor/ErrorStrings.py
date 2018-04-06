#
# ErrorStrings.py
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
from pynestml.modelprocessor.ASTSourceLocation import ASTSourceLocation


class ErrorStrings(object):
    """
    These error strings are part of the type calculation system and are kept separated from the message class
    for the sake of a clear and direct maintenance of type system as an individual component.
    """

    SEPARATOR = " : "

    @classmethod
    def code(cls, _origin=None):
        """
        Helper method returning a unique identifier for the various classes that produce and log error messages
        :param _origin: the class reporting an error
        :return: identifier unique to that class
        :rtype: str
        """
        assert _origin is not None
        from pynestml.modelprocessor.ASTUnaryVisitor import ASTUnaryVisitor
        if isinstance(_origin, ASTUnaryVisitor):
            return "SPL_UNARY_VISITOR"
        from pynestml.modelprocessor.ASTPowerVisitor import ASTPowerVisitor
        if isinstance(_origin, ASTPowerVisitor):
            return "SPL_POW_VISITOR"
        from pynestml.modelprocessor.ASTLogicalNotVisitor import ASTLogicalNotVisitor
        if isinstance(_origin, ASTLogicalNotVisitor):
            return "SPL_LOGICAL_NOT_VISITOR"
        from pynestml.modelprocessor.ASTDotOperatorVisitor import ASTDotOperatorVisitor
        if isinstance(_origin, ASTDotOperatorVisitor):
            return "SPL_DOT_OPERATOR_VISITOR"
        from pynestml.modelprocessor.ASTLineOperationVisitor import ASTLineOperatorVisitor
        if isinstance(_origin, ASTLineOperatorVisitor):
            return "SPL_LINE_OPERATOR_VISITOR"
        from pynestml.modelprocessor.ASTNoSemantics import ASTNoSemantics
        if isinstance(_origin, ASTNoSemantics):
            return "SPL_NO_SEMANTICS"
        from pynestml.modelprocessor.ASTComparisonOperatorVisitor import ASTComparisonOperatorVisitor
        if isinstance(_origin, ASTComparisonOperatorVisitor):
            return "SPL_COMPARISON_OPERATOR_VISITOR"
        from pynestml.modelprocessor.ASTBinaryLogicVisitor import ASTBinaryLogicVisitor
        if isinstance(_origin, ASTBinaryLogicVisitor):
            return "SPL_BINARY_LOGIC_VISITOR"
        from pynestml.modelprocessor.ASTConditionVisitor import ASTConditionVisitor
        if isinstance(_origin, ASTConditionVisitor):
            return "SPL_CONDITION_VISITOR"
        from pynestml.modelprocessor.ASTFunctionCallVisitor import ASTFunctionCallVisitor
        if isinstance(_origin, ASTFunctionCallVisitor):
            return "SPL_FUNCTION_CALL_VISITOR"
        return ""

    @classmethod
    def messageNonNumericType(cls, _origin=None, _typeName=None, _sourcePosition=None):
        """
        construct an error message indicating an expected numeric type is not, in fact, numeric
        :param _origin: the class reporting the error
        :param _typeName: plain text representation of the wrong type that was encountered
        :type _typeName: str
        :param _sourcePosition: The location where the error was encountered
        :type _sourcePosition: ASTSourceLocation 
        :return: the error message
        :rtype: str 
        """
        assert _origin is not None
        assert _typeName is not None and isinstance(_typeName, str)
        assert _sourcePosition is not None and isinstance(_sourcePosition, ASTSourceLocation)
        ERROR_MSG_FORMAT = "Cannot perform an arithmetic operation on a non-numeric type: " + _typeName
        return cls.code(_origin) + cls.SEPARATOR + ERROR_MSG_FORMAT + "(" + str(_sourcePosition) + ")"

    @classmethod
    def messageTypeError(cls, _origin=None, _expressionText=None, _sourcePosition=None):
        """
        construct an error message indicating a generic error in rhs type calculation
        :param _origin: the class reporting the error
        :param _expressionText: plain text representation of the offending rhs
        :type _expressionText: str
        :param _sourcePosition: The location where the error was encountered
        :type _sourcePosition: ASTSourceLocation
        :return: the error message
        :rtype: str
        """
        assert _origin is not None
        assert _expressionText is not None and isinstance(_expressionText, str)
        assert _sourcePosition is not None and isinstance(_sourcePosition, ASTSourceLocation)
        ERROR_MSG_FORMAT = "Cannot determine the type of the rhs: " + _expressionText
        return cls.code(_origin) + cls.SEPARATOR + ERROR_MSG_FORMAT + "(" + str(_sourcePosition) + ")"

    @classmethod
    def messageUnitBase(cls, _origin=None, _sourcePosition=None):
        """
        construct an error message indicating that a non-int type was given as exponent to a unit type
        :param _origin: the class reporting the error
        :param _sourcePosition: The location where the error was encountered
        :type _sourcePosition: ASTSourceLocation
        :return: the error message
        :rtype: str
        """
        assert _origin is not None
        assert _sourcePosition is not None and isinstance(_sourcePosition, ASTSourceLocation)
        ERROR_MSG_FORMAT = "With a Unit base, the exponent must be an integer."
        return cls.code(_origin) + cls.SEPARATOR + ERROR_MSG_FORMAT + "(" + str(_sourcePosition) + ")"

    @classmethod
    def messageNonConstantExponent(cls, _origin=None, _sourcePosition=None):
        """
        construct an error message indicating that the exponent given to a unit base is not a constant value
        :param _origin: the class reporting the error
        :param _sourcePosition: The location where the error was encountered
        :type _sourcePosition: ASTSourceLocation
        :return: the error message
        :rtype: str
        """
        assert _origin is not None
        assert _sourcePosition is not None and isinstance(_sourcePosition, ASTSourceLocation)
        ERROR_MSG_FORMAT = "Cannot calculate value of exponent. Must be a constant value!"
        return cls.code(_origin) + cls.SEPARATOR + ERROR_MSG_FORMAT + "(" + str(_sourcePosition) + ")"

    @classmethod
    def messageExpectedBool(cls, _origin=None, _sourcePosition=None):
        """
        construct an error message indicating that an expected bool value was not found
        :param _origin: the class reporting the error
        :param _sourcePosition: The location where the error was encountered
        :type _sourcePosition: ASTSourceLocation
        :return: the error message
        :rtype: str
        """
        assert _origin is not None
        assert _sourcePosition is not None and isinstance(_sourcePosition, ASTSourceLocation)
        ERROR_MSG_FORMAT = "Expected a bool"
        return cls.code(_origin) + cls.SEPARATOR + ERROR_MSG_FORMAT + "(" + str(_sourcePosition) + ")"

    @classmethod
    def messageExpectedInt(cls, _origin=None, _sourcePosition=None):
        """
        construct an error message indicating that an expected int value was not found
        :param _origin: the class reporting the error
        :param _sourcePosition: The location where the error was encountered
        :type _sourcePosition: ASTSourceLocation
        :return: the error message
        :rtype: str
        """
        assert _origin is not None
        assert _sourcePosition is not None and isinstance(_sourcePosition, ASTSourceLocation)
        ERROR_MSG_FORMAT = "Expected an int"
        return cls.code(_origin) + cls.SEPARATOR + ERROR_MSG_FORMAT + "(" + str(_sourcePosition) + ")"

    @classmethod
    def messageTypeMismatch(cls, _origin=None, _mismatchText=None, _sourcePosition=None):
        """
        construct an error message indicating that an expected int value was not found
        :param _origin: the class reporting the error
        :param _mismatchText: the operation with mismatched types printed in plain text
        :type _mismatchText: str
        :param _sourcePosition: The location where the error was encountered
        :type _sourcePosition: ASTSourceLocation
        :return: the error message
        :rtype: str
        """
        assert _origin is not None
        assert _mismatchText is not None and isinstance(_mismatchText, str)
        assert _sourcePosition is not None and isinstance(_sourcePosition, ASTSourceLocation)
        ERROR_MSG_FORMAT = "Operation not defined: " + _mismatchText
        return cls.code(_origin) + cls.SEPARATOR + ERROR_MSG_FORMAT + "(" + str(_sourcePosition) + ")"

    @classmethod
    def messageAddSubTypeMismatch(cls, _origin=None, _lhsTypeText=None, _rhsTypeText=None, _resultTypeText=None,
                                  _sourcePosition=None):
        """
        construct an message indicating that the types of an addition/substraction are not compatible
        and that the result is implicitly cast to a different type
        :param _origin: the class reporting the error
        :param _lhsTypeText: plain text of Lhs type
        :type _lhsTypeText: str
        :param _rhsTypeText: plain text of Rhs type
        :type _rhsTypeText: str
        :param _resultTypeText: plain text of resulting type (implicit cast)
        :type _resultTypeText: str
        :param _sourcePosition: The location where the error was encountered
        :type _sourcePosition: ASTSourceLocation
        :return: the error message
        :rtype: str
        """
        assert (_origin is not None), '(PyNestML.Utils.ErrorStrings) No origin provided (%s)!' % type(_origin)
        assert (_lhsTypeText is not None and isinstance(_lhsTypeText, str)), \
            '(PyNestML.Utils.ErrorStrings) No or wrong type of lhs-type text provided (%s)!' % type(_lhsTypeText)
        assert (_rhsTypeText is not None and isinstance(_rhsTypeText, str)), \
            '(PyNestML.Utils.ErrorStrings) No or wrong type of rhs-type text provided (%s)!' % type(_rhsTypeText)
        assert (_resultTypeText is not None and isinstance(_resultTypeText, str)), \
            '(PyNestML.Utils.ErrorStrings) No or wrong type of rhs-type text provided (%s)!' % type(_resultTypeText)
        assert (_sourcePosition is not None and isinstance(_sourcePosition, ASTSourceLocation)), \
            '(PyNestML.Utils.ErrorStrings) No or wrong type of source position provided (%s)!' % type(_sourcePosition)
        ERROR_MSG_FORMAT = "Addition/substraction of " + _lhsTypeText + " and " + _rhsTypeText + \
                           ". Assuming: " + _resultTypeText + "."
        return cls.code(_origin) + cls.SEPARATOR + ERROR_MSG_FORMAT + "(" + str(_sourcePosition) + ")"

    @classmethod
    def messageNoSemantics(cls, _origin=None, _exprText=None, _sourcePosition=None):
        """
        construct an error message indicating that an rhs is not implemented
        :param _origin: the class reporting the error
        :param _exprText: plain text of the unimplemented rhs
        :type _exprText: str
        :param _sourcePosition: The location where the error was encountered
        :type _sourcePosition: ASTSourceLocation
        :return: the error message
        :rtype: str
        """
        assert _origin is not None
        assert _exprText is not None and isinstance(_exprText, str)
        assert _sourcePosition is not None and isinstance(_sourcePosition, ASTSourceLocation)
        ERROR_MSG_FORMAT = "This rhs is not implemented: " + _exprText
        return cls.code(_origin) + cls.SEPARATOR + ERROR_MSG_FORMAT + "(" + str(_sourcePosition) + ")"

    @classmethod
    def messageComparison(cls, _origin=None, _sourcePosition=None):
        """
        construct an error message indicating that an a comparison operation has incompatible operands
        :param _origin: the class reporting the error
        :param _sourcePosition: The location where the error was encountered
        :type _sourcePosition: ASTSourceLocation
        :return: the error message
        :rtype: str
        """
        assert _origin is not None
        assert _sourcePosition is not None and isinstance(_sourcePosition, ASTSourceLocation)
        ERROR_MSG_FORMAT = "Operands of a logical rhs not compatible."
        return cls.code(_origin) + cls.SEPARATOR + ERROR_MSG_FORMAT + "(" + str(_sourcePosition) + ")"

    @classmethod
    def messageLogicOperandsNotBool(cls, _origin=None, _sourcePosition=None):
        """
        construct an error message indicating that an a comparison operation has incompatible operands
        :param _origin: the class reporting the error
        :param _sourcePosition: The location where the error was encountered
        :type _sourcePosition: ASTSourceLocation
        :return: the error message
        :rtype: str
        """
        assert _origin is not None
        assert _sourcePosition is not None and isinstance(_sourcePosition, ASTSourceLocation)
        ERROR_MSG_FORMAT = "Both operands of a logical rhs must be boolean."
        return cls.code(_origin) + cls.SEPARATOR + ERROR_MSG_FORMAT + "(" + str(_sourcePosition) + ")"

    @classmethod
    def messageTernary(cls, _origin=None, _sourcePosition=None):
        """
        construct an error message indicating that an a comparison operation has incompatible operands
        :param _origin: the class reporting the error
        :param _sourcePosition: The location where the error was encountered
        :type _sourcePosition: ASTSourceLocation
        :return: the error message
        :rtype: str
        """
        assert _origin is not None
        assert _sourcePosition is not None and isinstance(_sourcePosition, ASTSourceLocation)
        ERROR_MSG_FORMAT = "The ternary operator condition must be boolean."
        return cls.code(_origin) + cls.SEPARATOR + ERROR_MSG_FORMAT + "(" + str(_sourcePosition) + ")"

    @classmethod
    def messageTernaryMismatch(cls, _origin=None, _ifTrueText=None, _ifNotText=None, _sourcePosition=None):
        """
        construct an error message indicating that an a comparison operation has incompatible operands
        :param _origin: the class reporting the error
        :param _ifTrueText: plain text of the positive branch of the ternary operator
        :type _ifTrueText: str
        :param _ifNotText: plain text of the negative branch of the ternary operator
        :type _ifNotText: str
        :param _sourcePosition: The location where the error was encountered
        :type _sourcePosition: ASTSourceLocation
        :return: the error message
        :rtype: str
        """
        assert _origin is not None
        assert _ifTrueText is not None and isinstance(_ifTrueText, str)
        assert _ifNotText is not None and isinstance(_ifNotText, str)
        assert _sourcePosition is not None and isinstance(_sourcePosition, ASTSourceLocation)
        ERROR_MSG_FORMAT = "Mismatched conditional alternatives " + _ifTrueText + " and " + \
                           _ifNotText + "-> Assuming real."
        return cls.code(_origin) + cls.SEPARATOR + ERROR_MSG_FORMAT + "(" + str(_sourcePosition) + ")"

    @classmethod
    def messageResolveFail(cls, _origin=None, _symbolName=None, _sourcePosition=None):
        """
        construct an error message indicating that a symbol could not be resolved
        :param _origin: the class reporting the error
        :param _symbolName: the name of the symbol
        :type _symbolName: str
        :param _sourcePosition: The location where the error was encountered
        :type _sourcePosition: ASTSourceLocation
        :return: the error message
        :rtype: str
        """
        assert _origin is not None
        assert _symbolName is not None and isinstance(_symbolName, str)
        assert _sourcePosition is not None and isinstance(_sourcePosition, ASTSourceLocation)
        ERROR_MSG_FORMAT = "Cannot resolve the symbol: " + _symbolName + "."
        return cls.code(_origin) + cls.SEPARATOR + ERROR_MSG_FORMAT + "(" + str(_sourcePosition) + ")"

    @classmethod
    def messageCannotCalculateConvolveType(cls, _origin=None, _sourcePosition=None):
        """
        construct an error message indicating that the type of a convolve() call is ill-defined
        :param _origin: the class reporting the error
        :param _sourcePosition: The location where the error was encountered
        :type _sourcePosition: ASTSourceLocation
        :return: the error message
        :rtype: str
        """
        assert _origin is not None
        assert _sourcePosition is not None and isinstance(_sourcePosition, ASTSourceLocation)
        error_msg_format = "Cannot calculate return type of convolve()."
        return cls.code(_origin) + cls.SEPARATOR + error_msg_format + "(" + str(_sourcePosition) + ")"

    @classmethod
    def messageVoidFunctionOnRhs(cls, _origin=None, _functionName=None, _sourcePosition=None):
        """
        construct an error message indicating that a void function cannot be used on a RHS
        :param _origin: the class reporting the error
        :param _functionName: the offending function
        :type _functionName: str
        :param _sourcePosition: The location where the error was encountered
        :type _sourcePosition: ASTSourceLocation
        :return: the error message
        :rtype: str
        """
        assert _origin is not None
        assert _sourcePosition is not None and isinstance(_sourcePosition, ASTSourceLocation)
        error_msg_format = "Function " + _functionName + " with the return-type 'void' cannot be used in expressions."
        return cls.code(_origin) + cls.SEPARATOR + error_msg_format + "(" + str(_sourcePosition) + ")"
