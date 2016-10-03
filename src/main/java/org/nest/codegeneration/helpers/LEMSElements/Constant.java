package org.nest.codegeneration.helpers.LEMSElements;

import org.nest.codegeneration.helpers.LEMSCollector;
import org.nest.symboltable.symbols.VariableSymbol;

/**
 * This class represents a static non-alias element of the modeled neuron derived from the parameter or the internal
 * block located in the source NESTML model.
 * @author  perun
 */
public class Constant{
  private String name;/**The name of the constant, e.g "V_ref" */
  private String dimension;/**Name of the dimension of the constant, e.g "voltage"*/
  private String value="0";/**A concrete value or a derivation instruction*/
  private String unit = "";/**if this.value consists solely of a single value, e.g 42, the unit have also to be stated*/
  private boolean parameter = false;

  public Constant(VariableSymbol variable, boolean init, boolean par, LEMSCollector container) {
    this.name = variable.getName();
    this.dimension = container.getHelper().typeToDimensionConverter(variable.getType());
    this.parameter = par;

    if (!parameter) {//a parameter does not have a unit or a value, thus should not be checked
      if (variable.getDeclaringExpression().isPresent()) {
        this.value = container.getLEMSExpressionsPrettyPrinter().print(variable.getDeclaringExpression().get(), false);
      }
      if (!parameter && this.dimension.equals("not_supported")) {
        //store an adequate message if the data type is not supported
        container.getHelper().printNotSupportedDataType(variable);
      }
      if (init) {
        this.name = "INIT" + this.name;//init values are extended by a label in order to indicate as such
        if (container.getHelper().dataTypeNotSupported(variable.getType())) {
          //TODO: maybe a different approach here?
          this.unit = "not_supported" + variable.getType().getName();
        }
        else {
          this.unit = variable.getType().prettyPrint();
        }
      }
      //check whether the variable has a function call
      //TODO:this case should be dealt with at a different please
      if (variable.getDeclaringExpression().get().functionCallIsPresent()) {
        container.getHelper().printNotSupportedDataType(variable);
      }
      //a unit is only required if it is not parameter and an actual unit
      if (!this.dimension.equals("none") && !this.dimension.equals("not_supported")) {
        this.unit = variable.getType().prettyPrint();
      }
      this.value = this.value.replaceAll(this.unit, "");//delete the unit if appended
    }
  }

  /**
   * This constructor is used to generate handmade constants if required.
   * @param name the name of the new constant
   * @param dimension the dimension
   * @param value the value of the concrete constant
   * @param unit the unit of the concrete constant
   */
  public Constant(String name,String dimension,String value,String unit,boolean par){
    this.name = name;
    this.dimension=dimension;
    this.value = value;
    this.unit = unit;
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
  public String getValue() {
    return this.value;
  }

  @SuppressWarnings("unused")//used in the template
  public String getUnit() {
    return this.unit;
  }

  /**
   * Returns the the value of the this constant. If
   * a concrete numerical value is utilized, the unit is appended to the value.
   * @return Value as String.
   */
  @SuppressWarnings("unused")//used in the template
  public String getValueUnit() {
    if(!this.unit.equals("")){
      return this.value+this.unit;
    }
    else{
      return this.value;
    }
  }

  public boolean isParameter(){
    return this.parameter;
  }

  @Override
  public boolean equals(Object obj) {
    return obj.getClass().equals(this.getClass())&&
    this.name.equals(((Constant)obj).getName())&&
    this.value.equals(((Constant)obj).getValue())&&
    this.dimension.equals(((Constant)obj).getDimension())&&
    this.unit.equals(((Constant)obj).getUnit());
  }

  @Override
  public int hashCode(){
    return this.name.hashCode()+this.value.hashCode()+this.unit.hashCode()+this.dimension.hashCode();
  }

}