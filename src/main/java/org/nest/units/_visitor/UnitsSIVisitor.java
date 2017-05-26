/*
 * Copyright (c) 2015 RWTH Aachen. All rights reserved.
 *
 * http://www.se-rwth.de/
 */
package org.nest.units._visitor;

import com.google.common.collect.Lists;
import de.se_rwth.commons.logging.Finding;
import de.se_rwth.commons.logging.Log;
import org.nest.commons._ast.ASTExpr;
import org.nest.nestml._ast.ASTNESTMLNode;
import org.nest.nestml._visitor.NESTMLVisitor;
import org.nest.spl._ast.ASTSPLNode;
import org.nest.units._ast.ASTUnitType;
import org.nest.units._ast.ASTUnitsNode;
import org.nest.units.unitrepresentation.SIData;
import org.nest.units.unitrepresentation.UnitTranslator;
import org.nest.utils.LogHelper;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Type checking visitor for the UNITS grammar. Verifies that all units used are comprised of SI units.
 *
 * @author ptraeder
 */
public class UnitsSIVisitor implements NESTMLVisitor {
  private final static String ERROR_CODE = "NESTML_UnitsSIVisitor";
  private UnitTranslator translator = new UnitTranslator();

  /**
   * Use the static factory method: convertSiUnitsToSignature
   */
  private UnitsSIVisitor() {

  }

  /**
   * Checks that the given unit is well defined. In case of SI unit, converts to its signature
   * representation. In case of errors reports them as non empty return list
   * @param unit ASTUnitNode to check
   * @return The list all type finding. Is emtpty iff the model doesn't contain any type issues.
   */
  public static List<Finding> convertSiUnitsToSignature(final ASTUnitsNode unit) {
    final UnitsSIVisitor unitsSIVisitor = new UnitsSIVisitor();
    unit.accept(unitsSIVisitor);
    final List<Finding> findings = LogHelper.getModelFindings(Log.getFindings());
    return findings;
  }

  /**
   * Checks that all units used in the models are well defined. In case of SI units, converts them to its signature
   * representation. In case of errors reports them as non empty return list
   * @param compilationUnit Input model to check
   * @return The list all type finding. Is emtpty iff the model doesn't contain any type issues.
   */
  public static List<Finding> convertSiUnitsToSignature(final ASTSPLNode compilationUnit) {
    final UnitsSIVisitor unitsSIVisitor = new UnitsSIVisitor();
    compilationUnit.accept(unitsSIVisitor);
    final List<Finding> findings = LogHelper.getModelFindings(Log.getFindings());
    return findings;
  }

  /**
   * Checks that all units used in the models are well defined. In case of SI units, converts them to its signature
   * representation. In case of errors reports them as non empty return list
   * @param compilationUnit Input model to check
   * @return The list all type finding. Is emtpty iff the model doesn't contain any type issues.
   */
  public static List<Finding> convertSiUnitsToSignature(final ASTNESTMLNode compilationUnit) {
    final UnitsSIVisitor unitsSIVisitor = new UnitsSIVisitor();
    compilationUnit.accept(unitsSIVisitor);
    final Collection<Finding> findings = LogHelper.getErrorsByPrefix("NESTML_", Log.getFindings());

    return Lists.newArrayList(findings);
  }


  /**
   * Verify that the given Unit is valid. Use UnitTranslator to generate serialization of Unit.
   * Set the nodes' "serializedUnit" field with the serialization.
   */
  public void visit(ASTUnitType astUnitType){
    //String unit = astUnitType.getmUnit().get();
    final Optional<String> convertedUnit = translator.calculateUnitType(astUnitType);

    if (convertedUnit.isPresent()) {
      astUnitType.setSerializedUnit(convertedUnit.get());
    }
    else {
      Log.error(ERROR_CODE + "The unit " +( astUnitType.unitIsPresent()? astUnitType.getUnit().get() : astUnitType.toString() )+ " is not an SI unit.", astUnitType.get_SourcePositionStart());
    }

  }

  /**
   * Verify that if a literal is followed directly (only seperated by whitespaces) by a variable,
   * that the variable is one of the predefined unit variables.
   */
  public void visit(ASTExpr node) {
    if(node.numericLiteralIsPresent() && node.variableIsPresent()){
      final String varName = node.getVariable().get().toString();
      final List<String> validUnits = SIData.getCorrectSIUnits();
      boolean valid = false;

      for(String validUnit : validUnits){
        if(varName.equals(validUnit)){
          valid = true;
          break;
        }
      }

      if(!valid){
        Log.error(ERROR_CODE + varName +"is not an SI unit.", node.get_SourcePositionStart());
      }

    }
  }

}
