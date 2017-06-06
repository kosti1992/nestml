package org.nest.codegeneration.helpers.LEMS.Elements;

import org.nest.codegeneration.helpers.LEMS.Expressions.*;
import org.nest.codegeneration.helpers.LEMS.helpers.EitherTuple;
import org.nest.symboltable.symbols.TypeSymbol;
import org.nest.symboltable.symbols.VariableSymbol;
import org.nest.units._ast.ASTUnitType;
import org.w3c.dom.Node;

import java.util.Optional;

/**
 * This class represents a static non-alias element of the modeled neuron derived from the mParameter or the internal
 * block located in the source NESTML model.
 *
 * @author perun
 */
public class Constant extends LEMSElement{
    private String mName;/*The mName of the constant, e.g "V_ref" */
    private String mDimension;/* Name of the mDimension of the constant, e.g "voltage"*/
    private Expression mValue;
    private boolean mParameter = false;/*indicates whether this constant is a parameter or a real constant*/

    /**
     * This constructor can be used to create a new Constant object representing either a constant or a mParameter.
     *
     * @param _variable    a symbol from which a constant will be created
     * @param _isInit      indicates whether this mValue represents a init mValue of variable or not
     * @param _isParameter indicates whether it is a mParameter or not
     * @param _container   the container in which the new object will be stored and whose prettyPrinter shall be used
     */
    public Constant(VariableSymbol _variable, boolean _isInit, boolean _isParameter, LEMSCollector _container) {
        this.mName = _variable.getName();
        this.mDimension = HelperCollection.typeToDimensionConverter(_variable.getType());
        this.mDimension = HelperCollection.dimensionFormatter(mDimension);//format the mDimension to make it LEMS readable
        this.mParameter = _isParameter;
        if (_isInit) {
            this.mName = HelperCollection.PREFIX_INIT + this.mName;//init values are extended by a label in order to indicate as such
        }
        if (!mParameter) {//a parameter does not have a unit or a value, thus should not be checked
            //if a declaring expression is present, convert it
            if (_variable.getDeclaringExpression().isPresent()) {
                //check whether the variable has a function call
                if (_variable.getDeclaringExpression().get().functionCallIsPresent()) {
                    this.mValue = this.processFunctionCallInConstantDefinition(_variable, _container);
                } else {
                    this.mValue = new Expression(_variable);
                    HelperCollection.retrieveUnitsFromExpression(this.mValue, _container);
                }
            } else {//otherwise this is an initialization, thus create a new numerical literal or variable
                if (_variable.getType().getType() == TypeSymbol.Type.UNIT) {// in case a unit is used, we have to create num
                    /*
					ASTUnitType tempType = new ASTUnitType();
					tempType.setUnit(variable.getType().prettyPrint());
					NumericLiteral literal = new NumericLiteral(0, tempType);*/
                    this.mValue = new Expression(_variable.getDeclaringExpression().get());
                } else {//var does not have unit, a variable with mValue 0 is sufficient
                    this.mValue = new NumericLiteral(0);
                }
            }
        }

        //store an adequate message if the data type is not supported
        if (this.mDimension.equals(HelperCollection.NOT_SUPPORTED)) {
            Messages.printNotSupportedDataType(_variable, _container);
        }
    }

    /**
     * This constructor can be used to generate a Constant from an external artifact.
     *
     * @param xmlNode an XML node.
     */
    public Constant(Node xmlNode) {
        try {
            this.mName = xmlNode.getAttributes().getNamedItem("name").getNodeValue();
            this.mDimension = xmlNode.getAttributes().getNamedItem("dimension").getNodeValue();
            if (xmlNode.getNodeName().equals("Parameter")) {
                this.mParameter = true;
            } else {
                this.mParameter = false;
                this.mValue = new Expression(xmlNode.getAttributes().getNamedItem("value").getNodeValue());
            }
        } catch (Exception e) {
            System.err.println("LEMS Error: Constant artifact wrongly formatted.");
        }
    }

    /**
     * This constructor is used to generate concrete constants from stated implicit units. E.g. the variable
     * ms is transformed to the constant 1 ms, where ms can still be used as reference.
     * @param _name the name of the implicit unit
     * @param _type the type
     */
    public Constant(String _name, TypeSymbol _type){
        this.mName = _name;
        this.mDimension = HelperCollection.typeToDimensionConverter(_type);
        //now format also the dimension to a lems processable format, e.g. 1 / ms -> 1_per_ms
        this.mDimension = HelperCollection.dimensionFormatter(this.mDimension);

        Expression tExpression = new Expression();
        Operator tOperator = new Operator();
        tOperator.setNon(true);
        Expression rhs = new Variable(_type.prettyPrint(),_type);
        tExpression.replaceLhs(new NumericLiteral(1));
        tExpression.replaceOp(tOperator);
        tExpression.replaceRhs(rhs);
        this.mValue = tExpression;
        this.mParameter = false;
    }


    /**
     * This constructor is used to generate handmade constants if required.
     *
     * @param _name      the mName of the new constant
     * @param _dimension the mDimension
     * @param _value     the mValue of the concrete constant
     */
    public Constant(String _name, String _dimension, Expression _value, boolean _isParameter) {
        this.mName = _name;
        this.mDimension = HelperCollection.formatComplexUnit(_dimension);
        this.mValue = _value;
        this.mParameter = _isParameter;
    }

    @SuppressWarnings("unused")//used in the template
    public String getName() {
        return this.mName;
    }

    @SuppressWarnings("unused")//used in the template
    public String getDimension() {
        return this.mDimension;
    }

    @SuppressWarnings("unused")//used in the template
    public Expression getValue() {
        return this.mValue;
    }

    /*
    TODO:Unused since 10.05.2017 -> check and delete
    @SuppressWarnings("unused")//used in the template
    public String getmUnit() {
        if (mValue.getClass().equals(NumericLiteral.class) && ((NumericLiteral) mValue).hasType()) {
            return ((NumericLiteral) mValue).getType().get().getmUnit().get();
        }
        return "";
    }
    */

    /**
     * Returns the the mValue of the this constant. If
     * a concrete numerical mValue is utilized, the unit is appended to the mValue.
     *
     * @return Value as String.
     */
    @SuppressWarnings("unused")//used in the template
    public String getValueUnit() {
        return this.mValue.print(new LEMSSyntaxContainer());
    }

    public boolean isParameter() {
        return this.mParameter;
    }

    @Override
    public boolean equals(Object _o) {
        if (this == _o)
            return true;
        if (_o == null || getClass() != _o.getClass())
            return false;

        Constant constant = (Constant) _o;

        if (mParameter != constant.mParameter)
            return false;
        if (!mName.equals(constant.mName))
            return false;
        if (!mDimension.equals(constant.mDimension))
            return false;
        if (this.mValue == null && constant.mValue == null) {
            return true;
        }
        return mValue.equals(constant.mValue);

    }

    @Override
    public int hashCode() {
        int result = mName.hashCode();
        result = 31 * result + mDimension.hashCode();
        result = 31 * result + mValue.hashCode();
        result = 31 * result + (mParameter ? 1 : 0);
        return result;
    }

    /**
     * Processes a given function call and returns, if the corresponding function call is supported, a corresponding
     * expression. This function actually deals with the resolution function call, whenever a constant is defined
     * which calls the resolution function, e.g. ref_time ms = resolution().
     *
     * @param _variable  a variable containing a function call
     * @param _container a LEMSCollector for storage of error messages
     * @return a new expression
     */
    private Expression processFunctionCallInConstantDefinition(VariableSymbol _variable, LEMSCollector _container) {
        if (_variable.getDeclaringExpression().get().getFunctionCall().get().getCalleeName().equals("resolution")) {
            ASTUnitType tempType = new ASTUnitType();
            tempType.setUnit(_container.getConfig().getSimulationStepsUnit().getSymbol());
            return new NumericLiteral(_container.getConfig().getSimulationStepsLength());
        } else {
            Messages.printNotSupportedFunctionCallInExpression(_variable, _container);
            return new Variable(HelperCollection.NOT_SUPPORTED
                    + ":" + _variable.getDeclaringExpression().get().getFunctionCall().get().getName().toString());
        }
    }
}