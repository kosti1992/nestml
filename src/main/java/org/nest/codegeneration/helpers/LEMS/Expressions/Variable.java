package org.nest.codegeneration.helpers.LEMS.Expressions;


import org.nest.codegeneration.helpers.LEMS.Elements.HelperCollection;
import org.nest.symboltable.symbols.TypeSymbol;
import org.nest.symboltable.symbols.VariableSymbol;
import org.nest.units._ast.ASTUnitType;

import java.util.Optional;

/**
 * This class is used to store variables of an expression.
 *
 * @author perun
 */
public class Variable extends Expression {
	private String mVariable;
	private Optional<TypeSymbol> mType;
	private boolean mIsImplicitUnit = false;


    /**
     * This constructor can be used to generate variable symbols. It checks if the handed over variable is a predefined
     * and sets the corresponding argument.
     * @param variable a variable symbol object
     */
	public Variable(VariableSymbol variable){
	    this.mVariable = HelperCollection.dimensionFormatter(variable.getName());
	    this.mType = Optional.of(variable.getType());
	    if(variable.isPredefined()){
	    	mIsImplicitUnit = true;
		}
    }

    /**
     * This constructor can be used to generate a variable symbol with a concretely handed over type symbol.
     * @param _variableName the name of the new variable
     * @param _typeSymbol the type of the variable
     */
	public Variable(String _variableName,TypeSymbol _typeSymbol) {
		this.mVariable = HelperCollection.dimensionFormatter(_variableName);
		this.mType = Optional.of(_typeSymbol);
		this.mIsImplicitUnit = false;
	}

    /**
     * This constructor can be used to generate variables which consist only of a name.
     * @param _variableName the name of the new variable symbol
     */
	public Variable(String _variableName){
	    this.mVariable = _variableName;
	    this.mType = Optional.empty();
	    this.mIsImplicitUnit = false;
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
            return new Variable(this.mVariable,this.mType.get());
        }else{
	        return new Variable(this.mVariable);
        }
	}

	public boolean isImplicitUnit() {
		return mIsImplicitUnit;
	}
}
