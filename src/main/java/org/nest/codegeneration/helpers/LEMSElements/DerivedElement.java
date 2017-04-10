package org.nest.codegeneration.helpers.LEMSElements;

import org.nest.codegeneration.helpers.Expressions.Expression;
import org.nest.codegeneration.helpers.Expressions.Operator;
import org.nest.symboltable.symbols.VariableSymbol;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This class represents a derived element, a characteristic of the model which possibly
 * changes over the time and is derived from other elements of the model.
 *
 * @author perun
 */
public class DerivedElement {
	private String name;
	private String dimension;
	private Expression derivationInstruction;
	private boolean dynamic;//distinguish between derived variables and derived parameter
	private boolean external;//uses an external source, i.e. other tags are used
	private String reduce;//the reduce indicates (in the case of external vars) how to combine values to a single one
	private Optional<Map<Expression, Expression>> conditionalDerivedValues;


	public DerivedElement(VariableSymbol variable, LEMSCollector container, boolean dynamic, boolean init) {
		if (init||variable.getDeclaringExpression().get().conditionIsPresent()) {
			this.name = container.getHelper().PREFIX_INIT + variable.getName();
		} else {
			this.name = variable.getName();
		}
		this.dynamic = dynamic;
		//data types boolean and void are not supported by lems
		if (container.getHelper().dataTypeNotSupported(variable.getType())) {
			dimension = LEMSCollector.helper.NOT_SUPPORTED;
			//print an adequate error message
			container.getHelper().printNotSupportedDataType(variable);
		} else {
			dimension = container.getHelper().typeToDimensionConverter(variable.getType());
		}
		if(!variable.getDeclaringExpression().get().conditionIsPresent()) {
			//get the derivation instruction in LEMS format
			derivationInstruction = new Expression(variable);
			//replace the resolution function call with a reference to the constant
			derivationInstruction = container.getHelper().replaceResolutionByConstantReference(container, derivationInstruction);
			//replace constants with references
			derivationInstruction = container.getHelper().replaceConstantsWithReferences(container, derivationInstruction);
		}else {
			handleTernaryOp(variable,container);
		}
	}



	/**
	 * This constructor can be used to generate a hand made derived element.
	 *
	 * @param name                  the name of the new derived element
	 * @param dimension             the dimension of the new derived element
	 * @param derivationInstruction the instructions for derivation
	 * @param dynamic               true, if derived element contains changeable elements
	 */
	public DerivedElement(String name, String dimension,
	                      Expression derivationInstruction, boolean dynamic, boolean external) {
		this.name = name;
		this.dimension = dimension;
		this.derivationInstruction = derivationInstruction;
		this.dynamic = dynamic;
		this.external = external;
	}

	/**
	 * This method can be used to generate a new DerivedElement from a given XML node.
	 *
	 * @param xmlNode an DerivedElement xml node
	 */
	public DerivedElement(Node xmlNode) {
		NamedNodeMap tempMap = xmlNode.getAttributes();
		for (int i = 0; i < tempMap.getLength(); i++) {
			if (tempMap.item(i).getNodeName().equals("name")) {
				this.name = tempMap.item(i).getNodeValue();
			} else if (tempMap.item(i).getNodeName().equals("dimension")) {
				this.dimension = tempMap.item(i).getNodeValue();
			} else if (tempMap.item(i).getNodeName().equals("value")) {
				this.derivationInstruction = new Expression(tempMap.item(i).getNodeValue());
				this.external = false;
			} else if (tempMap.item(i).getNodeName().equals("select")) {
				this.derivationInstruction = new Expression(tempMap.item(i).getNodeValue());
			} else if (tempMap.item(i).getNodeName().equals("reduce")) {
				this.external = true;
				this.reduce = tempMap.item(i).getNodeValue();
			}
		}
		if (xmlNode.getNodeName().equals("DerivedParameter")) {
			this.dynamic = false;
		} else {
			this.dynamic = true;
		}
	}


	/**
	 * This method can be used to generate a conditional derived variable representing a ternary operator.
	 *
	 * @param variable  the variable which shall be a conditional derived var
	 * @param container a lems collector file
	 */
	private void handleTernaryOp(VariableSymbol variable, LEMSCollector container) {
		Map<Expression,Expression> tempMap = new HashMap<>();
		//first create the first part of the expression, namely the one which applies if condition is true
		Expression firstSubCondition = new Expression(variable.getDeclaringExpression().get().getCondition().get());
		firstSubCondition = Expression.encapsulateInBrackets(firstSubCondition);
		Expression firstSubValue = new Expression(variable.getDeclaringExpression().get().getIfTrue().get());
		firstSubValue = container.getHelper().replaceConstantsWithReferences(container,firstSubValue);
		firstSubValue = container.getHelper().replaceResolutionByConstantReference(container,firstSubValue);
		tempMap.put(firstSubCondition,firstSubValue);
		//now create the second part which applies if the condition is not true
		Expression secondSubCondition = firstSubCondition.deepClone();
		secondSubCondition.negateLogic();
		Expression secondSubValue = new Expression(variable.getDeclaringExpression().get().getIfNot().get());
		secondSubValue = container.getHelper().replaceConstantsWithReferences(container,secondSubValue);
		secondSubValue = container.getHelper().replaceResolutionByConstantReference(container,secondSubValue);
		tempMap.put(secondSubCondition,secondSubValue);
		this.conditionalDerivedValues = Optional.of(tempMap);
		this.dynamic = true;
	}


	@SuppressWarnings("unused")//used in the template
	public String getName() {
		return this.name;
	}

	@SuppressWarnings("unused")//used in the template
	public Expression getDerivationInstruction() {
		return this.derivationInstruction;
	}

	@SuppressWarnings("unused")//used in the template
	public String getDimension() {
		return this.dimension;
	}

	public boolean isDynamic() {
		return dynamic;
	}

	public boolean isExternal() {
		return external;
	}

	@SuppressWarnings("unused")//used in the template
	public boolean isConditionalDerived(){
		return this.conditionalDerivedValues.isPresent();
	}

	public Map<Expression,Expression> getConditionalDerivedValues(){
		if(this.conditionalDerivedValues.isPresent()){
			return this.conditionalDerivedValues.get();
		}else{
			return new HashMap<>();
		}
	}

	/**
	 * This method is required since the api of freemarker has been deactivated in monticore, thus a direct fetching of
	 * values by keys from maps is not possible.
	 * @return a string,string map with conditions,values
	 */
	public Map<String,String> getConditionalDerivedValuesAsStrings(){
		if(this.conditionalDerivedValues.isPresent()) {
			Map<String, String> tempMap = new HashMap<>();
			for (Expression key : this.conditionalDerivedValues.get().keySet()) {
				tempMap.put(key.print(),this.conditionalDerivedValues.get().get(key).print());
			}
			return tempMap;
		}
		else{
			return new HashMap<>();
		}
	}

	@SuppressWarnings("unused")//used in the template
	public String getReduce() {
		return this.reduce;
	}

	public boolean equals(Object o) {
		return this.getClass().equals(o.getClass()) &&
				this.name.equals(((DerivedElement) o).getName()) &&
				this.derivationInstruction.equals(((DerivedElement) o).derivationInstruction) &&
				this.dynamic == (((DerivedElement) o).dynamic);
	}

	public int hashCode() {
		if (this.dynamic) {
			return this.getClass().hashCode() +
					this.name.hashCode() +
					this.derivationInstruction.hashCode();
		}
		return -(this.getClass().hashCode() +
				this.name.hashCode() +
				this.derivationInstruction.hashCode());
	}
}

