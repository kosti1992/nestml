package org.nest.codegeneration.helpers.Expressions;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.nest.codegeneration.helpers.LEMSElements.DynamicRoutine;
import org.nest.codegeneration.helpers.LEMSElements.LEMSCollector;
import org.nest.commons._ast.ASTExpr;
import org.nest.symboltable.symbols.VariableSymbol;

/**
 * This class represents an internal representation of an expression, e.g
 * 10mV + V_reset .
 *
 * @author perun
 */
public class Expression {
	private Optional<Expression> lhs = Optional.empty();
	private Optional<Operator> operator = Optional.empty();
	private Optional<Expression> rhs = Optional.empty();

	//TODO: this is actually not really nice, a workaround to generate from string would be good
	public Expression(String value) {
		this.rhs = Optional.of(new Variable(value));
	}

	public Expression() {
	}

	public Expression(ASTExpr expr) {
		checkNotNull(expr);
		this.handleExpression(expr);
	}

	public Expression(VariableSymbol expr) {
		checkNotNull(expr);
		if (expr.getDeclaringExpression().isPresent()) {
			this.handleExpression(expr.getDeclaringExpression().get());
		}
	}

	/**
	 * The main routine which deals with the correct creation of an internal representation of
	 * an expression.
	 *
	 * @param expr The expression which shall be transformed to the internal representation.
	 *             Caution: the AST expression is not changed.
	 */
	private void handleExpression(ASTExpr expr) {
		if (expr.getTerm().isPresent()) {
			this.operator = Optional.of(new Operator(expr));
			if (expr.getTerm().get().nESTMLNumericLiteralIsPresent()) {
				this.rhs = Optional.of(new NumericalLiteral(expr.getTerm().get().getNESTMLNumericLiteral().get()));
			} else if (expr.getTerm().get().variableIsPresent()) {
				this.rhs = Optional.of(new Variable(expr.getTerm().get().getVariable().get()));
			} else if (expr.getTerm().get().functionCallIsPresent()) {
				this.rhs = Optional.of(new Function(expr.getTerm().get().getFunctionCall().get()));
			}
		} else if (expr.nESTMLNumericLiteralIsPresent()) {
			this.rhs = Optional.of(new NumericalLiteral(expr.getNESTMLNumericLiteral().get()));
		} else if (expr.variableIsPresent()) {
			this.rhs = Optional.of(new Variable(expr.getVariable().get().getName().toString()));
		} else if (expr.functionCallIsPresent()) {
			this.rhs = Optional.of(new Function(expr.getFunctionCall().get()));
		} else if (expr.exprIsPresent()) {
			this.operator = Optional.of(new Operator(expr));
			this.rhs = Optional.of(new Expression(expr.getExpr().get()));
		} else if (expr.baseIsPresent() && expr.exponentIsPresent()) {
			this.lhs = Optional.of(new Expression(expr.getBase().get()));
			this.rhs = Optional.of(new Expression(expr.getExponent().get()));
			Operator tempOperator = new Operator();
			tempOperator.setPower(true);
			this.operator = Optional.of(tempOperator);
		} else {
			if (expr.leftIsPresent()) {
				this.lhs = Optional.of(new Expression(expr.getLeft().get()));
			}
			this.operator = Optional.of(new Operator(expr));
			if (expr.rightIsPresent()) {
				this.rhs = Optional.of(new Expression(expr.getRight().get()));
			}
			if(expr.isLogicalAnd()||expr.isLogicalOr()||expr.isLogicalNot()){
				this.lhs = Optional.of(Expression.encapsulateInBrackets(this.lhs.get()));
				this.rhs = Optional.of(Expression.encapsulateInBrackets(this.rhs.get()));
			}
		}
	}

	/**
	 * Returns a list of all Operator objects stored in an expression.
	 *
	 * @return a list of operators
	 */
	public List<Expression> getOperators() {
		List<Expression> resOps = new ArrayList<>();
		if (this.getClass().equals(Operator.class)) {
			resOps.add(this);
		}
		if (this.operator.isPresent()) {
			resOps.add(this.operator.get());
		}
		if (this.lhs.isPresent()) {
			resOps.addAll(lhs.get().getOperators());
		}
		if (this.rhs.isPresent()) {
			resOps.addAll(rhs.get().getOperators());
		}
		return resOps;
	}

	/**
	 * Returns a list of all Function objects stored in an expression.
	 *
	 * @return a list of operators
	 */
	public List<Expression> getFunctions() {
		List<Expression> resFunc = new ArrayList<>();
		if (this.getClass().equals(Function.class)) {
			resFunc.add(this);
		}
		if (this.lhs.isPresent()) {
			resFunc.addAll(lhs.get().getFunctions());
		}
		if (this.rhs.isPresent()) {
			resFunc.addAll(rhs.get().getFunctions());
		}
		return resFunc;
	}

	/**
	 * Returns a list of all Variable objects stored in an expression.
	 *
	 * @return a list of operators
	 */
	public List<Expression> getVariables() {
		List<Expression> resVars = new ArrayList<>();
		if (this.getClass().equals(Variable.class)) {
			resVars.add(this);
		}
		if (this.lhs.isPresent()) {
			resVars.addAll(lhs.get().getVariables());
		}
		if (this.rhs.isPresent()) {
			resVars.addAll(rhs.get().getVariables());
		}
		return resVars;
	}

	/**
	 * Returns a list of all Numericals objects stored in an expression.
	 *
	 * @return a list of operators
	 */
	public List<Expression> getNumericals() {
		List<Expression> resNums = new ArrayList<>();
		if (this.getClass().equals(NumericalLiteral.class)) {
			resNums.add(this);
		}
		if (this.lhs.isPresent()) {
			resNums.addAll(lhs.get().getNumericals());
		}
		if (this.rhs.isPresent()) {
			resNums.addAll(rhs.get().getNumericals());
		}
		return resNums;
	}


	/**
	 * Prints a String representation of the stored expression. The syntax is
	 * of the corresponding elements is determined by the handed over SyntaxContainer
	 * object.
	 *
	 * @param container determines which syntax shall be used.
	 * @return a string representation of the expression
	 */
	public String print(SyntaxContainer container) {
		String ret = "";
		//This is a special case, since brackets have to be around the whole expr.
		if (this.operator.isPresent() && this.operator.get().isLeftParentheses() &&
				this.operator.get().isLeftParentheses() && this.rhs.isPresent()) {
			ret = "(" + this.rhs.get().print(container) + ")";
		} else {
			if (this.lhs.isPresent()) {
				ret = ret + this.lhs.get().print(container);
			}
			if (this.operator.isPresent()) {
				ret = ret + this.operator.get().print(container);
			}
			if (this.rhs.isPresent()) {
				ret = ret + this.rhs.get().print(container);
			}
		}
		return ret;
	}

	/**
	 * Prints the whole expression in LEMS syntax. This method is required since it is not possible
	 * to generate new objects from within the template.
	 *
	 * @return a string representation of the expression
	 */
	public String print() {
		return this.print(new LEMSSyntaxContainer());
	}

	/**
	 * Traverses through the expression tree and replaces each occurrence of pre by post.
	 *
	 * @param pre  the expression which will be replaced
	 * @param post the expression which replaces
	 */
	public void replaceElement(Expression pre, Expression post) {
		if (this.lhs.isPresent() && this.lhs.get().equals(pre)) {
			this.lhs = Optional.of(post);
		}
		if (this.operator.isPresent() && this.operator.get().equals(pre)) {
			this.operator = Optional.of((Operator) post);
		}
		if (this.rhs.isPresent() && this.rhs.get().equals(pre)) {
			this.rhs = Optional.of(post);
		}
		if (this.lhs.isPresent()) {
			this.lhs.get().replaceElement(pre, post);
		}
		if (this.rhs.isPresent()) {
			this.rhs.get().replaceElement(pre, post);
		}
	}

	public void replaceLhs(Expression expr) {
		this.lhs = Optional.of(expr);
	}

	public void replaceRhs(Expression expr) {
		this.rhs = Optional.of(expr);
	}

	public void replaceOp(Operator op) {
		this.operator = Optional.of(op);
	}

	/**
	 * Negates the logical expression located in this expression.
	 */
	public void negateLogic() {
		if (this.lhs.isPresent()) {
			this.lhs.get().negateLogic();
		}
		if (this.operator.isPresent()) {
			this.operator.get().negate();
		}
		if (this.rhs.isPresent()) {
			this.rhs.get().negateLogic();
		}
	}

	/**
	 * Generates a new expression which indicates that the handed over expression
	 * is currently not supported.
	 *
	 * @return a new expression with "not_supported" prefix.
	 */
	public Expression setNotSupported() {
		Expression ret = new Expression();
		ret.replaceRhs(this);
		Variable var = new Variable(LEMSCollector.helper.NOT_SUPPORTED);
		ret.replaceRhs(var);
		return ret;
	}

	/**
	 * Generates an expression which represent the logical value TRUE.
	 *
	 * @return TRUE expression
	 */
	public static Expression generateTrue() {
		//first the whole expression
		Expression ret = new Expression();
		//since both sides equal, it is sufficient to create a single object
		NumericalLiteral lhs_rhs = new NumericalLiteral(1, null);
		//connection between
		Operator op = new Operator();
		op.setEq(true);
		//now combine these elements
		ret.replaceLhs(lhs_rhs);
		ret.replaceRhs(lhs_rhs);
		ret.replaceOp(op);
		return ret;

	}

	/**
	 * Generates an expression which represent the logical value FALSE.
	 *
	 * @return FALSE expression.
	 */
	public static Expression generateFalse() {
		Expression ret = generateTrue();
		ret.negateLogic();
		return ret;
	}

	/**
	 * Encapsulates a given expression object in brackets. e.g V_m+10mV -> (V_m+10mV)
	 *
	 * @param expr the expression which will be encapsulated.
	 * @return the encapsulated expression.
	 */
	public static Expression encapsulateInBrackets(Expression expr) {
		Expression ret = new Expression();
		Operator op = new Operator();
		op.setLeftParentheses(true);
		op.setRightParentheses(true);
		ret.replaceOp(op);
		ret.replaceRhs(expr);
		return ret;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Expression that = (Expression) o;

		if (!lhs.equals(that.lhs))
			return false;
		if (!operator.equals(that.operator))
			return false;
		return rhs.equals(that.rhs);

	}

	@Override
	public int hashCode() {
		int result = lhs.hashCode();
		result = 31 * result + operator.hashCode();
		result = 31 * result + rhs.hashCode();
		return result;
	}

	public boolean containsNamedFunction(String funcName, List<Expression> args) {
		boolean contains = false;
		if (this instanceof Function &&
				((Function) this).getFunctionName().equals(funcName) &&
				equalArgs(((Function) this).getArguments(), args)) {
			return true;
		}
		if (this.rhs.isPresent() && this.rhs.get() instanceof Function &&
				((Function) this.rhs.get()).getFunctionName().equals(funcName) &&
				equalArgs(((Function) this.rhs.get()).getArguments(), args)) {
			return true;
		}
		if (this.lhs.isPresent() && this.lhs.get() instanceof Function &&
				((Function) this.lhs.get()).getFunctionName().equals(funcName) &&
				equalArgs(((Function) this.lhs.get()).getArguments(), args)) {
			return true;
		}
		if (this.rhs.isPresent()) {
			contains |= this.rhs.get().containsNamedFunction(funcName, args);
		}
		if (this.lhs.isPresent()) {
			contains |= this.lhs.get().containsNamedFunction(funcName, args);
		}
		return contains;
	}

	private boolean equalArgs(List<Expression> args1, List<Expression> args2) {
		if (args1 == null && args2 == null) {
			return true;
		}
		if (args1 == null ^ args2 == null || args1.size() != args2.size()) {
			return false;
		}
		for (int i = 0; i < args1.size(); i++) {
			if (!args1.get(i).equals(args2.get(i))) {
				return false;
			}
		}
		return true;
	}


	private void parseStringToExpression(String expressionAsString) {
		//TODO: write a parser+lexer for expression in string form
	}


}
