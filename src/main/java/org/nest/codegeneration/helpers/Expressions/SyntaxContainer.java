package org.nest.codegeneration.helpers.Expressions;

/**
 * This class represent an abstract precursor for syntax container which
 * indicate how to print certain elements in certain target languages.
 * @author perun
 */
interface SyntaxContainer {

  public String print(Expression expr);

  public String printNumericalLiteral(NumericalLiteral expr);

  public String printVariable(Variable expr);

  public String printOperator(Operator expr);

  public String printFunction(Function expr);

}
