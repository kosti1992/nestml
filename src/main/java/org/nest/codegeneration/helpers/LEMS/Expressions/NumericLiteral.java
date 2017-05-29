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
 * This class stores a numerical literal (e.g. 10mV) together with its type.
 *
 * @author perun
 */
public class NumericLiteral extends Expression {
    private double mValue;
    private Optional<EitherTuple<TypeSymbol,ASTUnitType>> mType = Optional.empty();

    public NumericLiteral(ASTNumericLiteral _literal, Optional<EitherTuple<TypeSymbol,ASTUnitType>> _type) {
        this.mValue = Double.parseDouble(typesPrinter().prettyprint(_literal));
        if (_type.isPresent()) {
            this.mType = _type;
        }
    }

    public NumericLiteral(double _value, Optional<EitherTuple<TypeSymbol,ASTUnitType>> _type) {
        this.mValue = _value;
        if (_type.isPresent()) {
            this.mType = _type;
        }
    }

    public NumericLiteral(double value) {
        this.mValue = value;
        this.mType = Optional.empty();
    }


    public double getValue() {
        return mValue;
    }

    public boolean hasType() {
        return mType.isPresent();
    }

    public Optional<EitherTuple<TypeSymbol,ASTUnitType>> getType() {
        return mType;
    }

    private TypesPrettyPrinterConcreteVisitor typesPrinter() {
        final IndentPrinter printer = new IndentPrinter();
        return new TypesPrettyPrinterConcreteVisitor(printer);
    }

    public void setType(Optional<EitherTuple<TypeSymbol,ASTUnitType>> _type) {
        if (_type.isPresent()) {
            this.mType = _type;
        }
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
    public String printValueType() {
        if (this.mType.isPresent() && this.mType.get().isRight()) {
            if (this.mValue - (int) this.mValue == 0) {
                return String.valueOf((int) this.mValue) + "_" +
                        HelperCollection.formatComplexUnit(HelperCollection.getExpressionFromUnitType(this.mType.get().getRight()).print());
            } else {
                return String.valueOf(this.mValue) + "_" +
                    //TODO
                HelperCollection.formatComplexUnit(HelperCollection.getExpressionFromUnitType(this.mType.get().getRight()).print());
            }
        } else {
            if (this.mValue - (int) this.mValue == 0) {
                return String.valueOf((int) this.mValue);
            } else {
                return String.valueOf(this.mValue);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        NumericLiteral that = (NumericLiteral) o;

        if (Double.compare(that.mValue, mValue) != 0) return false;
        return mType != null ? mType.equals(that.mType) : that.mType == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(mValue);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (mType != null ? mType.hashCode() : 0);
        return result;
    }

    /**
     * This is a deepClone method which generates a clone of this object whenever required, e.g. when it has to be
     * mirrored to other parts of the expression tree.
     *
     * @return a deep clone of this
     */
    public NumericLiteral deepClone() {
        if (this.mType.isPresent()) {
            return new NumericLiteral(this.mValue, this.mType);
        } else {
            return new NumericLiteral(this.mValue);
        }
    }

}
