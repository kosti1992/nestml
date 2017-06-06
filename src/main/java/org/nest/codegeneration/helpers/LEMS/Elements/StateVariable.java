package org.nest.codegeneration.helpers.LEMS.Elements;

import java.util.Optional;

//import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import org.nest.codegeneration.helpers.LEMS.Expressions.*;
import org.nest.codegeneration.helpers.LEMS.helpers.EitherTuple;
import org.nest.codegeneration.helpers.Names;
import org.nest.spl.symboltable.typechecking.Either;
import org.nest.symboltable.symbols.VariableSymbol;
import org.nest.units._ast.ASTUnitType;
import org.w3c.dom.Node;

import javax.swing.text.html.Option;

/**
 * This class represents a state-variable, a characteristic of the modeled neuron which possibly changes over the time.
 *
 * @author perun
 */
public class StateVariable extends LEMSElement {
    private String mName;
    private String mDimension;
    //all state variables have to be initialized with 0 if not other requested
    private Optional<Expression> mDefaultValue = Optional.empty();
    private Optional<String> mUnit = Optional.empty();

    public StateVariable(VariableSymbol _variable, LEMSCollector _container) {
        this.mName = Names.convertToCPPName(_variable.getName());
        this.mDimension = HelperCollection.typeToDimensionConverter(_variable.getType());
        this.mDimension = HelperCollection.dimensionFormatter(this.mDimension);

        //check whether data type is supported or not and print a message
        if (HelperCollection.dataTypeNotSupported(_variable.getType())) {
            Messages.printNotSupportedDataType(_variable, _container);
        }

        //check if a standard value is set and an actual dimension is used
        if (_variable.getDeclaringExpression().isPresent()) {
            //in the case it is a reference just set it as it is, here check if it is really just a reference
            if (_variable.getDeclaringExpression().get().variableIsPresent() && !_variable.getDeclaringExpression().get().numericLiteralIsPresent()) {
                this.mDefaultValue = Optional.of(new Variable(HelperCollection.resolveVariableSymbol(
                        _variable.getDeclaringExpression().get()).get()));
            }
            //now otherwise, if it is a single value multiplied with the unit, generate a constant representing the unit,
            //a derived parameter representing the init value of this variable and finally set the reference to it
            else if ((_variable.getDeclaringExpression().get().numericLiteralIsPresent() &&
                    _variable.getDeclaringExpression().get().variableIsPresent())||HelperCollection.isLiteralUnit(_variable.getDeclaringExpression().get())) {
                Expression tDefaultValue = new Expression(_variable.getDeclaringExpression().get());
                // the replacement routine takes all required actions, e.g. define implicit units
                tDefaultValue = HelperCollection.replacementRoutine(_container,tDefaultValue);
                this.mDefaultValue = Optional.of(tDefaultValue);
            //if it is a ternary op, handle it correctly by means of a derived variable
        } else if (_variable.getDeclaringExpression().get().conditionIsPresent()) {
            DerivedElement temp = new DerivedElement(_variable.getName(),
                    HelperCollection.typeToDimensionConverter(_variable.getType()),
                    _variable.getDeclaringExpression().get(),
                    _container, true);
            _container.addDerivedElement(temp);
            this.mDefaultValue = Optional.of(new Variable(temp.getName()));
            //a normal expression, e.g. 10mV + V_init
        } else {
            DerivedElement temp = new DerivedElement(_variable, _container, true, true);
            _container.addDerivedElement(temp);
            this.mDefaultValue = Optional.of(new Variable(temp.getName()));
        }
    } else

    {
        //no declaration is present, generate a 0 as init value, but with a unit if present
        if (this.mDimension.equals(HelperCollection.DIMENSION_NONE) ||
                this.mDimension.equals(HelperCollection.NOT_SUPPORTED)) {
            this.mDefaultValue = Optional.of(new NumericLiteral(0));
        } else {
            Variable tVariable = new Variable(_variable);
            Expression tExpr = new Expression();
            tExpr.replaceLhs(new NumericLiteral(0));
            Operator tOpr = new Operator();
            tOpr.setTimesOp(true);
            tExpr.replaceOp(tOpr);
            tExpr.replaceRhs(tVariable);
            tExpr = HelperCollection.replacementRoutine(_container,tExpr);
            this.mDefaultValue = Optional.of(tExpr);
        }
    }

        if(!this.mDimension.equals(HelperCollection.DIMENSION_NONE)
            &&!this.mDimension.equals(HelperCollection.NOT_SUPPORTED))

    {
        //state variables are often generated outside the main routine, thus a processing of units has to be triggered
        Unit tempUnit = new Unit(_variable.getType());
        this.mUnit = Optional.of(tempUnit.getSymbol());
        _container.addDimension(tempUnit.getDimension());
        _container.addUnit(tempUnit);
    }

}

    /**
     * This constructor can be used to generate new StateVariables from an external artifact stored as an xml file
     *
     * @param _xmlNode the xml node of a state variable artifact
     */
    public StateVariable(Node _xmlNode) {
        this.mName = _xmlNode.getAttributes().getNamedItem("name").getNodeValue();
        this.mDimension = _xmlNode.getAttributes().getNamedItem("dimension").getNodeValue();
    }


    /**
     * This method can be used to generate handmade state variables. Caution:
     * the integrity of such values is not assured.
     *
     * @param _name         the mName of the new variable
     * @param _dimension    the mDimension as a string
     * @param _defaultValue the default value as a string
     * @param _unit         the mUnit as a string
     */
    public StateVariable(String _name, String _dimension, Expression _defaultValue,
                         Optional<String> _unit) {
        this.mName = _name;
        this.mDimension = HelperCollection.dimensionFormatter(_dimension);
        this.mDefaultValue = Optional.of(_defaultValue);
        this.mUnit = _unit;

    }

    @SuppressWarnings("unused")//used in the template
    public String print() {
        if (mDefaultValue.isPresent()) {
            return mDefaultValue.get().print(new LEMSSyntaxContainer());
        } else {
            return "0";
        }
    }

    public Optional<Expression> getDefaultValue() {
        return this.mDefaultValue;
    }

    public void setDefaultValue(Expression _value) {
        this.mDefaultValue = Optional.of(_value);
    }

    @SuppressWarnings("unused")//used in the template
    public String getName() {
        return this.mName;
    }

    @SuppressWarnings("unused")//used in the template
    public String getDimension() {
        return this.mDimension;
    }

    public String getUnit() {
        return this.mUnit.get();
    }

    public boolean equals(Object _o) {
        //it is sufficient to only check the mName since otherwise the model would crash
        return this.getClass().equals(_o.getClass()) && this.getName().equals(((StateVariable) _o).getName());
    }

    public int hashCode() {
        return this.getName().hashCode() + this.mDimension.hashCode() + this.mDefaultValue.hashCode() + this.mUnit.hashCode();
    }

}
