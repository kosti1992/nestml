package org.nest.codegeneration.helpers.LEMS.Elements.Dynamics;

import org.nest.codegeneration.helpers.LEMS.Expressions.Expression;
import org.nest.codegeneration.helpers.LEMS.Expressions.Function;
import org.nest.codegeneration.helpers.LEMS.Expressions.LEMSSyntaxContainer;
import org.nest.commons._ast.ASTFunctionCall;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This class is used to store concrete instructions, namely function calls.
 */
public class FunctionCall extends Instruction {
    private final String mClassIdentifier = "FunctionCall";//required by the backend
    private Function mFunctionCallExpr;

    public FunctionCall(ASTFunctionCall _astFunctionCall) {
        checkNotNull(_astFunctionCall);
        this.mFunctionCallExpr = new Function(_astFunctionCall);
    }

    public FunctionCall(String _functionName, List<Expression> _arguments) {
        checkNotNull(_functionName);
        checkNotNull(_arguments);
        this.mFunctionCallExpr = new Function(_functionName, _arguments);
    }

    @SuppressWarnings("unused")//used in the template
    public String printName() {
        return this.mFunctionCallExpr.getFunctionName();
    }

    @SuppressWarnings("unused")//used in the template
    public String printArgs() {
        StringBuilder newBuilder = new StringBuilder();
        for (Expression expr : this.mFunctionCallExpr.getArguments()) {
            newBuilder.append(expr.print(new LEMSSyntaxContainer()));
            newBuilder.append(",");
        }
        newBuilder.deleteCharAt(newBuilder.length() - 1);//delete the last "," before the end of the string
        return newBuilder.toString();
    }

    public String getClassIdentifier() {
        return mClassIdentifier;
    }

    public List<Expression> getArgs() {
        return mFunctionCallExpr.getArguments();
    }
}