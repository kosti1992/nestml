package org.nest.codegeneration.helpers.LEMSElements;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.nest.codegeneration.helpers.Collector;
import org.nest.codegeneration.helpers.Expressions.*;
import org.nest.commons._ast.ASTExpr;
import org.nest.commons._ast.ASTFunctionCall;
import org.nest.commons._ast.ASTVariable;
import org.nest.nestml._ast.ASTBody;
import org.nest.ode._ast.ASTEquation;
import org.nest.ode._ast.ASTShape;
import org.nest.spl._ast.ASTBlock;
import org.nest.spl._ast.ASTELIF_Clause;
import org.nest.spl._ast.ASTSmall_Stmt;
import org.nest.spl._ast.ASTStmt;
import org.nest.spl.prettyprinter.LEMS.LEMSExpressionsPrettyPrinter;
import org.nest.symboltable.symbols.TypeSymbol;
import org.nest.symboltable.symbols.VariableSymbol;
import org.nest.units._ast.ASTDatatype;
import org.nest.units._ast.ASTUnitType;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toList;

/**
 * This class provides a set of methods which are used during the transformation in order to retrieve or
 * transform certain values.
 *
 * @author perun
 */
public class HelperCollection {
    //this is a collection of global constants and prefixes. in case something has to be changed, this is the point where
    public static final String NOT_SUPPORTED = "NOT_SUPPORTED";

    public static final String GUARD_NAME = "GUARD";
    public static final String DIMENSION_NONE = "none";

    public static final String PREFIX_INIT = "INIT_";
    public static final String PREFIX_DIMENSION = "DIM_";
    public static final String PREFIX_CONSTANT = "CON_";
    public static final String PREFIX_ACT = "ACT_";

    public static final String CURRENT_BUFFER_INPUT_VAR = "i";
    public static final String SPIKE_BUFFER_INPUT_VAR = "i";


    /**
     * Returns all spike input ports of a given set.
     *
     * @param ports a list of used ports
     * @return a list of all spike ports
     */
    @SuppressWarnings("unused")//used in the template
    public static List<String> getSpikePorts(List<String> ports) {
        return ports.stream().filter(st -> st.endsWith("spikes")).collect(Collectors.toList());
    }

    /**
     * This function avoids problems with locale settings regarding number formats and print a number with "." as sole
     * separator.
     *
     * @return number as string
     */
    @SuppressWarnings("unused")//used in the template
    public static String getNumberFormatted(double input) {
        return String.valueOf(input);
    }

    /**
     * TypeSymbol version:
     * Extracts the dimension of a given variable-type. This class has to be modified if new units and dimensions
     * a introduced to NESTML.
     *
     * @param input the type-symbol of the a variable.
     * @return the name of the dimension as String
     */
    public static String typeToDimensionConverter(TypeSymbol input) {
        if (input.getName().equals("void") || input.getName().equals("string")) {
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
     * ASTDatatype version:
     * Extracts the dimension of a given variable-type. This class has to be modified if new units and dimensions
     * a introduced to NESTML.
     *
     * @param input the type-symbol of the a variable. Here,a ASTDatatype is expected.
     * @return the name of the dimension as String
     */
    public static String typeToDimensionConverter(ASTDatatype input) {
        if (input.getUnitType().isPresent() && input.getUnitType().get().getUnit().isPresent()) {
            return PREFIX_DIMENSION + input.getUnitType().get().getUnit().get();//TODO
        } else {
            if (input.isBoolean()||input.isInteger()||input.isReal()) {
                return DIMENSION_NONE;
            } else {
                return NOT_SUPPORTED;
            }
        }
    }

    /**
     * Converts a unit prefix to power of base 10. This method is probably obsolete since the introduction of the
     * new unit handling in the frontend.
     *
     * @param var_type Name of the unit represented as String.
     * @return Power of the prefix as int.
     */
    public static int powerConverter(String var_type) {
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
     *
     * @param expr The function call name which will be checked.
     * @return true, if supported
     */
    public static boolean mathematicalFunctionIsSupported(String expr) {
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
            case "resolution":
                return true;
            case "steps":
                return true;
            default:
                return false;
        }
    }

    private static boolean userDefinedFunctionIsSupported(String expr,LEMSCollector container){
        for(DerivedElement elem:container.getDerivedElementList()){
            if(elem.getName().equals(expr)){
                return true;
            }
        }
        return false;
    }


    /**
     * Indicates whether a given ASTExpression contains a function call or not.
     *
     * @param expr          the expression which will be checked.
     * @param skipSupported if true, all supported function calls will be skipped
     * @param container the container holds user defined functions which have to be seen as supported
     * @return true, if expression or sub-expression contains call.
     */
    public static boolean containsFunctionCall(ASTExpr expr, boolean skipSupported,LEMSCollector container) {
        boolean temp = false;
        //if more functions are supported
        if (expr.functionCallIsPresent() && !(skipSupported && (mathematicalFunctionIsSupported(
                expr.getFunctionCall().get().getName().toString()))||userDefinedFunctionIsSupported(expr.getFunctionCall().get().getName().toString(),
                container))) {
            temp = true;
        }
        if (expr.exprIsPresent()) {
            temp = temp || containsFunctionCall(expr.getExpr().get(), skipSupported,container);
        }
        if (expr.leftIsPresent()) {
            temp = temp || containsFunctionCall(expr.getLeft().get(), skipSupported,container);
        }
        if (expr.rightIsPresent()) {
            temp = temp || containsFunctionCall(expr.getRight().get(), skipSupported,container);
        }
        return temp;
    }

    /**
     * Inspects an expression and replaces all directly stated constants with references
     * to additionally created constants, e.g. V_m +10mV -> V_m + CON10mV
     *
     * @param container a container which will be used to store new constants
     * @param expr      the expression in which a constant will be replaced
     * @return an expression with replaced constants
     */
    public static Expression replaceConstantsWithReferences(LEMSCollector container, Expression expr) {
        List<Expression> temp = expr.getNumericals();
        //in the case we get a numerical literal directly
        if(expr instanceof NumericalLiteral){
            if((((NumericalLiteral) expr).hasType())){
                int[] dec = convertTypeDeclToArray(((NumericalLiteral) expr).getType().get().getSerializedUnit());
                //create the required units and dimensions
                Dimension tempDimension =
                        new Dimension(PREFIX_DIMENSION +
                                HelperCollection.getExpressionFromUnitType(((NumericalLiteral) expr).getType().get()).print(),
                                dec[2], dec[3], dec[1], dec[6], dec[0], dec[5], dec[4]);

                Unit tempUnit = new Unit(HelperCollection.getExpressionFromUnitType(((NumericalLiteral) expr).getType().get()).print(),
                        dec[7], tempDimension);

                container.addDimension(tempDimension);
                container.addUnit(tempUnit);

                //finally a constant representing the concrete value, a reference is set to this constant
                Constant tempConstant = new Constant(PREFIX_CONSTANT + ((NumericalLiteral) expr).printValueType(),
                        tempDimension.getName(), expr, false);
                container.addConstant(tempConstant);

                return new Variable(tempConstant.getName());
            }
        }
        //otherwise it is an expression
        for (Expression exp : temp) {
            if (((NumericalLiteral) exp).hasType()) {
                if (((NumericalLiteral) exp).getType().isPresent() &&
                        ((NumericalLiteral) exp).getType().get()
                                .getSerializedUnit() == null) {
                    //this is a rather bad approach, since it requires that the same unit is already somewhere defined
                    //in the model, however, this part of the method it only invoked for artificial extensions, thus not critical
                    Dimension tempDimension = null;
                    for (Unit u : container.getUnitsSet()) {
                        if (u.getSymbol().equals(((NumericalLiteral) exp).getType().get().getUnit().get())) {
                            tempDimension = u.getDimension();
                            break;
                        }
                    }
                    if (tempDimension != null) {
                        Constant tempConstant = new Constant(PREFIX_CONSTANT + ((NumericalLiteral) exp).printValueType(),
                                tempDimension.getName(), exp, false);
                        container.addConstant(tempConstant);
                        Variable var = new Variable(tempConstant.getName());
                        expr.replaceElement(exp, var);
                    } else {
                        System.err.println("A problematic case occurred during constant replacement!");
                    }
                } else {
                    int[] dec = convertTypeDeclToArray(((NumericalLiteral) exp).getType().get().getSerializedUnit());
                    //create the required units and dimensions
                    Dimension tempDimension =
                            new Dimension(PREFIX_DIMENSION +
                                    HelperCollection.getExpressionFromUnitType(((NumericalLiteral) exp).getType().get()).print(),
                                    dec[2], dec[3], dec[1], dec[6], dec[0], dec[5], dec[4]);
                    Unit tempUnit = new Unit(HelperCollection.getExpressionFromUnitType(((NumericalLiteral) exp).getType().get()).print(),
                            dec[7], tempDimension);
                    container.addDimension(tempDimension);
                    container.addUnit(tempUnit);
                    //finally a constant representing the concrete value, a reference is set to this constant
                    Constant tempConstant = new Constant(PREFIX_CONSTANT + ((NumericalLiteral) exp).printValueType(),
                            tempDimension.getName(), exp, false);
                    container.addConstant(tempConstant);
                    Variable var = new Variable(tempConstant.getName());
                    expr.replaceElement(exp, var);
                }
            }
        }

        return expr;
    }


    /**
     * Inspects a given expression and replaces boolean atoms, e.g. bool_var ist replaced by 1.eq.bool_var
     * to represent an equal behavior.
     *
     * @param container a lems container containing further specifications.
     * @param expr      the expression in which bool vars will be replaced.
     * @return an expression with replaced bool parts.
     */
    public static Expression replaceBooleanAtomByExpression(LEMSCollector container, Expression expr) {

        if (expr instanceof Variable && container.getBooleanElements().contains(((Variable) expr).getVariable())) {
            NumericalLiteral lit1 = new NumericalLiteral(1, null);
            Operator op1 = new Operator();
            op1.setEq(true);
            Expression ex1 = new Expression();
            ex1.replaceLhs(lit1);
            ex1.replaceOp(op1);
            ex1.replaceRhs(expr);
            return ex1;
        }
        if (expr.lhsIsPresent()) {
            //if it is a variable, then check if it is a boolean var
            expr.replaceLhs(replaceBooleanAtomByExpression(container, expr.getLhs().get()));
        }
        if (expr.rhsIsPresent()) {
            expr.replaceRhs(replaceBooleanAtomByExpression(container, expr.getRhs().get()));
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
    public static String getArgs(ASTFunctionCall functionCall, LEMSCollector container) {
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
    public static boolean containsNamedFunction(String funcName, List<DynamicRoutine.Instruction> list) {
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
    public static List<DynamicRoutine.FunctionCall> getNamedFunction(String funcName, List<DynamicRoutine.Instruction> list) {
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
    public static boolean blockContainsFunction(String function, List<String> args, ASTBlock block, LEMSCollector container) {
        boolean temp = false;
        boolean temp2 = false;
        for (ASTStmt stmt : block.getStmts()) {
            if (stmt.compound_StmtIsPresent() &&
                    (stmt.getCompound_Stmt().get().fOR_StmtIsPresent() ||
                            stmt.getCompound_Stmt().get().wHILE_StmtIsPresent())) {
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
                temp = temp || blockContainsFunction(function, args,
                        stmt.getCompound_Stmt().get().getIF_Stmt().get().getIF_Clause().getBlock(), container);
                for (ASTELIF_Clause clause : stmt.getCompound_Stmt().get().getIF_Stmt().get().getELIF_Clauses()) {
                    temp = temp || blockContainsFunction(function, args, clause.getBlock(), container);
                }
                if (stmt.getCompound_Stmt().get().getIF_Stmt().get().eLSE_ClauseIsPresent()) {
                    temp = temp || blockContainsFunction(function, args,
                            stmt.getCompound_Stmt().get().getIF_Stmt().get().getELSE_Clause().get().getBlock(), container);
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
    public static int[] convertTypeDeclToArray(String arrayAsString) {
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
    public static boolean dataTypeNotSupported(TypeSymbol type) {
        if ((type.getType() == TypeSymbol.Type.PRIMITIVE) && (
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
    public static void printNotSupportedDataType(VariableSymbol variable, LEMSCollector container) {
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
    public static void printNotSupportedFunctionCallInExpression(VariableSymbol variable, LEMSCollector container) {
        System.err.println(
                "LEMS Error (Line: "
                        + container.getNeuronName() + "/"
                        + variable.getSourcePosition().getLine()
                        + "):"
                        + " Function call found in (constant|parameter) declaration." + "("
                        + container.getPrettyPrint().print(variable.getDeclaringExpression().get(), false) + ")");
    }

    public static void printNotSupportedFunctionCallInEquations(ASTShape variable, LEMSCollector container) {
        System.err.println(
                "LEMS-Error (Line: "
                        + container.getNeuronName() + "/"
                        + variable.get_SourcePositionStart().getLine()
                        + "):"
                        + " Not supported function call in equation. (" +
                        (new Expression(variable.getRhs())).print()
                        + ")");

    }

    public static void printNotSupportedFunctionInBlock(ASTSmall_Stmt input, LEMSCollector container) {
        System.err.print(
                "LEMS-Error (Line: "
                        + container.getNeuronName() + "/"
                        + input.get_SourcePositionStart().getLine()
                        + "):"
                        + " Not supported function call in function. ");

        if (input.getAssignment().isPresent()) {
            System.err.print("(" + container.getPrettyPrint().print(input.getAssignment().get().getExpr(), false) + ")\n");
        }
        if (input.getDeclaration().isPresent()) {
            System.err.print("(" + container.getPrettyPrint().print(input.getDeclaration().get().getExpr().get(), false) + ")\n");
        }
        if (input.getFunctionCall().isPresent()) {
            System.err.print("(" + input.getFunctionCall().get().getCalleeName() + ")\n");
        }
        container.addNotConverted("Not supported function call in update block, lines " + input.get_SourcePositionStart() + " to " + input.get_SourcePositionEnd());
    }


    /**
     * Extends the input by an activator variable whose name is handed over.
     *
     * @param var  The name of the variable whose activator will be used.
     * @param expr The expression which will be modified by an activator.
     * @return the extended expression
     */
    public static Expression buildExpressionWithActivator(String var, Expression expr) {
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
    public static Expression extendExpressionByCON1ms(Expression expr) {
        Expression leftSubExpr = new Expression();
        Operator parenthesis = new Operator();
        parenthesis.setLeftParentheses(true);
        parenthesis.setRightParentheses(true);
        leftSubExpr.replaceOp(parenthesis);
        leftSubExpr.replaceRhs(expr);
        Variable var = new Variable(PREFIX_CONSTANT + "1_ms");
        Expression exp = new Expression();
        Operator op = new Operator();
        op.setDivOp(true);
        exp.replaceLhs(leftSubExpr);
        exp.replaceOp(op);
        exp.replaceRhs(var);
        return exp;
    }


    /**
     * Examines a given expression and replaces all occurrences of the function call "resolution" by the corresponding
     * constant as provided in the artifact.
     *
     * @param container the container in which the config file (i.e. artifact) has been stored.
     * @param expr      the expression which will be modified
     * @return the modified expression
     */
    public static Expression replaceResolutionByConstantReference(LEMSCollector container, Expression expr) {
        if (expr.containsNamedFunction("resolution", new ArrayList<>())) {
            ASTUnitType tempType = new ASTUnitType();
            tempType.setUnit(container.getConfig().getSimulation_steps_unit().getSymbol());
            Function tempFunction = new Function("resolution", new ArrayList<>());
            NumericalLiteral literal = new NumericalLiteral(container.getConfig().getSimulation_steps_length(),
                    tempType);
            Constant tempConstant = new Constant(HelperCollection.PREFIX_CONSTANT + container.getConfig().getSimulation_steps_length_asString() +
                    tempType.getUnit().get().toString(), HelperCollection.PREFIX_DIMENSION + tempType.getUnit().get().toString(), literal, false);
            container.addConstant(tempConstant);
            expr.replaceElement(tempFunction, new Variable(tempConstant.getName()));
            return expr;
        }
        return expr;
    }

    /**
     * Resolves the problem with not present logical-not in LEMS by using de Morgan to reconfigure the
     * expression to a logical equivalent counter piece without logical-not. Logical-not not required in literals
     * since expression language is a strict equality logic without boolean atoms.
     *
     * @param expr the expression whose "not" part will be resolved
     */
    public static Expression replaceNotByLogicalEquivalent(LEMSCollector container, Expression expr) {
        if (expr.opIsPresent() && expr.getOperator().get().isLogicalNot()) {
            expr.negateLogic();
        }
        if (expr.lhsIsPresent()) {
            expr.replaceLhs(replaceNotByLogicalEquivalent(container, expr.getLhs().get()));
        }
        if (expr.rhsIsPresent()) {
            expr.replaceRhs(replaceNotByLogicalEquivalent(container, expr.getRhs().get()));
        }
        return expr;
    }

    public static void printArrayNotSupportedMessage(VariableSymbol var, LEMSCollector container) {
        System.err.println(
                "LEMS Error (Line: "
                        + container.getNeuronName() + "/"
                        + var.getSourcePosition().getLine()
                        + "):"
                        + " Array declaration found. ("
                        + var.getName()
                        + ")");

    }

    public static String getArrayNotSupportedMessage(VariableSymbol var) {
        return "Array declaration in lines" + var.getSourcePosition().getLine();
    }


    public static void printNotSupportedFunctionCallFoundMessage(ASTEquation eq, LEMSCollector container) {
        System.err.println(
                "LEMS Error (Line: "
                        + container.getNeuronName() + "/"
                        + eq.get_SourcePositionStart().getLine()
                        + "):"
                        + " Not supported function call in expression. ("
                        + container.getPrettyPrint().print(eq.getRhs(), false)
                        + ")");

    }

    /**
     * Inspects a given dimension and transforms it to a LEMS near representation, e.g.  A / s => DimensionOf_A_per_s
     *
     * @param unformattedDimension the string representation of a yet unformatted dimension
     * @return a formatted string
     */
    public static String dimensionFormatter(String unformattedDimension) {
        return unformattedDimension.replaceAll("\\*", "_times_").replaceAll("/", "_per_").replaceAll(" ", "");
    }


    /**
     * This method inspects a handed over unit type declaration and transforms it to an expression. It should only be
     * used in case a complex unit is used, e.g. pA*1/mV^2
     *
     * @param unitType a unit type declaring a combined unit
     */
    public static Expression getExpressionFromUnitType(ASTUnitType unitType) {
        checkNotNull(unitType);
        Expression ret = new Expression();
        if (unitType.isPow()) {
            Operator op = new Operator();
            op.setPower(true);
            ret.replaceOp(op);
        }
        if (unitType.isDivOp()) {
            Operator op = new Operator();
            op.setDivOp(true);
            ret.replaceOp(op);
        }
        if (unitType.isTimesOp()) {
            Operator op = new Operator();
            op.setTimesOp(true);
            ret.replaceOp(op);
        }
        if (unitType.unitIsPresent()) {
            Variable var = new Variable(unitType.getUnit().get());
            ret.replaceLhs(var);
        }
        if (unitType.unitlessLiteralIsPresent()) {
            NumericalLiteral lit = new NumericalLiteral(unitType.getUnitlessLiteral().get().getValue(), null);
            ret.replaceLhs(lit);
        }
        if (unitType.baseIsPresent() && unitType.exponentIsPresent()) {
            Operator op = new Operator();
            op.setPower(true);
            ret.replaceLhs(getExpressionFromUnitType(unitType.getBase().get()));
            ret.replaceOp(op);
            ret.replaceRhs(new NumericalLiteral(unitType.getExponent().get().getValue(), null));
        }
        if (unitType.unitTypeIsPresent()) {
            ret.replaceLhs(getExpressionFromUnitType(unitType.getUnitType().get()));
        }
        if (unitType.leftIsPresent()) {
            ret.replaceLhs(getExpressionFromUnitType(unitType.getLeft().get()));
        }
        if (unitType.rightIsPresent()) {
            ret.replaceRhs(getExpressionFromUnitType(unitType.getRight().get()));
        }
        if (unitType.leftParenthesesIsPresent() && unitType.rightParenthesesIsPresent()) {
            ret = Expression.encapsulateInBrackets(ret);
        }
        return ret;
    }

    /**
     * For a given string,this method formats it to a LEMS processable format by deleting arithmetic operators and
     * brackets.
     *
     * @param expression a yet unformatted expression as string
     * @return a formatted string
     */
    public static String formatComplexUnit(String expression) {
        if (expression.equals(HelperCollection.DIMENSION_NONE) ||
                expression.equals(HelperCollection.NOT_SUPPORTED)) {
            return expression;
        }
        String temp = expression;
        temp = temp.replaceAll(" ", "");
        temp = temp.replaceAll("/", "_per_");
        temp = temp.replaceAll("\\*", "_times_");
        temp = temp.replaceAll("\\^", "_to_");
        temp = temp.replaceAll("\\-", "m_");
        temp = temp.replace("(", "__");
        temp = temp.replace(")", "__");
        return temp;
    }

    /**
     * Collects all units from a handed over numerical. This method is required in order to process
     * complex units stated as part of constants in expressions.
     *
     * @return a list of ASTUnitTypes of the numerical stated in a given expression.
     */
    public static List<ASTUnitType> collectUnitsFromNumericals(Expression expr) {
        List<ASTUnitType> ret = new ArrayList<>();
        for (Expression lit : expr.getNumericals()) {
            if (((NumericalLiteral) lit).hasType()) {
                ret.add(((NumericalLiteral) lit).getType().get());
            }
        }
        return ret;
    }

    public static void retrieveUnitsFromExpression(Expression expr, LEMSCollector container) {
        for (Expression numerical : expr.getNumericals()) {
            if (((NumericalLiteral) numerical).hasType()) {
                Unit temp = new Unit(((NumericalLiteral) numerical).getType().get());
                container.addUnit(temp);
                container.addDimension(temp.getDimension());
            }
        }
    }

    /**
     * Checks if the given expression consists of a logical operator and replaces all occurrences of a expression on
     * one side of the expression by the same expression but encapsulated in brackets in order to enable LEMS to read
     * the expression and process it.
     *
     * @return a Expression with references where needed
     */
    public static Expression encapsulateExpressionInConditions(Expression expression) {
        if (expression.opIsPresent()) {
            if (expression.getOperator().get().isLt() || expression.getOperator().get().isLe() ||
                    expression.getOperator().get().isEq() || expression.getOperator().get().isNe() ||
                    expression.getOperator().get().isGt() || expression.getOperator().get().isGe()) {
                if (expression.getLhs().get().isExpression()) {
                    expression.replaceLhs(Expression.encapsulateInBrackets((expression.getLhs().get())));
                }
                if (expression.getRhs().get().isExpression()) {
                    expression.replaceRhs(Expression.encapsulateInBrackets((expression.getRhs().get())));
                }
            }
            if (expression.lhsIsPresent()) {
                expression.replaceLhs(encapsulateExpressionInConditions(expression.getLhs().get()));
            }
            if (expression.rhsIsPresent()) {
                expression.replaceRhs(encapsulateExpressionInConditions(expression.getRhs().get()));
            }
        }
        return expression;
    }

    /**
     * Generates an expression 0/0 which leads to an exception whenever executed. This method is required in order to
     * implemented guards and their correct behavior.
     *
     * @return the Expression 0/0.
     */
    public static Expression generateExceptionCondition() {
        Expression lhs = new NumericalLiteral(0, null);
        Operator op = new Operator();
        op.setDivOp(true);
        Expression rhs = lhs.deepClone();
        Expression ret = new Expression();
        ret.replaceLhs(lhs);
        ret.replaceOp(op);
        ret.replaceRhs(rhs);
        return ret;
    }

    /**
     * Currently, the ASTBody does not provide any functionality to retrieve all invariants of the state block.
     * This method avoids this problem and implements such a routine.
     *
     * @param astBody a astBody possibly containing invariants
     * @return a list of expressions representing the invariants.
     */
    public static List<ASTExpr> getStateInvariants(ASTBody astBody) {
        return astBody.getStateDeclarations().stream()
                .filter(param -> param.getInvariant().isPresent())
                .map(param -> param.getInvariant().get())
                .collect(toList());
    }

    /**
     * Currently, the ASTBody does not provide any functionality to retrieve all invariants of the internals block.
     * This method avoids this problem and implements such a routine.
     *
     * @param astBody a astBody possibly containing invariants
     * @return a list of expressions representing the invariants.
     */
    public static List<ASTExpr> getInternalsInvariants(ASTBody astBody) {
        return astBody.getInternalDeclarations().stream()
                .filter(param -> param.getInvariant().isPresent())
                .map(param -> param.getInvariant().get())
                .collect(toList());
    }

    /**
     * A routine encapsulating both, replacing of constants and the resolution function call.
     *
     * @param expr      an expression possibly containing this elements
     * @param container a container or added elements
     * @return an expression with replaced elements
     */
    public static Expression replacementRoutine(Expression expr, LEMSCollector container) {
        Expression tempExpression = replaceConstantsWithReferences(container, expr);
        tempExpression = replaceFunctionCallByReference(container,tempExpression);
        return replaceResolutionByConstantReference(container, tempExpression);
    }


    public static Expression replaceFunctionCallByReference(LEMSCollector container,Expression expr){
        checkNotNull(expr);
        checkNotNull(container);
        if(expr instanceof Function){
            return new Variable(((Function) expr).getFunctionName());
        }else{
            List<Expression> listOfCalls = expr.getFunctions();
            //collect the names of all derived variables
            List<String> udFNames = container.getDerivedElementList().stream().map(param->param.getName()).collect(toList());
            for(Expression call:listOfCalls){
                if(udFNames.contains(((Function) call).getFunctionName())){
                    expr.replaceElement(call,new Variable(((Function) call).getFunctionName()));
                }
            }
            return expr;
        }
    }

}
