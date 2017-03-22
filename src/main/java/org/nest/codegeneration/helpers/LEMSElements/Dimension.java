package org.nest.codegeneration.helpers.LEMSElements;

import org.nest.symboltable.symbols.TypeSymbol;

/**
 * This class represents a dimension required for constants and other constructs in the model.
 * In case that NESTML enables the handling of proper units rather than strings, this class has to be altered.
 *
 * @author perun
 */
public class Dimension {
	private String name;//The name of the dimension

	@SuppressWarnings("unused")
	private int LENGTH, //The exponents of the seven base units
			MASS,
			TIME,
			ELECTRIC_CURRENT,
			THERMODYNAMIC_TEMPERATURE,
			AMOUNT_OF_SUBSTANCE,
			LUMINOUS_INTENSITY;

	protected Dimension(TypeSymbol input) {
		this.name = this.dimensionFormatter(LEMSCollector.helper.typeToDimensionConverter(input));
		int[] definition = LEMSCollector.helper.convertTypeDeclToArray(input.toString());
		this.THERMODYNAMIC_TEMPERATURE = definition[0];
		this.TIME = definition[1];
		this.LENGTH = definition[2];
		this.MASS = definition[3];
		this.LUMINOUS_INTENSITY = definition[4];
		this.AMOUNT_OF_SUBSTANCE = definition[5];
		this.ELECTRIC_CURRENT = definition[6];
		;
	}

	public Dimension(String name, int LENGTH, int MASS, int TIME,
	                 int ELECTRIC_CURRENT, int THERMODYNAMIC_TEMPERATURE, int AMOUNT_OF_SUBSTANCE, int LUMINOUS_INTENSITY) {
		this.name = name;
		this.LENGTH = LENGTH;
		this.MASS = MASS;
		this.TIME = TIME;
		this.ELECTRIC_CURRENT = ELECTRIC_CURRENT;
		this.THERMODYNAMIC_TEMPERATURE = THERMODYNAMIC_TEMPERATURE;
		this.AMOUNT_OF_SUBSTANCE = AMOUNT_OF_SUBSTANCE;
		this.LUMINOUS_INTENSITY = LUMINOUS_INTENSITY;
	}

	/**
	 * Required by the Generator-engine!
	 *
	 * @return parameter of the base-unit "amount of substance" as int.
	 */
	@SuppressWarnings("unused")//used in the template
	public int getAMOUNT_OF_SUBSTANCE() {
		return AMOUNT_OF_SUBSTANCE;
	}

	/**
	 * Required by the Generator-engine!
	 *
	 * @return parameter of the base-unit "electric current" as int.
	 */
	@SuppressWarnings("unused")//used in the template
	public int getELECTRIC_CURRENT() {
		return ELECTRIC_CURRENT;
	}

	/**
	 * Required by the Generator-engine!
	 *
	 * @return parameter of the base-unit "length" as int.
	 */
	@SuppressWarnings("unused")//used in the template
	public int getLENGTH() {
		return LENGTH;
	}

	/**
	 * Required by the Generator-engine!
	 *
	 * @return parameter of the base-unit "luminous intensity" as int.
	 */
	@SuppressWarnings("unused")//used in the template
	public int getLUMINOUS_INTENSITY() {
		return LUMINOUS_INTENSITY;
	}

	/**
	 * Required by the Generator-engine!
	 *
	 * @return parameter of the base-unit "mass" as int.
	 */
	@SuppressWarnings("unused")//used in the template
	public int getMASS() {
		return MASS;
	}

	/**
	 * Required by the Generator-engine!
	 *
	 * @return parameter of the base-unit "thermodynamic temperature" as int.
	 */
	@SuppressWarnings("unused")//used in the template
	public int getTHERMODYNAMIC_TEMPERATURE() {
		return THERMODYNAMIC_TEMPERATURE;
	}

	/**
	 * Required by the Generator-engine!
	 *
	 * @return parameter of the base-unit "time" as int.
	 */
	@SuppressWarnings("unused")//used in the template
	public int getTIME() {
		return TIME;
	}

	public String getName() {
		return this.name;
	}

	/**
	 * Returns the hash of the current dimension.
	 *
	 * @return Hash as int.
	 */
	public int hashCode() {
		return name.hashCode();//the name of a dimension is unique, thus, it is sufficient solely to hash the name
	}

	/**
	 * Compares this dimension to a given object. Required in order to
	 * identify duplicates in dimensionsSet.
	 *
	 * @param other Object which will be compared to this dimension.
	 * @return true, if objects equals
	 */
	public boolean equals(Object other) {
		return (this.getClass() == other.getClass()) &&
				this.name.equals(((Dimension) other).getName()) &&
				this.getELECTRIC_CURRENT() == ((Dimension) other).getELECTRIC_CURRENT() &&
				this.getLENGTH() == ((Dimension) other).getLENGTH() &&
				this.getAMOUNT_OF_SUBSTANCE() == ((Dimension) other).getAMOUNT_OF_SUBSTANCE() &&
				this.getTHERMODYNAMIC_TEMPERATURE() == ((Dimension) other).getTHERMODYNAMIC_TEMPERATURE() &&
				this.getTIME() == ((Dimension) other).getTIME() &&
				this.getLUMINOUS_INTENSITY() == ((Dimension) other).getLUMINOUS_INTENSITY() &&
				this.getMASS() == ((Dimension) other).getMASS();
	}

	/**
	 * Inspects a given dimension and transforms it to a LEMS near representation, e.g.  A / s => DimensionOf_A_per_s
	 *
	 * @param unformattedDimension the string representation of a yet unformatted dimension
	 * @return a formatted string
	 */
	private String dimensionFormatter(String unformattedDimension) {
		return unformattedDimension.replaceAll("\\*", "_times_").replaceAll("/", "_per_").replaceAll(" ", "");
	}

}