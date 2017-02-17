<#--
  Generates C++ declaration
  @grammar: AliasDecl = ([hide:"-"])? ([function:"function"])?
                        Declaration ("[" invariants:Expr (";" invariants:Expr)* "]")?;
                        Declaration = vars:Name ("," vars:Name)* (type:QualifiedName | primitiveType:PrimitiveType) ( "=" Expr )? ;
  @param ast ASTAliasDecl
  @param tc templatecontroller
  @result TODO
-->
${signature("variable")}

<#assign varDomain = declarations.getDomainFromType(variable.getType())>

<#if varDomain == "double" && variable.isRecordable()>
insert_("${variable.getName()}", &${neuronName}::${names.getter(variable)});
<#else>
// ignores the ${variable.getName()} with the domain type ${varDomain}
</#if>
