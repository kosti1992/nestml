#
# nestml_error_listener.py
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

from antlr4 import DiagnosticErrorListener, Utils as AntlrUtil
from pynestml.utils.logger import Logger, LoggingLevel
from pynestml.utils.messages import Messages
from pynestml.meta_model.ast_source_location import ASTSourceLocation
import os


class NestMLErrorListener(DiagnosticErrorListener):

    def __init__(self, report_ambiguity = False):
        super(NestMLErrorListener, self).__init__()
        self.report_ambiguity = report_ambiguity

    def syntaxError(self, recognizer, offending_symbol, line, column, msg, e):
        code, message = Messages.get_syntax_error_in_model("%s at: %s" % (msg, offending_symbol.text))
        _, file_name = os.path.split(offending_symbol.source[1].fileName)
        Logger.log_message(code=code, message=message, error_position=ASTSourceLocation(line, column, line, column),
                           log_level=LoggingLevel.ERROR, neuron=file_name)

    def reportAmbiguity(self, recognizer, dfa, start_index,
                        stop_index, exact, ambig_alts, configs):
        if self.report_ambiguity:
            msg = u"reportAmbiguity d="
            msg += self.getDecisionDescription(recognizer, dfa)
            msg += u": ambigAlts="
            msg += AntlrUtil.str_set(self.getConflictingAlts(ambig_alts, configs))
            msg += u", input='"
            msg += recognizer.getTokenStream().getText((start_index, stop_index))
            msg += u"'"
            code, message = Messages.get_syntax_warning_in_model(msg)
            Logger.log_message(code=code, message=message, error_position=ASTSourceLocation(start_index, stop_index,
                                                                                            start_index, stop_index),
                               log_level=LoggingLevel.ERROR)

    def reportAttemptingFullContext(self, recognizer, dfa, start_index,
                                    stop_index, conflicting_alts, configs):
        if self.report_ambiguity:
            msg = u"reportAttemptingFullContext d="
            msg += self.getDecisionDescription(recognizer, dfa)
            msg += u", input='"
            msg += recognizer.getTokenStream().getText((start_index, stop_index))
            msg += u"'"
            code, message = Messages.get_syntax_warning_in_model(msg)
            Logger.log_message(code=code, message=message, error_position=ASTSourceLocation(start_index, stop_index,
                                                                                            start_index, stop_index),
                               log_level=LoggingLevel.ERROR)

    def reportContextSensitivity(self, recognizer, dfa, start_index,
                                 stop_index, prediction, configs):
        if self.report_ambiguity:
            msg = u"reportContextSensitivity d="
            msg += self.getDecisionDescription(recognizer, dfa)
            msg += u", input='"
            msg += recognizer.getTokenStream().getText((start_index, stop_index))
            msg += u"'"
            code, message = Messages.get_syntax_warning_in_model(msg)
            Logger.log_message(code=code, message=message, error_position=ASTSourceLocation(start_index, stop_index,
                                                                                            start_index, stop_index),
                               log_level=LoggingLevel.ERROR)
