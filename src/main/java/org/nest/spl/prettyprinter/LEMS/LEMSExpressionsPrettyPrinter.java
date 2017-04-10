package org.nest.spl.prettyprinter.LEMS;

/**
 * A modified version of the ExpressionPrettyPrinter class which is able to convert internal representation of
 * expression to a human readable/LEMS processable format.
 *
 * @author plotnikov, perun
 */

import de.monticore.prettyprint.IndentPrinter;
import de.monticore.types.prettyprint.TypesPrettyPrinterConcreteVisitor;
//import org.nest.codegeneration.converters.IdempotentReferenceConverter;
import org.nest.codegeneration.helpers.LEMSElements.LEMSCollector;
import org.nest.codegeneration.helpers.LEMSElements.Unit;
import org.nest.commons._ast.ASTExpr;
import org.nest.commons._ast.ASTFunctionCall;
import org.nest.commons._ast.ASTVariable;
import org.nest.spl.prettyprinter.IReferenceConverter;
import org.nest.spl.prettyprinter.IdempotentReferenceConverter;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class LEMSExpressionsPrettyPrinter {
	private final LEMSCollector container;
	private final IReferenceConverter referenceConverter;

	public LEMSExpressionsPrettyPrinter(LEMSCollector container) {
		this.container = container;
		this.referenceConverter = new IdempotentReferenceConverter();
	}

	public LEMSExpressionsPrettyPrinter(LEMSCollector container, final IReferenceConverter referenceConverter) {
		this.container = container;
		this.referenceConverter = referenceConverter;
	}

	/**
	 * Executes the print command.
	 * @param expr the expression which will be converted
	 * @param isNegated indicated whether logical expression have to be negated
	 * @return a strin representation of the expr.
	 */
	public String print(final ASTExpr expr, boolean isNegated) {
		checkNotNull(expr);
		return doPrint(expr, isNegated);
	}

	/**
	 * This method converts a given expression to a corresponding string. The second argument,namely "isNegated"
	 * indicates whether the whole expression has to be returned negated or not, e.g. if the else branch has to
	 * be generated. This class is a modified version of the original ExpressionPrettyPrinter as provided by @plotnikov.
	 * @param expr the expression which will be represented as string.
	 * @param isNegated true, if expression has to be negated.
	 * @return the expression as string.
	 */
	public String doPrint(final ASTExpr expr, boolean isNegated) {
		checkNotNull(expr);
		if (expr.getNESTMLNumericLiteral().isPresent()) {
			//copy the numerical value
			String temp = typesPrinter().prettyprint(expr.getNESTMLNumericLiteral().get().getNumericLiteral());
			//now check whether a unit is present

			if (expr.getNESTMLNumericLiteral().get().getType().isPresent()) {
				container.addUnit(new Unit(expr.getType().getValue()));
				temp = temp + expr.getType().getValue().prettyPrint();
			}
			return temp;
		}
		if (expr.isInf()) {
			return handleConstant("inf");
		} else if (expr.getStringLiteral().isPresent()) { // string
			return typesPrinter().prettyprint(expr.getStringLiteral().get());
		} else if (expr.getBooleanLiteral().isPresent()) { // boolean
			return typesPrinter().prettyprint(expr.getBooleanLiteral().get());
		} else if (expr.getVariable().isPresent()) { // var
			return handleQualifiedName(expr.getVariable().get());
		} else if (expr.getFunctionCall().isPresent()) { // function
			if (expr.getFunctionCall().get().getName().toString().equals("pow")) {
				//pow have to be printed in a different manner
				final String leftOperand = print(expr.getFunctionCall().get().getArgs().get(0), isNegated);
				final String rightOperand = print(expr.getFunctionCall().get().getArgs().get(1), isNegated);
				final String powTemplate = referenceConverter.convertBinaryOperator("^");
				return String.format(powTemplate, leftOperand, rightOperand);
			}
			final ASTFunctionCall astFunctionCall = expr.getFunctionCall().get();
			return printMethodCall(astFunctionCall);

		} else if (expr.isUnaryPlus()) {
			return "(" + "+" + print(expr.getTerm().get(), isNegated) + ")";
		} else if (expr.isUnaryMinus()) {
			return "-" + print(expr.getTerm().get(), isNegated);//TODO:why ( ) ?
		} else if (expr.isUnaryTilde()) {
			return "(" + "~" + print(expr.getTerm().get(), isNegated) + ")";
		} else if (expr.isLeftParentheses() && expr.isRightParentheses()) {
			return "(" + print(expr.getExpr().get(), isNegated) + ")";
		} else if (expr.isPlusOp() || expr.isMinusOp() || expr.isTimesOp() || expr.isDivOp()) {
			final StringBuilder expression = new StringBuilder();
			final String leftOperand = print(expr.getLeft().get(), isNegated);
			final String rightOperand = print(expr.getRight().get(), isNegated);
			expression.append(leftOperand);
			expression.append(getArithmeticOperator(expr));
			expression.append(rightOperand);
			return expression.toString();
		} else if (expr.isPow()) {
			final String leftOperand = print(expr.getBase().get(), isNegated);
			final String rightOperand = print(expr.getExponent().get(), isNegated);
			final String powTemplate = referenceConverter.convertBinaryOperator("^");
			return String.format(powTemplate, leftOperand, rightOperand);
		} else if (expr.isShiftLeft() ||
				expr.isShiftRight() ||
				expr.isModuloOp() ||
				expr.isBitAnd() ||
				expr.isBitOr() ||
				expr.isBitXor()) {
			final StringBuilder expression = new StringBuilder();
			final String leftOperand = print(expr.getLeft().get(), isNegated);
			final String rightOperand = print(expr.getRight().get(), isNegated);
			expression.append(leftOperand);
			expression.append(printBitOperator(expr));
			expression.append(rightOperand);
			return expression.toString();
		}
		// left:Expr (lt:["<"] | le:["<="] | eq:["=="] | ne:["!="] | ne2:["<>"] | ge:[">="] | gt:[">"]) right:Expr
		else if (expr.isLt() ||
				expr.isLe() ||
				expr.isEq() ||
				expr.isNe() ||
				expr.isNe2() ||
				expr.isGe() ||
				expr.isGt()) {
			final StringBuilder expression = new StringBuilder();
			final String leftOperand = print(expr.getLeft().get(), isNegated);
			final String rightOperand = print(expr.getRight().get(), isNegated);
			expression.append(leftOperand).append(printComparisonOperator(expr, isNegated)).append(rightOperand);
			return expression.toString();
		} else if (expr.isLogicalOr() || expr.isLogicalAnd()) {
			final String leftOperand = print(expr.getLeft().get(), isNegated);
			final String rightOperand = print(expr.getRight().get(), isNegated);

			if (expr.isLogicalAnd()) {
				String operatorTemplate;
				if (isNegated) {//if the expression has to be negated, print the opposite log. operator -> or
					operatorTemplate = referenceConverter.convertBinaryOperator(".or.");
				} else {
					operatorTemplate = referenceConverter.convertBinaryOperator(".and.");
				}
				return String.format(operatorTemplate, leftOperand, rightOperand);
			} else { // it is an or-operator
				String operatorTemplate;
				if (isNegated) {//if the expression has to be negated, print the opposite log. operator -> and
					operatorTemplate = referenceConverter.convertBinaryOperator(".and.");
				} else {
					operatorTemplate = referenceConverter.convertBinaryOperator(".or.");
				}
				return String.format(operatorTemplate, leftOperand, rightOperand);
			}
		} else if (expr.isLogicalNot()) {
			return "not " + print(expr.getExpr().get(), isNegated);
		} else if (expr.getCondition().isPresent()) {
			final String condition = print(expr.getCondition().get(), isNegated);
			final String ifTrue = print(expr.getIfTrue().get(), isNegated); // guaranteed by grammar
			final String ifNot = print(expr.getIfNot().get(), isNegated); // guaranteed by grammar
			return "(" + condition + ")?(" + ifTrue + "):(" + ifNot + ")";
		}

	final String errorMsg = "Cannot determine the type of the Expression-Node @{" + expr.get_SourcePositionStart() +
			", " + expr.get_SourcePositionEnd() + "}";

	throw new RuntimeException(errorMsg);

}

	public String printMethodCall(final ASTFunctionCall astFunctionCall) {
		final String nestFunctionName = referenceConverter.convertFunctionCall(astFunctionCall);
		if (referenceConverter.needsArguments(astFunctionCall)) {
			final StringBuilder argsListAsString = printFunctionCallArguments(astFunctionCall);
			return String.format(nestFunctionName, argsListAsString);
		} else {
			return nestFunctionName;
		}
	}

	public StringBuilder printFunctionCallArguments(final ASTFunctionCall astFunctionCall) {
		final StringBuilder argsListAsString = new StringBuilder();

		final List<ASTExpr> functionArgs = astFunctionCall.getArgs();
		for (int i = 0; i < functionArgs.size(); ++i) {
			boolean isLastArgument = (i + 1) == functionArgs.size();
			if (!isLastArgument) {
				argsListAsString.append(print(functionArgs.get(i), false));//function calls shall not be optional negatable
				argsListAsString.append(", ");
			} else {
				// last argument, don't append ','
				argsListAsString.append(print(functionArgs.get(i), false));//function calls shall not be optional negatable
			}

		}
		return argsListAsString;
	}

	protected String handleConstant(final String constantName) {
		return referenceConverter.convertConstant(constantName);
	}

	protected String handleQualifiedName(final ASTVariable astVariableName) {
		if (astVariableName.toString().equals("e")) {
			return "exp(1)";
		}
		return referenceConverter.convertNameReference(astVariableName);
	}

	private String printComparisonOperator(final ASTExpr expr, boolean isNegated) {
		if (expr.isLt()) {
			if (isNegated) {
				return ".geq";
			}
			return ".lt.";
		}
		if (expr.isLe()) {
			if (isNegated) {
				return ".gt.";
			}
			return ".leq.";
		}
		if (expr.isEq()) {
			if (isNegated) {
				return ".neq.";
			}
			return ".eq.";
		}
		if (expr.isNe() || expr.isNe2()) {
			if (isNegated) {
				return ".eq.";
			}
			return ".neq.";
		}
		if (expr.isGe()) {
			if (isNegated) {
				return ".lt.";
			}
			return ".geq.";
		}
		if (expr.isGt()) {
			if (isNegated) {
				return ".leq.";
			}
			return ".gt.";
		}
		throw new RuntimeException("Cannot determine comparison operator");
	}

	private String printBitOperator(final ASTExpr expr) {
		if (expr.isShiftLeft()) {
			return " NotSupportedOperator:BitShift<< ";//not supported by lems
		}
		if (expr.isShiftRight()) {//not supported by lems
			return " NotSupportedOperator:BitShift >> ";
		}
		if (expr.isModuloOp()) {//the same in lems
			return "%";
		}
		if (expr.isBitAnd()) {//not supported by lems
			return " NotSupportedOperator:BitAnd & ";
		}
		if (expr.isBitOr()) {//not supported by lems
			return " NotSupportedOperator:BitOr | ";
		}
		if (expr.isBitXor()) {//not supported by lems
			return " NotSupportedOperator:BitXor ^";
		}

		throw new RuntimeException("Cannot determine mathematical operator");
	}

	private String getArithmeticOperator(final ASTExpr expr) {
		if (expr.isPlusOp()) {
			return "+";
		}
		if (expr.isMinusOp()) {
			return "-";
		}
		if (expr.isTimesOp()) {
			return "*";
		}
		if (expr.isDivOp()) {
			return "/";
		}
		throw new RuntimeException("Cannot determine mathematical operator");
	}

	private TypesPrettyPrinterConcreteVisitor typesPrinter() {
		final IndentPrinter printer = new IndentPrinter();
		return new TypesPrettyPrinterConcreteVisitor(printer);
	}


}
