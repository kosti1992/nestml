package org.nest.codegeneration.helpers.LEMSElements;

import java.util.List;
import java.util.stream.Collectors;

import org.nest.codegeneration.helpers.Expressions.Expression;
import org.nest.codegeneration.helpers.Expressions.NumericalLiteral;
import org.nest.codegeneration.helpers.Expressions.Operator;
import org.nest.codegeneration.helpers.Expressions.Variable;
import org.nest.commons._ast.ASTExpr;
import org.nest.commons._ast.ASTFunctionCall;
import org.nest.commons._ast.ASTVariable;
import org.nest.ode._ast.ASTEquation;
import org.nest.spl._ast.ASTBlock;
import org.nest.spl._ast.ASTELIF_Clause;
import org.nest.spl._ast.ASTSmall_Stmt;
import org.nest.spl._ast.ASTStmt;
import org.nest.spl.prettyprinter.LEMS.LEMSExpressionsPrettyPrinter;
import org.nest.symboltable.symbols.TypeSymbol;
import org.nest.symboltable.symbols.VariableSymbol;

/**
 * This class provides a set of methods which are used during the transformation in order to retrieve or
 * transform certain values.
 *
 * @author perun
 */
public class HelperCollection {
	LEMSCollector container;

	//this is a collection of global constants and prefixes. in case something has to be changed, this is the point where
	public String NOT_SUPPORTED = "NOT_SUPPORTED";
	public String NO_UNIT = "";
	public String DIMENSION_NONE = "none";
	public String PREFIX_INIT = "INIT";
	public String PREFIX_DIMENSION = "DimensionOf_";
	public String PREFIX_CONSTANT = "CON";
	public String PREFIX_ACT = "ACT";


	public HelperCollection(LEMSCollector collector) {
		container = collector;
	}


	/**
	 * Returns all spike input ports of a given set.
	 *
	 * @param ports a list of used ports
	 * @return a list of all spike ports
	 */
	@SuppressWarnings("unused")//used in the template
	public List<String> getSpikePorts(List<String> ports) {
		return ports.stream().filter(st -> st.endsWith("spikes")).collect(Collectors.toList());
	}

	/**
	 * This function avoids problems with locale settings regarding number formats and print a number with "." as sole
	 * separator.
	 *
	 * @return number as string
	 */
	@SuppressWarnings("unused")//used in the template
	public String getNumberFormatted(double input) {
		return String.valueOf(input);
	}

	/**
	 * Extracts the dimension of a given variable-type. This class has to be modified if new units and dimensions
	 * a introduced to NESTML.
	 *
	 * @param input the type-symbol of the a variable.
	 * @return the name of the dimension as String
	 */
	public String typeToDimensionConverter(TypeSymbol input) {
		if (input.getName().equals("boolean") || input.getName().equals("void") || input.getName().equals("string")) {
			return NOT_SUPPORTED;
		}
		if (input.getType() == TypeSymbol.Type.PRIMITIVE) {
			return DIMENSION_NONE;
		} else if (input.getType() == TypeSymbol.Type.UNIT) {
			return PREFIX_DIMENSION + input.prettyPrint();
		}
		System.err.println(input.prettyPrint() + " : not supported!");
		return null;
	}

	/**
	 * Converts a unit prefix to power of base 10.
	 *
	 * @param var_type Name of the unit represented as String.
	 * @return Power of the prefix as int.
	 */
	public int powerConverter(String var_type) {
		if (var_type == null || var_type.length() < 2) {
			return 0;
		}
		if (var_type.startsWith("d")) {
			return -1;
		} else if (var_type.startsWith("c")) {
			return -2;
		} else if (var_type.startsWith("m") && var_type.length() > 1) {// in order to avoid confusion between meter and mili
			return -3;
		} else if (var_type.startsWith("mu")) {
			return -6;
		} else if (var_type.startsWith("n")) {
			return -9;
		} else if (var_type.startsWith("p")) {
			return -12;
		} else if (var_type.startsWith("f")) {
			return -15;
		} else if (var_type.startsWith("a")) {
			return -18;
		}
		//positive powers
		else if (var_type.startsWith("da")) {
			return 1;
		} else if (var_type.startsWith("h")) {
			return 2;
		} else if (var_type.startsWith("k")) {
			return 3;
		} else if (var_type.startsWith("M")) {
			return 6;
		} else if (var_type.startsWith("G")) {
			return 9;
		} else if (var_type.startsWith("T")) {
			return 12;
		} else if (var_type.startsWith("P")) {
			return 15;
		} else if (var_type.startsWith("E")) {
			return 18;
		}
		System.err.println(var_type + " prefix not supported!");
		return 1;
	}

	/**
	 * Checks whether a given function call name is supported by the target modeling language or not.
	 * If new concepts are supported, add here.
	 * @param expr The function call name which will be checked.
	 * @return true, if supported
	 */
	public boolean mathematicalFunctionIsSupported(String expr) {
		switch (expr) {
			case "exp":
				return true;
			case "log":
				return true;
			case "pow":
				return true;
			case "sin":
				return true;
			case "cos":
				return true;
			case "tan":
				return true;
			case "sinh":
				return true;
			case "cosh":
				return true;
			case "tanh":
				return true;
			case "sqrt":
				return true;
			case "ceil":
				return true;
			case "ln":
				return true;
			case "random":
				return true;
			case "abs":
				return true;
			case "factorial":
				return true;
			case "product":
				return true;
			case "sum":
				return true;
			default:
				return false;
		}
	}

	/**
	 * Indicates whether a given ASTExpression contains a function call or not.
	 *
	 * @param expr          the expression which will be checked.
	 * @param skipSupported if true, all supported function calls will be skipped
	 * @return true, if expression or sub-expression contains call.
	 */
	public boolean containsFunctionCall(ASTExpr expr, boolean skipSupported) {
		boolean temp = false;
		//if more functions are supported
		if (expr.functionCallIsPresent() && !(skipSupported && mathematicalFunctionIsSupported(
				expr.getFunctionCall().get().getName().toString()))) {
			temp = true;
		}
		if (expr.exprIsPresent()) {
			temp = temp || containsFunctionCall(expr.getExpr().get(), skipSupported);
		}
		if (expr.leftIsPresent()) {
			temp = temp || containsFunctionCall(expr.getLeft().get(), skipSupported);
		}
		if (expr.rightIsPresent()) {
			temp = temp || containsFunctionCall(expr.getRight().get(), skipSupported);
		}
		return temp;
	}

	/**
	 * Inspects an expression and replaces all directly stated constants with references
	 * to additionally created constants, e.g. V_m +10mV -> V_m + CON10mV
	 *
	 * @param container a container which will be used to store new constants
	 * @param expr      the expression in which a constant will be replaced
	 * @return
	 */
	public Expression replaceConstantsWithReferences(LEMSCollector container, Expression expr) {
		List<Expression> temp = expr.getNumericals();
		for (Expression exp : temp) {
			if (((NumericalLiteral) exp).hasType()) {
				int[] dec = convertTypeDeclToArray(((NumericalLiteral) exp).getType().get().getSerializedUnit());
				//create the required units and dimensions
				Dimension tempDimension =
						new Dimension(PREFIX_DIMENSION + ((NumericalLiteral) exp).getType().get().getUnit().get().toString(),
								dec[2], dec[3], dec[1], dec[6], dec[0], dec[5], dec[4]);
				Unit tempUnit = new Unit(((NumericalLiteral) exp).getType().get().getUnit().get().toString(),
						tempDimension);
				container.addDimension(tempDimension);
				container.addUnit(tempUnit);

				Constant tempConstant = new Constant(PREFIX_CONSTANT + ((NumericalLiteral) exp).printValueType(),
						tempDimension.getName(), exp, false);
				container.addConstant(tempConstant);
				Variable var = new Variable(tempConstant.getName());
				expr.replaceElement(exp, var);
			}
		}
		return expr;
	}


	/**
	 * Returns the arguments of a function separated by ",".
	 * E.g: sum(x,y) -> x,y
	 *
	 * @param functionCall the function-call whose arguments will be extracted.
	 * @return Arguments as String
	 */
	public String getArgs(ASTFunctionCall functionCall) {
		StringBuilder newBuilder = new StringBuilder();
		for (ASTExpr expr : functionCall.getArgs()) {
			newBuilder.append(container.getLEMSExpressionsPrettyPrinter().print(expr, false));
			newBuilder.append(",");
		}
		newBuilder.deleteCharAt(newBuilder.length() - 1);//delete the last "," before the end of the string
		return newBuilder.toString();
	}

	/**
	 * Returns true if a handed over list of instructions contains a named function call
	 *
	 * @param funcName the name of the called function
	 * @param list     the list which holds instruction
	 * @return true if function call is present
	 */
	public boolean containsNamedFunction(String funcName, List<DynamicRoutine.Instruction> list) {
		for (DynamicRoutine.Instruction instr : list) {
			if (instr.getClass() == DynamicRoutine.FunctionCall.class &&
					((DynamicRoutine.FunctionCall) instr).printName().equals(funcName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns all instructions which declare a function call with a given name.
	 *
	 * @param funcName the name of the function call
	 * @param list     the list of instructions which will be searched through
	 * @return a list of function calls with the given name,
	 */
	public List<DynamicRoutine.FunctionCall> getNamedFunction(String funcName, List<DynamicRoutine.Instruction> list) {
		return list.stream()
				.filter(call -> call.getClass().equals(DynamicRoutine.FunctionCall.class) &&
						((DynamicRoutine.FunctionCall) call).printName().equals(funcName))
				.map(call -> (DynamicRoutine.FunctionCall) call).collect(Collectors.toList());
	}

	/**
	 * Checks whether a given block or a sub block contains a certain function call.
	 *
	 * @param function The name of the function which will be checked
	 * @param args     the handed over arguments of the function
	 * @param block    the block in which a function is possibly contained
	 * @return true if function call is present
	 */
	public boolean blockContainsFunction(String function, List<String> args, ASTBlock block) {
		boolean temp = false;
		boolean temp2 = false;
		for (ASTStmt stmt : block.getStmts()) {
			if (stmt.compound_StmtIsPresent() &&
					(stmt.getCompound_Stmt().get().fOR_StmtIsPresent() ||
							stmt.getCompound_Stmt().get().wHILE_StmtIsPresent())){
						continue;//while and fors are not supported, so just ignore them
			}
			if (stmt.small_StmtIsPresent()) {
				if (stmt.getSmall_Stmt().get().functionCallIsPresent() &&
						stmt.getSmall_Stmt().get().getFunctionCall().get().getName().toString()
								.equals(function) &&
						stmt.getSmall_Stmt().get().getFunctionCall().get().getArgs().size() == args
								.size()) {
					temp2 = true;
					for (int i = 0; i < args.size(); i++) {
						//check if some args. are different
						if (!args.get(i).equals(container.getLEMSExpressionsPrettyPrinter()
								.print(stmt.getSmall_Stmt().get().getFunctionCall().get().getArgs().get(i),
										false))) {
							temp2 = false;
						}
					}
				}
				temp = temp || temp2;
			} else {
				temp = temp || this.blockContainsFunction(function, args,
						stmt.getCompound_Stmt().get().getIF_Stmt().get().getIF_Clause().getBlock());
				for (ASTELIF_Clause clause : stmt.getCompound_Stmt().get().getIF_Stmt().get().getELIF_Clauses()) {
					temp = temp || blockContainsFunction(function, args, clause.getBlock());
				}
				if (stmt.getCompound_Stmt().get().getIF_Stmt().get().eLSE_ClauseIsPresent()) {
					temp = temp || blockContainsFunction(function, args,
							stmt.getCompound_Stmt().get().getIF_Stmt().get().getELSE_Clause().get().getBlock());
				}
			}
		}
		return temp;
	}

	/**
	 * This method converts the internal representation of the type of a variable to
	 * an array of integer values.
	 *
	 * @param arrayAsString a string representation of the data type array
	 * @return an array of data
	 */
	public int[] convertTypeDeclToArray(String arrayAsString) {
		int res[] = new int[8];
		String[] tempArray = (arrayAsString.replaceAll("[^\\d\\-]", ":")).split(":");
		int tempIndex = 0;
		for (int i = 0; i < tempArray.length; i++) {
			if (!tempArray[i].equals("")) {
				res[tempIndex] = Integer.parseInt(tempArray[i]);
				tempIndex++;
			}
		}
		return res;
	}

	/**
	 * This method is used to check whether a data type is not supported.
	 *
	 * @param type the type symbol of an attribute
	 * @return true, if the type is not supported
	 */
	public boolean dataTypeNotSupported(TypeSymbol type) {
		if ((type.getType() == TypeSymbol.Type.PRIMITIVE) && (type.prettyPrint().equals("boolean") ||
				type.prettyPrint().equals("void") ||
				type.prettyPrint().equals("String"))) {
			return true;
		}
		return false;
	}

	/**
	 * This method prints and stores an adequate message regarding not supported
	 * elements during the transformation
	 *
	 * @param variable the variable whose type is not supported
	 */
	public void printNotSupportedDataType(VariableSymbol variable) {
		System.err.println("Not supported data-type found: \"" + variable.getType().getName() + "\".");
		container.addNotConverted("Not supported data-type "
				+ variable.getType().prettyPrint() + " in lines "
				+ variable.getAstNode().get().get_SourcePositionStart()
				+ " to " + variable.getAstNode().get().get_SourcePositionEnd()
				+ ".");
	}

	/**
	 * This method prints and stores a message regarding a not supported yet found
	 * function call inside a expression.
	 *
	 * @param variable the variable symbol whose declaration has a function call which is not supportd
	 */
	public void printNotSupportedFunctionCallInExpression(VariableSymbol variable) {
		System.err.println("Function call found in constant/parameter declaration"
				+ " in lines " + variable.getSourcePosition().getLine() + ". Please declare as "
				+ "derived variable!");
	}

	public void printNotSupportedFunctionCallInEquations(ASTVariable variable) {
		System.err
				.println("Not supported function call in equation: " + variable.getName().toString());
	}

	public void printNotSupportedFunctionInBlock(ASTSmall_Stmt input) {
		System.err.println("Not supported function call(s) found in update block.");
		container.addNotConverted("Not supported function call in update block, lines " + input.get_SourcePositionStart() + " to " + input.get_SourcePositionEnd());
	}

	/**
	 * Extends the input by an activator variable whose name is handed over.
	 *
	 * @param var  The name of the variable whose activator will be used.
	 * @param expr The expression which will be modified by an activator.
	 * @return the extended expression
	 */
	public Expression buildExpressionWithActivator(String var, Expression expr) {
		String temp = PREFIX_ACT + var.replaceAll("'", "");
		Expression lhs = new Variable(temp);
		Expression ret = new Expression();
		Expression rightSubExpr = new Expression();
		Operator parenthesis = new Operator();
		parenthesis.setLeftParentheses(true);
		parenthesis.setRightParentheses(true);
		rightSubExpr.replaceOp(parenthesis);
		rightSubExpr.replaceRhs(expr);
		Operator op = new Operator();
		op.setTimesOp(true);
		ret.replaceLhs(lhs);
		ret.replaceOp(op);
		ret.replaceRhs(rightSubExpr);
		return ret;
	}

	/**
	 * Encapsulates the expression by brackets and extends by /CON1ms
	 *
	 * @param expr the expression which will be extended
	 * @return the extended expr
	 */
	public Expression extendExpressionByCON1ms(Expression expr) {
		Expression leftSubExpr = new Expression();
		Operator parenthesis = new Operator();
		parenthesis.setLeftParentheses(true);
		parenthesis.setRightParentheses(true);
		leftSubExpr.replaceOp(parenthesis);
		leftSubExpr.replaceRhs(expr);
		Variable var = new Variable(PREFIX_CONSTANT+"1ms");
		Expression exp = new Expression();
		Operator op = new Operator();
		op.setDivOp(true);
		exp.replaceLhs(leftSubExpr);
		exp.replaceOp(op);
		exp.replaceRhs(var);
		return exp;
	}


	public void printArrayNotSupportedMessage(VariableSymbol var){
		System.err.println("Not supported array-declaration found \"" + var.getName() + "\".");
	}

	public void printNotSupportedFunctionCallFoundMessage(ASTEquation eq, LEMSExpressionsPrettyPrinter prettyPrinter){
		System.err.println("Not supported function call in expression found: " + prettyPrinter.print(eq.getRhs(), false));
	}



}
