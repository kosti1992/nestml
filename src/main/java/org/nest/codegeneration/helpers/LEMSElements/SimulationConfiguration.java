package org.nest.codegeneration.helpers.LEMSElements;

import org.nest.codegeneration.helpers.Expressions.Expression;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * This class stores all configuration of the simulation as handed over it the simulation script.
 *
 * @author perun
 */
public class SimulationConfiguration {
	//indicates whether units and dimensions have to be generated externally
	private boolean unitsExternal = false;
	//stores the length of single simulation step in ms
	private double simulation_steps_length;
	private String simulation_steps_unit;
	//the path to a configuration file
	private Path configPath = null;

	public SimulationConfiguration() {
	}

	public SimulationConfiguration(Path configPath) {
		this.configPath = configPath;
	}

	/**
	 * Reads an external artifact in XML format and extracts all required information.
	 *
	 * @throws IOException thrown if non file is given.
	 */
	public void adaptSettings(LEMSCollector container) {
		if (configPath == null) {
			return;
		}
		try {
			File inputFile = new File(configPath.toAbsolutePath().toString());
			DocumentBuilderFactory dbFactory
					= DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(inputFile);
			doc.getDocumentElement().normalize();
			NodeList outerList = doc.getElementsByTagName("Target");
			NodeList innerList;
			Node outerNode;
			Node innerNode;
			for (int i = 0; i < outerList.getLength(); i++) {
				outerNode = outerList.item(i);
				//first check if the object we are looking for is in the list
				if (outerNode.getAttributes().getNamedItem("specification") != null &&
						outerNode.getAttributes().getNamedItem("specification").getNodeValue().equals("LEMS") &&
						outerNode.getAttributes().getNamedItem("name") != null) {
					List target_models =
							Arrays.asList(outerNode.getAttributes().getNamedItem("name").getNodeValue().split(";"));
					if (target_models.contains(container.getNeuronName())) {
						//now check if the name list contains the name of the current neuron
						//if so, start to extract
						innerList = outerNode.getChildNodes();
						for (int j = 0; j < innerList.getLength(); j++) {
							innerNode = innerList.item(j);
							if (innerNode.getNodeName().equals("Attachments")) {
								container.addAttachment(new Attachment(innerNode));
							}
							if (innerNode.getNodeName().equals("Parameter") || innerNode.getNodeName().equals("Constant")) {
								container.addConstant(new Constant(innerNode));
							}
							if (innerNode.getNodeName().equals("DerivedParameter") || innerNode.getNodeName().equals("DerivedVariable")) {
								container.addDerivedElement(new DerivedElement(innerNode));
							}
							if (innerNode.getNodeName().equals("EventPort")) {
								container.addEventPort(new EventPort(innerNode));
							}
							if (innerNode.getNodeName().equals("StateVariable")) {
								container.addStateVariable(new StateVariable(innerNode));
							}
							if (innerNode.getNodeName().equals("TimeDerivative")) {
								container.addEquation(innerNode.getAttributes().getNamedItem("variable").getNodeValue()
										, new Expression(innerNode.getAttributes().getNamedItem("value").getNodeValue()));
							}

						}
						if (outerNode.getAttributes().getNamedItem("units_external").getNodeValue() != null) {
							this.unitsExternal =
									outerNode.getAttributes().getNamedItem("units_external").getNodeValue().contentEquals("true");
						}
						if (outerNode.getAttributes().getNamedItem("simulation_steps").getNodeValue() != null) {
							//TODO: extract the unit and the value
						}
					}
				}
			}

		} catch (SAXException e) {
			System.err.println("Invalid artifact skipped: "+ configPath);
			return;
		} catch (ParserConfigurationException e) {
			System.err.println("Invalid artifact skipped: "+ configPath);
			return;
		} catch (IOException e) {
			System.err.println("Invalid artifact skipped: "+ configPath);
			return;
		}
	}

	public boolean isUnitsExternal() {
		return unitsExternal;
	}

	public double getSimulation_steps_length() {
		return simulation_steps_length;
	}

	public String getSimulation_steps_unit() {
		return simulation_steps_unit;
	}
}
