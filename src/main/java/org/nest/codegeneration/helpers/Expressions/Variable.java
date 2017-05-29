package org.nest.codegeneration.helpers.Expressions;


import org.nest.symboltable.symbols.TypeSymbol;
import org.nest.symboltable.symbols.VariableSymbol;

import java.util.Optional;

/**
 * This class is used to store variables of an expression.
 *
 * @author perun
 */
public class Variable extends Expression {
	private String mVariable;
	private Optional<TypeSymbol> mType;

	public Variable(VariableSymbol variable){
	    this.mVariable = variable.getName();
	    this.mType = Optional.of(variable.getType());
    }

	public Variable(String _variableName,TypeSymbol _typeSymbol) {
		this.mVariable = _variableName;
		this.mType = Optional.of(_typeSymbol);
	}

	public Variable(String _variableName){
	    this.mVariable = _variableName;
	    this.mType = Optional.empty();
    }

	public String getVariable() {
		return mVariable;
	}

	public void setVariable(String variable) {
		this.mVariable = variable;
	}

	public String print(SyntaxContainer container) {
		return container.print(this);
	}

    public TypeSymbol getType() {
        return mType.get();
    }

    public boolean typeIsPresent(){
	    return mType.isPresent();
    }

    @Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		Variable variable1 = (Variable) o;

		if (!mVariable.equals(variable1.mVariable)) return false;
		return mType.equals(variable1.mType);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + mVariable.hashCode();
		result = 31 * result + mType.hashCode();
		return result;
	}

	/**
	 * This is a deepClone method which generates a clone of this object whenever required, e.g. when it has to be
	 * mirrored to other parts of the expression tree.
	 *
	 * @return a deep clone of this
	 */
	public Variable deepClone() {
	    if(mType.isPresent()){
            return new Variable(this.mVariable,mType.get());
        }else{
	        return new Variable(this.mVariable);
        }
	}
}
