<#--This template generates a LEMS model from the internal representation of a NESTML model stored in the -->
<#--glex global-value "container".-->
<#--@author perun -->
<!-- Generated on ${.now} from  NESTML-model "${container.getNeuronName()}".-->

<Lems>
<#if (container.getConfig().isUnitsExternal())==false>
${tc.includeArgs("org.nest.lems.units_dimensions",[container.getUnitsSet(),container.getDimensionsSet()])}
<#else>
    <Include file="units_dimensions.xml"/>
</#if>

    <ComponentType name="${container.getNeuronName()}"<#if container.getModelIsExtension()>
                   extends="${container.getExtendedModel()}" </#if>>
    <#if (container.getNotConvertedElements()?size > 0)>
        <!--Following elements are not supported or required by LEMS:-->
        <#list container.getNotConvertedElements() as notSupported>
        <!--${notSupported}-->
        </#list>
    </#if>

    <#list container.getConstantsList() as constant>
    <#if constant.isParameter()=true>
        <Parameter name="${constant.getName()}" dimension="${constant.getDimension()}"/>
    <#else>
        <Constant <@compress single_line=true>name="${constant.getName()}" dimension="${constant.getDimension()}"
                      value="${constant.getValueUnit()}"/></@compress>
    </#if>
    </#list>

    <#list container.getDerivedParametersList() as derivedParameter>
        <DerivedParameter <@compress single_line=true>name="${derivedParameter.getName()}"
                              dimension="${derivedParameter.getDimension()}"
                              value="${derivedParameter.getDerivationInstruction().print()}"/></@compress>
    </#list>

    <#list container.getPortsList() as port>
        <EventPort name="${port.getName()}" direction="${port.getDirection()}"/>
    </#list>

    <#list container.getAttachments() as att>
        <Attachments name="${att.getBindName()}" type="${att.getBindType()}"/>
    </#list>

    <#if container.getDynamicElementsArePresent()>
        <Dynamics>
          <#list container.getStateVariablesList() as stateVariable>
              <StateVariable name="${stateVariable.getName()}" dimension="${stateVariable.getDimension()}"/>
          </#list>
          <#list container.getDerivedVariablesList() as derivedVariable>
            <#if !derivedVariable.isExternal()>
              <DerivedVariable <@compress single_line=true> name="${derivedVariable.getName()}"
                                                            dimension="${derivedVariable.getDimension()}"
                                                            value="${derivedVariable.getDerivationInstruction().print()}" </@compress>/>
            <#else>
              <DerivedVariable <@compress single_line=true> name="${derivedVariable.getName()}"
                                                            dimension="${derivedVariable.getDimension()}"
                                                            select="${derivedVariable.getDerivationInstruction().print()}"
                                                            reduce="${derivedVariable.getReduce()}"</@compress>/>
            </#if>
          </#list>

          <#if (container.getStateVariablesList()?size>0) >
              <OnStart>
                <#list container.getStateVariablesList() as defaults>
                <StateAssignment variable="${defaults.getName()}" value="${defaults.print()}"/>
                </#list>
              </OnStart>
          </#if>

          <#list (container.getEquations())?keys as var>
              <TimeDerivative variable="${var}" value="${container.getEquations()[var].print()}"/>
          </#list>

          <#if container.conditionsPresent()>
            <#list container.getConditionalBlocks() as condBlock>
              <#list condBlock.getInitialCode() as line><#--print the information header-->
              <!--${line}-->
              </#list>
              <OnCondition test="${condBlock.getCondition().print()}">
                  <#list condBlock.getInstructions() as instr>
                  <#if condBlock.getInstructionType(instr)=="Assignment">
                  <StateAssignment variable="${container.getAutomaton().getAssignmentFromInstruction(instr).printAssignedVariable()}" value="${container.getAutomaton().getAssignmentFromInstruction(instr).printAssignedValue()}"/>
                  <#elseif condBlock.getInstructionType(instr)=="FunctionCall">
                  <#attempt>
                  ${tc.includeArgs("org.nest.lems.functions.${container.getAutomaton().getFunctionCallFromInstruction(instr).printName()}",
                  [container.getAutomaton().getFunctionCallFromInstruction(instr),container])}
                  <#recover>
                  <Text name="function not defined:${container.getAutomaton().getFunctionCallFromInstruction(instr).printName()}"/>
                  </#attempt>
                  </#if>
                  </#list>
              </OnCondition>
            </#list>
          </#if>
        </Dynamics>
    </#if>
    </ComponentType>
</Lems>