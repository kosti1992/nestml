<#--This template generates all dimenions and units used in the concrete model-->
<#-- @param units List of all used units-->
<#-- @param dimensions List of all used dimensions-->
<#-- @author perun-->
<#compress>
<#attempt>
<#if global==true>
<Lems>
    <!-- Generated on ${.now}".-->
    <!-- Units and dimensions of:-->
    <!--<#list namesOfNeurons as neuronName> ${neuronName} <#if neuronName_has_next>;</#if></#list>.-->
</#if>
<#recover>
</#attempt>
<#attempt>
${signature("units","dimensions")}
<#recover>
</#attempt>
<#list dimensions as dimension>
<Dimension name="${dimension.getName()}" <@compress single_line=true>
	<#if dimension.getMASS()!=0> m="${dimension.getMASS()}"</#if>
	<#if dimension.getLENGTH()!=0>  l="${dimension.getLENGTH()}"</#if>
	<#if dimension.getTIME()!=0> t="${dimension.getTIME()}"</#if>
	<#if dimension.getELECTRIC_CURRENT()!=0> i="${dimension.getELECTRIC_CURRENT()}"</#if>
	<#if dimension.getTHERMODYNAMIC_TEMPERATURE()!=0> k="${dimension.getTHERMODYNAMIC_TEMPERATURE()}"</#if>
	<#if dimension.getAMOUNT_OF_SUBSTANCE()!=0> n="${dimension.getAMOUNT_OF_SUBSTANCE()}"</#if>
	<#if dimension.getLUMINOUS_INTENSITY()!=0> j="${dimension.getLUMINOUS_INTENSITY()}"</#if></@compress>/>
</#list>

<#list units as unit>
<Unit symbol="${unit.getSymbol()}" dimension="${unit.getDimensionName()}" power="${unit.getPower()}"/>
</#list>
<#attempt>
<#if global==true>
</Lems>
</#if>
<#recover>
</#attempt>
</#compress>