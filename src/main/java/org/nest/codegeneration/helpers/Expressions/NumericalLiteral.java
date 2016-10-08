package org.nest.codegeneration.helpers.Expressions;

import java.util.Optional;

import de.monticore.prettyprint.IndentPrinter;
import de.monticore.types.prettyprint.TypesPrettyPrinterConcreteVisitor;
import org.nest.commons._ast.ASTNESTMLNumericLiteral;
import org.nest.units._ast.ASTUnitType;

/**
 * @author perun
 */
public class NumericalLiteral extends Expression{
  private double value;
  private Optional<ASTUnitType> type = Optional.empty();


  public NumericalLiteral(ASTNESTMLNumericLiteral literal){
    this.value = Double.parseDouble(typesPrinter().prettyprint(literal.getNumericLiteral()));
    if(literal.typeIsPresent()){
      this.type = Optional.of(literal.getType().get());
    }
  }

  public double getValue() {
    return value;
  }

  public boolean hasType(){
    return type.isPresent();
  }

  public String getValueAsString(){
    return String.valueOf(this.value);
  }

  public Optional<ASTUnitType> getType() {
    return type;
  }

  private TypesPrettyPrinterConcreteVisitor typesPrinter() {
    final IndentPrinter printer = new IndentPrinter();
    return new TypesPrettyPrinterConcreteVisitor(printer);
  }


}
