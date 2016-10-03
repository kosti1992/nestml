package org.nest.codegeneration.helpers.LEMSElements;

import org.nest.codegeneration.helpers.LEMSCollector;
import org.nest.symboltable.symbols.VariableSymbol;

  /**
   * This class represents a derived element, a characteristic of the model which possibly
   * changes over the time and is derived from other elements of the model.
   * @author perun
   */
  public class DerivedElement{
    private String name;
    private String dimension;
    private String derivationInstruction;
    private boolean dynamic;//distinguish between derived variables and derived parameter
    private boolean external;//uses an external source, i.e. other tags are used

    public DerivedElement(VariableSymbol variable, LEMSCollector container, boolean dynamic, boolean init) {
      if (init) {
        this.name = "INIT" + variable.getName();
      }
      else {
        this.name = variable.getName();
      }
      this.dynamic = dynamic;
      //data types boolean and void are not supported by lems
      if (LEMSCollector.helper.dataTypeNotSupported(variable.getType())) {
        dimension = "not_supported";
        //print an adequate error message
        LEMSCollector.helper.printNotSupportedDataType(variable);
      }
      else {
        dimension = container.getHelper().typeToDimensionConverter(variable.getType());
      }
      //get the derivation instruction in LEMS format
      derivationInstruction = container.getLEMSExpressionsPrettyPrinter().print(variable.getDeclaringExpression().get(), false);
      //replace constants with references
      derivationInstruction = container.getHelper().replaceConstantsWithReferences(container, derivationInstruction);
    }

    /**
     * This constructor can be used to generate a hand made derived element.
     * @param name the name of the new derived element
     * @param dimension the dimension of the new derived element
     * @param derivationInstruction the instructions for derivation
     * @param dynamic true, if derived element contains changeable elements
     */
    public DerivedElement(String name,String dimension,
        String derivationInstruction,boolean dynamic, boolean external){
      this.name = name;
      this.dimension = dimension;
      this.derivationInstruction = derivationInstruction;
      this.dynamic = dynamic;
      this.external = external;
    }

    @SuppressWarnings("unused")//used in the template
    public String getName() {
      return this.name;
    }
    @SuppressWarnings("unused")//used in the template
    public String getDerivationInstruction() {
      return this.derivationInstruction;
    }
    @SuppressWarnings("unused")//used in the template
    public String getDimension() {
      return this.dimension;
    }

    public boolean isDynamic(){
      return dynamic;
    }

    public boolean isExternal(){return external;}

    public boolean equals(Object o){
      return this.getClass().equals(o.getClass())&&
          this.name.equals(((DerivedElement)o).getName())&&
          this.derivationInstruction.equals(((DerivedElement)o).derivationInstruction)&&
          this.dynamic==(((DerivedElement)o).dynamic);
    }

    public int hashCode(){
      if(this.dynamic){
        return this.getClass().hashCode()+
            this.name.hashCode()+
            this.derivationInstruction.hashCode();
      }
      return -(this.getClass().hashCode()+
          this.name.hashCode()+
          this.derivationInstruction.hashCode());
    }
  }

