                    <#-- This template is currently not used since integrate is replaces by an activator-->
<#-- This function simulates the integration of the given argument -->
<#-- @param functionCall ASTExpr
     @param container LEMSCollector
-->
<#--@author perun -->
<#compress>
${signature("functionCall","container")}
<#if functionCall.getFunctionName()=="integrate"><#assign args = functionCall.getArgs()>
<TimeDerivative variable="${args}" mValue="${container.getDifferentialEquationOnVariable(args)}"/>
</#if>
</#compress>