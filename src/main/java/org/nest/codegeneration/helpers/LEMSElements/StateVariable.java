package org.nest.codegeneration.helpers.LEMSElements;

import java.util.Optional;

//import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import org.nest.codegeneration.helpers.Expressions.Expression;
import org.nest.codegeneration.helpers.Expressions.LEMSSyntaxContainer;
import org.nest.codegeneration.helpers.Expressions.NumericalLiteral;
import org.nest.codegeneration.helpers.Expressions.Variable;
import org.nest.codegeneration.helpers.Names;
import org.nest.symboltable.symbols.VariableSymbol;
import org.nest.units._ast.ASTUnitType;
import org.w3c.dom.Node;

/**
 * This class represents a state-variable, a characteristic of the modeled neuron which possibly changes over the time.
 *
 * @author perun
 */
public class StateVariable extends LEMSElement{
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
            //in the case it is a reference just set it as it is
            if (_variable.getDeclaringExpression().get().variableIsPresent()) {
                this.mDefaultValue = Optional.of(new Variable(_variable.getDeclaringExpression().get().getVariable().get()));
            }
            //now otherwise, if it is a single value, e.g. 10mV, generate a constant and set
            //reference to it.
            else if (_variable.getDeclaringExpression().get().numericLiteralIsPresent()) {
                if (this.mDimension.equals(HelperCollection.DIMENSION_NONE)) {
                    this.mDefaultValue = Optional.of(new NumericalLiteral(_variable.getDeclaringExpression().
                            get().getNumericLiteral().get(),Optional.empty()));
                } else {
                    Constant temp = new Constant(_variable, true, false, _container);
                    _container.addConstant(temp);
                    this.mDefaultValue = Optional.of(new Variable(temp.getName()));
                }
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
        } else {
            //no declaration is present, generate a 0 as init value, but with a unit if present
            if (this.mDimension.equals(HelperCollection.DIMENSION_NONE) ||
                    this.mDimension.equals(HelperCollection.NOT_SUPPORTED)) {
                this.mDefaultValue = Optional.of(new NumericalLiteral(0, null));
            } else {
                ASTUnitType tempType = new ASTUnitType();
                tempType.setUnit(_variable.getType().prettyPrint());
                tempType.setSerializedUnit(_variable.getType().getName());
                Constant defaultValue = new Constant(HelperCollection.PREFIX_INIT + Names.convertToCPPName(_variable.getName()),
                        this.mDimension, new NumericalLiteral(0, tempType), false);
                _container.addConstant(defaultValue);
                this.mDefaultValue = Optional.of(new Variable(defaultValue.getName()));
            }
        }

        if (!this.mDimension.equals(HelperCollection.DIMENSION_NONE)
                && !this.mDimension.equals(HelperCollection.NOT_SUPPORTED)) {
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
