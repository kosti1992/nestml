package org.nest.codegeneration.helpers.LEMSElements;

import org.nest.codegeneration.helpers.Expressions.Expression;
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


}
