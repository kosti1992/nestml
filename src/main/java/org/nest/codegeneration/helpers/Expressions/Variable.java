package org.nest.codegeneration.helpers.Expressions;

/**
 * @author perun
 */
public class Variable extends Expression {
  private String variable;

  public Variable(String variable){
    this.variable = variable;
  }

  public String getVariable() {
    return variable;
  }

  public void setVariable(String variable) {
    this.variable = variable;
  }
}
