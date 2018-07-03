from pynestml.codegeneration.expressions_pretty_printer import ExpressionsPrettyPrinter
from pynestml.meta_model.ast_expression_node import ASTExpressionNode


class SpiNNakerPrinter(object):

    def __init__(self, expression_pretty_printer, reference_convert = None):
        """
        The standard constructor.
        :param reference_convert: a single reference converter
        :type reference_convert: IReferenceConverter
        """
        if expression_pretty_printer is not None:
            self.expression_pretty_printer = expression_pretty_printer
        else:
            self.expression_pretty_printer = ExpressionsPrettyPrinter(reference_convert)
        return

    def print_expression(self, node):
        # type: (ASTExpressionNode) -> str
        """
        Pretty Prints the handed over rhs to a nest readable format.
        :param node: a single meta_model node.
        :type node: ASTExpressionNode
        :return: the corresponding string representation
        :rtype: str
        """
        return self.expression_pretty_printer.print_expression(node)

    def print_origin(self, node):
        return 'neuron->'

    def print_function_call(self, node):
        return 'todo: print function call'

    def print_method_call(self, node):
        return 'todo: print method call'
