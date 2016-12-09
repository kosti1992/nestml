package org.nest.codegeneration.helpers.LEMSElements;

import java.util.Optional;

import org.nest.codegeneration.helpers.Expressions.Expression;
import org.nest.codegeneration.helpers.Expressions.LEMSSyntaxContainer;
import org.nest.codegeneration.helpers.Expressions.NumericalLiteral;
import org.nest.codegeneration.helpers.Expressions.Variable;
import org.nest.symboltable.symbols.VariableSymbol;
import org.w3c.dom.Node;

/**
 * This class represents a state-variable, a characteristic of the modeled neuron which possibly changes over the time.
 *
 * @author perun
 */
public class StateVariable {
	private String name;
	private String dimension;
	//all state variables have to be initialized with 0 if not other requested
	private Optional<Expression> defaultValue = Optional.empty();
	private String unit;

	public StateVariable(VariableSymbol variable, LEMSCollector container) {
		this.name = variable.getName();

		//check whether data type is supported or not
		if (LEMSCollector.helper.dataTypeNotSupported(variable.getType())) {
			this.dimension = "not_supported";
			container.getHelper().printNotSupportedDataType(variable);
		} else {
			this.dimension = container.getHelper().typeToDimensionConverter(variable.getType());
		}

		//check if a standard value is set and an actual dimension is used
		if ((!this.dimension.equals("none") && !this.dimension.equals("not_supported")) && variable.getDeclaringExpression().isPresent()) {
			//in case it is a reference just set it as it is
			if (variable.getDeclaringExpression().get().variableIsPresent()) {
				this.defaultValue = Optional.of(new Variable(variable.getDeclaringExpression().get().getVariable().get()));
				//this.defaultValue = variable.getDeclaringExpression().get().getVariable().get().toString();
			}
	  /*
      * now otherwise, if it is a single value, e.g. 10mV, generate a constant and set
      * reference to it.
      */
			else if (variable.getDeclaringExpression().get().getNESTMLNumericLiteral().isPresent()) {
				Constant temp = new Constant(variable, true, false, container);
				container.addConstant(temp);
				this.defaultValue = Optional.of(new Variable(temp.getName()));
			}
      /*
       *it is not a single reference or a single value, therefore have to be an expression
       */
			else {
				DerivedElement temp = new DerivedElement(variable, container, true, true);
				container.addDerivedElement(temp);
				this.defaultValue = Optional.of(new Variable(temp.getName()));
			}
		} else if (variable.getDeclaringExpression().isPresent()) {
			//otherwise just copy the value, but first replace the constants with references
			Expression tempExpression = new Expression(variable.getDeclaringExpression().get());
			tempExpression=LEMSCollector.helper.replaceConstantsWithReferences(container,tempExpression);
			this.defaultValue = Optional.of(tempExpression);
		}
		if (!this.dimension.equals("none") && !this.dimension.equals("not_supported")) {
			//state variables are often generated outside the main routine, thus a processing of units has to be triggered
			this.unit = (new Unit(variable.getType())).getSymbol();
		}
	}

	public StateVariable(Node xmlNode) {
		this.name = xmlNode.getAttributes().getNamedItem("name").getNodeValue();
		this.dimension = xmlNode.getAttributes().getNamedItem("dimension").getNodeValue();
	}


	/**
	 * This method can be used to generate handmade state variables. Caution:
	 * the integrity of such values is not assured.
	 *
	 * @param name         the name of the new variable
	 * @param dimension    the dimension as a string
	 * @param defaultValue the default value as a string
	 * @param unit         the unit as a string
	 * @param container    the container in order to ensure a correct converting.
	 */
	public StateVariable(String name, String dimension, Expression defaultValue,
	                     String unit, LEMSCollector container) {
		this.name = name;
		this.dimension = dimension;
		this.defaultValue = Optional.of(defaultValue);
		//this.defaultValue = container.getHelper().replaceConstantsWithReferences(container,defaultValue);
		this.unit = unit;

	}

	@SuppressWarnings("unused")//used in the template
	public String print() {
		if (defaultValue.isPresent()) {
			return defaultValue.get().print(new LEMSSyntaxContainer());
		}
		return "0";
	}

	public Optional<Expression> getDefaultValue() {
		return this.defaultValue;
	}

	public void setDefaultValue(Expression value) {
		this.defaultValue = Optional.of(value);
	}

	@SuppressWarnings("unused")//used in the template
	public String getName() {
		return this.name;
	}

	@SuppressWarnings("unused")//used in the template
	public String getDimension() {
		return this.dimension;
	}

	public String getUnit() {
		return this.unit;
	}

	public boolean equals(Object o) {
		//it is sufficient to only check the name
		return this.getClass().equals(o.getClass()) && this.getName().equals(((StateVariable) o).getName());
	}

	public int hashCode() {
		return this.getName().hashCode() + this.dimension.hashCode() + this.defaultValue.hashCode() + this.unit.hashCode();
	}

}