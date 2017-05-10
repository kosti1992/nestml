package org.nest.codegeneration.helpers.LEMSElements;

import org.nest.symboltable.symbols.TypeSymbol;
import org.nest.units._ast.ASTUnitType;

/**
 * This class represents a concrete Unit used in the model.
 * In case that NESTML enables the handling of proper units rather than strings, this class has to be altered.
 *
 * @author perun
 */
public class Unit extends LEMSElement{
	/**
	 * the concrete mSymbol of the unit, e.g "mV"
	 */
	private String mSymbol;
	/**
	 * the mDimension of the unit, caution, due to the implicit derivation of
	 * the name, the mDimension is called "DimensionOf${..}" in order to enable
	 * the processing of arbitrary dimensions
	 */
	private Dimension mDimension;
	/**
	 * the mPower of the unit, i.e decimal representation of the prefix
	 */
	private int mPower;

	public Unit(TypeSymbol _input) {
		mSymbol = HelperCollection.formatComplexUnit(_input.prettyPrint());
		mDimension = new Dimension(_input);
		mPower = HelperCollection.convertTypeDeclToArray(_input.toString())[7];
	}

	public Unit(ASTUnitType _input){
		if(_input.getUnit().isPresent()){
			this.mSymbol = HelperCollection.formatComplexUnit(_input.getUnit().get());
		}else{
			this.mSymbol = HelperCollection.formatComplexUnit(HelperCollection.getExpressionFromUnitType(_input).print());
		}
		mDimension = new Dimension(_input);
		mPower = HelperCollection.convertTypeDeclToArray(_input.getSerializedUnit())[7];
	}

	/**
	 * This constructor can be used to generate handmade units.
	 *
	 * @param _symbol a string containing a unit symbol
	 */
	public Unit(String _symbol,int _power, Dimension _dimension) {
		this.mSymbol =  HelperCollection.formatComplexUnit(_symbol);
		this.mDimension = _dimension;
		this.mPower = _power;
	}

	/**
	 * Returns the mPower of this unit. Used by the template
	 *
	 * @return the mPower as init
	 */
	public int getPower() {
		return this.mPower;
	}

	/**
	 * Returns the mSymbol of this unit.
	 *
	 * @return mSymbol as String.
	 */
	public String getSymbol() {
		return this.mSymbol;
	}

	/**
	 * Returns the mDimension of this unit.
	 *
	 * @return mDimension as Dimension.
	 */
	public Dimension getDimension() {
		return this.mDimension;
	}

	/**
	 * Returns the name of the mDimension of this unit.
	 *
	 * @return name as String
	 */
	@SuppressWarnings("unused")//used in the template
	public String getDimensionName() {
		return this.mDimension.getName();
	}

	/**
	 * Returns the hash of the this unit.
	 *
	 * @return Hash as int.
	 */
	public int hashCode() {
		return this.mSymbol.hashCode();//each unit has a unique mSymbol, thus a hash of the unit-mSymbol is sufficient
	}

	/**
	 * Compares this unit to a given object. Required in order to
	 * identify duplicates in unitsSet.
	 *
	 * @param _other Object which will be compared to this mDimension.
	 * @return true, if objects equals
	 */
	public boolean equals(Object _other) {
		return (this.getClass() == _other.getClass()) &&//same class
				this.getSymbol().equals(((Unit) _other).getSymbol()) &&//same mSymbol
				this.getPower() == ((Unit) _other).getPower();//same mPower
	}
}