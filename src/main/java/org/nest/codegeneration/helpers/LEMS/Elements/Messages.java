package org.nest.codegeneration.helpers.LEMS.Elements;

import org.nest.codegeneration.helpers.LEMS.Expressions.Expression;
import org.nest.commons._ast.ASTFunctionCall;
import org.nest.ode._ast.ASTEquation;
import org.nest.ode._ast.ASTShape;
import org.nest.spl._ast.ASTSmall_Stmt;
import org.nest.symboltable.symbols.VariableSymbol;

/**
 * This class encapsulates all messages which are either printed to the console or to the target file.
 * @author kperun
 */
public class Messages {

    public static void printArrayNotSupportedMessage(VariableSymbol _varSymbol, LEMSCollector _container) {
        System.err.println(
                "LEMS Error (Line: "
                        + _container.getNeuronName() + "/"
                        + _varSymbol.getSourcePosition().getLine()
                        + "):"
                        + " Array declaration found. ("
                        + _varSymbol.getName()
                        + ")");

    }

    public static String getArrayNotSupportedMessage(VariableSymbol _varSymbol) {
        return "Array declaration in lines" + _varSymbol.getSourcePosition().getLine();
    }


    public static void printNotSupportedFunctionCallFoundMessage(ASTEquation _equation, LEMSCollector _container) {
        System.err.println(
                "LEMS Error (Line: "
                        + _container.getNeuronName() + "/"
                        + _equation.get_SourcePositionStart().getLine()
                        + "):"
                        + " Not supported function call in expression. ("
                        + _container.getPrettyPrint().print(_equation.getRhs(), false)
                        + ")");

    }

    /**
     * This method prints and stores a message regarding a not supported yet found
     * function call inside a expression.
     *
     * @param _varSymbol the variable symbol whose declaration has a function call which is not supportd
     */
    public static void printNotSupportedFunctionCallInExpression(VariableSymbol _varSymbol, LEMSCollector _container) {
        System.err.println(
                "LEMS Error (Line: "
                        + _container.getNeuronName() + "/"
                        + _varSymbol.getSourcePosition().getLine()
                        + "):"
                        + " Function call found in (constant|parameter) declaration." + "("
                        + _container.getPrettyPrint().print(_varSymbol.getDeclaringExpression().get(), false) + ")");
    }

    public static void printNotSupportedFunctionCallInEquations(ASTShape _shape, LEMSCollector _container) {
        System.err.println(
                "LEMS-Error (Line: "
                        + _container.getNeuronName() + "/"
                        + _shape.get_SourcePositionStart().getLine()
                        + "):"
                        + " Not supported function call in equation. (" +
                        (new Expression(_shape.getRhs())).print()
                        + ")");

    }

    public static void printNotSupportedFunctionInBlock(ASTSmall_Stmt _smallStmt, LEMSCollector _container) {
        System.err.print(
                "LEMS-Error (Line: "
                        + _container.getNeuronName() + "/"
                        + _smallStmt.get_SourcePositionStart().getLine()
                        + "):"
                        + " Not supported function call in function. ");

        if (_smallStmt.getAssignment().isPresent()) {
            System.err.print("(" + _container.getPrettyPrint().print(_smallStmt.getAssignment().get().getExpr(), false) + ")\n");
        }
        if (_smallStmt.getDeclaration().isPresent()) {
            System.err.print("(" + _container.getPrettyPrint().print(_smallStmt.getDeclaration().get().getExpr().get(), false) + ")\n");
        }
        if (_smallStmt.getFunctionCall().isPresent()) {
            System.err.print("(" + _smallStmt.getFunctionCall().get().getCalleeName() + ")\n");
        }
        _container.addNotConverted("Not supported function call in update block, lines " + _smallStmt.get_SourcePositionStart() + " to " + _smallStmt.get_SourcePositionEnd());
    }



    /**
     * This method prints and stores an adequate message regarding not supported
     * elements during the transformation
     *
     * @param _varSymbol the variable whose type is not supported
     */
    public static void printNotSupportedDataType(VariableSymbol _varSymbol, LEMSCollector _container) {
        System.err.println("Not supported data-type found: \"" + _varSymbol.getType().getName() + "\".");
        _container.addNotConverted("Not supported data-type "
                + _varSymbol.getType().prettyPrint() + " in lines "
                + _varSymbol.getAstNode().get().get_SourcePositionStart()
                + " to " + _varSymbol.getAstNode().get().get_SourcePositionEnd()
                + ".");
    }

    /**
     * Prints a message in the error out-stream which states that the handed over integration function call has a
     * wrong number of arguments.
     * @param _functionCall the function call in which integration is contained
     * @param _container a container as required to retrieve the name of the neuron
     */
    public static void printIntegrateWronglyDeclared(ASTFunctionCall _functionCall,LEMSCollector _container){
        System.err.println(
                "LEMS Error (Line: "
                        + _container.getNeuronName() + "/"
                        + _functionCall.get_SourcePositionStart().getLine()
                        + "):"
                        + " Integrate is wrongly declared, <>1 argument provided.");
    }


    /**
     * Prints a message which states that a error during processing of a small statement occured.
     *
     */
    public static void printErrorInSmallStatement(ASTSmall_Stmt _astSmall_stmt,LEMSCollector _container){
        System.err.println(
                "LEMS Error (Line: "
                        + _container.getNeuronName() + "/"
                        + _astSmall_stmt.get_SourcePositionStart().getLine()
                        + "):"
                        + " Something went wrong in small statement processing.");
    }

}
