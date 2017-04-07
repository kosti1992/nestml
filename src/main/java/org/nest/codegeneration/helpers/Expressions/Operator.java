package org.nest.codegeneration.helpers.Expressions;

import org.nest.commons._ast.ASTExpr;

/**
 * This class stores an operator, e.g. +,-,OR,AND,>> ....
 *
 * @author perun
 */
public class Operator extends Expression {
	private boolean inf;
	private boolean logicalOr;
	private boolean logicalAnd;
	private boolean logicalNot;
	private boolean gt;
	private boolean ge;
	private boolean ne;
	private boolean eq;
	private boolean le;
	private boolean lt;
	private boolean bitOr;
	private boolean bitXor;
	private boolean bitAnd;
	private boolean shiftRight;
	private boolean shiftLeft;
	private boolean minusOp;
	private boolean plusOp;
	private boolean moduloOp;
	private boolean timesOp;
	private boolean divOp;
	private boolean unaryTilde;
	private boolean unaryMinus;
	private boolean unaryPlus;
	private boolean power;
	private boolean leftParentheses;
	private boolean rightParentheses;
	private boolean non;//this operator indicates that non operator is selected, e.g. in case a negation has been negated

	public Operator(ASTExpr expr) {
		if (expr.isInf()) {
			this.inf = true;
		} else if (expr.isLogicalOr()) {
			this.logicalOr = true;
		} else if (expr.isLogicalAnd()) {
			this.logicalAnd = true;
		} else if (expr.isLogicalNot()) {
			this.logicalNot = true;
		} else if (expr.isGt()) {
			this.gt = true;
		} else if (expr.isGe()) {
			this.ge = true;
		} else if (expr.isNe()) {
			this.ne = true;
		} else if (expr.isEq()) {
			this.eq = true;
		} else if (expr.isLe()) {
			this.le = true;
		} else if (expr.isLt()) {
			this.lt = true;
		} else if (expr.isBitOr()) {
			this.bitOr = true;
		} else if (expr.isBitXor()) {
			this.bitXor = true;
		} else if (expr.isBitAnd()) {
			this.bitAnd = true;
		} else if (expr.isShiftRight()) {
			this.shiftRight = true;
		} else if (expr.isShiftLeft()) {
			this.shiftLeft = true;
		} else if (expr.isMinusOp()) {
			this.minusOp = true;
		} else if (expr.isPlusOp()) {
			this.plusOp = true;
		} else if (expr.isModuloOp()) {
			this.moduloOp = true;
		} else if (expr.isDivOp()) {
			this.divOp = true;
		} else if (expr.isTimesOp()) {
			this.timesOp = true;
		} else if (expr.isUnaryTilde()) {
			this.unaryTilde = true;
		} else if (expr.isUnaryPlus()) {
			this.unaryPlus = true;
		} else if (expr.isUnaryMinus()) {
			this.unaryMinus = true;
		} else if (expr.isPow()) {
			this.power = true;
		} else if (expr.isLeftParentheses()) {
			this.leftParentheses = true;
		} else if (expr.isRightParentheses()) {
			this.rightParentheses = true;
		} else {
			this.non = true;
		}

	}

	public Operator() {
	}

	public boolean isInf() {
		return inf;
	}

	public boolean isLogicalOr() {
		return logicalOr;
	}

	public boolean isLogicalAnd() {
		return logicalAnd;
	}

	public boolean isLogicalNot() {
		return logicalNot;
	}

	public boolean isGt() {
		return gt;
	}

	public boolean isGe() {
		return ge;
	}

	public boolean isNe() {
		return ne;
	}

	public boolean isEq() {
		return eq;
	}

	public boolean isLe() {
		return le;
	}

	public boolean isLt() {
		return lt;
	}

	public boolean isBitOr() {
		return bitOr;
	}

	public boolean isBitXor() {
		return bitXor;
	}

	public boolean isBitAnd() {
		return bitAnd;
	}

	public boolean isShiftRight() {
		return shiftRight;
	}

	public boolean isShiftLeft() {
		return shiftLeft;
	}

	public boolean isMinusOp() {
		return minusOp;
	}

	public boolean isPlusOp() {
		return plusOp;
	}

	public boolean isModuloOp() {
		return moduloOp;
	}

	public boolean isTimesOp() {
		return timesOp;
	}

	public boolean isDivOp() {
		return divOp;
	}

	public boolean isUnaryTilde() {
		return unaryTilde;
	}

	public boolean isUnaryMinus() {
		return unaryMinus;
	}

	public boolean isUnaryPlus() {
		return unaryPlus;
	}

	public boolean isPower() {
		return power;
	}

	public boolean isLeftParentheses() {
		return leftParentheses;
	}

	public boolean isRightParentheses() {
		return rightParentheses;
	}

	public boolean isNon() {
		return non;
	}

	public void setInf(boolean inf) {
		this.inf = inf;
	}

	public void setLogicalOr(boolean logicalOr) {
		this.logicalOr = logicalOr;
	}

	public void setLogicalAnd(boolean logicalAnd) {
		this.logicalAnd = logicalAnd;
	}

	public void setLogicalNot(boolean logicalNot) {
		this.logicalNot = logicalNot;
	}

	public void setGt(boolean gt) {
		this.gt = gt;
	}

	public void setGe(boolean ge) {
		this.ge = ge;
	}

	public void setNe(boolean ne) {
		this.ne = ne;
	}

	public void setEq(boolean eq) {
		this.eq = eq;
	}

	public void setLe(boolean le) {
		this.le = le;
	}

	public void setLt(boolean lt) {
		this.lt = lt;
	}

	public void setBitOr(boolean bitOr) {
		this.bitOr = bitOr;
	}

	public void setBitXor(boolean bitXor) {
		this.bitXor = bitXor;
	}

	public void setBitAnd(boolean bitAnd) {
		this.bitAnd = bitAnd;
	}

	public void setShiftRight(boolean shiftRight) {
		this.shiftRight = shiftRight;
	}

	public void setShiftLeft(boolean shiftLeft) {
		this.shiftLeft = shiftLeft;
	}

	public void setMinusOp(boolean minusOp) {
		this.minusOp = minusOp;
	}

	public void setPlusOp(boolean plusOp) {
		this.plusOp = plusOp;
	}

	public void setModuloOp(boolean moduloOp) {
		this.moduloOp = moduloOp;
	}

	public void setTimesOp(boolean timesOp) {
		this.timesOp = timesOp;
	}

	public void setDivOp(boolean divOp) {
		this.divOp = divOp;
	}

	public void setUnaryTilde(boolean unaryTilde) {
		this.unaryTilde = unaryTilde;
	}

	public void setUnaryMinus(boolean unaryMinus) {
		this.unaryMinus = unaryMinus;
	}

	public void setUnaryPlus(boolean unaryPlus) {
		this.unaryPlus = unaryPlus;
	}

	public void setPower(boolean power) {
		this.power = power;
	}

	public void setLeftParentheses(boolean leftParentheses) {
		this.leftParentheses = leftParentheses;
	}

	public void setRightParentheses(boolean rightParentheses) {
		this.rightParentheses = rightParentheses;
	}

	public void setNon(boolean non) {
		this.non = non;
	}

	public void negate() {
		if (this.logicalAnd) {
			this.logicalAnd = false;
			this.logicalOr = true;
		} else if (this.logicalOr) {
			this.logicalOr = false;
			this.logicalAnd = true;
		} else if (this.logicalNot) {
			this.logicalNot = false;
			this.non = true;
		} else if (this.le) {
			this.le = false;
			this.gt = true;
		} else if (this.lt) {
			this.lt = false;
			this.ge = true;
		} else if (this.gt) {
			this.gt = false;
			this.le = true;
		} else if (this.ge) {
			this.ge = false;
			this.lt = true;
		} else if (this.eq) {
			this.eq = false;
			this.ne = true;
		} else if (this.ne) {
			this.eq = true;
			this.ne = false;
		} else if (this.non) {
			this.non = false;
			this.logicalNot = true;
		}
	}

	public String print(SyntaxContainer container) {
		return container.print(this);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Operator operator = (Operator) o;

		if (inf != operator.inf)
			return false;
		if (logicalOr != operator.logicalOr)
			return false;
		if (logicalAnd != operator.logicalAnd)
			return false;
		if (logicalNot != operator.logicalNot)
			return false;
		if (gt != operator.gt)
			return false;
		if (ge != operator.ge)
			return false;
		if (ne != operator.ne)
			return false;
		if (eq != operator.eq)
			return false;
		if (le != operator.le)
			return false;
		if (lt != operator.lt)
			return false;
		if (bitOr != operator.bitOr)
			return false;
		if (bitXor != operator.bitXor)
			return false;
		if (bitAnd != operator.bitAnd)
			return false;
		if (shiftRight != operator.shiftRight)
			return false;
		if (shiftLeft != operator.shiftLeft)
			return false;
		if (minusOp != operator.minusOp)
			return false;
		if (plusOp != operator.plusOp)
			return false;
		if (moduloOp != operator.moduloOp)
			return false;
		if (timesOp != operator.timesOp)
			return false;
		if (divOp != operator.divOp)
			return false;
		if (unaryTilde != operator.unaryTilde)
			return false;
		if (unaryMinus != operator.unaryMinus)
			return false;
		if (unaryPlus != operator.unaryPlus)
			return false;
		if (power != operator.power)
			return false;
		if (leftParentheses != operator.leftParentheses)
			return false;
		return rightParentheses == operator.rightParentheses;

	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (inf ? 1 : 0);
		result = 31 * result + (logicalOr ? 1 : 0);
		result = 31 * result + (logicalAnd ? 1 : 0);
		result = 31 * result + (logicalNot ? 1 : 0);
		result = 31 * result + (gt ? 1 : 0);
		result = 31 * result + (ge ? 1 : 0);
		result = 31 * result + (ne ? 1 : 0);
		result = 31 * result + (eq ? 1 : 0);
		result = 31 * result + (le ? 1 : 0);
		result = 31 * result + (lt ? 1 : 0);
		result = 31 * result + (bitOr ? 1 : 0);
		result = 31 * result + (bitXor ? 1 : 0);
		result = 31 * result + (bitAnd ? 1 : 0);
		result = 31 * result + (shiftRight ? 1 : 0);
		result = 31 * result + (shiftLeft ? 1 : 0);
		result = 31 * result + (minusOp ? 1 : 0);
		result = 31 * result + (plusOp ? 1 : 0);
		result = 31 * result + (moduloOp ? 1 : 0);
		result = 31 * result + (timesOp ? 1 : 0);
		result = 31 * result + (divOp ? 1 : 0);
		result = 31 * result + (unaryTilde ? 1 : 0);
		result = 31 * result + (unaryMinus ? 1 : 0);
		result = 31 * result + (unaryPlus ? 1 : 0);
		result = 31 * result + (power ? 1 : 0);
		result = 31 * result + (leftParentheses ? 1 : 0);
		result = 31 * result + (rightParentheses ? 1 : 0);
		return result;
	}

	/**
	 * This is a deepClone method which generates a clone of this object whenever required, e.g. when it has to be
	 * mirrored to other parts of the expression tree.
	 *
	 * @return a deep clone of this
	 */
	public Operator deepClone() {
		Operator temp = new Operator();
		if (this.isInf()) {
			temp.setInf(true);
		}
		if (this.isLogicalOr()) {
			temp.setLogicalOr(true);
		}
		if (this.isLogicalAnd()) {
			temp.setLogicalAnd(true);
		}
		if (this.isLogicalNot()) {
			temp.setLogicalNot(true);
		}
		if (this.isGt()) {
			temp.setGt(true);
		}
		if (this.isGe()) {
			temp.setGe(true);
		}
		if (this.isNe()) {
			temp.setNe(true);
		}
		if (this.isEq()) {
			temp.setEq(true);
		}
		if (this.isLe()) {
			temp.setLe(true);
		}
		if (this.isLt()) {
			temp.setLt(true);
		}
		if (this.isBitOr()) {
			temp.setBitOr(true);
		}
		if (this.isBitXor()) {
			temp.setBitXor(true);
		}
		if (this.isBitAnd()) {
			temp.setBitAnd(true);
		}
		if (this.isShiftRight()) {
			temp.setShiftRight(true);
		}
		if (this.isShiftLeft()) {
			temp.setShiftLeft(true);
		}
		if (this.isMinusOp()) {
			temp.setMinusOp(true);
		}
		if (this.isPlusOp()) {
			temp.setPlusOp(true);
		}
		if (this.isModuloOp()) {
			temp.setModuloOp(true);
		}
		if (this.isTimesOp()) {
			temp.setTimesOp(true);
		}
		if (this.isDivOp()) {
			temp.setDivOp(true);
		}
		if (this.isUnaryTilde()) {
			temp.setUnaryTilde(true);
		}
		if (this.isUnaryMinus()) {
			temp.setUnaryMinus(true);
		}
		if (this.isUnaryPlus()) {
			temp.setUnaryPlus(true);
		}
		if (this.isPower()) {
			temp.setPower(true);
		}
		if (this.isLeftParentheses()) {
			temp.setLeftParentheses(true);
		}
		if (this.isRightParentheses()) {
			temp.setRightParentheses(true);
		}
		if (this.isNon()) {
			temp.setNon(true);
		}
		return temp;
	}


}
