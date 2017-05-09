package org.nest.codegeneration.helpers.Expressions;

import org.nest.codegeneration.LEMSCodeGenerator;
import org.nest.codegeneration.helpers.LEMSElements.HelperCollection;

import static de.se_rwth.commons.logging.Log.info;

/**
 * A concrete syntax container for the target modeling language LEMS.
 *
 * @author perun
 */
public class LEMSSyntaxContainer implements SyntaxContainer {

	public String print(NumericalLiteral expr) {
		if (expr.getValue() - (int) expr.getValue() == 0) {
			if (expr.hasType()) {
				/*
				return String.valueOf((int) expr.getValue()) +" "+ HelperCollection.formatComplexUnit(
						expr.getType().get().getUnit().get().toString());
						*/
				return String.valueOf((int) expr.getValue()) +" "+
						HelperCollection.formatComplexUnit(HelperCollection.getExpressionFromUnitType(expr.getType().get()).print());
			} else {
				return String.valueOf((int) expr.getValue());
			}
		}
		if (expr.hasType()) {
			return String.valueOf(expr.getValue()) +" "+
					HelperCollection.formatComplexUnit(HelperCollection.getExpressionFromUnitType(expr.getType().get()).print());
		} else {
			return String.valueOf(expr.getValue());
		}
	}

	public String print(Variable expr) {
		return expr.getVariable();
	}

	public String print(Operator expr) {
		if (expr.isInf()) {
			return String.valueOf(Integer.MAX_VALUE);
		}
		if (expr.isLogicalOr()) {
			return ".or.";
		}
		if (expr.isLogicalAnd()) {
			return ".and.";
		}
		if (expr.isLogicalNot()) {
			info("Logical NOT found. This should not happen.",LEMSCodeGenerator.LOG_NAME);
			return ".not.";
		}
		if (expr.isGt()) {
			return ".gt.";
		}
		if (expr.isGe()) {
			return ".geq.";
		}
		if (expr.isNe()) {
			return ".neq.";
		}
		if (expr.isEq()) {
			return ".eq.";
		}
		if (expr.isLe()) {
			return ".leq.";
		}
		if (expr.isLt()) {
			return ".lt.";
		}
		if (expr.isBitOr()) {
			return "[BitOr_not_supported]";
		}
		if (expr.isBitXor()) {
			return "[BitXor_not_supported]";
		}
		if (expr.isBitAnd()) {
			return "[BitAnd_not_supported]";
		}
		if (expr.isShiftRight()) {
			return "[BitShiftR_not_supported]";
		}
		if (expr.isShiftLeft()) {
			return "[BitShiftL_not_supported]";
		}
		if (expr.isMinusOp()) {
			return "-";
		}
		if (expr.isPlusOp()) {
			return "+";
		}
		if (expr.isModuloOp()) {
			return "%";
		}
		if (expr.isDivOp()) {
			return "/";
		}
		if (expr.isTimesOp()) {
			return "*";
		}
		if (expr.isUnaryTilde()) {
			return "[UnaryTilde_not_supported]";
		}
		if (expr.isUnaryPlus()) {
			return "+";
		}
		if (expr.isUnaryMinus()) {
			return "-";
		}
		if (expr.isPower()) {
			return "^";
		}
		if (expr.isLeftParentheses()) {
			return "(";
		}
		if (expr.isRightParentheses()) {
			return ")";
		}
		if (expr.isNon()) {
			return "";
		} else {
			info("New type of operator found. Please implement first.",LEMSCodeGenerator.LOG_NAME);
			return "";
		}

	}

	public String print(Function expr) {
		String ret = expr.getFunctionName() + "(";
		StringBuilder newBuilder = new StringBuilder();
		for (Expression arg : expr.getArguments()) {
			newBuilder.append(arg.print(this));
			newBuilder.append(",");
		}
		if (expr.getArguments().size() > 0) {
			newBuilder.deleteCharAt(newBuilder.length() - 1);//delete the last "," before the end of the string
		}
		return ret + newBuilder.toString() + ")";
	}


}
