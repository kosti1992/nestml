package org.nest.codegeneration.helpers.LEMSElements;

import org.nest.codegeneration.helpers.Expressions.Expression;
import org.nest.codegeneration.helpers.Expressions.LEMSSyntaxContainer;
import org.nest.codegeneration.helpers.Expressions.NumericalLiteral;
import org.nest.symboltable.symbols.VariableSymbol;
import org.w3c.dom.Node;

/**
 * This class represents a static non-alias element of the modeled neuron derived from the parameter or the internal
 * block located in the source NESTML model.
 *
 * @author perun
 */
public class Constant {
	private String name;
	/**
	 * The name of the constant, e.g "V_ref"
	 */
	private String dimension;
	/**
	 * Name of the dimension of the constant, e.g "voltage"
	 */
	private Expression value;
	private boolean parameter = false;

	public Constant(VariableSymbol variable, boolean init, boolean par, LEMSCollector container) {
		this.name = variable.getName();
		this.dimension = container.getHelper().typeToDimensionConverter(variable.getType());
		this.parameter = par;
		if (init) {
			this.name = "INIT" + this.name;//init values are extended by a label in order to indicate as such
		}
		if (!parameter) {//a parameter does not have a unit or a value, thus should not be checked
			if (variable.getDeclaringExpression().isPresent()) {
				this.value = new Expression(variable);
			}
			if (!parameter && this.dimension.equals("not_supported")) {
				//store an adequate message if the data type is not supported
				container.getHelper().printNotSupportedDataType(variable);
			}
			//check whether the variable has a function call
			//TODO:this case should be dealt with at a different please
			if (variable.getDeclaringExpression().get().functionCallIsPresent()) {
				container.getHelper().printNotSupportedDataType(variable);
			}
		}
	}

	/**
	 * This constructor can be used to generate a Constant from an external artifact.
	 *
	 * @param xmlNode an XML node.
	 */
	public Constant(Node xmlNode) {
		try {
			this.name = xmlNode.getAttributes().getNamedItem("name").getNodeValue();
			this.dimension = xmlNode.getAttributes().getNamedItem("dimension").getNodeValue();
			if (xmlNode.getNodeName().equals("Parameter")) {
				this.parameter = true;
			} else {
				this.parameter = false;
				this.value = new Expression(xmlNode.getAttributes().getNamedItem("value").getNodeValue());
			}
		} catch (Exception e) {
			System.err.println("Constant artifact wrongly formatted.");
		}
	}

	/**
	 * This constructor is used to generate handmade constants if required.
	 *
	 * @param name      the name of the new constant
	 * @param dimension the dimension
	 * @param value     the value of the concrete constant
	 */
	public Constant(String name, String dimension, Expression value, boolean par) {
		this.name = name;
		this.dimension = dimension;
		this.value = value;
		this.parameter = par;
	}

	@SuppressWarnings("unused")//used in the template
	public String getName() {
		return this.name;
	}

	@SuppressWarnings("unused")//used in the template
	public String getDimension() {
		return this.dimension;
	}

	@SuppressWarnings("unused")//used in the template
	public Expression getValue() {
		return this.value;
	}

	@SuppressWarnings("unused")//used in the template
	public String getUnit() {
		if (value.getClass().equals(NumericalLiteral.class)&&((NumericalLiteral) value).hasType()) {
			return ((NumericalLiteral) value).getType().get().getUnit().get();
		}
		return "";
	}

	/**
	 * Returns the the value of the this constant. If
	 * a concrete numerical value is utilized, the unit is appended to the value.
	 *
	 * @return Value as String.
	 */
	@SuppressWarnings("unused")//used in the template
	public String getValueUnit() {
		return this.value.print(new LEMSSyntaxContainer());
	/*
    if(!this.unit.equals("")){
      return this.value+this.unit;
    }
    else{
      return this.value.print(new LEMSSyntaxContainer());
    }*/
	}

	public boolean isParameter() {
		return this.parameter;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Constant constant = (Constant) o;

		if (parameter != constant.parameter)
			return false;
		if (!name.equals(constant.name))
			return false;
		if (!dimension.equals(constant.dimension))
			return false;
		if (this.value == null && constant.value == null) {
			return true;
		}
		return value.equals(constant.value);

	}

	@Override
	public int hashCode() {
		int result = name.hashCode();
		result = 31 * result + dimension.hashCode();
		result = 31 * result + value.hashCode();
		result = 31 * result + (parameter ? 1 : 0);
		return result;
	}
}