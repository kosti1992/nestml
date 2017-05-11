package org.nest.codegeneration.helpers.LEMSElements;

import org.nest.codegeneration.helpers.Expressions.Expression;
import org.nest.codegeneration.helpers.Expressions.NumericalLiteral;
import org.nest.codegeneration.helpers.Expressions.Operator;
import org.nest.codegeneration.helpers.Expressions.Variable;
import org.nest.commons._ast.ASTExpr;

import org.nest.nestml._ast.ASTInputLine;
import org.nest.spl._ast.ASTDeclaration;
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
public class DerivedElement extends LEMSElement{
    private String mName;
    private String mDimension;
    private Expression mValue;
    private boolean mIsDynamic;//distinguish between derived variables and derived parameters
    private boolean mIsExternal;//uses an mIsExternal source, i.e. other tags are used
    private Optional<String> mReduceOption = Optional.empty();//the reduce indicates (in the case of mIsExternal vars) how to combine values to a single one
    private Optional<Map<Expression, Expression>> mConditionalDerivedValues = Optional.empty();


    public DerivedElement(VariableSymbol _variable, LEMSCollector _container, boolean _isDynamic, boolean _isInitialization) {
        if (_isInitialization) {
            this.mName = HelperCollection.PREFIX_INIT + _variable.getName();
        } else {
            this.mName = _variable.getName();
        }
        this.mIsDynamic = _isDynamic;
        //data types boolean and void are not supported by lems
        if (HelperCollection.dataTypeNotSupported(_variable.getType())) {
            mDimension = HelperCollection.NOT_SUPPORTED;
            //print an adequate error message
            Messages.printNotSupportedDataType(_variable, _container);
        } else {
            mDimension = HelperCollection.typeToDimensionConverter(_variable.getType());
            mDimension = HelperCollection.formatComplexUnit(mDimension);
        }
        //get the derivation instruction in LEMS format
        mValue = new Expression(_variable);
        //replace the resolution function call with a reference to the constant, replace direct constants with references
        mValue = HelperCollection.replacementRoutine(_container, mValue);
    }


    /**
     * This constructor can be used to generate a hand made derived element.
     *
     * @param _name the name of the new derived element
     * @param _dimension the dimension of the new derived element
     * @param _value the instructions for derivation
     * @param _isDynamic true, if derived element contains changeable elements
     * @param _isExternal true, if the derived element is derived from external parts
     */
    public DerivedElement(String _name, String _dimension,
                          Expression _value, boolean _isDynamic, boolean _isExternal) {
        this.mName = _name;
        this.mDimension = HelperCollection.formatComplexUnit(_dimension);
        this.mValue = _value;
        this.mIsDynamic = _isDynamic;
        this.mIsExternal = _isExternal;
    }

    /**
     * This method can be used to generate a new DerivedElement from a given XML node.
     *
     * @param _xmlNode an DerivedElement xml node
     */
    public DerivedElement(Node _xmlNode) {
        NamedNodeMap tempMap = _xmlNode.getAttributes();
        for (int i = 0; i < tempMap.getLength(); i++) {
            if (tempMap.item(i).getNodeName().equals("name")) {
                this.mName = tempMap.item(i).getNodeValue();
            } else if (tempMap.item(i).getNodeName().equals("dimension")) {
                this.mDimension = tempMap.item(i).getNodeValue();
            } else if (tempMap.item(i).getNodeName().equals("value")) {
                this.mValue = new Expression(tempMap.item(i).getNodeValue());
                this.mIsExternal = false;
            } else if (tempMap.item(i).getNodeName().equals("select")) {
                this.mValue = new Expression(tempMap.item(i).getNodeValue());
            } else if (tempMap.item(i).getNodeName().equals("reduce")) {
                this.mIsExternal = true;
                this.mReduceOption = Optional.of(tempMap.item(i).getNodeValue());
            }
        }
        if (_xmlNode.getNodeName().equals("DerivedParameter")) {
            this.mIsDynamic = false;
        } else {
            this.mIsDynamic = true;
        }
    }

    /**
     * This constructor is used whenever a ternary operator is located in the expression.
     *
     * @param _name      the mName of the derived var
     * @param _dimension the mDimension of the derived var
     * @param _value      the expr containing the ternary op
     * @param _container the lems collector fo further operations
     */
    public DerivedElement(String _name, String _dimension, ASTExpr _value, LEMSCollector _container, boolean _isInitialization) {
        this.mName = _name;
        if (_isInitialization) {
            this.mName = HelperCollection.PREFIX_INIT + this.mName;
        }
        this.mDimension = HelperCollection.formatComplexUnit(_dimension);
        this.handleTernaryOp(_value, _container);
    }


    /**
     * This method can be used to generate a conditional derived variable representing a ternary operator.
     * It generates a conditional derived variable instead of a normally derived variable.
     *
     * @param _value the variable which shall be a conditional derived var
     * @param _container a LEMS collector file
     */
    private void handleTernaryOp(ASTExpr _value, LEMSCollector _container) {
        Map<Expression, Expression> tempMap = new HashMap<>();
        //first create the first part of the expression, namely the one which applies if condition is true
        Expression firstSubCondition;

        if (_value.getCondition().get().booleanLiteralIsPresent()) {
            if (_value.getCondition().get().getBooleanLiteral().get().getValue()) {
                firstSubCondition = Expression.generateTrue();
            } else {
                firstSubCondition = Expression.generateFalse();
            }
        } else {
            firstSubCondition = new Expression(_value.getCondition().get());
        }

        firstSubCondition = HelperCollection.replaceBooleanAtomByExpression(_container, firstSubCondition);
        if (!(firstSubCondition.opIsPresent() && firstSubCondition.getOperator().get().isLeftParentheses() &&
                firstSubCondition.opIsPresent() && firstSubCondition.getOperator().get().isRightParentheses()))
            firstSubCondition = Expression.encapsulateInBrackets(firstSubCondition);
        Expression firstSubValue = new Expression(_value.getIfTrue().get());
        firstSubValue = HelperCollection.replaceConstantsWithReferences(_container, firstSubValue);
        firstSubValue = HelperCollection.replaceResolutionByConstantReference(_container, firstSubValue);
        firstSubCondition = HelperCollection.encapsulateExpressionInConditions(firstSubCondition);
        tempMap.put(firstSubCondition, firstSubValue);
        //now create the second part which applies if the condition is not true
        Expression secondSubCondition = firstSubCondition.deepClone();
        secondSubCondition.negateLogic();
        Expression secondSubValue = new Expression(_value.getIfNot().get());
        secondSubValue = HelperCollection.replaceConstantsWithReferences(_container, secondSubValue);
        secondSubValue = HelperCollection.replaceResolutionByConstantReference(_container, secondSubValue);
        tempMap.put(secondSubCondition, secondSubValue);
        this.mConditionalDerivedValues = Optional.of(tempMap);
        this.mIsDynamic = true;
    }

    /**
     * This constructor is used whenever a buffer is processed by the system. Here, a new derived variable is
     * created and the reduce and select operation is set accordingly.
     *
     * @param _buffer a buffer definition in the source model
     */
    public DerivedElement(ASTInputLine _buffer) {
        this.mName = _buffer.getName();
        //first the mDimension
        if (_buffer.isSpike()) {//spike buffers do not have a dimension
            this.mDimension = HelperCollection.DIMENSION_NONE;
        } else {//current buffers have "current" as dimension
            this.mDimension = HelperCollection.PREFIX_DIMENSION + "pA";
        }
        //now the reduction option, which is in the case of NEST always add
        this.mReduceOption = Optional.of("add");

        //finally the derivation expression
        this.mValue = new Expression();
        Variable lhs = new Variable(_buffer.getName() + "[*]");//select all
        Variable rhs;
        if (_buffer.isCurrent()) {
            rhs = new Variable(HelperCollection.CURRENT_BUFFER_INPUT_VAR);
        } else {
            rhs = new Variable(HelperCollection.SPIKE_BUFFER_INPUT_VAR);
        }
        Operator tempOp = new Operator();
        tempOp.setDivOp(true);
        this.mValue.replaceLhs(lhs);
        this.mValue.replaceOp(tempOp);
        this.mValue.replaceRhs(rhs);
        this.mIsDynamic = true;//a buffer is a is a dynamic element
    }

    /**
     * Handles a ASTDeclaration of an variable declared inside a block.
     * @param _declaration the ast declaration of the element
     */
    public DerivedElement(String _name,ASTDeclaration _declaration,LEMSCollector _container){
        this.mName = _name;
        this.mDimension = HelperCollection.typeToDimensionConverter(_declaration.getDatatype());
        if(_declaration.getExpr().isPresent()){
            this.mValue = new Expression(_declaration.getExpr().get());
        }else{
            if(_declaration.getDatatype().getUnitType().isPresent()){
                this.mValue = new NumericalLiteral(0,_declaration.getDatatype().getUnitType().get());
            }else {
                this.mValue = new NumericalLiteral(0,null);
            }
        }
        //TODO: ternary op handling -> this is only in user defined functions
        this.mValue = HelperCollection.replacementRoutine(_container,this.mValue);
        this.mDimension = HelperCollection.typeToDimensionConverter(_declaration.getDatatype());
        this.mIsDynamic = true;
        this.mIsExternal = false;
        this.mReduceOption = Optional.empty();
    }


    @SuppressWarnings("unused")//used in the template
    public String getName() {
        return this.mName;
    }

    @SuppressWarnings("unused")//used in the template
    public Expression getValue() {
        return this.mValue;
    }

    @SuppressWarnings("unused")//used in the template
    public String getDimension() {
        return this.mDimension;
    }

    public boolean isDynamic() {
        return mIsDynamic;
    }

    public boolean isExternal() {
        return mIsExternal;
    }

    @SuppressWarnings("unused")//used in the template
    public boolean isConditionalDerived() {
        return this.mConditionalDerivedValues.isPresent();
    }

    @SuppressWarnings("unused")//used in the template
    public Map<Expression, Expression> getConditionalDerivedValues() {
        if (this.mConditionalDerivedValues.isPresent()) {
            return this.mConditionalDerivedValues.get();
        } else {
            return new HashMap<>();
        }
    }

    /**
     * This method is required since the api of freemarker has been deactivated in Monticore, thus a direct fetching of
     * values by keys (which are not strings) from maps is not possible.
     *
     * @return a <string,string> map with conditions,values
     */
    public Map<String, String> getConditionalDerivedValuesAsStrings() {
        if (this.mConditionalDerivedValues.isPresent()) {
            Map<String, String> tempMap = new HashMap<>();
            for (Expression key : this.mConditionalDerivedValues.get().keySet()) {
                tempMap.put(key.print(), this.mConditionalDerivedValues.get().get(key).print());
            }
            return tempMap;
        } else {
            return new HashMap<>();
        }
    }

    public boolean hasReduceOption() {
        return this.mReduceOption.isPresent();
    }

    @SuppressWarnings("unused")//used in the template
    public String getReduce() {
        return this.mReduceOption.get();
    }

    public boolean equals(Object _o) {
        return this.getClass().equals(_o.getClass()) &&
                this.mName.equals(((DerivedElement) _o).getName()) &&
                this.mValue.equals(((DerivedElement) _o).mValue) &&
                this.mIsDynamic == (((DerivedElement) _o).mIsDynamic);
    }

    public int hashCode() {
        if (this.mIsDynamic) {
            return this.getClass().hashCode() +
                    this.mName.hashCode() +
                    this.mValue.hashCode();
        }
        return -(this.getClass().hashCode() +
                this.mName.hashCode() +
                this.mValue.hashCode());
    }
}

