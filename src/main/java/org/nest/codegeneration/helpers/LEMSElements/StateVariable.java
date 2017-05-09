package org.nest.codegeneration.helpers.LEMSElements;

import java.util.List;
import java.util.Optional;

//import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import org.nest.codegeneration.helpers.Expressions.Expression;
import org.nest.codegeneration.helpers.Expressions.LEMSSyntaxContainer;
import org.nest.codegeneration.helpers.Expressions.NumericalLiteral;
import org.nest.codegeneration.helpers.Expressions.Variable;
import org.nest.symboltable.symbols.VariableSymbol;
import org.nest.units._ast.ASTUnitType;
import org.w3c.dom.Node;

/**
 * This class represents a state-variable, a characteristic of the modeled neuron which possibly changes over the time.
 *
 * @author perun
 */
public class StateVariable {
    private String name;
    private String dimension;
    //all state variables have to be initialized with 0 if not other requested
    private Optional<Expression> defaultValue = Optional.empty();
    private Optional<String> unit = Optional.empty();

    public StateVariable(VariableSymbol variable, LEMSCollector container) {
        this.name = variable.getName();
        this.dimension = HelperCollection.typeToDimensionConverter(variable.getType());
        this.dimension = HelperCollection.dimensionFormatter(this.dimension);

        //check whether data type is supported or not and print a message
        if (HelperCollection.dataTypeNotSupported(variable.getType())) {
            HelperCollection.printNotSupportedDataType(variable, container);
        }

        //check if a standard value is set and an actual dimension is used
        if (variable.getDeclaringExpression().isPresent()) {
            //in the case it is a reference just set it as it is
            if (variable.getDeclaringExpression().get().variableIsPresent()) {
                this.defaultValue = Optional.of(new Variable(variable.getDeclaringExpression().get().getVariable().get()));
            }
            //now otherwise, if it is a single value, e.g. 10mV, generate a constant and set
            //reference to it.
            else if (variable.getDeclaringExpression().get().nESTMLNumericLiteralIsPresent()) {
                if (this.dimension.equals(HelperCollection.DIMENSION_NONE)) {
                    this.defaultValue = Optional.of(new NumericalLiteral(variable.getDeclaringExpression().
                            get().getNESTMLNumericLiteral().get()));
                } else {
                    Constant temp = new Constant(variable, true, false, container);
                    container.addConstant(temp);
                    this.defaultValue = Optional.of(new Variable(temp.getName()));
                }
                //if it is a ternary op, handle it correctly by means of a derived va
            } else if (variable.getDeclaringExpression().get().conditionIsPresent()) {
                DerivedElement temp = new DerivedElement(variable.getName(),
                        HelperCollection.typeToDimensionConverter(variable.getType()),
                        variable.getDeclaringExpression().get(),
                        container, true);
                container.addDerivedElement(temp);
                this.defaultValue = Optional.of(new Variable(temp.getName()));
                //a normal expression, e.g. 10mV + V_init
            } else {
                DerivedElement temp = new DerivedElement(variable, container, true, true);
                container.addDerivedElement(temp);
                this.defaultValue = Optional.of(new Variable(temp.getName()));
            }
        } else {
            //no declaration is present, generate a 0 as init value, but with a unit if present
            if (this.dimension.equals(HelperCollection.DIMENSION_NONE) ||
                    this.dimension.equals(HelperCollection.NOT_SUPPORTED)) {
                this.defaultValue = Optional.of(new NumericalLiteral(0, null));
            } else {
                ASTUnitType tempType = new ASTUnitType();
                tempType.setUnit(variable.getType().prettyPrint());
                tempType.setSerializedUnit(variable.getType().getName());
                Constant defaultValue = new Constant(HelperCollection.PREFIX_INIT + variable.getName(),
                        this.dimension, new NumericalLiteral(0, tempType), false);
                container.addConstant(defaultValue);
                this.defaultValue = Optional.of(new Variable(defaultValue.getName()));
            }
        }

        if (!this.dimension.equals(HelperCollection.DIMENSION_NONE)
                && !this.dimension.equals(HelperCollection.NOT_SUPPORTED)) {
            //state variables are often generated outside the main routine, thus a processing of units has to be triggered
            Unit tempUnit = new Unit(variable.getType());
            this.unit = Optional.of(tempUnit.getSymbol());
            container.addDimension(tempUnit.getDimension());
            container.addUnit(tempUnit);
        }

    }

    /**
     * This constructor can be used to generate new StateVariables from an external artifact stored as an xml file
     *
     * @param xmlNode the xml node of a state variable artifact
     */
    public StateVariable(Node xmlNode) {
        this.name = xmlNode.getAttributes().getNamedItem("name").getNodeValue();
        this.dimension = xmlNode.getAttributes().getNamedItem("dimension").getNodeValue();
    }


    /**
     * This method can be used to generate handmade state variables. Caution:
     * the integrity of such values is not assured.
     *
     * @param name         the name of the new variable
     * @param dimension    the dimension as a string
     * @param defaultValue the default value as a string
     * @param unit         the unit as a string
     */
    public StateVariable(String name, String dimension, Expression defaultValue,
                         Optional<String> unit) {
        this.name = name;
        this.dimension = HelperCollection.dimensionFormatter(dimension);
        this.defaultValue = Optional.of(defaultValue);
        this.unit = unit;

    }

    @SuppressWarnings("unused")//used in the template
    public String print() {
        if (defaultValue.isPresent()) {
            return defaultValue.get().print(new LEMSSyntaxContainer());
        } else {
            return "0";
        }
    }

    public Optional<Expression> getDefaultValue() {
        return this.defaultValue;
    }

    public void setDefaultValue(Expression value) {
        this.defaultValue = Optional.of(value);
    }

    @SuppressWarnings("unused")//used in the template
    public String getName() {
        return this.name;
    }

    @SuppressWarnings("unused")//used in the template
    public String getDimension() {
        return this.dimension;
    }

    public String getUnit() {
        return this.unit.get();
    }

    public boolean equals(Object o) {
        //it is sufficient to only check the name since otherwise the model would crash
        return this.getClass().equals(o.getClass()) && this.getName().equals(((StateVariable) o).getName());
    }

    public int hashCode() {
        return this.getName().hashCode() + this.dimension.hashCode() + this.defaultValue.hashCode() + this.unit.hashCode();
    }

}
