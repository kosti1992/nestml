package org.nest.codegeneration.helpers.LEMS.Elements;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nest.commons._ast.ASTFunctionCall;

/**
 * Caution: Due to the problems regarding regimes, especially their handling as
 * provided by LEMS, this class is no longer used.
 * This class represents a state/regime in the dynamic routine of the target model.
 * @author perun
 */
public class Regime {
  private String state_name;
  private Map<String,String> assignments;//<Variable,Assigned value>
  private List<ASTFunctionCall> functionCalls;//collection of all function-calls
  private Map<String,Regime> conditionTarget;//outgoing transitions with corresponding conditions
  private String initialCode;//the raw code required for a header


  protected Regime(String name,List<ASTFunctionCall> fCalls,Map<String,
      Regime> targets,Map<String,String> assigns, String initCode){
    this.state_name = name;
    this.conditionTarget = targets;
    this.functionCalls = fCalls;
    this.assignments = assigns;
    this.initialCode = initCode;
  }

  @SuppressWarnings("unused")//Used in the template
  public String getStateName() {
    return this.state_name;
  }

  @SuppressWarnings("unused")//Used in the template
  public Map<String,Regime> getConditionTarget(){
    if(this.conditionTarget!=null){
      return this.conditionTarget;
    }
    return new HashMap<>();

  }

  @SuppressWarnings("unused")//Used in the template
  public Map<String,String> getAssignments(){
    if(this.assignments!=null){
      return this.assignments;
    }
    return new HashMap<>();
  }

  @SuppressWarnings("unused")//Used in the template
  public List<ASTFunctionCall> getFunctionCalls(){
    if(this.functionCalls!=null) {
      return this.functionCalls;
    }
    return new ArrayList<>();
  }

  @SuppressWarnings("unused")//Used in the template
  public String getAssignedValue(Object o){
    return this.getAssignments().get(o);
  }

  @SuppressWarnings("unused")//Used in the template
  public Set<String> getAssignedVariables(){
    return this.getAssignments().keySet();
  }

  @SuppressWarnings("unused")//Used in the template
  public Set<String> getConditions(){
    return this.getConditionTarget().keySet();
  }

  @SuppressWarnings("unused")//Used in the template
  public Regime getTransitionOnCondition(Object o){
    return this.getConditionTarget().get(o);
  }

  @SuppressWarnings("unused")//Used in the template
  public Object[] getInitialCode() {
    BufferedReader bufReader = new BufferedReader(new StringReader(initialCode));
    return bufReader.lines().toArray();
  }
}

