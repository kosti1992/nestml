package org.nest.codegeneration.helpers.LEMSElements;

import org.w3c.dom.Node;

/**
 * This class represents an attachment to the model, e.g. an external current generator.
 * @author  perun
 */
public class Attachment extends LEMSElement {
	private String mBindName;
	private String mBindType;

	public Attachment(String _bindName, String _bindType) {
		this.mBindName = _bindName;
		this.mBindType = _bindType;
	}

	/**
	 * This constructor can be used to generate a new attachment from a given XML node.
	 * @param _xmlNode an attachment xml node
	 */
	public Attachment(Node _xmlNode){
		try{
			this.mBindName = _xmlNode.getAttributes().getNamedItem("name").getNodeValue();
			this.mBindType = _xmlNode.getAttributes().getNamedItem("type").getNodeValue();
		}
		catch (Exception e){
			System.err.println("LEMS ERROR: Attachment artifact wrongly formatted.");
		}
	}

	@SuppressWarnings("unused")//used in the template
	public String getBindName() {
		return mBindName;
	}

	@SuppressWarnings("unused")//used in the template
	public String getBindType() {
		return mBindType;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Attachment that = (Attachment) o;

		if (!mBindName.equals(that.mBindName)) return false;
		return mBindType.equals(that.mBindType);

	}

	@Override
	public int hashCode() {
		int result = mBindName.hashCode();
		result = 31 * result + mBindType.hashCode();
		return result;
	}
}
