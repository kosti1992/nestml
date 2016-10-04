package org.nest.codegeneration.helpers.LEMSElements;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import de.monticore.emf._ast.ASTECNode;
import org.nest.codegeneration.helpers.LEMSCollector;
import org.nest.commons._ast.ASTExpr;
import org.nest.commons._ast.ASTFunctionCall;
import org.nest.nestml._ast.ASTDynamics;
import org.nest.spl._ast.ASTBlock;
import org.nest.spl._ast.ASTCompound_Stmt;
import org.nest.spl._ast.ASTELIF_Clause;
import org.nest.spl._ast.ASTSmall_Stmt;
import org.nest.spl._ast.ASTStmt;
import org.nest.spl.prettyprinter.SPLPrettyPrinter;
import org.nest.spl.prettyprinter.SPLPrettyPrinterFactory;

/**
 * This class represents a transformed representation of the dynamic routine extracted from the source-model
 * in order to generate corresponding conditional blocks.
 *
 * @author perun
 */
public class DynamicRoutine {
  private List<ConditionalBlock> blocks;//A List of all states of the automaton.

  private LEMSCollector container;//required in order to use the same pretty printer as in other parts of transformation

  public DynamicRoutine(List<ASTDynamics> input, LEMSCollector container) {
    this.blocks = new ArrayList<>();
    this.container = container;
    for (ASTDynamics dyn : input) {//for all dynamic blocks in the model
      //for all outer statements in the model
      dyn.getBlock().getStmts().forEach(this::handleStatement);
    }
  }

  /**
   * Handles a given statement by evoking an adequate subroutine.
   *
   * @param input the processed AST statement.
   */
  private void handleStatement(ASTStmt input) {
    checkNotNull(input);
    if (input.small_StmtIsPresent()) {
      //a simple statement can be processed directly
      List<Instruction> tempList = new ArrayList<>();
      if (input.getSmall_Stmt().get().getFunctionCall().isPresent()) {
        Instruction temp = handleFunctionCall(input.getSmall_Stmt().get().getFunctionCall().get());
        if (temp!= null) {
          //some function calls do not generate an instruction, thus it is not required to add null to the list
          tempList.add(temp);
        }
      }
      else {
        tempList.add(handleSmallStatement(input.getSmall_Stmt().get()));
      }
      //generate a description header
      SPLPrettyPrinter tempPrettyPrinter = SPLPrettyPrinterFactory.createDefaultPrettyPrinter();
      tempPrettyPrinter.print(input);
      String rawCodeTemp = this.buildHeader(input,tempPrettyPrinter.result());
      if (tempList.size() > 0) {//generate a new block
        this.blocks.add(new ConditionalBlock(tempList, "1.eq.1", rawCodeTemp));
      }
    }
    else if (input.compound_StmtIsPresent()) {
      handleCompoundStatement(input.getCompound_Stmt().get(), "");
    }
    else {
      System.err.println("Not supported type of statement in line " + input.get_SourcePositionStart());
    }
  }

  /**
   * Handles a given compound statement and converts to a corresponding regime.
   *
   * @param input a compound statement, i.e a block
   */
  private void handleCompoundStatement(ASTCompound_Stmt input, String condition) {
    List<ASTExpr> tempList = new ArrayList<>();//used for generating the final else block by concatenating all conditions
    SPLPrettyPrinter tempPrettyPrinter = SPLPrettyPrinterFactory.createDefaultPrettyPrinter();

    if (input.getIF_Stmt().isPresent()) {//this block is an if-block
      //process the if statement
      tempList.add(input.getIF_Stmt().get().getIF_Clause().getExpr());//store the if-condition
      String tempCondition;
      //store the condition of this if-block temporary
      if (!condition.equals("")) {//required in order to process nested blocks
        tempCondition = condition + ".and.(" + LEMSCollector.helper.replaceConstantsWithReferences(container,
            container.getLEMSExpressionsPrettyPrinter().print(input.getIF_Stmt().get().getIF_Clause().getExpr(), false))
            + ")";
      }
      else {
        tempCondition = "(" + LEMSCollector.helper.replaceConstantsWithReferences(container,
            container.getLEMSExpressionsPrettyPrinter().print(input.getIF_Stmt().get().getIF_Clause().getExpr(), false))
            + ")";
      }
      tempPrettyPrinter.print(input.getIF_Stmt().get().getIF_Clause());
      //store a new conditional block
      handleASTBlock(input.getIF_Stmt().get().getIF_Clause().getBlock(), tempCondition, tempPrettyPrinter.result());

      //process all elif statements
      for (ASTELIF_Clause clause : input.getIF_Stmt().get().getELIF_Clauses()) {
        tempList.add(clause.getExpr());//store each elif condition
        tempPrettyPrinter = SPLPrettyPrinterFactory.createDefaultPrettyPrinter();
        tempPrettyPrinter.print(clause);//collect raw code for the header
        tempCondition = "(" + LEMSCollector.helper.replaceConstantsWithReferences(container,
            container.getLEMSExpressionsPrettyPrinter().print(clause.getExpr(), false)) + ")";
        if (!condition.equals("")) {
          tempCondition = condition + ".and." + tempCondition;
        }
        handleASTBlock(clause.getBlock(), tempCondition, tempPrettyPrinter.result());
      }

      //process the else statement
      if (input.getIF_Stmt().get().getELSE_Clause().isPresent()) {
        tempPrettyPrinter = SPLPrettyPrinterFactory.createDefaultPrettyPrinter();
        //collect raw code for the header
        tempPrettyPrinter.print(input.getIF_Stmt().get().getELSE_Clause().get());
        //now generate a proper condition
        tempCondition = LEMSCollector.helper.replaceConstantsWithReferences(container,this.buildElseCondition(tempList));
        if (!condition.equals("")) {
          tempCondition = condition + ".and." + tempCondition;
        }
        //create the corresponding block
        handleASTBlock(input.getIF_Stmt().get().getELSE_Clause().get().getBlock(),
            tempCondition,
            tempPrettyPrinter.result());
      }
    }
    //TODO: are these blocks really not supported?
    else if (input.getWHILE_Stmt().isPresent()) {//the block is a while block-> not supported yet
      System.err.println("WHILE blocks are not yet supported.");
    }
    else if (input.getFOR_Stmt().isPresent()) {//the block is a for block-> not supported yet
      System.err.println("FOR blocks are not yet supported.");
    }
  }

  /**
   * Handles a block of instructions consisting of e.g. assignments, function-calls and further blocks.
   *
   * @param input block which will be processed
   */
  private void handleASTBlock(ASTBlock input, String condition, String rawCode) {
    List<Instruction> tempInstruction = new ArrayList<>();
    //generate a description header
    String rawCodeTemp = this.buildHeader(input,rawCode);
    //iterate over all statements in the block
    for (ASTStmt stmt : input.getStmts()) {
      //if a compound block has been found, generate a cond. block for all previously found directives if required
      if (stmt.compound_StmtIsPresent()) {
        if (tempInstruction.size() > 0) {
          tempInstruction = deactivateIntegration(tempInstruction);
          this.blocks.add(new ConditionalBlock(tempInstruction, condition, rawCodeTemp));//add a new condition
        }
        tempInstruction = new ArrayList<>();//delete all processed statements in order to avoid duplicates
        handleCompoundStatement(stmt.getCompound_Stmt().get(), condition);
      }
      else if (stmt.small_StmtIsPresent()) {
        Instruction notNullCheck = handleSmallStatement(stmt.getSmall_Stmt().get());
        if (notNullCheck != null) {
          tempInstruction.add(notNullCheck);
        }
      }
      else {
        System.err.println("Error in if-processing.");
      }
    }
    //if no "integrate" directives have been found in this block but there exist some local "integrates", we
    //have to deactivate it in this block in order to stop the integration
    tempInstruction = deactivateIntegration(tempInstruction);
    //blocks without any instructions can be skipped
    if (tempInstruction != null && tempInstruction.size() > 0) {
      this.blocks.add(new ConditionalBlock(tempInstruction, condition, rawCodeTemp));
    }
  }

  /**
   * Handles a simple statement, e.g assignment of function-call.
   *
   * @param input a statement
   * @return a instruction encapsulated in a object
   */
  private Instruction handleSmallStatement(ASTSmall_Stmt input) {
      if (input.assignmentIsPresent()) {
        //check if not supported functions are part of the assignment
        if (LEMSCollector.helper.containsFunctionCall(input.getAssignment().get().getExpr(), true)) {
          //Generate a proper error message
          container.getHelper().printNotSupportedFunctionInBlock(input);
          return new Assignment(input.getAssignment().get().getLhsVarialbe().toString(),
              "not_supported: "
                  + container.getLEMSExpressionsPrettyPrinter()
                  .print(input.getAssignment().get().getExpr(), false));
        }
        else {
          //in order to process assignments of type x-=y
          if (input.getAssignment().get().isCompoundMinus()) {
            return new Assignment(input.getAssignment().get().getLhsVarialbe().toString(),
                input.getAssignment().get().getLhsVarialbe().toString()
                    + "-" + LEMSCollector.helper.replaceConstantsWithReferences(container,
                    container.getLEMSExpressionsPrettyPrinter()
                        .print(input.getAssignment().get().getExpr(), false)));
          }//in order to process assignments of type x*=y
          else if (input.getAssignment().get().isCompoundProduct()) {
            return new Assignment(input.getAssignment().get().getLhsVarialbe().toString(),
                input.getAssignment().get().getLhsVarialbe().toString()
                    + "*" + LEMSCollector.helper.replaceConstantsWithReferences(container,
                    container.getLEMSExpressionsPrettyPrinter()
                        .print(input.getAssignment().get().getExpr(), false)));
          }//in order to process assignments of type x+=y
          else if (input.getAssignment().get().isCompoundSum()) {
            return new Assignment(input.getAssignment().get().getLhsVarialbe().toString(),
                input.getAssignment().get().getLhsVarialbe().toString()
                    + "+" + LEMSCollector.helper.replaceConstantsWithReferences(container,
                    container.getLEMSExpressionsPrettyPrinter()
                        .print(input.getAssignment().get().getExpr(), false)));
          }//in order to process assignments of type x/=y
          else if (input.getAssignment().get().isCompoundQuotient()) {
            return new Assignment(input.getAssignment().get().getLhsVarialbe().toString(),
                input.getAssignment().get().getLhsVarialbe().toString()
                    + "/" + LEMSCollector.helper.replaceConstantsWithReferences(container,
                    container.getLEMSExpressionsPrettyPrinter()
                        .print(input.getAssignment().get().getExpr(), false)));
          }
          else {
            return new Assignment(input.getAssignment().get().getLhsVarialbe().toString(),
                LEMSCollector.helper.replaceConstantsWithReferences(container,
                    container.getLEMSExpressionsPrettyPrinter()
                        .print(input.getAssignment().get().getExpr(), false)));
          }
        }
      }
      if (input.functionCallIsPresent()) {
        return handleFunctionCall(input.getFunctionCall().get());
      }
    System.err.println("Something went wrong in small statement processing.");
    return new Assignment("", "");
  }

  /**
   * This functions evokes further processing of a given function call.
   *
   * @param functionCall the function call.
   * @return a instruction which states steps steps need to be done
   */
  private Instruction handleFunctionCall(ASTFunctionCall functionCall) {
    switch (functionCall.getName().toString()) {
      case "integrate":
        return this.handleIntegrate(functionCall);
      case "emit_spike":
        return this.handleEmitSpike(functionCall);
      default:
        System.err.println("Not supported function call found.");
        return null;
    }
  }

  /**
   * Emit spike calls are simply transformed to a new function call.
   *
   * @param functionCall an function call containing "emitSpiek()"
   * @return a new function call which imitates the spike emission
   */
  private Instruction handleEmitSpike(ASTFunctionCall functionCall) {
    return new FunctionCall(functionCall);
  }

  /**
   * Checks a given set of instructions for existence of integrate directives. If non found,
   * a corresponding assignment which deactivates the integration in this step is generated.
   *
   * @param list a list of instructions
   * @return a possibly modified list of instructions
   */
  private List<Instruction> deactivateIntegration(List<Instruction> list) {
    boolean temp;
    //check all local dime derivatives
    for (String var : container.getLocalTimeDerivative()) {
      temp = false;
      //for all elements in the list, check if an integration directive has been found
      for (Instruction call : list) {
        if (call.getClass().equals(Assignment.class) && ((Assignment) call).getAssignedVariable().equals("ACT" + var)) {
          temp = true;
        }
      }
      //add a deactivation assignment to the list of directives if no integrate directive has been found
      if (!temp) {
        list.add(new Assignment("ACT" + var, "0"));
      }
    }
    return list;
  }

  /**
   * Generates a "integrate" counter piece in the target modeling language by replacing it with assignments.
   *
   * @param functionCall a integrate function call
   * @return a instruction which represents the integrate directive
   */
  private Instruction handleIntegrate(ASTFunctionCall functionCall) {
    //add a new state variable which symbolize that integration should be activated in necessary
    if (functionCall.getArgs().size() != 1) {
      System.err.println("Integrate is wrongly declared, >1 arguments provided!");
      return null;
    }
    else {
      for (StateVariable var : container.getStateVariablesList()) {
        if (var.getName().equals("ACT" + functionCall.getArgs().get(0).getVariable().get().getName().toString())) {
          var.setDefaultValue("0");//is should be now only activated if required
        }
      }
      //integrate the corresponding variable in this block
      return new Assignment("ACT" + functionCall.getArgs().get(0).getVariable().get().getName().toString(), "1");
    }
  }


  private String buildElseCondition(List<ASTExpr> list){
    StringBuilder builder = new StringBuilder();
    for (ASTExpr expr : list) {
      builder.append("(" + container.getLEMSExpressionsPrettyPrinter().print(expr, true) + ")").append(".and.");
    }
    builder.delete(builder.toString().length() - 5, builder.toString().length());//delete the last .and.
    return builder.toString();
  }

  /**
   * This method is called whenever it is required to generate a proper header
   * for a block of the dynamic routine.
   * @param input a node whose code is processed
   * @param rawCode
   * @return
   */
  private String buildHeader(ASTECNode input,String rawCode){
    String rawCodeTemp =
        "Generated from source lines " + input.get_SourcePositionStart().toString() +
            " to " + input.get_SourcePositionEnd().toString() + ".\n";
    rawCodeTemp = rawCodeTemp + rawCode;
    rawCodeTemp = rawCodeTemp.trim();
    rawCodeTemp = rawCodeTemp.replaceAll("\\n\\s*\\n", "\n");//kill empty lines
    return rawCodeTemp;
  }

  @SuppressWarnings("unused")//DebugMethod
  private void printHashMap(LinkedHashMap<String, String> input) {
    for (String key : input.keySet()) {
      System.out.println(key + "=" + input.get(key));
    }
  }

  @SuppressWarnings("unused")//DebugMethod
  private void printHashSet(HashSet<String> input) {
    for (String key : input) {
      System.out.println("Calls: " + key);
    }
  }

  public List<ConditionalBlock> getConditionalBlocks() {
    return this.blocks;
  }

  /**
   * This method casts a given instruction to a assignment call and is only used in the
   * template.
   */
  @SuppressWarnings("unused")//used in the template
  public Assignment getAssignmentFromInstruction(Instruction instr) {
    return (Assignment) instr;
  }

  /**
   * This method casts a given instruction to a function call and  is only used in the template.
   */
  @SuppressWarnings("unused")//used in the template
  public FunctionCall getFunctionCallFromInstruction(Instruction instr) {
    return (FunctionCall) instr;
  }

  /**
   * An instruction superclass used required in order to store all types of instructions in a single list.
   */
  public abstract class Instruction {
    //an empty super class
  }

  /**
   * This class stores a concrete instructions, namely an assignments.
   */
  public class Assignment extends Instruction {
    private String assignedVariable = "";

    private String assignedValue = "";

    public Assignment(String assignedVariable, String assignedValue) {
      this.assignedVariable = assignedVariable;
      this.assignedValue = assignedValue;
    }

    @SuppressWarnings("unused")//used in the template
    public String getAssignedVariable() {
      return this.assignedVariable;
    }

    @SuppressWarnings("unused")//used in the template
    public String getAssignedValue() {
      return this.assignedValue;
    }

  }

  /**
   * This class is used to store concrete instructions, namely function calls.
   */
  public class FunctionCall extends Instruction {
    private String functionName;

    private List<ASTExpr> args;

    public FunctionCall(ASTFunctionCall call) {
      this.args = call.getArgs();
      this.functionName = call.getName().toString();
    }

    @SuppressWarnings("unused")//used in the template
    public String getFunctionName() {
      return this.functionName;
    }

    @SuppressWarnings("unused")//used in the template
    public String getArgs() {
      StringBuilder newBuilder = new StringBuilder();
      for (ASTExpr expr : args) {
        newBuilder.append(container.getLEMSExpressionsPrettyPrinter().print(expr, false));
        newBuilder.append(",");
      }
      newBuilder.deleteCharAt(newBuilder.length() - 1);//delete the last "," before the end of the string
      return newBuilder.toString();
    }

    public List<ASTExpr> getArgsAsList() {
      return this.args;
    }
  }
}