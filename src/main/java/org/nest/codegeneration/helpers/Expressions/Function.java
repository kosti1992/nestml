package org.nest.codegeneration.helpers.Expressions;

import java.util.ArrayList;
import java.util.List;

import org.nest.commons._ast.ASTExpr;
import org.nest.commons._ast.ASTFunctionCall;

/**
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
}
