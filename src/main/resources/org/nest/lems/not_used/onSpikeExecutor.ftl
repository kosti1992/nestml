<#-- This template generates the initial regime which executes the update-routine each time a spike is received. -->
<#-- @param functionCall ASTExpr
     @param container LEMSCollector
-->
<#--@author perun -->
${signature("container")}
<#if container.getUsedExecutor()=="onSpikeExecutor"&&container.automatonIsPresent()>
              <!--Executor generated from org.nest.lems.executor.onSpikeExecutor-->
              <Regime name="idle" initial="true">
                <#if container.getPortsSet()?size == 0>
                <OnEvent port="Please fill by hand!">
                   <Transition regime="${container.getFirstRegime().getStateName()}"/>
                </OnEvent>
                </#if>
                <#list container.getPortsSet() as port>
                <#if port.getDirection()=="in">
                <OnEvent port="${port.getName()}">
                   <Transition regime="${container.getFirstRegime().getStateName()}"/>
                </OnEvent>
                    </#if>
                </#list>
              </Regime>
</#if>