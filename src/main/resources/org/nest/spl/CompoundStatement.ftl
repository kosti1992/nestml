<#--
  Handles the compound statement.
  @grammar: Compound_Stmt = IF_Stmt | FOR_Stmt | WHILE_Stmt;
-->
<#if ast.getIF_Stmt().isPresent()>
${tc.include("org.nest.spl.compound_statement.IfStatement", ast.getIF_Stmt().get())}
<#elseif ast.getFOR_Stmt().isPresent()>
${tc.include("org.nest.spl.compound_statement.ForStatement", ast.getFOR_Stmt().get())}
<#elseif ast.getWHILE_Stmt().isPresent()>
${tc.include("org.nest.spl.compound_statement.WhileStatement", ast.getWHILE_Stmt().get())}
</#if>