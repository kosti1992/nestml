package org.nest.codegeneration.helpers.Expressions;


import java.util.Optional;
import org.nest.commons._ast.ASTVariable;
import org.nest.symboltable.symbols.TypeSymbol;
import org.nest.symboltable.symbols.VariableSymbol;

/**
 * This class is used to store variables of an expression.
 *
 * @author perun
 */
public class Variable extends Expression {
	private String variable;
	private TypeSymbol mType;

	public Variable(ASTVariable variable) {
		this.variable = variable.getName().toString();
	}

	public Variable(VariableSymbol variable){
	    this.variable = variable.getName();
	    this.mType = variable.getType();
    }

	public Variable(String variable) {
		this.variable = variable;
	}

	public String getVariable() {
		return variable;
	}

	public void setVariable(String variable) {
		this.variable = variable;
	}

	public String print(SyntaxContainer container) {
		return container.print(this);
	}

	public int hashCode() {
		return this.variable.hashCode();
	}

	public boolean equals(Object obj) {
		return obj.getClass().equals(this.getClass()) && ((Variable) obj).getVariable().equals(this.variable);
	}

	/**
	 * This is a deepClone method which generates a clone of this object whenever required, e.g. when it has to be
	 * mirrored to other parts of the expression tree.
	 *
	 * @return a deep clone of this
	 */
	public Variable deepClone() {
		return new Variable(this.variable);
	}
}
