package org.nest.codegeneration.helpers.LEMSElements;

import org.nest.symboltable.symbols.TypeSymbol;
import org.nest.units._ast.ASTUnitType;

/**
 * This class represents a dimension required for constants and other constructs in the model.
 * In case that NESTML enables the handling of proper units rather than strings, this class has to be altered.
 *
 * @author perun
 */
public class Dimension {
	private String mName;//The mName of the dimension

	@SuppressWarnings("unused")
	private int mLENGTH, //The exponents of the seven base units
			mMASS,
			mTIME,
			mELECTRIC_CURRENT,
			mTHERMODYNAMIC_TEMPERATURE,
			mAMOUNT_OF_SUBSTANCE,
			mLUMINOUS_INTENSITY;

	protected Dimension(TypeSymbol _input) {
		this.mName = HelperCollection.formatComplexUnit(HelperCollection.typeToDimensionConverter(_input));
		int[] definition = HelperCollection.convertTypeDeclToArray(_input.toString());
		this.mTHERMODYNAMIC_TEMPERATURE = definition[0];
		this.mTIME = definition[1];
		this.mLENGTH = definition[2];
		this.mMASS = definition[3];
		this.mLUMINOUS_INTENSITY = definition[4];
		this.mAMOUNT_OF_SUBSTANCE = definition[5];
		this.mELECTRIC_CURRENT = definition[6];
	}

	protected Dimension(ASTUnitType _input){
		this.mName = HelperCollection.PREFIX_DIMENSION +
				HelperCollection.formatComplexUnit(HelperCollection.getExpressionFromUnitType(_input).print());
		int[] definition = HelperCollection.convertTypeDeclToArray(_input.getSerializedUnit());
		this.mTHERMODYNAMIC_TEMPERATURE = definition[0];
		this.mTIME = definition[1];
		this.mLENGTH = definition[2];
		this.mMASS = definition[3];
		this.mLUMINOUS_INTENSITY = definition[4];
		this.mAMOUNT_OF_SUBSTANCE = definition[5];
		this.mELECTRIC_CURRENT = definition[6];
	}



	public Dimension(String _name, int _LENGTH, int _MASS, int _TIME,
	                 int _ELECTRIC_CURRENT, int _THERMODYNAMIC_TEMPERATURE, int _AMOUNT_OF_SUBSTANCE, int _LUMINOUS_INTENSITY) {
		this.mName = HelperCollection.formatComplexUnit(_name);
		this.mLENGTH = _LENGTH;
		this.mMASS = _MASS;
		this.mTIME = _TIME;
		this.mELECTRIC_CURRENT = _ELECTRIC_CURRENT;
		this.mTHERMODYNAMIC_TEMPERATURE = _THERMODYNAMIC_TEMPERATURE;
		this.mAMOUNT_OF_SUBSTANCE = _AMOUNT_OF_SUBSTANCE;
		this.mLUMINOUS_INTENSITY = _LUMINOUS_INTENSITY;
	}

	/**
	 * Required by the Generator-engine!
	 *
	 * @return parameter of the base-unit "amount of substance" as int.
	 */
	@SuppressWarnings("unused")//used in the template
	public int getAMOUNT_OF_SUBSTANCE() {
		return mAMOUNT_OF_SUBSTANCE;
	}

	/**
	 * Required by the Generator-engine!
	 *
	 * @return parameter of the base-unit "electric current" as int.
	 */
	@SuppressWarnings("unused")//used in the template
	public int getELECTRIC_CURRENT() {
		return mELECTRIC_CURRENT;
	}

	/**
	 * Required by the Generator-engine!
	 *
	 * @return parameter of the base-unit "length" as int.
	 */
	@SuppressWarnings("unused")//used in the template
	public int getLENGTH() {
		return mLENGTH;
	}

	/**
	 * Required by the Generator-engine!
	 *
	 * @return parameter of the base-unit "luminous intensity" as int.
	 */
	@SuppressWarnings("unused")//used in the template
	public int getLUMINOUS_INTENSITY() {
		return mLUMINOUS_INTENSITY;
	}

	/**
	 * Required by the Generator-engine!
	 *
	 * @return parameter of the base-unit "mass" as int.
	 */
	@SuppressWarnings("unused")//used in the template
	public int getMASS() {
		return mMASS;
	}

	/**
	 * Required by the Generator-engine!
	 *
	 * @return parameter of the base-unit "thermodynamic temperature" as int.
	 */
	@SuppressWarnings("unused")//used in the template
	public int getTHERMODYNAMIC_TEMPERATURE() {
		return mTHERMODYNAMIC_TEMPERATURE;
	}

	/**
	 * Required by the Generator-engine!
	 *
	 * @return parameter of the base-unit "time" as int.
	 */
	@SuppressWarnings("unused")//used in the template
	public int getTIME() {
		return mTIME;
	}

	public String getName() {
		return this.mName;
	}

	/**
	 * Returns the hash of the current dimension.
	 *
	 * @return Hash as int.
	 */
	public int hashCode() {
		return mName.hashCode();//the mName of a dimension is unique, thus, it is sufficient solely to hash the mName
	}

	/**
	 * Compares this dimension to a given object. Required in order to
	 * identify duplicates in dimensionsSet.
	 *
	 * @param _other Object which will be compared to this dimension.
	 * @return true, if objects equals
	 */
	public boolean equals(Object _other) {
		return (this.getClass() == _other.getClass()) &&
				this.mName.equals(((Dimension) _other).getName()) &&
				this.getELECTRIC_CURRENT() == ((Dimension) _other).getELECTRIC_CURRENT() &&
				this.getLENGTH() == ((Dimension) _other).getLENGTH() &&
				this.getAMOUNT_OF_SUBSTANCE() == ((Dimension) _other).getAMOUNT_OF_SUBSTANCE() &&
				this.getTHERMODYNAMIC_TEMPERATURE() == ((Dimension) _other).getTHERMODYNAMIC_TEMPERATURE() &&
				this.getTIME() == ((Dimension) _other).getTIME() &&
				this.getLUMINOUS_INTENSITY() == ((Dimension) _other).getLUMINOUS_INTENSITY() &&
				this.getMASS() == ((Dimension) _other).getMASS();
	}



}