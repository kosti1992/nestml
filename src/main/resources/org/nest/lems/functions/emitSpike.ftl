<#--This template generates an outgoing event if a spike has been emitted -->
<#-- @param functionCall ASTExpr
     @param container LEMSCollector
-->
<#--@author perun -->
<#compress>
${signature("functionCall","container")}
<#if container.outputPortDefined()&&functionCall.getFunctionName()=="emitSpike">
<EventOut port="${container.getOutputPort().getName()}"/>
</#if>
</#compress>