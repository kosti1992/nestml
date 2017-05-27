<#--
  Assigns a tmp mValue which was read from the dictionary to the corresponding block variable.

  @param variable VariableSymbol
  @result C++ Block
-->
${signature("variable")}
<#if variable.hasSetter() || !variable.isFunction()>
  ${names.setter(variable)}(tmp_${statusNames.name(variable)});
<#else>
  // ignores '${statusNames.name(variable)}' ${declarations.printVariableType(variable)}' since it is an function and setter isn't defined
</#if>
