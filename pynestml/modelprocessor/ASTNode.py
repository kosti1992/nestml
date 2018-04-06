#
# ASTNode.py
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
from abc import ABCMeta, abstractmethod
from pynestml.modelprocessor.ASTSourceLocation import ASTSourceLocation
from pynestml.modelprocessor.Scope import Scope


class ASTNode(object):
    """
    This class is not a part of the grammar but is used to store commonalities of all possible ast classes, e.g.,
    the source position. This class is abstract, thus no instances can be created.
    """
    __metaclass__ = ABCMeta
    sourcePosition = None
    scope = None
    comment = None

    def __init__(self, source_position, scope=None):
        """
        The standard constructor.
        :param source_position: a source position element.
        :type source_position: ASTSourceLocation
        :param scope: the scope in which this element is embedded in.
        :type scope: Scope
        """
        self.sourcePosition = source_position
        self.scope = scope
        
    def get_source_position(self):
        """
        Returns the source position of the element.
        :return: a source position object.
        :rtype: ASTSourceLocation
        """
        if self.sourcePosition is not None:
            return self.sourcePosition
        else:
            return ASTSourceLocation.get_predefined_source_position()

    def set_source_position(self, new_position):
        """
        Updates the source position of the element.
        :param new_position: a new source position
        :type new_position: ASTSourcePosition
        :return: a source position object.
        :rtype: ASTSourceLocation
        """
        self.sourcePosition = new_position
        return

    def get_scope(self):
        """
        Returns the scope of this element.
        :return: a scope object.
        :rtype: Scope 
        """
        return self.scope

    def update_scope(self, _scope):
        """
        Updates the scope of this element.
        :param _scope: a scope object.
        :type _scope: Scope
        """
        self.scope = _scope

    def get_comment(self):
        """
        Returns the comment of this element.
        :return: a comment.
        :rtype: str
        """
        return self.comment

    def set_comment(self, comment):
        """
        Updates the comment of this element.
        :param comment: a comment
        :type comment: str
        """
        self.comment = comment

    def has_comment(self):
        """
        Indicates whether this element stores a prefix.
        :return: True if has comment, otherwise False.
        :rtype: bool
        """
        return self.comment is not None and len(self.comment) > 0

    def print_comment(self, prefix):
        """
        Prints the comment of this ast element.
        :param prefix: a prefix string
        :type prefix: str
        :return: a comment
        :rtype: str
        """
        ret = ''
        if not self.has_comment():
            return prefix if prefix is not None else ''
        # in the last part, delete the new line if it is the last comment, otherwise there is an ugly gap
        # between the comment and the element
        for comment in self.get_comment():
            ret += (prefix + ' ' if prefix is not None else '') + comment + \
                   ('\n' if self.get_comment().index(comment) < len(self.get_comment()) - 1 else '')
        return ret

    @abstractmethod
    def get_parent(self, ast):
        """
        Indicates whether a this node contains the handed over node.
        :param ast: an arbitrary ast node.
        :type ast: AST_
        :return: AST if this or one of the child nodes contains the handed over element.
        :rtype: AST_ or None
        """
        pass

    def accept(self, visitor):
        """
        Double dispatch for visitor pattern.
        :param visitor: A visitor.
        :type visitor: Inherited from NESTMLVisitor.
        """
        from pynestml.modelprocessor.ASTVisitor import ASTVisitor
        assert (visitor is not None and isinstance(visitor, ASTVisitor)), \
            '(PyNestML.AST.Element) No or wrong type of visitor provided (%s)!' % type(visitor)
        visitor.handle(self)
        return

    @abstractmethod
    def __str__(self):
        """
        Prints the node to a readable format.
        :return: a string representation of the node.
        :rtype: str
        """
        pass

    @abstractmethod
    def equals(self, other):
        """
        The equals operation.
        :param other: a different object.
        :type other: object
        :return: True if equal, otherwise False.
        :rtype: bool
        """
        pass
