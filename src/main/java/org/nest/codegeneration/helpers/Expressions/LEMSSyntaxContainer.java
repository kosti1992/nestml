package org.nest.codegeneration.helpers.Expressions;

import org.nest.commons._ast.ASTExpr;

/**
 * @author perun
 */
public class LEMSSyntaxContainer implements SyntaxContainer{

  public String print(Expression expr){
    if(expr.getClass().equals(NumericalLiteral.class)){
     return this.printNumericalLiteral((NumericalLiteral) expr);
    }
    else if(expr.getClass().equals(Variable.class)){
     return this.printVariable((Variable) expr);
    }
    else if(expr.getClass().equals(Operator.class)){
     return this.printOperator((Operator) expr);
    }
    else{//the last case -> function
     return this.printFunction((Function) expr);
    }
  }

  public String printNumericalLiteral(NumericalLiteral expr){
    return expr.getValueAsString();
  }

  public String printVariable(Variable expr){
    return expr.getVariable();
  }

  public String printOperator(Operator expr){
    if(expr.isInf()){
      return "[Inf_not_supported]";
    }
    else if(expr.isLogicalOr()){
      return ".or.";
    }
    else if(expr.isLogicalAnd()){
      return ".and.";
    }
    else if(expr.isLogicalNot()){
      //TODO
      //caution: this case seems to be rather fishy in LEMS, not sure if it works
      return ".not.";
    }
    else if(expr.isGt()){
      return ".gt.";
    }
    else if(expr.isGe()){
      return ".geq.";
    }
    else if(expr.isNe()){
      return ".ne.";
    }
    else if(expr.isEq()){
      return ".eq.";
    }
    else if(expr.isLe()){
      return ".leq.";
    }
    else if(expr.isLt()){
      return ".lt.";
    }
    else if(expr.isBitOr()){
      return "[BitOr_not_supported]";
    }
    else if(expr.isBitXor()){
      return "[BitXor_not_supported]";
    }
    else if(expr.isBitAnd()){
      return "[BitAnd_not_supported]";
    }
    else if(expr.isShiftRight()){
      return "[BitShiftR_not_supported]";
    }
    else if(expr.isShiftLeft()){
      return "[BitShiftL_not_supported]";
    }
    else if(expr.isMinusOp()){
      return "-";
    }
    else if(expr.isPlusOp()){
      return "+";
    }
    else if(expr.isModuloOp()){
      return "%";
    }
    else if(expr.isDivOp()){
      return "/";
    }
    else if(expr.isTimesOp()){
      return "*";
    }
    else if(expr.isUnaryTilde()){
      return "[UnaryTilde_not_supported]";
    }
    else if(expr.isUnaryPlus()){
      return "+";
    }
    else if(expr.isUnaryMinus()){
      return "-";
    }
    else if(expr.isPower()){
      //TODO:this is a big todo <-
      return "TODO";
    }
    else if(expr.isLeftParentheses()){
      return "(";
    }
    else if(expr.isRightParentheses()){
      return ")";
    }
    else{
      return "";
    }

  }

  public String printFunction(Function expr){
    String ret = expr.getFunctionName() + "(";
    StringBuilder newBuilder = new StringBuilder();
    for (Expression  arg: expr.getArguments()) {
      newBuilder.append(arg.printExpression(this));
      newBuilder.append(",");
    }
    newBuilder.deleteCharAt(newBuilder.length() - 1);//delete the last "," before the end of the string
    return ret + newBuilder.toString() + ")";
  }



}
