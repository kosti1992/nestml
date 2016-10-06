package org.nest.codegeneration.helpers.LEMSElements;

import org.nest.codegeneration.helpers.LEMSCollector;
import org.nest.symboltable.symbols.VariableSymbol;

/**
 * This class represents a state-variable, a characteristic of the modeled neuron which possibly changes over the time.
 * @author perun
 */
public class StateVariable{
  private final String name;
  private final String dimension;
  private String defaultValue = "0";//all state variables have to be initialized with 0 if not other requested
  private String unit;

  public StateVariable(VariableSymbol variable, LEMSCollector container) {
    this.name = variable.getName();
    //check whether data type is supported or not
    if (LEMSCollector.helper.dataTypeNotSupported(variable.getType())) {
      this.dimension = "not_supported";
      container.getHelper().printNotSupportedDataType(variable);
    }
    else {
      this.dimension = container.getHelper().typeToDimensionConverter(variable.getType());
    }

    //check if a standard value is set and an actual dimension is used
    if (!this.dimension.equals("none") && !this.dimension.equals("not_supported") && variable.getDeclaringExpression().isPresent()) {
      //in case it is a reference just set it as it is
      if (variable.getDeclaringExpression().get().variableIsPresent()) {
        this.defaultValue = variable.getDeclaringExpression().get().getVariable().get().toString();
      }
      /*
      * now otherwise, if it is a single value, e.g. 10mV, generate a constant and set
      * reference to it.
      */
      else if (variable.getDeclaringExpression().get().getNESTMLNumericLiteral().isPresent()) {
        Constant temp = new Constant(variable, true, false, container);
        container.addConstant(temp);
        this.defaultValue = temp.getName();
      }
      /*
       *it is not a single reference or a single value, therefore have to be an expression
       */
      else {
        DerivedElement temp = new DerivedElement(variable, container, false, true);
        container.addDerivedElement(temp);
        this.defaultValue = temp.getName();
      }
    }
    else if (variable.getDeclaringExpression().isPresent()) {
      //otherwise just copy the value, but first replace the constants with references
      this.defaultValue = container.getHelper().replaceConstantsWithReferences(container,
          container.getLEMSExpressionsPrettyPrinter().print(variable.getDeclaringExpression().get(), false));
    }
    if (!this.dimension.equals("none") && !this.dimension.equals("not_supported")) {
      //state variables are often generated outside the main routine, thus a processing of units has to be triggered
      this.unit = (new Unit(variable.getType())).getSymbol();
    }
  }

  /**
   * This method can be used to generate handmade state variables. Caution:
   * the integrity of such values is not assured.
   * @param name the name of the new variable
   * @param dimension the dimension as a string
   * @param defaultValue the default value as a string
   * @param unit the unit as a string
   * @param container the container in order to ensure a correct converting.
   */
  public StateVariable(String name, String dimension, String defaultValue,
      String unit, LEMSCollector container) {
    this.name = name;
    this.dimension = dimension;
    this.defaultValue = container.getHelper().replaceConstantsWithReferences(container,defaultValue);
    this.unit = unit;

  }

  @SuppressWarnings("unused")//used in the template
  public String getDefaultValue() {
    return this.defaultValue;
  }

  public void setDefaultValue(String value){
    this.defaultValue = value;
  }

  @SuppressWarnings("unused")//used in the template
  public String getName() {
    return this.name;
  }

  @SuppressWarnings("unused")//used in the template
  public String getDimension() {
    return this.dimension;
  }

  public String getUnit(){
    return this.unit;
  }

  public boolean equals(Object o){
    //it is sufficient to only check the name
    return this.getClass().equals(o.getClass())&&this.getName().equals(((StateVariable)o).getName());
  }

  public int hashCode(){
    return this.getName().hashCode()+this.dimension.hashCode()+this.defaultValue.hashCode()+this.unit.hashCode();
  }

}