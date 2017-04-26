package org.nest.codegeneration.helpers.LEMSElements;

import org.eclipse.emf.ecore.xmi.impl.EMOFHandler;
import org.nest.codegeneration.helpers.Expressions.Expression;
import org.nest.codegeneration.helpers.Expressions.LEMSSyntaxContainer;
import org.nest.codegeneration.helpers.Expressions.NumericalLiteral;
import org.nest.codegeneration.helpers.Expressions.Variable;
import org.nest.symboltable.symbols.TypeSymbol;
import org.nest.symboltable.symbols.VariableSymbol;
import org.nest.units._ast.ASTUnitType;
import org.w3c.dom.Node;

/**
 * This class represents a static non-alias element of the modeled neuron derived from the parameter or the internal
 * block located in the source NESTML model.
 *
 * @author perun
 */
public class Constant {
	private String name;/*The name of the constant, e.g "V_ref" */
	private String dimension;/* Name of the dimension of the constant, e.g "voltage"*/
	private Expression value;
	private boolean parameter = false;//indicates whether this constant is a parameter or a real constant

	/**
	 * This constructor can be used to create a new Constant object representing either a constant or a parameter.
	 *
	 * @param variable  a symbol from which a constant will be created
	 * @param init      indicates whether this value represents a init value of variable or not
	 * @param par       indicates whether it is a parameter or not
	 * @param container the container in which the new object will be stored and whose prettyPrinter shall be used
	 */

	public Constant(VariableSymbol variable, boolean init, boolean par, LEMSCollector container) {
		this.name = variable.getName();
		this.dimension = HelperCollection.typeToDimensionConverter(variable.getType());
		this.dimension = HelperCollection.dimensionFormatter(dimension);//format the dimension to make it LEMS readable
		this.parameter = par;
		if (init) {
			this.name = HelperCollection.PREFIX_INIT + this.name;//init values are extended by a label in order to indicate as such
		}
		if (!parameter) {//a parameter does not have a unit or a value, thus should not be checked
			//if a declaring expression is present, convert it
			if (variable.getDeclaringExpression().isPresent()) {
				//check whether the variable has a function call
				if (variable.getDeclaringExpression().get().functionCallIsPresent()) {
					this.value = this.processFunctionCallInConstantDefinition(variable, container);
				} else {
					this.value = new Expression(variable);
					HelperCollection.retrieveUnitsFromExpression(this.value,container);
				}
			} else {//otherwise this is an initialization, thus create a new numerical literal or variable
				if (variable.getType().getType() == TypeSymbol.Type.UNIT) {// in case a unit is used, we have to create num
					/*
					ASTUnitType tempType = new ASTUnitType();
					tempType.setUnit(variable.getType().prettyPrint());
					NumericalLiteral literal = new NumericalLiteral(0, tempType);*/
					this.value = new Expression(variable.getDeclaringExpression().get());
				} else {//var does not have unit, a variable with value 0 is sufficient
					this.value = new NumericalLiteral(0, null);
				}
			}
		}

		//store an adequate message if the data type is not supported
		if (this.dimension.equals(HelperCollection.NOT_SUPPORTED)) {
			HelperCollection.printNotSupportedDataType(variable, container);
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
			System.err.println("Constant artifact/xmlNode wrongly formatted.");
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
		this.dimension = HelperCollection.formatComplexUnit(dimension);
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
		if (value.getClass().equals(NumericalLiteral.class) && ((NumericalLiteral) value).hasType()) {
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

	/**
	 * Processes a given function call and returns, if the corresponding function call is supported, a corresponding
	 * expression.
	 * @param variable a variable containing a function call
	 * @param container a LEMSCollector for storage of error messages
	 * @return a new expression
	 */
	private Expression processFunctionCallInConstantDefinition(VariableSymbol variable, LEMSCollector container) {
		if (variable.getDeclaringExpression().get().getFunctionCall().get().getCalleeName().equals("resolution")) {
			ASTUnitType tempType = new ASTUnitType();
			tempType.setUnit(container.getConfig().getSimulation_steps_unit().getSymbol());
			return new NumericalLiteral(container.getConfig().getSimulation_steps_length(), tempType);
		} else {
			HelperCollection.printNotSupportedFunctionCallInExpression(variable,container);
			return new Variable(HelperCollection.NOT_SUPPORTED
					+ ":" + variable.getDeclaringExpression().get().getFunctionCall().get().getName().toString());
		}
	}
}