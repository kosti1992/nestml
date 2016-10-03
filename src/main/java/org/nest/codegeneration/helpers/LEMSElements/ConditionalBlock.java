package org.nest.codegeneration.helpers.LEMSElements;


import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;

/**
 * This class stores a block which will be evoked during the simulation on a certain condition. This block is
 * a part of the dynamic routine.
 * @author perun
 */
public class ConditionalBlock {
  private List<DynamicRoutine.Instruction> instructions;
  private String condition;
  private String rawCode;//stores a header printed next to the block

  public ConditionalBlock(List<DynamicRoutine.Instruction> instructions,String condition,String rawCode){
    this.instructions = instructions;
    this.condition = condition;
    this.rawCode = rawCode;
  }

  @SuppressWarnings("unused")//used in the template
  public Object[] getInitialCode() {
    BufferedReader bufReader = new BufferedReader(new StringReader(rawCode));
    return bufReader.lines().toArray();
  }
  @SuppressWarnings("unused")//used in the template
  public String getCondition(){
    return this.condition;
  }
  @SuppressWarnings("unused")//used in the template
  public List<DynamicRoutine.Instruction> getInstructions(){
    return this.instructions;
  }
  @SuppressWarnings("unused")//used in the template
  public String getInstructionType(DynamicRoutine.Instruction instr){
    if(instr.getClass().equals(DynamicRoutine.Assignment.class)){
      return "Assignment";
    }
    if(instr.getClass().equals(DynamicRoutine.FunctionCall.class)){
      return "FunctionCall";
    }
    else{
      return "Class not defined";
    }

  }
}
