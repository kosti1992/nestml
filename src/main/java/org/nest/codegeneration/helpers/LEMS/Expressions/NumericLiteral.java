package org.nest.codegeneration.helpers.LEMS.Expressions;

import java.util.Optional;

import de.monticore.literals.literals._ast.ASTNumericLiteral;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.types.prettyprint.TypesPrettyPrinterConcreteVisitor;
import org.nest.codegeneration.helpers.LEMS.Elements.HelperCollection;
import org.nest.codegeneration.helpers.LEMS.helpers.EitherTuple;
import org.nest.spl.symboltable.typechecking.Either;
import org.nest.symboltable.symbols.TypeSymbol;
import org.nest.units._ast.ASTUnitType;

/**
 * This class stores a numeric literal.
 *
 * @author perun
 */
public class NumericLiteral extends Expression {
    private double mValue;

    public NumericLiteral(ASTNumericLiteral _literal) {
        this.mValue = Double.parseDouble(typesPrinter().prettyprint(_literal));
    }

    public NumericLiteral(double _value) {
        this.mValue = _value;
    }

    public double getValue() {
        return mValue;
    }

    /**
     * This method is required in order to be able to print the value of a handed over literal
     *
     * @return a TypesPrettyPrinterConcreteVisitor object which can be used to visit an ast node
     */
    private TypesPrettyPrinterConcreteVisitor typesPrinter() {
        final IndentPrinter printer = new IndentPrinter();
        return new TypesPrettyPrinterConcreteVisitor(printer);
    }


    public String print(SyntaxContainer container) {
        return container.print(this);
    }

    public void setValue(double _value) {
        this.mValue = _value;
    }

    /**
     * This method prints the mValue of the numerical literal and also - if present- the unit/type.
     *
     * @return a string representation of the literal.
     */
    public String printValue() {
        if (this.mValue - (int) this.mValue == 0) {
            return String.valueOf((int) this.mValue);
        } else {
            return String.valueOf(this.mValue);
        }

    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        NumericLiteral that = (NumericLiteral) o;

        return Double.compare(that.mValue, mValue) == 0;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(mValue);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    /**
     * This is a deepClone method which generates a clone of this object whenever required, e.g. when it has to be
     * mirrored to other parts of the expression tree.
     *
     * @return a deep clone of this
     */
    public NumericLiteral deepClone() {
        return new NumericLiteral(this.mValue);
    }
}
