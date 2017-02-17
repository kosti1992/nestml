package org.nest.codegeneration.sympy;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import de.monticore.ast.ASTNode;
import org.nest.commons._ast.ASTExpr;
import org.nest.commons._ast.ASTFunctionCall;
import org.nest.symboltable.predefined.PredefinedFunctions;
import org.nest.utils.AstUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Provides methods to manipulate ODEs: replace all I_sum(shape, buffer) and Cond_sum(shape, buffer) through the
 * corresponding shape. This class is used also in generator templates
 *
 * @author plotnikov
 */
public class OdeTransformer {
  private static final List<String> functions = Lists.newArrayList(
      PredefinedFunctions.CURR_SUM,
      PredefinedFunctions.COND_SUM,
      PredefinedFunctions.BOUNDED_MIN,
      PredefinedFunctions.BOUNDED_MAX);

  private static final List<String> sumFunctions = Lists.newArrayList(
      PredefinedFunctions.CURR_SUM,
      PredefinedFunctions.COND_SUM);


  // this function is used in freemarker templates und must be public
  public static <T extends ASTNode> T replaceFunctions(final T astOde) {
    // since the transformation replaces the call inplace, make a copy to preserve the information for further steps
    final List<ASTFunctionCall> functionsCalls = getFunctionCalls(astOde, functions);

    final T workingCopy = (T) astOde.deepClone(); // IT is OK, since the deepClone returns T
    functionsCalls.forEach(functionCall -> replaceFunctionCallThroughFirstArgument(astOde, functionCall)); // TODO deepClone
    return astOde;
  }

  // this function is used in freemarker templates und must be public
  public static <T extends ASTNode> T replaceSumCalls(final T astOde) {
    // since the transformation replaces the call inplace, make a copy to preserve the information for further steps
    final List<ASTFunctionCall> functionsCalls = get_sumFunctionCalls(astOde);

    final T workingCopy = (T) astOde.deepClone(); // IT is OK, since the deepClone returns T
    functionsCalls.forEach(functionCall -> replaceFunctionCallThroughFirstArgument(astOde, functionCall)); // TODO deepClone
    return astOde;
  }


  // this function is used in freemarker templates und must be public
  static List<ASTFunctionCall> get_sumFunctionCalls(final ASTNode workingCopy) {
    return getFunctionCalls(workingCopy, sumFunctions);
  }

  // this function is used in freemarker templates und must be public
  public static boolean containsSumFunctionCall(final ASTNode workingCopy) {
    return !getFunctionCalls(workingCopy, sumFunctions).isEmpty();
  }

  // this function is used in freemarker templates und must be public
  private static List<ASTFunctionCall> getFunctionCalls(final ASTNode workingCopy, final List<String> functionNames) {
    return AstUtils.getAll(workingCopy, ASTFunctionCall.class)
        .stream()
        .filter(astFunctionCall -> functionNames.contains(astFunctionCall.getCalleeName()))
        .collect(Collectors.toList());
  }

  public static List<ASTFunctionCall> getCondSumFunctionCall(final ASTNode workingCopy) {
    return AstUtils.getAll(workingCopy, ASTFunctionCall.class)
        .stream()
        .filter(astFunctionCall -> astFunctionCall.getCalleeName().equals(PredefinedFunctions.COND_SUM))
        .collect(Collectors.toList());
  }

  private static void replaceFunctionCallThroughFirstArgument(final ASTNode astOde, final ASTFunctionCall node) {
    final Optional<ASTNode> parent = AstUtils.getParent(node, astOde);
    Preconditions.checkState(parent.isPresent());
    final ASTExpr expr = (ASTExpr) parent.get();
    expr.setFunctionCall(null);
    expr.setVariable(node.getArgs().get(0).getVariable().get());
  }

}