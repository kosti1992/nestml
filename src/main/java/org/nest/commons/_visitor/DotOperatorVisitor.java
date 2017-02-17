package org.nest.commons._visitor;

import org.nest.commons._ast.ASTExpr;
import org.nest.spl.symboltable.typechecking.Either;
import org.nest.symboltable.symbols.TypeSymbol;
import org.nest.units.unitrepresentation.UnitRepresentation;
import org.nest.utils.AstUtils;

import static de.se_rwth.commons.logging.Log.error;
import static org.nest.spl.symboltable.typechecking.TypeChecker.isInteger;
import static org.nest.spl.symboltable.typechecking.TypeChecker.isNumeric;
import static org.nest.symboltable.predefined.PredefinedTypes.*;
/**
 * @author ptraeder
 */
public class DotOperatorVisitor implements CommonsVisitor{
  final String ERROR_CODE = "SPL_DOT_OPERATOR_VISITOR";

  @Override
  public void visit(ASTExpr expr) {
    final Either<TypeSymbol, String> lhsType = expr.getLeft().get().getType();
    final Either<TypeSymbol, String> rhsType = expr.getRight().get().getType();

    if (lhsType.isError()) {
      expr.setType(lhsType);
      return;
    }
    if (rhsType.isError()) {
      expr.setType(rhsType);
      return;
    }

    if(expr.isModuloOp()){
      if(isInteger(lhsType.getValue())&&isInteger(rhsType.getValue())){
        expr.setType(Either.value(getIntegerType()));
        return;
      }else{
        final String errorMsg = ERROR_CODE + " " + AstUtils.print(expr.get_SourcePositionStart()) + " : " +"Modulo with non integer parameters";
        expr.setType(Either.error(errorMsg));
        error(errorMsg,expr.get_SourcePositionStart());
        return;
      }
    }
    if(expr.isDivOp() || expr.isTimesOp()) {
      if (isNumeric(lhsType.getValue()) && isNumeric(rhsType.getValue())) {

        // If both are units, calculate resulting Type
        if (lhsType.getValue().getType() == TypeSymbol.Type.UNIT
            && rhsType.getValue().getType() == TypeSymbol.Type.UNIT) {
          UnitRepresentation leftRep = UnitRepresentation.getBuilder().serialization(lhsType.getValue().getName()).build();
          UnitRepresentation rightRep = UnitRepresentation.getBuilder().serialization(rhsType.getValue().getName()).build();
          if (expr.isTimesOp()) {
            TypeSymbol returnType = getTypeIfExists((leftRep.multiplyBy(rightRep)).serialize())
                .get();//Register type on the fly
            expr.setType(Either.value(returnType));
            return;
          }
          else if (expr.isDivOp()) {
            TypeSymbol returnType = getTypeIfExists((leftRep.divideBy(rightRep)).serialize())
                .get();//Register type on the fly
            expr.setType(Either.value(returnType));
            return;
          }
        }
        //if lhs is Unit, and rhs real or integer, return same Unit
        if (lhsType.getValue().getType() == TypeSymbol.Type.UNIT) {
          expr.setType(Either.value(lhsType.getValue()));
          return;
        }
        //if lhs is real or integer and rhs a unit, return unit for timesOP and inverse(unit) for divOp
        if (rhsType.getValue().getType() == TypeSymbol.Type.UNIT) {
          if (expr.isTimesOp()) {
            expr.setType(Either.value(rhsType.getValue()));
            return;
          }
          else if (expr.isDivOp()) {
            UnitRepresentation rightRep = UnitRepresentation.getBuilder().serialization(rhsType.getValue().getName()).build();
            TypeSymbol returnType = getTypeIfExists((rightRep.invert()).serialize()).get();//Register type on the fly
            expr.setType(Either.value(returnType));
            return;
          }

        }
        //if no Units are involved, Real takes priority
        if (lhsType.getValue() == getRealType() || rhsType.getValue() == getRealType()) {
          expr.setType(Either.value(getRealType()));
          return;
        }

        // e.g. both are integers, but check to be sure
        if (lhsType.getValue() == getIntegerType() || rhsType.getValue() == getIntegerType()) {
          expr.setType(Either.value(getIntegerType()));
          return;
        }
      }
      //If a buffer is involved, the other unit takes precedent TODO: is this the intended semantic?
      if(lhsType.getValue() == getBufferType()){
        expr.setType(Either.value(rhsType.getValue()));
        return;
      }
      if(rhsType.getValue() == getBufferType()){
        expr.setType(Either.value(lhsType.getValue()));
        return;
      }
    }

    //Catch-all if no case has matched
    final String errorMsg = ERROR_CODE+ " " + AstUtils.print(expr.get_SourcePositionStart()) + " : " +"Cannot determine the type of the expression: " +lhsType.getValue().prettyPrint()
        +(expr.isDivOp()?" / ":" * ")+rhsType.getValue().prettyPrint();
    expr.setType(Either.error(errorMsg));
    error(errorMsg,expr.get_SourcePositionStart());
  }
}
