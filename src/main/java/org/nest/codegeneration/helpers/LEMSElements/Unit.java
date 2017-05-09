package org.nest.codegeneration.helpers.LEMSElements;

import org.nest.symboltable.symbols.TypeSymbol;
import org.nest.units._ast.ASTUnitType;

/**
 * This class represents a concrete Unit used in the model.
 * In case that NESTML enables the handling of proper units rather than strings, this class has to be altered.
 *
 * @author perun
 */
public class Unit {
	/**
	 * the concrete symbol of the unit, e.g "mV"
	 */
	private String symbol;
	/**
	 * the dimension of the unit, caution, due to the implicit derivation of
	 * the name, the dimension is called "DimensionOf${..}" in order to enable
	 * the processing of arbitrary dimensions
	 */
	private Dimension dimension;
	/**
	 * the power of the unit, i.e decimal representation of the prefix
	 */
	private int power;

	public Unit(TypeSymbol input) {
		symbol = HelperCollection.formatComplexUnit(input.prettyPrint());
		dimension = new Dimension(input);
		power = HelperCollection.convertTypeDeclToArray(input.toString())[7];
	}

	public Unit(ASTUnitType input){
		if(input.getUnit().isPresent()){
			this.symbol = HelperCollection.formatComplexUnit(input.getUnit().get());
		}else{
			this.symbol = HelperCollection.formatComplexUnit(HelperCollection.getExpressionFromUnitType(input).print());
		}
		dimension = new Dimension(input);
		power = HelperCollection.convertTypeDeclToArray(input.getSerializedUnit())[7];
	}

	/**
	 * This constructor can be used to generate handmade units.
	 *
	 * @param input a string containing a unit symbol
	 */
	public Unit(String input,int power, Dimension dimension) {
		this.symbol =  HelperCollection.formatComplexUnit(input);
		this.dimension = dimension;
		this.power = power;
	}

	/**
	 * Returns the power of this unit. Used by the template
	 *
	 * @return the power as init
	 */
	public int getPower() {
		return this.power;
	}

	/**
	 * Returns the symbol of this unit.
	 *
	 * @return symbol as String.
	 */
	public String getSymbol() {
		return this.symbol;
	}

	/**
	 * Returns the dimension of this unit.
	 *
	 * @return dimension as Dimension.
	 */
	public Dimension getDimension() {
		return this.dimension;
	}

	/**
	 * Returns the name of the dimension of this unit.
	 *
	 * @return name as String
	 */
	@SuppressWarnings("unused")//used in the template
	public String getDimensionName() {
		return this.dimension.getName();
	}

	/**
	 * Returns the hash of the this unit.
	 *
	 * @return Hash as int.
	 */
	public int hashCode() {
		return this.symbol.hashCode();//each unit has a unique symbol, thus a hash of the unit-symbol is sufficient
	}

	/**
	 * Compares this unit to a given object. Required in order to
	 * identify duplicates in unitsSet.
	 *
	 * @param other Object which will be compared to this dimension.
	 * @return true, if objects equals
	 */
	public boolean equals(Object other) {
		return (this.getClass() == other.getClass()) &&//same class
				this.getSymbol().equals(((Unit) other).getSymbol()) &&//same symbol
				this.getPower() == ((Unit) other).getPower();//same power
	}


	/**
	 * Inspects a given unit and transforms it to a LEMS near representation, e.g.  A / s => A_per_s
	 *
	 * @param unformattedUnit the string representation of a yet unformatted unit
	 * @return a formatted string
	 */
	private String unitFormatter(String unformattedUnit) {
		return unformattedUnit.replaceAll("\\*", "_times_").replaceAll("/", "_per_").replaceAll(" ", "");
	}

}