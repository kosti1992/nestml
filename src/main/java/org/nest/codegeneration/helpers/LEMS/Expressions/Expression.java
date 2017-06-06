package org.nest.codegeneration.helpers.LEMS.Expressions;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.nest.codegeneration.helpers.LEMS.Elements.HelperCollection;
import org.nest.codegeneration.helpers.LEMS.helpers.EitherTuple;
import org.nest.codegeneration.helpers.Names;
import org.nest.commons._ast.ASTExpr;
import org.nest.spl.symboltable.typechecking.Either;
import org.nest.symboltable.symbols.VariableSymbol;

/**
 * This class represents an internal representation of an expression, e.g
 * 10mV + V_reset .
 *
 * @author perun
 */
public class Expression {
	private Optional<Expression> mLhs = Optional.empty();
	private Optional<Operator> mOperator = Optional.empty();
	private Optional<Expression> mRhs = Optional.empty();

	//TODO: write a lexer+parser to generate from string
	public Expression(String _value) {
		this.mRhs = Optional.of(new Variable(_value));
	}

	public Expression() {
	}

	public Expression(ASTExpr _expr) {
		checkNotNull(_expr);
		this.handleExpression(_expr);
	}

	public Expression(VariableSymbol _expr) {
		checkNotNull(_expr);
		if (_expr.getDeclaringExpression().isPresent()) {
			this.handleExpression(_expr.getDeclaringExpression().get());
		}
	}

	/**
	 * The main routine which deals with the correct creation of an internal representation of
	 * an expression.
	 *
	 * @param _expr The expression which shall be transformed to the internal representation.
	 *             Caution: the AST expression is not changed.
	 */
	private void handleExpression(ASTExpr _expr) {
		if (_expr.getTerm().isPresent()) {
			this.mOperator = Optional.of(new Operator(_expr));
			this.mRhs = Optional.of(new Expression(_expr.getTerm().get()));
		} else if (_expr.numericLiteralIsPresent()&&_expr.getType().isValue()) {
			this.mRhs = Optional.of(new NumericLiteral(_expr.getNumericLiteral().get()));
		} else if (_expr.variableIsPresent()) {
		    if(_expr.getVariable().get().getDifferentialOrder().size()>0){
		        this.mRhs = Optional.of(new Variable(Names.convertToCPPName(_expr.getVariable().get().toString()),_expr.getType().getValue()));
            }else{
                this.mRhs = Optional.of(new Variable(HelperCollection.resolveVariableSymbol(_expr).get()));
            }
		} else if (_expr.functionCallIsPresent()) {
			this.mRhs = Optional.of(new Function(_expr.getFunctionCall().get()));
		} else if (_expr.exprIsPresent()) {
			this.mOperator = Optional.of(new Operator(_expr));
			this.mRhs = Optional.of(new Expression(_expr.getExpr().get()));
		} else if (_expr.baseIsPresent() && _expr.exponentIsPresent()) {
			this.mLhs = Optional.of(new Expression(_expr.getBase().get()));
			this.mRhs = Optional.of(new Expression(_expr.getExponent().get()));
			Operator tempOperator = new Operator();
			tempOperator.setPower(true);
			this.mOperator = Optional.of(tempOperator);
		} else if (_expr.booleanLiteralIsPresent()) {
			if (_expr.getBooleanLiteral().get().getValue()) {
				this.mRhs = Optional.of(new NumericLiteral(1));
			} else {
				this.mRhs = Optional.of(new NumericLiteral(0));
			}
		} else {
			if (_expr.leftIsPresent()) {//check if the left hand side is a single boolean atom, e.g.: true and 1<2
				if (_expr.getLeft().get().booleanLiteralIsPresent()) {
					if (_expr.getLeft().get().getBooleanLiteral().get().getValue()) {
						this.mLhs = Optional.of(Expression.generateTrue());
					} else {
						this.mLhs = Optional.of(Expression.generateFalse());
					}
				} else {
					this.mLhs = Optional.of(new Expression(_expr.getLeft().get()));
				}
			}
			this.mOperator = Optional.of(new Operator(_expr));
			if (_expr.rightIsPresent()) {//check if the right hand side is a single boolean atom, e.g.: 1<2 and true
				if (_expr.getRight().get().booleanLiteralIsPresent()) {
					//if it is a boolean atom, generate true, respectively false according to the model
					if (_expr.getRight().get().getBooleanLiteral().get().getValue()) {
						this.mRhs = Optional.of(Expression.generateTrue());
					} else {
						this.mRhs = Optional.of(Expression.generateFalse());
					}
				} else {
					this.mRhs = Optional.of(new Expression(_expr.getRight().get()));
				}
			}
			if (_expr.isLogicalAnd() || _expr.isLogicalOr() || _expr.isLogicalNot()) {
				this.mLhs = Optional.of(Expression.encapsulateInBrackets(this.mLhs.get()));
				this.mRhs = Optional.of(Expression.encapsulateInBrackets(this.mRhs.get()));
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
		if (this.mOperator.isPresent()) {
			resOps.add(this.mOperator.get());
		}
		if (this.mLhs.isPresent()) {
			resOps.addAll(mLhs.get().getOperators());
		}
		if (this.mRhs.isPresent()) {
			resOps.addAll(mRhs.get().getOperators());
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
		if (this.mLhs.isPresent()) {
			resFunc.addAll(mLhs.get().getFunctions());
		}
		if (this.mRhs.isPresent()) {
			resFunc.addAll(mRhs.get().getFunctions());
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
		if (this.mLhs.isPresent()) {
			resVars.addAll(mLhs.get().getVariables());
		}
		if (this.mRhs.isPresent()) {
			resVars.addAll(mRhs.get().getVariables());
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
		if (this.getClass().equals(NumericLiteral.class)) {
			resNums.add(this);
		}
		if (this.mLhs.isPresent()) {
			resNums.addAll(mLhs.get().getNumericals());
		}
		if (this.mRhs.isPresent()) {
			resNums.addAll(mRhs.get().getNumericals());
		}
		return resNums;
	}


	/**
	 * Prints a String representation of the stored expression. The syntax is
	 * of the corresponding elements is determined by the handed over SyntaxContainer
	 * object.
	 *
	 * @param _container determines which syntax shall be used.
	 * @return a string representation of the expression
	 */
	public String print(SyntaxContainer _container) {
		String ret = "";
		//This is a special case, since brackets have to be around the whole expr.
		if (this.mOperator.isPresent() && this.mOperator.get().isLeftParentheses() &&
				this.mOperator.get().isRightParentheses() && this.mRhs.isPresent()) {
			ret = "(" + this.mRhs.get().print(_container) + ")";
		} else {
			if (this.mLhs.isPresent()) {
				ret = ret + this.mLhs.get().print(_container);
			}
			if (this.mOperator.isPresent()) {
				ret = ret + this.mOperator.get().print(_container);
			}
			if (this.mRhs.isPresent()) {
				ret = ret + this.mRhs.get().print(_container);
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
	 * @param _pre  the expression which will be replaced
	 * @param _post the expression which replaces
	 */
	public void replaceElement(Expression _pre, Expression _post) {
		if (this.mLhs.isPresent() && this.mLhs.get().equals(_pre)) {
			this.mLhs = Optional.of(_post);
		}
		if (this.mOperator.isPresent() && this.mOperator.get().equals(_pre)) {
			this.mOperator = Optional.of((Operator) _post);
		}
		if (this.mRhs.isPresent() && this.mRhs.get().equals(_pre)) {
			this.mRhs = Optional.of(_post);
		}
		if (this.mLhs.isPresent()) {
			this.mLhs.get().replaceElement(_pre, _post);
		}
		if (this.mRhs.isPresent()) {
			this.mRhs.get().replaceElement(_pre, _post);
		}
	}

	public void replaceLhs(Expression _expression) {
		this.mLhs = Optional.of(_expression);
	}

	public void replaceRhs(Expression _expression) {
		this.mRhs = Optional.of(_expression);
	}

	public void replaceOp(Operator _operator) {
		this.mOperator = Optional.of(_operator);
	}

	/**
	 * Negates the logical expression located in this expression.
	 */
	public void negateLogic() {
		if (this.mLhs.isPresent()) {
			this.mLhs.get().negateLogic();
		}
		if (this.mOperator.isPresent()) {
			this.mOperator.get().negate();
		}
		if (this.mRhs.isPresent()) {
			this.mRhs.get().negateLogic();
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
		Variable var = new Variable(HelperCollection.NOT_SUPPORTED);
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
		NumericLiteral lhs_rhs = new NumericLiteral(1);
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
	 * @param _expression the expression which will be encapsulated.
	 * @return the encapsulated expression.
	 */
	public static Expression encapsulateInBrackets(Expression _expression) {
		Expression ret = new Expression();
		Operator op = new Operator();
		op.setLeftParentheses(true);
		op.setRightParentheses(true);
		ret.replaceOp(op);
		ret.replaceRhs(_expression);
		return ret;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Expression that = (Expression) o;

		if (!mLhs.equals(that.mLhs))
			return false;
		if (!mOperator.equals(that.mOperator))
			return false;
		return mRhs.equals(that.mRhs);

	}

	@Override
	public int hashCode() {
		int result = mLhs.hashCode();
		result = 31 * result + mOperator.hashCode();
		result = 31 * result + mRhs.hashCode();
		return result;
	}

	public boolean containsNamedFunction(String funcName, List<Expression> args) {
		boolean contains = false;
		if (this instanceof Function &&
				((Function) this).getFunctionName().equals(funcName) &&
				equalArgs(((Function) this).getArguments(), args)) {
			return true;
		}
		if (this.mRhs.isPresent() && this.mRhs.get() instanceof Function &&
				((Function) this.mRhs.get()).getFunctionName().equals(funcName) &&
				equalArgs(((Function) this.mRhs.get()).getArguments(), args)) {
			return true;
		}
		if (this.mLhs.isPresent() && this.mLhs.get() instanceof Function &&
				((Function) this.mLhs.get()).getFunctionName().equals(funcName) &&
				equalArgs(((Function) this.mLhs.get()).getArguments(), args)) {
			return true;
		}
		if (this.mRhs.isPresent()) {
			contains |= this.mRhs.get().containsNamedFunction(funcName, args);
		}
		if (this.mLhs.isPresent()) {
			contains |= this.mLhs.get().containsNamedFunction(funcName, args);
		}
		return contains;
	}

	private boolean equalArgs(List<Expression> _args1, List<Expression> _args2) {
		if (_args1 == null && _args2 == null) {
			return true;
		}
		if (_args1 == null ^ _args2 == null || _args1.size() != _args2.size()) {
			return false;
		}
		for (int i = 0; i < _args1.size(); i++) {
			if (!_args1.get(i).equals(_args2.get(i))) {
				return false;
			}
		}
		return true;
	}

	public Optional<Expression> getLhs() {
		return mLhs;
	}

	public Optional<Operator> getOperator() {
		return mOperator;
	}

	public Optional<Expression> getRhs() {
		return mRhs;
	}

	public boolean lhsIsPresent() {
		return this.mLhs.isPresent();
	}

	public boolean rhsIsPresent() {
		return this.mRhs.isPresent();
	}

	public boolean opIsPresent() {
		return this.mOperator.isPresent();
	}

	/**
	 * This is a deepClone method which generates a clone of this object whenever required, e.g. when it has to be
	 * mirrored to other parts of the expression tree.
	 *
	 * @return a deep clone of this
	 */
	public Expression deepClone() {
		Expression ret = new Expression();
		if (this.lhsIsPresent()) {
			ret.replaceLhs(this.getLhs().get().deepClone());
		}
		if (this.opIsPresent()) {
			ret.replaceOp(this.getOperator().get().deepClone());
		}
		if (this.rhsIsPresent()) {
			ret.replaceRhs(this.getRhs().get().deepClone());
		}
		return ret;
	}

	public boolean isEmpty() {
		return !this.lhsIsPresent() && !this.opIsPresent() && !this.rhsIsPresent();
	}

	public boolean isExpression() {
		return this.lhsIsPresent() && this.opIsPresent() && this.rhsIsPresent();
	}

	/**
	 * For a given expression, this method checks if it is a logical expression an if so, if it contains a
	 * sub expression which ist not a combination of: (LIT|VAR|EXPR) OP (LIT|VAR|EXPR). An expression which is
	 * not of this form is: 10mV >=V_m >= 0mV and should be transformed 10mV >= V_m and V_m >= 0mV
	 *
	 * @return
	 */
	//TODO: complete this method
	public Expression splitExpression(Expression _expr) {
		//first check if it is a EXPR OP EXPR form. a>b>c>d is hereby constructed as a left deep tree
		if (_expr.opIsPresent() && _expr.getOperator().get().isRelationalOperator()) {

		}
		return null;//TODO
	}

	/**
	 * Returns the the most right bottom, right node in the left subtree.
	 * @param _expr an expression representing an expression tree.
	 * @return the right most bottom node in the left sub tree of expression
	 *///TODO complete this method
	private Expression getLeftBottomNode(Expression _expr){
		if(_expr.lhsIsPresent()&&!_expr.getLhs().get().opIsPresent()){
			return _expr.getLhs().get();
		}
		else{
			return getLeftBottomNode(_expr.getLhs().get());
		}
	}

	private void parseStringToExpression(String expressionAsString) {
		//TODO: write a parser+lexer for expression in string form
	}


}
