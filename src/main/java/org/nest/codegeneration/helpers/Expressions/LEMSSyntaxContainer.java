package org.nest.codegeneration.helpers.Expressions;

import org.nest.units._ast.ASTUnitType;

/**
 * A concrete syntax container for the target modeling language LEMS.
 *
 * @author perun
 */
public class LEMSSyntaxContainer implements SyntaxContainer {

	public String print(NumericalLiteral expr) {
		if (expr.getValue() - (int) expr.getValue() == 0) {
			if (expr.hasType()) {
				return String.valueOf((int) expr.getValue()) + expr.getType().get().getUnit().get().toString();
			} else {
				return String.valueOf((int) expr.getValue());
			}
		}
		if (expr.hasType() && expr.getType().isPresent() && expr.getType().get().getUnit().isPresent()) {
			//System.out.print(String.valueOf(expr.getValue())
			//		+ expr.getType().get().getUnit().get().toString());
			return String.valueOf(expr.getValue()) + expr.getType().get().getUnit().get().toString();
		} else {
			return String.valueOf(expr.getValue());
		}
	}

	public String print(Variable expr) {
		return expr.getVariable();
	}

	public String print(Operator expr) {
		if (expr.isInf()) {
			return "[Inf_not_supported]";
		}
		if (expr.isLogicalOr()) {
			return ".or.";
		}
		if (expr.isLogicalAnd()) {
			return ".and.";
		}
		if (expr.isLogicalNot()) {
			//TODO:this case seems to be rather fishy in LEMS, not sure if it works
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
		} else {//TODO: Yet to implement more
			return "TODO";
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
