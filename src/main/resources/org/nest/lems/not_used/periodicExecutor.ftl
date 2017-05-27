                      <#--!!!!!!This template is currently not used!!!!!!-->
<#-- This template generates the initial regime which executes the update-routine periodically after -->
<#-- a certain given amout of time passed.                                                           -->
<#--
     @param container LEMSCollector
-->
<#--@author perun -->
${signature("container")}
  <#if container.getUsedExecutor()=="periodicExecutor"&&container.automatonIsPresent()>
              <!--Executor generated from org.nest.lems.executor.periodicExecutor-->
              <Regime name="idle" initial="true">
                  <StateVariable name="tsince" dimension="time"/>
                  <TimeDerivative variable="tsince" mValue="1"/>
                  <OnCondition test="tsince .gt. ${container.getPeriodicExecutorTime()}">
                      <StateAssignment variable="tsince" mValue="0"/>
                      <Transition regime="${container.getFirstRegime().getStateName()}"/>
                  </OnCondition>
              </Regime>
  </#if>
