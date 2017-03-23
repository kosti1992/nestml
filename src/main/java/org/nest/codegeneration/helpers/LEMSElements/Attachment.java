package org.nest.codegeneration.helpers.LEMSElements;

import org.w3c.dom.Node;

/**
 * This class represents an attachment to the model, e.g. an external current generator.
 * @author  perun
 */
public class Attachment {
	private String bindName;
	private String bindType;

	public Attachment(String bindName, String bindType) {
		this.bindName = bindName;
		this.bindType = bindType;
	}

	/**
	 * This constructor can be used to generate a new attachment from a given XML node.
	 * @param xmlNode an attachment xml node
	 */
	public Attachment(Node xmlNode){
		try{
			this.bindName = xmlNode.getAttributes().getNamedItem("name").getNodeValue();
			this.bindType = xmlNode.getAttributes().getNamedItem("type").getNodeValue();
		}
		catch (Exception e){
			System.err.println("Attachment artifact wrongly formatted.");
		}
	}

	@SuppressWarnings("unused")//used in the template
	public String getBindName() {
		return bindName;
	}

	@SuppressWarnings("unused")//used in the template
	public String getBindType() {
		return bindType;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Attachment that = (Attachment) o;

		if (!bindName.equals(that.bindName)) return false;
		return bindType.equals(that.bindType);

	}

	@Override
	public int hashCode() {
		int result = bindName.hashCode();
		result = 31 * result + bindType.hashCode();
		return result;
	}
}
