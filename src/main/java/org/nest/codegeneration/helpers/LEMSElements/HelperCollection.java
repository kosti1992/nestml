package org.nest.codegeneration.helpers.LEMSElements;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.nest.codegeneration.helpers.LEMSCollector;
import org.nest.commons._ast.ASTExpr;
import org.nest.commons._ast.ASTFunctionCall;
import org.nest.spl._ast.ASTBlock;
import org.nest.spl._ast.ASTELIF_Clause;
import org.nest.spl._ast.ASTStmt;
import org.nest.spl.prettyprinter.LEMS.LEMSExpressionsPrettyPrinter;
import org.nest.symboltable.symbols.TypeSymbol;
import org.nest.symboltable.symbols.VariableSymbol;

/**
 * This class provides a set of methods which are used during the transformation in order to retrieve or
 * transform certain values.
 *
 * @author perun
 */
public class HelperCollection {
  LEMSCollector container;

  public HelperCollection(LEMSCollector collector) {
    container = collector;
  }

  /**
   * Generates a random HEX Code as required for coloring the graphs.
   *
   * @return a HEX color code as String
   */
  @SuppressWarnings("unused")//used in the template
  public String gencode() {
    String[] letters = "0123456789ABCDEF".split("");
    String code = "#";
    for (int i = 0; i < 6; i++) {
      double ind = Math.random() * 15;
      int index = (int) Math.round(ind);
      code += letters[index];
    }
    return code;
  }

  /**
   * Returns all spike input ports of a given set.
   *
   * @param ports a list of used ports
   * @return a list of all spike ports
   */
  @SuppressWarnings("unused")//used in the template
  public List<String> getSpikePorts(List<String> ports) {
    return ports.stream().filter(st -> st.endsWith("spikes")).collect(Collectors.toList());
  }

  /**
   * This function avoids problems with locale settings regarding number formats and print a number with "." as sole
   * separator.
   *
   * @return number as string
   */
  @SuppressWarnings("unused")//used in the template
  public String getNumberFormatted(double input) {
    return String.valueOf(input);
  }

  /**
   * Extracts the dimension of a given variable-type. This class has to be modified if new units and dimensions
   * a introduced to NESTML.
   *
   * @param input the type-symbol of the a variable.
   * @return the name of the dimension as String
   */
  public String typeToDimensionConverter(TypeSymbol input) {
    if (input.getName().equals("boolean") || input.getName().equals("void")) {
      return "not_supported";
    }
    if (input.getType() == TypeSymbol.Type.PRIMITIVE) {
      return "none";
    }
    else if (input.getType() == TypeSymbol.Type.UNIT) {
      return "DimensionOf_"+input.prettyPrint();
    }
    System.err.println(input.prettyPrint() + " : not supported!");
    return null;
  }

  /**
   * Converts a unit prefix to power of base 10.
   *
   * @param var_type Name of the unit represented as String.
   * @return Power of the prefix as int.
   */
  public int powerConverter(String var_type) {
    if (var_type == null || var_type.length() < 2) {
      return 0;
    }
    if (var_type.startsWith("d")) {
      return -1;
    }
    else if (var_type.startsWith("c")) {
      return -2;
    }
    else if (var_type.startsWith("m") && var_type.length() > 1) {// in order to avoid confusion between meter and mili
      return -3;
    }
    else if (var_type.startsWith("mu")) {
      return -6;
    }
    else if (var_type.startsWith("n")) {
      return -9;
    }
    else if (var_type.startsWith("p")) {
      return -12;
    }
    else if (var_type.startsWith("f")) {
      return -15;
    }
    else if (var_type.startsWith("a")) {
      return -18;
    }
    //positive powers
    else if (var_type.startsWith("da")) {
      return 1;
    }
    else if (var_type.startsWith("h")) {
      return 2;
    }
    else if (var_type.startsWith("k")) {
      return 3;
    }
    else if (var_type.startsWith("M")) {
      return 6;
    }
    else if (var_type.startsWith("G")) {
      return 9;
    }
    else if (var_type.startsWith("T")) {
      return 12;
    }
    else if (var_type.startsWith("P")) {
      return 15;
    }
    else if (var_type.startsWith("E")) {
      return 18;
    }
    System.err.println(var_type + " prefix not supported!");
    return 1;
  }

  /**
   * Checks whether a given function call name is supported by the target modeling language or not.
   * If new concepts are supported, add here.
   *
   * @param expr The function call name which will be checked.
   * @return true, if supported
   */
  public boolean mathematicalFunctionIsSupported(String expr) {
    switch (expr) {
      case "exp":
        return true;
      case "log":
        return true;
      case "pow":
        return true;
      default:
        return false;
    }
  }

  /**
   * Indicates whether a given ASTExpression contains a function call or not.
   *
   * @param expr          the expression which will be checked.
   * @param skipSupported if true, all supported function calls will be skipped
   * @return true, if expression or sub-expression contains call.
   */
  public boolean containsFunctionCall(ASTExpr expr, boolean skipSupported) {
    boolean temp = false;
    //if more functions are supported
    if (expr.functionCallIsPresent() && !(skipSupported && mathematicalFunctionIsSupported(
        expr.getFunctionCall().get().getName().toString()))) {
      temp = true;
    }
    if (expr.exprIsPresent()) {
      temp = temp || containsFunctionCall(expr.getExpr().get(), skipSupported);
    }
    if (expr.leftIsPresent()) {
      temp = temp || containsFunctionCall(expr.getLeft().get(), skipSupported);
    }
    if (expr.rightIsPresent()) {
      temp = temp || containsFunctionCall(expr.getRight().get(), skipSupported);
    }
    return temp;
  }

  /**
   * This method can be used to extract from a given expression all constants, e.g. 70mv, generate corresponding
   * constant, and replace the constants in the expression with references.
   *
   * @param input the expression
   */
  public String replaceConstantsWithReferences(LEMSCollector container, String input) {
    String inputString = input;
    //split the expression on arithmetic operations and brackets, and logical expressions
    String[] parts = inputString.split("[+\\-*/%()]|.(gt|geq|eq|neq|leq|lt).");
    List<String> processed = new ArrayList<>();
    for (String part : parts) {//check for each part, if it ends with an unit -> is a constant
      for (Unit unit : container.getUnitsSet()) {//check all utilize units
        if (part.endsWith(unit.getSymbol()) && !processed.contains(part)) {//process only if not already processed
          processed.add(part);
          String numValue = part.replaceAll("\\D+", "");//extract the numerical value
          Constant temp = new Constant("CON" + part, unit.getDimension().getName(), numValue, unit.getSymbol(),false);
          if (!container.getConstantsList().contains(temp)) {//do not add any duplicates to the list of constants
            container.addConstant(temp);
          }
          inputString = inputString.replaceAll(part, temp.getName());//replace the corresponding part in the string
        }
      }
    }
    return inputString;
  }

  /**
   * Returns the arguments of a function separated by ",".
   * E.g: sum(x,y) -> x,y
   *
   * @param functionCall the function-call whose arguments will be extracted.
   * @return Arguments as String
   */
  public String getArgs(ASTFunctionCall functionCall) {
    StringBuilder newBuilder = new StringBuilder();
    for (ASTExpr expr : functionCall.getArgs()) {
      newBuilder.append(container.getLEMSExpressionsPrettyPrinter().print(expr, false));
      newBuilder.append(",");
    }
    newBuilder.deleteCharAt(newBuilder.length() - 1);//delete the last "," before the end of the string
    return newBuilder.toString();
  }

  /**
   * Returns true if a handed over list of instructions contains a named function call
   *
   * @param funcName the name of the called function
   * @param list     the list which holds instruction
   * @return true if function call is present
   */
  public boolean containsNamedFunction(String funcName, List<DynamicRoutine.Instruction> list) {
    for (DynamicRoutine.Instruction instr : list) {
      if (instr.getClass() == DynamicRoutine.FunctionCall.class &&
          ((DynamicRoutine.FunctionCall) instr).getFunctionName().equals(funcName)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns all instructions which declare a function call with a given name.
   *
   * @param funcName the name of the function call
   * @param list     the list of instructions which will be searched through
   * @return a list of function calls with the given name,
   */
  public List<DynamicRoutine.FunctionCall> getNamedFunction(String funcName, List<DynamicRoutine.Instruction> list) {
    return list.stream()
        .filter(call -> call.getClass().equals(DynamicRoutine.FunctionCall.class) &&
            ((DynamicRoutine.FunctionCall) call).getFunctionName().equals(funcName))
        .map(call -> (DynamicRoutine.FunctionCall) call).collect(Collectors.toList());
  }

  /**
   * Checks whether a given block or a sub block contains a certain function call.
   *
   * @param function The name of the function which will be checked
   * @param args     the handed over arguments of the function
   * @param block    the block in which a function is possibly contained
   * @return true if function call is present
   */
  public boolean blockContainsFunction(String function, List<String> args, ASTBlock block) {
    boolean temp = false;
    boolean temp2 = false;
    for (ASTStmt stmt : block.getStmts()) {
      if (stmt.small_StmtIsPresent()) {
        if (stmt.getSmall_Stmt().get().functionCallIsPresent() &&
            stmt.getSmall_Stmt().get().getFunctionCall().get().getName().toString()
                .equals(function) &&
            stmt.getSmall_Stmt().get().getFunctionCall().get().getArgs().size() == args
                .size()) {
          temp2 = true;
          for (int i = 0; i < args.size(); i++) {
            //check if some args. are different
            if (!args.get(i).equals(container.getLEMSExpressionsPrettyPrinter()
                .print(stmt.getSmall_Stmt().get().getFunctionCall().get().getArgs().get(i),
                    false))) {
              temp2 = false;
            }
          }
        }
        temp = temp || temp2;
      }
      else {
        temp = temp || this.blockContainsFunction(function, args,
            stmt.getCompound_Stmt().get().getIF_Stmt().get().getIF_Clause().getBlock());
        for (ASTELIF_Clause clause : stmt.getCompound_Stmt().get().getIF_Stmt().get().getELIF_Clauses()) {
          temp = temp || blockContainsFunction(function, args, clause.getBlock());
        }
        if (stmt.getCompound_Stmt().get().getIF_Stmt().get().eLSE_ClauseIsPresent()) {
          temp = temp || blockContainsFunction(function, args,
              stmt.getCompound_Stmt().get().getIF_Stmt().get().getELSE_Clause().get().getBlock());
        }
      }
    }
    return temp;
  }

  /**
   * This method converts the internal representation of the type of a variable to
   * an array of integer values.
   * @param arrayAsString a string representation of the data type array
   * @return an array of data
   */
  public int[] convertTypeDeclToArray(String arrayAsString){
    int res[] = new int[8];
    String[] tempArray = (arrayAsString.replaceAll("[^\\d\\-]",":")).split(":");
    int tempIndex = 0;
    for(int i=0;i<tempArray.length;i++){
      if(!tempArray[i].equals("")){
        res[tempIndex]= Integer.parseInt(tempArray[i]);
        tempIndex++;
      }
    }
    return res;
  }

  /**
   * This method is used to check whether a data type is not supported.
   * @param type the type symbol of an attribute
   * @return true, if the type is not supported
   */
  public boolean dataTypeNotSupported(TypeSymbol type){
    if((type.getType()== TypeSymbol.Type.PRIMITIVE)&&(type.prettyPrint().equals("boolean")||
        type.prettyPrint().equals("void")||
        type.prettyPrint().equals("String"))){
      return true;
    }
    return false;
  }

  /**
   * This method prints and stores an adequate message regarding not supported
   * elements during the transformation
   * @param variable the variable whose type is not supported
   */
  public void printNotSupportedDataType(VariableSymbol variable){
    System.err.println("Not supported data-type found: \"" + variable.getType().getName() + "\".");
    container.addNotConverted("Not supported data-type "
        + variable.getType().prettyPrint() + " in lines "
        + variable.getAstNode().get().get_SourcePositionStart()
        + " to " + variable.getAstNode().get().get_SourcePositionEnd()
        + ".");
  }

  public void printNotSupportedFunctionCallInExpression(VariableSymbol variable){
    System.err.println("Function call found in constant/parameter declaration"
        + " in lines "+variable.getSourcePosition().getLine()+ ". Please declare as "
        + "derived variable!");
  }
}
