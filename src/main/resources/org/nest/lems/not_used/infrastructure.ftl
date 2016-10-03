                    <#--!!!!!!This template is currently not used!!!!!!-->
<#--This template generates a LEMS infrastructure which is used to start the simulation.-->
<#--@author perun -->
<#--Modified Version of example1.xml as provided by http://lems.github.io/LEMS/-->
<!-- Generated on ${.now}.-->

<Lems><#--Include predefined components.-->
    <Include file="predefined_components.xml"/>
    <#--Include all models-->
<#list neuronPortMap?keys as neuron_name>
    <Include file="${neuron_name}.xml"/>
</#list>

<#-- initilize component elements-->
<#list components as component>
    <#assign keySet = component?keys>
    <#if keySet?seq_contains("Component")>
    <${component["type"]}<#list keySet as key><#if key!="type"&&key!="Component"&&key!="hide"> ${key}="${component[key]}"</#if></#list>/>
    </#if>
</#list>

    <#--The declaration of a network of neurons.-->
    <Network id="net1">
    <#list components as component>
        <#assign keySet = component?keys>
        <#if keySet?seq_contains("Component")&&!keySet?seq_contains("hide")>
        <Population id="${component["id"]}" component="${component["id"]}" size="1"/>
        </#if>
    </#list>
    <#--generate the connections-->
    <#list components as component>
        <#assign keySet = component?keys>
        <#if keySet?seq_contains("Connection")>
        <explicitInput input="${component["input"]}" target="${component["target"]}[0]" destination="${component["sourceType"]}"/>
        </#if>
    </#list>
    </Network>

    <#--The actual simulation.-->
    <Simulation id="sim1" length="${helper.getNumberFormatted(config.getSimLength())}ms" step="${helper.getNumberFormatted(config.getSimSteps())}ms" target="net1">
        <#list components as component>
            <#list neuronPortMap?keys as name><#--print all neurons with corresponding connections-->
            <#if ((component?keys)?seq_contains("Component"))&&name=component["type"]>
            <Display id="d${component?index}" title="${config.getSimName()} - ${component["id"]}"
                     timeScale="1ms" xmin="-10" xmax="90" ymin="-90" ymax="60">
            <#assign curCollecotor=neuronPortMap[name]>
            <#list curCollecotor.getStateVariablesList() as var>
            <#if var.isPlottable()><#--plot an variable only of required and not suppressed-->
                <Line id="${component["id"]}.${var.getName()}" quantity="${component["id"]}[0]/${var.getName()}" scale="1${var.getUnit()}" timeScale="1ms" color="${helper.gencode()}"/>
            </#if>
            </#list>
            </Display>

            </#if>
            </#list>
        </#list>
    </Simulation>

</Lems>

