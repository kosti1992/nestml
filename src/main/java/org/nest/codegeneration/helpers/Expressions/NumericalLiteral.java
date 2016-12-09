package org.nest.codegeneration.helpers.Expressions;

import java.util.Optional;

import de.monticore.prettyprint.IndentPrinter;
import de.monticore.types.prettyprint.TypesPrettyPrinterConcreteVisitor;
import org.nest.commons._ast.ASTNESTMLNumericLiteral;
import org.nest.units._ast.ASTUnitType;

/**
 * This class stores a numerical literal (e.g. 10mV) together with its type.
 *
 * @author perun
 */
public class NumericalLiteral extends Expression {
	private double value;
	private Optional<ASTUnitType> type = Optional.empty();

	public NumericalLiteral(ASTNESTMLNumericLiteral literal) {
		this.value = Double.parseDouble(typesPrinter().prettyprint(literal.getNumericLiteral()));
		if (literal.typeIsPresent()) {
			this.type = Optional.of(literal.getType().get());
		}
	}

	public NumericalLiteral(double value, ASTUnitType type) {
		this.value = value;
		if (type != null) {
			this.type = Optional.of(type);
		}
	}

	public double getValue() {
		return value;
	}

	public boolean hasType() {
		return type.isPresent();
	}

	public Optional<ASTUnitType> getType() {
		return type;
	}

	private TypesPrettyPrinterConcreteVisitor typesPrinter() {
		final IndentPrinter printer = new IndentPrinter();
		return new TypesPrettyPrinterConcreteVisitor(printer);
	}

	public String print(SyntaxContainer container) {
		return container.print(this);
	}

	public void setValue(double value) {
		this.value = value;
	}

	/**
	 * This method prints the value of the numerical literal and also - if present- the unit/type.
	 *
	 * @return a string representation of the literal.
	 */
	public String printValueType() {
		if (this.type.isPresent()) {
			return this.value + type.get().getUnit().get();
		} else {
			return String.valueOf(this.value);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		NumericalLiteral that = (NumericalLiteral) o;

		if (Double.compare(that.value, value) != 0)
			return false;
		return type != null ? type.toString().equals(that.type.toString()) : that.type == null;

	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		long temp;
		temp = Double.doubleToLongBits(value);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		result = 31 * result + (type != null ? type.hashCode() : 0);
		return result;
	}
}
