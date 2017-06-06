package org.nest.codegeneration.helpers.LEMS.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.nest.codegeneration.helpers.LEMS.Expressions.*;
import org.nest.codegeneration.helpers.Names;
import org.nest.commons._ast.ASTExpr;
import org.nest.commons._ast.ASTFunctionCall;
import org.nest.nestml._ast.ASTBody;
import org.nest.spl._ast.ASTBlock;
import org.nest.spl._ast.ASTELIF_Clause;
import org.nest.spl._ast.ASTStmt;
import org.nest.symboltable.symbols.TypeSymbol;
import org.nest.symboltable.symbols.VariableSymbol;
import org.nest.units._ast.ASTDatatype;
import org.nest.units._ast.ASTUnitType;
import org.nest.units.unitrepresentation.UnitRepresentation;

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
     * @param _ports a list of used ports
     * @return a list of all spike ports
     */
    @SuppressWarnings("unused")//used in the template
    public static List<String> getSpikePorts(List<String> _ports) {
        return _ports.stream().filter(st -> st.endsWith("spikes")).collect(Collectors.toList());
    }

    /**
     * This function avoids problems with locale settings regarding number formats and print a number with "." as sole
     * separator.
     *
     * @return number as string
     */
    @SuppressWarnings("unused")//used in the template
    public static String getNumberFormatted(double _input) {
        return String.valueOf(_input);
    }

    /**
     * TypeSymbol version:
     * Extracts the dimension of a given variable-type. This class has to be modified if new units and dimensions
     * a introduced to NESTML.
     *
     * @param _typeSymbol the type-symbol of the a variable.
     * @return the name of the dimension as String
     */
    public static String typeToDimensionConverter(TypeSymbol _typeSymbol) {
        if (_typeSymbol.getName().equals("void") || _typeSymbol.getName().equals("string")) {
            return NOT_SUPPORTED;
        }
        if (_typeSymbol.getType() == TypeSymbol.Type.PRIMITIVE) {
            return DIMENSION_NONE;
        } else if (_typeSymbol.getType() == TypeSymbol.Type.UNIT) {
            return PREFIX_DIMENSION + _typeSymbol.prettyPrint();
        }
        System.err.println(_typeSymbol.prettyPrint() + " : not supported!");
        return null;
    }

    /**
     * ASTDatatype version:
     * Extracts the dimension of a given variable-type. This class has to be modified if new units and dimensions
     * a introduced to NESTML.
     *
     * @param _dataType the type-symbol of the a variable. Here,a ASTDatatype is expected.
     * @return the name of the dimension as String
     */
    public static String typeToDimensionConverter(ASTDatatype _dataType) {
        if (_dataType.getUnitType().isPresent() && _dataType.getUnitType().get().getUnit().isPresent()) {
            return PREFIX_DIMENSION + _dataType.getUnitType().get().getUnit().get();//TODO
        } else {
            if (_dataType.isBoolean() || _dataType.isInteger() || _dataType.isReal()) {
                return DIMENSION_NONE;
            } else {
                return NOT_SUPPORTED;
            }
        }
    }

    /**
     * Converts a unit prefix to power of base 10. This method is probably obsolete since the introduction of the
     * new unit handling in the frontend.
     * TODO: Caution, this method is rather bad and relies on a pretty obsolete representation.
     *
     * @param _varSymbol Name of the unit represented as String.
     * @return Power of the prefix as int.
     */
    public static int powerConverter(String _varSymbol) {
        if (_varSymbol == null || _varSymbol.length() < 2) {
            return 0;
        }
        if (_varSymbol.startsWith("d")) {
            return -1;
        } else if (_varSymbol.startsWith("c")) {
            return -2;
        } else if (_varSymbol.startsWith("m") && _varSymbol.length() > 1) {// in order to avoid confusion between meter and mili
            return -3;
        } else if (_varSymbol.startsWith("mu")) {
            return -6;
        } else if (_varSymbol.startsWith("n")) {
            return -9;
        } else if (_varSymbol.startsWith("p")) {
            return -12;
        } else if (_varSymbol.startsWith("f")) {
            return -15;
        } else if (_varSymbol.startsWith("a")) {
            return -18;
        }
        //positive powers
        else if (_varSymbol.startsWith("da")) {
            return 1;
        } else if (_varSymbol.startsWith("h")) {
            return 2;
        } else if (_varSymbol.startsWith("k")) {
            return 3;
        } else if (_varSymbol.startsWith("M")) {
            return 6;
        } else if (_varSymbol.startsWith("G")) {
            return 9;
        } else if (_varSymbol.startsWith("T")) {
            return 12;
        } else if (_varSymbol.startsWith("P")) {
            return 15;
        } else if (_varSymbol.startsWith("E")) {
            return 18;
        }
        System.err.println(_varSymbol + " prefix not supported!");
        return 1;
    }

    /**
     * Checks whether a given function call name is supported by the target modeling language or not.
     * If new concepts are supported, add here.
     *
     * @param _functionName The function call name which will be checked.
     * @return true, if supported
     */
    public static boolean mathematicalFunctionIsSupported(String _functionName) {
        switch (_functionName) {
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

    private static boolean userDefinedFunctionIsSupported(String expr, LEMSCollector container) {
        for (DerivedElement elem : container.getDerivedElementList()) {
            if (elem.getName().equals(expr)) {
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
     * @param container     the container holds user defined functions which have to be seen as supported
     * @return true, if expression or sub-expression contains call.
     */
    public static boolean containsFunctionCall(ASTExpr expr, boolean skipSupported, LEMSCollector container) {
        boolean temp = false;
        //if more functions are supported
        if (expr.functionCallIsPresent() && !(skipSupported && (mathematicalFunctionIsSupported(
                expr.getFunctionCall().get().getName().toString())) || userDefinedFunctionIsSupported(expr.getFunctionCall().get().getName().toString(),
                container))) {
            temp = true;
        }
        if (expr.exprIsPresent()) {
            temp = temp || containsFunctionCall(expr.getExpr().get(), skipSupported, container);
        }
        if (expr.leftIsPresent()) {
            temp = temp || containsFunctionCall(expr.getLeft().get(), skipSupported, container);
        }
        if (expr.rightIsPresent()) {
            temp = temp || containsFunctionCall(expr.getRight().get(), skipSupported, container);
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

        /*
        //in the case we get a numerical literal directly
        if (expr instanceof NumericLiteral) {
            if ((((NumericLiteral) expr).hasType())) {
                Dimension tempDimension;
                Unit tempUnit;
                if(((NumericLiteral) expr).getType().get().isLeft()){//Value -> its a TypeSymbol
                    //create the required units and dimensions
                    tempDimension = new Dimension(((NumericLiteral) expr).getType().get().getLeft());
                    tempUnit = new Unit(((NumericLiteral) expr).getType().get().getLeft());
                }else{//error -> its a AStUnitType
                    int[] dec = convertTypeDeclToArray(((NumericLiteral) expr).getType().get().getRight().getSerializedUnit());
                    tempDimension = new Dimension(PREFIX_DIMENSION +
                                    HelperCollection.getExpressionFromUnitType(((NumericLiteral) expr).getType().get().getRight()).print(),
                                    dec[2], dec[3], dec[1], dec[6], dec[0], dec[5], dec[4]);
                    tempUnit = new Unit(HelperCollection.getExpressionFromUnitType(((NumericLiteral) expr).getType().get().getRight()).print(),
                            dec[7], tempDimension);
                }
                container.addDimension(tempDimension);
                container.addUnit(tempUnit);

                //finally a constant representing the concrete value, a reference is set to this constant
                Constant tempConstant = new Constant(PREFIX_CONSTANT + ((NumericLiteral) expr).printValueType(),
                        tempDimension.getName(), expr, false);
                container.addConstant(tempConstant);

                return new Variable(tempConstant.getName());
            }
        }
        //otherwise it is an expression
        for (Expression exp : temp) {
            if (((NumericLiteral) exp).hasType()) {
                if (((NumericLiteral) exp).getType().isPresent() && ((NumericLiteral) exp).getType().get().isRight()&&
                        ((NumericLiteral) exp).getType().get().getRight().getSerializedUnit() == null) {
                    //this is a rather bad approach, since it requires that the same unit is already somewhere defined
                    //in the model, however, this part of the method it only invoked for artificial extensions, thus not critical
                    Dimension tempDimension = null;
                    for (Unit u : container.getUnitsSet()) {
                        if (u.getSymbol().equals(((NumericLiteral) exp).getType().get().getRight().getUnit().get())) {
                            tempDimension = u.getDimension();
                            break;
                        }
                    }
                    if (tempDimension != null) {
                        Constant tempConstant = new Constant(PREFIX_CONSTANT + ((NumericLiteral) exp).printValueType(),
                                tempDimension.getName(), exp, false);
                        container.addConstant(tempConstant);
                        Variable var = new Variable(tempConstant.getName());
                        expr.replaceElement(exp, var);
                    } else {
                        System.err.println("A problematic case occurred during constant replacement!");
                    }
                } else {
                    Dimension tempDimension;
                    Unit tempUnit;
                    if(((NumericLiteral) exp).getType().get().isLeft()){//Value -> its a TypeSymbol
                        //create the required units and dimensions
                        tempDimension = new Dimension(((NumericLiteral) exp).getType().get().getLeft());
                        tempUnit = new Unit(((NumericLiteral) exp).getType().get().getLeft());
                    }else{//error -> its a AStUnitType
                        int[] dec = convertTypeDeclToArray(((NumericLiteral) exp).getType().get().getRight().getSerializedUnit());
                        tempDimension = new Dimension(PREFIX_DIMENSION +
                                HelperCollection.getExpressionFromUnitType(((NumericLiteral) exp).getType().get().getRight()).print(),
                                dec[2], dec[3], dec[1], dec[6], dec[0], dec[5], dec[4]);
                        tempUnit = new Unit(HelperCollection.getExpressionFromUnitType(((NumericLiteral) exp).getType().get().getRight()).print(),
                                dec[7], tempDimension);
                    }
                    //finally a constant representing the concrete value, a reference is set to this constant
                    Constant tempConstant = new Constant(PREFIX_CONSTANT + ((NumericLiteral) exp).printValueType(),
                            tempDimension.getName(), exp, false);
                    container.addConstant(tempConstant);
                    container.addUnit(tempUnit);
                    container.addDimension(tempDimension);
                    Variable var = new Variable(tempConstant.getName());
                    expr.replaceElement(exp, var);
                }
            }
        }
        */
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
            NumericLiteral lit1 = new NumericLiteral(1);
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
        return expr;
        /*
        if (expr.containsNamedFunction("resolution", new ArrayList<>())) {
            ASTUnitType tempType = new ASTUnitType();
            tempType.setUnit(container.getConfig().getSimulationStepsUnit().getSymbol());
            Function tempFunction = new Function("resolution", new ArrayList<>());
            NumericLiteral literal;// = new NumericLiteral(container.getConfig().getSimulationStepsLength(),
                   // tempType);TODO
            Constant tempConstant = new Constant(HelperCollection.PREFIX_CONSTANT + container.getConfig().getSimulationStepsLengthAsString() +
                    tempType.getUnit().get().toString(), HelperCollection.PREFIX_DIMENSION + tempType.getUnit().get().toString(), literal, false);
            container.addConstant(tempConstant);
            expr.replaceElement(tempFunction, new Variable(tempConstant.getName()));
            return expr;
        }*/
        //return new Expression();//expr;//TODO
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
            NumericLiteral lit = new NumericLiteral(unitType.getUnitlessLiteral().get().getValue());
            ret.replaceLhs(lit);
        }
        if (unitType.baseIsPresent() && unitType.exponentIsPresent()) {
            Operator op = new Operator();
            op.setPower(true);
            ret.replaceLhs(getExpressionFromUnitType(unitType.getBase().get()));
            ret.replaceOp(op);
            ret.replaceRhs(new NumericLiteral(unitType.getExponent().get().getValue()));
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
     */
    public static void retrieveUnitsFromExpression(Expression expr, LEMSCollector container) {
        /*
        for (Expression numeric : expr.getNumericals()) {
            if (((NumericLiteral) numeric).hasType()&& ((NumericLiteral) numeric).getType().get().isLeft()) {
                container.handleType(((NumericLiteral) numeric).getType().get().getLeft());
            }else if(((NumericLiteral) numeric).hasType()&& ((NumericLiteral) numeric).getType().get().isRight()){
                container.handleType(((NumericLiteral) numeric).getType().get().getRight());
            }
        }*/
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
        Expression lhs = new NumericLiteral(0);
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
    public static Expression replacementRoutine(LEMSCollector container, Expression expr) {
        Expression tempExpression = replaceConstantsWithReferences(container, expr);
        tempExpression = replaceFunctionCallByReference(container, tempExpression);
        tempExpression = replaceDifferentialVariable(tempExpression);
        tempExpression = replaceEulerByExponentialFunction(tempExpression);
        tempExpression = replaceResolutionByConstantReference(container, tempExpression);
        createConstantsFromPredefinedUnits(container,tempExpression);
        processUnitsOfImplicitVariables(container,tempExpression);
        return replaceResolutionByConstantReference(container, tempExpression);
    }

    /**
     * Replaces in a given expression function calls to user defined function by a proper derived variable.
     *
     * @param container a container containing the variable
     * @param expr      the expression
     * @return expression with replace function call
     */
    public static Expression replaceFunctionCallByReference(LEMSCollector container, Expression expr) {
        checkNotNull(expr);
        checkNotNull(container);
        if (expr instanceof Function) {
            return new Variable(((Function) expr).getFunctionName());
        } else {
            List<Expression> listOfCalls = expr.getFunctions();
            //collect the names of all derived variables
            List<String> udFNames = container.getDerivedElementList().stream().map(param -> param.getName()).collect(toList());
            for (Expression call : listOfCalls) {
                if (udFNames.contains(((Function) call).getFunctionName())) {
                    expr.replaceElement(call, new Variable(((Function) call).getFunctionName()));
                }
            }
            return expr;
        }
    }

    /**
     * Replaces the differential equation in a given expression by a proper representation.
     *
     * @param _expr the expression
     * @return the modified expression
     */
    public static Expression replaceDifferentialVariable(Expression _expr) {
        if (_expr instanceof Variable) {//if it is a variable replace by a proper name
            ((Variable) _expr).setVariable(Names.convertToCPPName(((Variable) _expr).getVariable()));
        }
        if (_expr.lhsIsPresent()) {
            HelperCollection.replaceDifferentialVariable(_expr.getLhs().get());
        }
        if (_expr.rhsIsPresent()) {
            HelperCollection.replaceDifferentialVariable(_expr.getRhs().get());
        }

        return _expr;
    }

    /**
     * LEMS does not support the constant e ( euler's number), thus each reference to this constant has to
     * be replaced by exp(1).
     *
     * @param _expr the expression possibly containing e
     * @return the expression without e
     */
    public static Expression replaceEulerByExponentialFunction(Expression _expr) {
        checkNotNull(_expr);
        List<Expression> tListOfArgs = new ArrayList<>();
        tListOfArgs.add(new NumericLiteral(1));
        for (Expression tExpr : _expr.getVariables()) {
            if (((Variable) tExpr).getVariable().equals("e")) {
                _expr.replaceElement(tExpr, new Function("exp",tListOfArgs));
            }
        }
        return _expr;
    }


    public static void createConstantsFromPredefinedUnits(LEMSCollector _container, Expression _expression){
        List<Expression> tVariableList = _expression.getVariables();
        Constant tConstant;
        for(Expression tVariable:tVariableList){
            if(((Variable) tVariable).typeIsPresent()){
                if(((Variable) tVariable).isImplicitUnit()){
                    tConstant = new Constant(((Variable) tVariable).getVariable(), ((Variable) tVariable).getType());
                    _container.addConstant(tConstant);
                }
            }
        }
    }


    /**
     * The latest update of NESTML proposed a new concept, where units are no longer part of a numeric literal, but
     * a separate variable, e.g. instead of 10mV we now use 10*mV where mV has the correct unit. This method checks
     * a given ASTExpr, extracts all implicitly declared variables (like mV) and stores them as new variables in
     * the handed over container.
     */
    public static void processUnitsOfImplicitVariables(LEMSCollector _container, Expression _expression) {
        List<Expression> tVariableList = _expression.getVariables();
        for (Expression tVariable : tVariableList) {
            if (((Variable) tVariable).typeIsPresent()) {
                _container.handleType(((Variable) tVariable).getType());
            }
        }
    }


    /**
     * Checks if the handed over expression contains a variable, resolves this variable and returns the corresponding variable
     * symbol.
     *
     * @param _expr an expression object possibly containing a variable
     * @return a optional variable symbol
     */
    public static Optional<VariableSymbol> resolveVariableSymbol(ASTExpr _expr) {
        Optional<VariableSymbol> tSymbol = Optional.empty();
        if (_expr.variableIsPresent()) {
            tSymbol = _expr.getEnclosingScope().get().resolve(_expr.getVariable().get().getName().toString(), VariableSymbol.KIND);
        }
        return tSymbol;
    }


    /**
     * Prints the name of a unit stored in the type symbol if present,otherwise only an empty optional.
     * @return
     */
    public static String getNameOfTypeSymbolUnit(TypeSymbol _tSymbol){
        UnitRepresentation uR = UnitRepresentation.getBuilder().serialization(_tSymbol.getName()).build();
        return uR.prettyPrint();
    }

    /**
     * Returns true iff no operator is active
     * @param _expr a expression object
     * @return true iff no operator
     */
    public static boolean hasNoOperator(ASTExpr _expr){
        return !_expr.isLeftParentheses()&&!_expr.isRightParentheses()&&!_expr.isPow()&&_expr.isUnaryPlus()&&
                !_expr.isUnaryMinus()&&!_expr.isUnaryTilde()&&!_expr.isTimesOp()&&!_expr.isDivOp()&&!_expr.isModuloOp()&&
                !_expr.isPlusOp()&&!_expr.isPlusOp()&&!_expr.isMinusOp()&&!_expr.isShiftLeft()&&!_expr.isShiftRight()&&
                !_expr.isBitXor()&&!_expr.isBitOr()&&!_expr.isBitAnd()&&!_expr.isLt()&&!_expr.isLe()&&!_expr.isEq()&&
                !_expr.isNe()&&!_expr.isNe2()&&!_expr.isGe()&&!_expr.isGt()&&!_expr.isLogicalAnd()&&!_expr.isLogicalOr()&&
                !_expr.isLogicalNot()&& !_expr.isInf();
    }

    public static boolean isLiteralUnit(ASTExpr _expr){
        if(!(_expr.leftIsPresent() && _expr.rightIsPresent())){
            return false;
        }
        if(!_expr.getLeft().get().getNumericLiteral().isPresent()){
            return false;
        }
        if(!_expr.getRight().get().getVariable().isPresent()){
            return false;
        }
        if(!_expr.isTimesOp()){
            return false;
        }
        return true;
    }




}
