/*
 * Copyright (c) 2015 RWTH Aachen. All rights reserved.
 *
 * http://www.se-rwth.de/
 */
package org.nest.nestml._cocos;

/**
 * Factory for CoCo error strings.
 *
 * @author traeder
 */
public class CocoErrorStrings {
  private static CocoErrorStrings instance = new CocoErrorStrings();

  private CocoErrorStrings() {
  }

  public static CocoErrorStrings getInstance() {
    return instance;
  }

  String getErrorMsgAssignment(LiteralsHaveTypes coco){
    return LiteralsHaveTypes.ERROR_CODE + ": Assignment of a literal to a UNIT type variable must carry a Unit Symbol";
  }

  String getErrorMsgReturn(LiteralsHaveTypes coco){
    return LiteralsHaveTypes.ERROR_CODE + ": Return statement must specify unit type";
  }

  String getErrorMsgCall(LiteralsHaveTypes coco){
    return LiteralsHaveTypes.ERROR_CODE + ": Parameters to function calls must specify unit type";
  }

  String getErrorMsg(AliasHasDefiningExpression coco) {
    return AliasHasDefiningExpression.ERROR_CODE + ":" + "'alias' must be defined through an expression.";
  }

  String getErrorMsg(AliasHasNoSetter coco, String aliasVar, String varTypeName) {
    return AliasHasNoSetter.ERROR_CODE + ":" + "Alias-variable '" + aliasVar + "' needs a setter-function: set_"
        + aliasVar + "(v " + varTypeName + ")";
  }

  String getErrorMsg(AliasHasOneVar coco) {
    return AliasHasOneVar.ERROR_CODE + ":" + "'alias' declarations must only declare one variable.";
  }

  String getErrorMsg(AliasInNonAliasDecl coco, String usedAlias) {
    return AliasInNonAliasDecl.ERROR_CODE + ":" + "Alias variable '"
        + usedAlias
        + "' cannot be used in default-value declaration of non-alias variables.";
  }

  String getErrorMsgInvariantMustBeBoolean(BooleanInvariantExpressions coco, String expressionType) {
    return BooleanInvariantExpressions.ERROR_CODE + ":" + "The type of the invariant expression must be boolean and not: " +
        expressionType;
  }

  String getErrorMsgCannotComputeType(BooleanInvariantExpressions coco, String invariantType) {
    return BooleanInvariantExpressions.ERROR_CODE + ":" + "Cannot compute the type: " + invariantType;
  }

  String getErrorMsg(BufferNotAssignable coco, String bufferName) {
    return BufferNotAssignable.ERROR_CODE + ":" + "Buffer '" + bufferName + "' cannot be reassigned.";
  }

  String getErrorMsg(ComponentHasNoDynamics coco) {
    return ComponentHasNoDynamics.ERROR_CODE + ":" + "Components do not have dynamics function.";
  }

  String getErrorMsg(ComponentNoInput coco) {
    return ComponentNoInput.ERROR_CODE + ":" + "Components cannot have inputs, since they are not elements of a "
        + "neuronal network.";
  }

  String getErrorMsg(ComponentNoOutput coco) {
    return ComponentNoOutput.ERROR_CODE + ":" + "Components do not have outputs, only neurons have outputs.";
  }

  String getErrorMsgWrongReturnType(CorrectReturnValues coco,
                                           String functionName, String functionReturnTypeName) {
    return CorrectReturnValues.ERROR_CODE + ":" + "Function '" + functionName + "' must return a result of type "
        + functionReturnTypeName + ".";
  }

  String getErrorMsgCannotConvertReturnValue(CorrectReturnValues coco,
                                                    String expressionTypeName, String functionReturnTypeName) {
    return CorrectReturnValues.ERROR_CODE + ":" + "Cannot convert from " + expressionTypeName
        + " (type of return expression) to " + functionReturnTypeName
        + " (return type), since the first is real domain and the second is in the integer "
        + "domain and conversion reduces the precision.";
  }

  String getErrorMsgCannotDetermineExpressionType(CorrectReturnValues coco) {
    return CorrectReturnValues.ERROR_CODE + ":" + "Cannot determine the type of the expression";
  }

  String getErrorMsg(CurrentInputIsNotInhExc coco) {
    return CurrentInputIsNotInhExc.ERROR_CODE + ":" + "Current input can neither be inhibitory nor excitatory.";
  }

  String getErrorMsgAssignToNonState(EquationsOnlyForStateVariables coco,
                                            String variableName) {
    return EquationsOnlyForStateVariables.ERROR_CODE + ":" + "The variable '" + variableName + "' is not a state"
        + " variable and, therefore, cannot be used on the left side of an equation.";
  }

  String getErrorMsgVariableNotDefined(EquationsOnlyForStateVariables coco) {
    return EquationsOnlyForStateVariables.ERROR_CODE + ":" + "Variable is not defined in the current scope.";
  }

  String getErrorMsg(FunctionHasReturnStatement coco, String functionName, String returnType) {
    return FunctionHasReturnStatement.ERROR_CODE + ":" + "Function '" + functionName
        + "' must return a result of type '"
        + returnType;
  }

  String getErrorMsgGet_InstanceDefined(GetterSetterFunctionNames coco) {
    return GetterSetterFunctionNames.ERROR_CODE + ":" + "The function 'get_instance' is going to be generated. Please use another name.";
  }

  String getErrorMsgGeneratedFunctionDefined(GetterSetterFunctionNames coco,
                                                    String functionName, String variableName) {
    return GetterSetterFunctionNames.ERROR_CODE + ":" + "The function '" + functionName + "' is going to be generated, since"
        + " there is a variable called '" + variableName + "'.";
  }

  String getErrorMsg(I_SumHasCorrectParameter coco, String expression) {
    return I_SumHasCorrectParameter.ERROR_CODE + ":" + "The arguments of the I_sum must be atomic expressions: "
        + "e.g. V_m and not : " + expression;
  }

  String getErrorMsg(InvalidTypesInDeclaration coco, String typeName) {
    return InvalidTypesInDeclaration.ERROR_CODE + ":" + "The type " + typeName + " is a neuron/component. No neurons/components allowed " +
        "in this place. Use the use-statement.";
  }

  String getErrorMsg(MemberVariableDefinedMultipleTimes coco, String varName,
                            int line, int column) {
    return MemberVariableDefinedMultipleTimes.ERROR_CODE + ":" + "Variable '" + varName + "' defined previously defined i line: "
        + line + ":" + column;
  }

  String getErrorMsgDeclaredInIncorrectOrder(MemberVariablesInitialisedInCorrectOrder coco,
                                                    String varName, String declaredName) {
    return MemberVariablesInitialisedInCorrectOrder.ERROR_CODE + ":" + "Variable '"
        + varName
        + "' must be declared before it can be used in declaration of '"
        + declaredName + "'.";
  }

  String getErrorMsgVariableNotDefined(MemberVariablesInitialisedInCorrectOrder coco,
                                              String pos, String varName) {
    return MemberVariablesInitialisedInCorrectOrder.ERROR_CODE + ":" + pos + ": Variable '" +
        varName + "' is undefined.";
  }

  String getErrorMsgNeuronHasNoSymbol(MultipleFunctionDeclarations coco, String neuronName) {
    return MultipleFunctionDeclarations.ERROR_CODE + ":" + "The neuron symbol: " + neuronName + " has no symbol.";
  }

  String getErrorMsgParameterDefinedMultipleTimes(MultipleFunctionDeclarations coco, String funname) {
    return MultipleFunctionDeclarations.ERROR_CODE + ":" + "The function '" + funname
        + " parameter(s) is defined multiple times.";
  }

  String getErrorMsgNoScopePresent(MultipleFunctionDeclarations coco) {
    return MultipleFunctionDeclarations.ERROR_CODE + ":" + "Run symbol table creator.";
  }

  String getErrorMsgMultipleInhibitory(MultipleInhExcInput coco) {
    return MultipleInhExcInput.ERROR_CODE + ":" + "Multiple occurrences of the keyword 'inhibitory' are not allowed.";
  }

  String getErrorMsgMultipleExcitatory(MultipleInhExcInput coco) {
    return MultipleInhExcInput.ERROR_CODE + ":" + "Multiple occurrences of the keyword 'excitatory' are not allowed.";
  }

  String getErrorMsg(MultipleOutputs coco, int numOutput) {
    return MultipleOutputs.ERROR_CODE + ":" + "Neurons have at most one output and not " + numOutput + ".";
  }

  String getErrorMsg(NESTFunctionNameChecker coco, String funName) {
    return NESTFunctionNameChecker.ERROR_CODE + ":" + "The function-name '" + funName
        + "' is already used by NEST. Please use another name.";
  }

  String getErrorMsgDynamicsNotPresent(NeuronNeedsDynamics coco) {
    return NeuronNeedsDynamics.ERROR_CODE + ":" + "Neurons need at least one dynamics function.";
  }

  String getErrorMsgMultipleDynamics(NeuronNeedsDynamics coco) {
    return NeuronNeedsDynamics.ERROR_CODE + ":" + "Neurons need at most one dynamics function.";
  }

  String getErrorMsg(NeuronWithoutInput coco) {
    return NeuronWithoutInput.ERROR_CODE + ":" + "Neurons need some inputs.";
  }

  String getErrorMsg(NeuronWithoutOutput coco) {
    return NeuronWithoutOutput.ERROR_CODE + ":" + "Neurons need some outputs.";
  }

  String getErrorMsg(TypeIsDeclaredMultipleTimes coco, String typeName) {
    return TypeIsDeclaredMultipleTimes.ERROR_CODE + ":" + "The type '" + typeName + "' is defined multiple times.";
  }

  String getErrorMsgOnlyComponentsForNeurons(UsesOnlyComponents coco, String typeName,
                                                    String predefinedTypeName) {
    return UsesOnlyComponents.ERROR_CODE + ":" + "Only components can be used by neurons/components and not " + typeName + " of the type: " +
        predefinedTypeName + " .";
  }

  String getErrorMsgOnlyComponentsForComponents(UsesOnlyComponents coco, String typeName) {
    return UsesOnlyComponents.ERROR_CODE + ":" + "Only components can be used by components and not " + typeName + " that is a neuron, not a "
        + "component";
  }
}
