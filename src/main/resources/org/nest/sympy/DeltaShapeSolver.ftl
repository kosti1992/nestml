from sympy import *
from sympy.matrices import zeros

__a__, __h__, delta = symbols('__a__ __h__ delta')
<#compress>
    <#list variables as variable> ${variable.getName()} , </#list> = symbols('<#list variables as variable> ${variable.getName()} </#list>')
</#compress>

# Handle aliases
<#list aliases as function>
${function.getName()} = ${printer.print(odeTransformer.replaceSumCalls(function.getDeclaringExpression().get()))}
</#list>

# Shapes must be symbolic for the differetiation step
rhsTmp = ${printer.print(odeTransformer.replaceSumCalls(ode.getRhs()))}
constantInputs = simplify(1/diff(rhsTmp, ${shapes[0].getLhs()}) * (rhsTmp - diff(rhsTmp, ${ode.getLhs().getSimpleName()})*${ode.getLhs().getSimpleName()}) - (
<#assign operator = "">
<#compress> <#list shapes as eq>
${operator} ${eq.getLhs()}
<#assign operator = "+">
</#list> </#compress>
))

# TODO take the comment for the source model
<#list shapes as eq>
${eq.getLhs()} = ${printer.print(odeTransformer.replaceSumCalls(eq.getRhs()))}
</#list>
rhs = ${printer.print(odeTransformer.replaceSumCalls(ode.getRhs()))}
dev${ode.getLhs().getSimpleName()} = diff(rhs, ${ode.getLhs().getSimpleName()})
dev_t_dev${ode.getLhs().getSimpleName()} = diff(dev${ode.getLhs().getSimpleName()}, t)

if dev_t_dev${ode.getLhs().getSimpleName()} == 0:
    solverType = open('${neuronName}.solverType.tmp', 'w')
    solverType.write("exact")

    propagatorStepFile = open('${neuronName}.propagator.step.tmp', 'w')
    propagatorStepFile.write("${ode.getLhs().getSimpleName()} += P30 * (" + str(constantInputs) + ") ")
    # calculate -1/Tau
    c1 = diff(rhs, ${ode.getLhs().getSimpleName()})
    # The symbol must be declared again. Otherwise, the right hand side will be used for the derivative
    ${shapes[0].getLhs()} = symbols("${shapes[0].getLhs()}")
    c2 = diff( ${printer.print(odeTransformer.replaceSumCalls(ode.getRhs()))} , ${shapes[0].getLhs()})
    f = open('${neuronName}.P30.tmp', 'w')
    f.write("P30 real = " + str(simplify(c2 / c1 * (exp(__h__ * c1) - 1))) + "# P00 expression")