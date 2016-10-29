package org.nest.codegeneration.helpers.Expressions;

import java.util.ArrayList;
import java.util.List;

import org.nest.commons._ast.ASTExpr;
import org.nest.commons._ast.ASTFunctionCall;

/**
 * This class is used to store function calls of an expression.
 * @author perun
 */
public class Function extends Expression{
  private String functionName;
  private List<Expression> arguments;

  public Function(ASTFunctionCall expr){
    functionName = expr.getName().toString();
    arguments = new ArrayList<>();
    for(ASTExpr arg:expr.getArgs()){
      Expression temp = new Expression(arg);
      arguments.add(temp);
    }
  }

  public Function(String functionName,List<Expression> args){
    this.functionName = functionName;
    this.arguments = args;
  }

  public String getFunctionName() {
    return functionName;
  }

  public void setFunctionName(String functionName) {
    this.functionName = functionName;
  }

  public List<Expression> getArguments() {
    return arguments;
  }

  public void setArguments(List<Expression> arguments) {
    this.arguments = arguments;
  }

  public String print(SyntaxContainer container){
    return container.print(this);
  }

  public int hashCode(){
    int ret =  this.functionName.hashCode();
    for(Expression exp: arguments){
      ret = ret + exp.hashCode();
    }
    return ret;
  }
  public boolean equals(Object obj){
    return obj.getClass().equals(this.getClass())&&
        ((Function)obj).getFunctionName().equals(this.functionName)&&
        this.arguments.equals(((Function)obj).getArguments());
  }
}
