package org.nest.codegeneration.helpers.Expressions;

/**
 * This class represent an abstract precursor for syntax container which
 * indicate how to print certain elements in certain target languages.
 * @author perun
 */
interface SyntaxContainer {

  public String print(NumericalLiteral expr);

  public String print(Variable expr);

  public String print(Operator expr);

  public String print(Function expr);

}
