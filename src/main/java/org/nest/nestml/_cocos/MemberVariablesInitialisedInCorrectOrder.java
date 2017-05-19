/*
 * Copyright (c) 2015 RWTH Aachen. All rights reserved.
 *
 * http://www.se-rwth.de/
 */
package org.nest.nestml._cocos;

import de.monticore.symboltable.Scope;
import de.se_rwth.commons.logging.Log;
import org.nest.commons._ast.ASTVariable;
import org.nest.spl._ast.ASTDeclaration;
import org.nest.spl._cocos.SPLASTDeclarationCoCo;
import org.nest.symboltable.predefined.PredefinedVariables;
import org.nest.symboltable.symbols.VariableSymbol;

import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;

import static com.google.common.base.Preconditions.checkState;
import static de.monticore.utils.ASTNodes.getSuccessors;
import static de.se_rwth.commons.logging.Log.error;

/**
 * Variables in a block must be defined before used. Only variables from parameters are allowed
 * to be used before definition
 *
 * @author ippen, plotnikov
 */
public class MemberVariablesInitialisedInCorrectOrder implements SPLASTDeclarationCoCo {
  public static final String ERROR_CODE = "NESTML_MEMBER_VARIABLES_INITIALISED_IN_CORRECT_ORDER";
  NestmlErrorStrings errorStrings = NestmlErrorStrings.getInstance();

  public void check(final ASTDeclaration declaration) {
    checkState(declaration.getEnclosingScope().isPresent(), "There is no scope assigned to the AST node: " + declaration);
    final Optional<? extends Scope> enclosingScope = declaration.getEnclosingScope();

    if (declaration.getExpr().isPresent()) {
      // has at least one declaration. it is ensured by the grammar
      final String lhsVariableName = declaration.getVars().get(0);

      final Optional<VariableSymbol> lhsVariable = enclosingScope.get().resolve(
          lhsVariableName,
          VariableSymbol.KIND);

      checkState(lhsVariable.isPresent(), "Variable '" + lhsVariableName + "' is not defined");

      final List<ASTVariable> variablesNames = getSuccessors(
          declaration.getExpr().get(),
          ASTVariable.class);

      checkVariables(lhsVariable.get(), variablesNames, enclosingScope.get(), (a,b) -> a >= b);

      if (declaration.getInvariant().isPresent()) {
        final List<ASTVariable> variablesInInvariant = getSuccessors(declaration.getInvariant().get(), ASTVariable.class);

        checkVariables(lhsVariable.get(), variablesInInvariant, enclosingScope.get(), (a,b) -> a > b);
      }

    }

  }

  private void checkVariables(
      final VariableSymbol lhsSymbol,
      final List<ASTVariable> variablesNames,
      final Scope enclosingScope,
      final BiPredicate<Integer, Integer> predicate) {
    for (final ASTVariable astVariable : variablesNames) {
      final String rhsVariableName = astVariable.toString();
      final Optional<VariableSymbol> rhsSymbol = enclosingScope.resolve(
          rhsVariableName,
          VariableSymbol.KIND);

      if (!rhsSymbol.isPresent()) { // actually redudant and it is should be checked through another CoCo
        final String msg = errorStrings.getErrorMsgVariableNotDefined(this,
                astVariable.get_SourcePositionStart().toString(),
                rhsVariableName);
        Log.warn(msg);
        return;
      }
      else  { //
        // not local, e.g. a variable in one of the blocks: state, parameter, or internal
        // both of same decl type
        checkIfDefinedInCorrectOrder(lhsSymbol, rhsSymbol.get(), predicate);

      }

    }

  }

  private void checkIfDefinedInCorrectOrder(
      final VariableSymbol lhsSymbol,
      final VariableSymbol rhsSymbol,
      final BiPredicate<Integer, Integer> isError) {
    // TODO actually, check if the variable symbol is predefined
    if (PredefinedVariables.getVariableIfExists(rhsSymbol.getName()).isPresent()) {
      return;
    }
    // ALL local variables can access member vraibles
    if (lhsSymbol.getBlockType() == VariableSymbol.BlockType.LOCAL) {
      return;
    }

    if (rhsSymbol.getBlockType() == lhsSymbol.getBlockType()) {
      // same block not parameter block
      if (isError.test(rhsSymbol.getSourcePosition().getLine(),
          lhsSymbol.getSourcePosition().getLine())) {
        final String msg = errorStrings.getErrorMsgDeclaredInIncorrectOrder(this,
                rhsSymbol.getName(),
                lhsSymbol.getName());

        error(msg, lhsSymbol.getSourcePosition());

      }

    }

    if (rhsSymbol.getBlockType() != lhsSymbol.getBlockType() &&
        rhsSymbol.getBlockType() != VariableSymbol.BlockType.PARAMETERS) {
       final String msg = errorStrings.getErrorMsgDeclaredInIncorrectOrder(this,
              rhsSymbol.getName(),
              lhsSymbol.getName());

      error(msg, lhsSymbol.getSourcePosition());
    }


  }

}
