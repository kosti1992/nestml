package org.nest.codegeneration.helpers.LEMSElements;

import org.nest.codegeneration.helpers.Expressions.Expression;
import org.nest.codegeneration.helpers.Expressions.Operator;
import org.nest.codegeneration.helpers.Expressions.Variable;
import org.nest.commons._ast.ASTExpr;
import org.nest.nestml._ast.ASTInput;
import org.nest.nestml._ast.ASTInputLine;
import org.nest.symboltable.symbols.VariableSymbol;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This class represents a derived element, a characteristic of the model which possibly
 * changes over the time and is derived from other elements of the model.
 *
 * @author perun
 */
public class DerivedElement {
    private String name;
    private String dimension;
    private Expression derivationInstruction;
    private boolean dynamic;//distinguish between derived variables and derived parameter
    private boolean external;//uses an external source, i.e. other tags are used
    private Optional<String> reduceOption;//the reduce indicates (in the case of external vars) how to combine values to a single one
    private Optional<Map<Expression, Expression>> conditionalDerivedValues = Optional.empty();


    public DerivedElement(VariableSymbol variable, LEMSCollector container, boolean dynamic, boolean init) {
        if (init) {
            this.name = HelperCollection.PREFIX_INIT + variable.getName();
        } else {
            this.name = variable.getName();
        }
        this.dynamic = dynamic;
        //data types boolean and void are not supported by lems
        if (HelperCollection.dataTypeNotSupported(variable.getType())) {
            dimension = HelperCollection.NOT_SUPPORTED;
            //print an adequate error message
            HelperCollection.printNotSupportedDataType(variable, container);
        } else {
            dimension = HelperCollection.typeToDimensionConverter(variable.getType());
            dimension = HelperCollection.formatComplexUnit(dimension);
        }

        //get the derivation instruction in LEMS format
        derivationInstruction = new Expression(variable);
        //replace the resolution function call with a reference to the constant
        derivationInstruction = HelperCollection.replaceResolutionByConstantReference(container, derivationInstruction);
        //replace constants with references
        derivationInstruction = HelperCollection.replaceConstantsWithReferences(container, derivationInstruction);
    }


    /**
     * This constructor can be used to generate a hand made derived element.
     *
     * @param name                  the name of the new derived element
     * @param dimension             the dimension of the new derived element
     * @param derivationInstruction the instructions for derivation
     * @param dynamic               true, if derived element contains changeable elements
     */
    public DerivedElement(String name, String dimension,
                          Expression derivationInstruction, boolean dynamic, boolean external) {
        this.name = name;
        this.dimension = HelperCollection.formatComplexUnit(dimension);
        this.derivationInstruction = derivationInstruction;
        this.dynamic = dynamic;
        this.external = external;
    }

    /**
     * This method can be used to generate a new DerivedElement from a given XML node.
     *
     * @param xmlNode an DerivedElement xml node
     */
    public DerivedElement(Node xmlNode) {
        NamedNodeMap tempMap = xmlNode.getAttributes();
        for (int i = 0; i < tempMap.getLength(); i++) {
            if (tempMap.item(i).getNodeName().equals("name")) {
                this.name = tempMap.item(i).getNodeValue();
            } else if (tempMap.item(i).getNodeName().equals("dimension")) {
                this.dimension = tempMap.item(i).getNodeValue();
            } else if (tempMap.item(i).getNodeName().equals("value")) {
                this.derivationInstruction = new Expression(tempMap.item(i).getNodeValue());
                this.external = false;
            } else if (tempMap.item(i).getNodeName().equals("select")) {
                this.derivationInstruction = new Expression(tempMap.item(i).getNodeValue());
            } else if (tempMap.item(i).getNodeName().equals("reduce")) {
                this.external = true;
                this.reduceOption = Optional.of(tempMap.item(i).getNodeValue());
            }
        }
        if (xmlNode.getNodeName().equals("DerivedParameter")) {
            this.dynamic = false;
        } else {
            this.dynamic = true;
        }
    }

    /**
     * This constructor is used whenever a ternary operator is located in the expression.
     *
     * @param name      the name of the derived var
     * @param dimension the dimension of the derived var
     * @param expr      the expr containing the ternary op
     * @param container the lems collector fo further operations
     */
    public DerivedElement(String name, String dimension, ASTExpr expr, LEMSCollector container, boolean init) {
        this.name = name;
        if (init) {
            this.name = HelperCollection.PREFIX_INIT + this.name;
        }
        this.dimension = HelperCollection.formatComplexUnit(dimension);
        this.handleTernaryOp(expr, container);
    }


    /**
     * This method can be used to generate a conditional derived variable representing a ternary operator.
     * It generates a conditional derived variable instead of a normally derived variable.
     *
     * @param expr      the variable which shall be a conditional derived var
     * @param container a lems collector file
     */
    private void handleTernaryOp(ASTExpr expr, LEMSCollector container) {
        Map<Expression, Expression> tempMap = new HashMap<>();
        //first create the first part of the expression, namely the one which applies if condition is true
        Expression firstSubCondition;

        if (expr.getCondition().get().booleanLiteralIsPresent()) {
            if (expr.getCondition().get().getBooleanLiteral().get().getValue()) {
                firstSubCondition = Expression.generateTrue();
            } else {
                firstSubCondition = Expression.generateFalse();
            }
        } else {
            firstSubCondition = new Expression(expr.getCondition().get());
        }

        firstSubCondition = HelperCollection.replaceBooleanAtomByExpression(container, firstSubCondition);
        if (!(firstSubCondition.opIsPresent() && firstSubCondition.getOperator().get().isLeftParentheses() &&
                firstSubCondition.opIsPresent() && firstSubCondition.getOperator().get().isRightParentheses()))
            firstSubCondition = Expression.encapsulateInBrackets(firstSubCondition);
        Expression firstSubValue = new Expression(expr.getIfTrue().get());
        firstSubValue = HelperCollection.replaceConstantsWithReferences(container, firstSubValue);
        firstSubValue = HelperCollection.replaceResolutionByConstantReference(container, firstSubValue);
        firstSubCondition = HelperCollection.encapsulateExpressionInConditions(firstSubCondition);
        tempMap.put(firstSubCondition, firstSubValue);
        //now create the second part which applies if the condition is not true
        Expression secondSubCondition = firstSubCondition.deepClone();
        secondSubCondition.negateLogic();
        Expression secondSubValue = new Expression(expr.getIfNot().get());
        secondSubValue = HelperCollection.replaceConstantsWithReferences(container, secondSubValue);
        secondSubValue = HelperCollection.replaceResolutionByConstantReference(container, secondSubValue);
        tempMap.put(secondSubCondition, secondSubValue);
        this.conditionalDerivedValues = Optional.of(tempMap);
        this.dynamic = true;
    }

    /**
     * This constructor is used whenever a buffer is processed by the system. Here, a new derived variable is
     * created and the reduce and select operation is set accordingly.
     *
     * @param buffer a buffer definition in the source model
     */
    public DerivedElement(ASTInputLine buffer) {
        this.name = buffer.getName();
        //first the dimension
        if (buffer.isSpike()) {
            this.dimension = HelperCollection.DIMENSION_NONE;
        } else {
            //TODO current buffers are pA <- create new Unit
            this.dimension = HelperCollection.PREFIX_DIMENSION + "pA";
        }
        //now the reduction option
        this.reduceOption = Optional.of("add");

        //finally the derivation expression
        this.derivationInstruction = new Expression();
        Variable lhs = new Variable(buffer.getName() + "[*]");//select all
        Variable rhs = null;
        if (buffer.isCurrent()) {
            rhs = new Variable(HelperCollection.CURRENT_BUFFER_INPUT_VAR);
        } else {
            rhs = new Variable(HelperCollection.SPIKE_BUFFER_INPUT_VAR);
        }
        Operator tempOp = new Operator();
        tempOp.setDivOp(true);
        this.derivationInstruction.replaceLhs(lhs);
        this.derivationInstruction.replaceOp(tempOp);
        this.derivationInstruction.replaceRhs(rhs);
        this.dynamic = true;//a buffer is a dynamic element
    }


    @SuppressWarnings("unused")//used in the template
    public String getName() {
        return this.name;
    }

    @SuppressWarnings("unused")//used in the template
    public Expression getDerivationInstruction() {
        return this.derivationInstruction;
    }

    @SuppressWarnings("unused")//used in the template
    public String getDimension() {
        return this.dimension;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public boolean isExternal() {
        return external;
    }

    @SuppressWarnings("unused")//used in the template
    public boolean isConditionalDerived() {
        return this.conditionalDerivedValues.isPresent();
    }

    public Map<Expression, Expression> getConditionalDerivedValues() {
        if (this.conditionalDerivedValues.isPresent()) {
            return this.conditionalDerivedValues.get();
        } else {
            return new HashMap<>();
        }
    }

    /**
     * This method is required since the api of freemarker has been deactivated in monticore, thus a direct fetching of
     * values by keys from maps is not possible.
     *
     * @return a string,string map with conditions,values
     */
    public Map<String, String> getConditionalDerivedValuesAsStrings() {
        if (this.conditionalDerivedValues.isPresent()) {
            Map<String, String> tempMap = new HashMap<>();
            for (Expression key : this.conditionalDerivedValues.get().keySet()) {
                tempMap.put(key.print(), this.conditionalDerivedValues.get().get(key).print());
            }
            return tempMap;
        } else {
            return new HashMap<>();
        }
    }

    public boolean hasReduceOption() {
        return this.reduceOption.isPresent();
    }

    @SuppressWarnings("unused")//used in the template
    public String getReduce() {
        return this.reduceOption.get();
    }

    public boolean equals(Object o) {
        return this.getClass().equals(o.getClass()) &&
                this.name.equals(((DerivedElement) o).getName()) &&
                this.derivationInstruction.equals(((DerivedElement) o).derivationInstruction) &&
                this.dynamic == (((DerivedElement) o).dynamic);
    }

    public int hashCode() {
        if (this.dynamic) {
            return this.getClass().hashCode() +
                    this.name.hashCode() +
                    this.derivationInstruction.hashCode();
        }
        return -(this.getClass().hashCode() +
                this.name.hashCode() +
                this.derivationInstruction.hashCode());
    }
}

