package org.nest.codegeneration.helpers.LEMS.Elements;

import org.nest.codegeneration.helpers.LEMS.Expressions.Expression;
import org.nest.codegeneration.helpers.LEMS.Expressions.Variable;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import static de.se_rwth.commons.logging.Log.info;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * This class stores all configuration of the simulation as handed over it the simulation script.
 *
 * @author perun
 */
public class SimulationConfiguration {
	//indicates whether units and dimensions have to be generated externally
	private boolean mIsUnitsExternal = false;
	//stores the length of single simulation step in ms
	private double mSimulationStepsLength = -1;//standard values to indicate errors
	private Unit mSimulationStepsUnit = null;
	//the path to a configuration file
	private Path mConfigPath = null;
	//should the concept of activator variables be used?
	public static boolean mWithActivator = false;

	public SimulationConfiguration() {
	}

	public SimulationConfiguration(Path configPath) {
		this.mConfigPath = configPath;
	}

	/**
	 * Reads an external artifact in XML format and extracts all required information.
	 *
	 * @throws IOException thrown if non file is given.
	 */
	public void adaptSettings(LEMSCollector _container) {
		if (mConfigPath == null || mConfigPath.hashCode()==0) {// a hash of 0 means that there is no path
			//if no artifact has been provided, use some standard settings:
			this.mSimulationStepsLength = 1;
			this.mSimulationStepsUnit = new Unit("ms",HelperCollection.powerConverter("ms"),
					new Dimension(HelperCollection.PREFIX_DIMENSION + "ms", 0, 0, 1, 0, 0, 0, 0));
			return;
		}
		try {
			File inputFile = new File(mConfigPath.toAbsolutePath().toString());
			if(!inputFile.exists()||!inputFile.isFile()){//first check if the artifact even exists
				throw new IOException();
			}
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
				if (outerNode.getAttributes().getNamedItem("name") != null) {
					List target_models =
							Arrays.asList(outerNode.getAttributes().getNamedItem("name").getNodeValue().split(";"));
					if (target_models.contains(_container.getNeuronName())) {
						//now check if the name list contains the name of the current neuron
						//if so, start to extract
						innerList = outerNode.getChildNodes();
						for (int j = 0; j < innerList.getLength(); j++) {
							innerNode = innerList.item(j);
							if (innerNode.getNodeName().equals("Attachments")) {
								_container.addAttachment(new Attachment(innerNode));
							}
							if (innerNode.getNodeName().equals("Parameter") || innerNode.getNodeName().equals("Constant")) {
								_container.addConstant(new Constant(innerNode));
							}
							if (innerNode.getNodeName().equals("DerivedParameter") || innerNode.getNodeName().equals("DerivedVariable")) {
								_container.addDerivedElement(new DerivedElement(innerNode));
							}
							if (innerNode.getNodeName().equals("EventPort")) {
								_container.addEventPort(new EventPort(innerNode));
							}
							if (innerNode.getNodeName().equals("StateVariable")) {
								_container.addStateVariable(new StateVariable(innerNode));
							}
							if (innerNode.getNodeName().equals("TimeDerivative")) {
								_container.addEquation(new Variable(innerNode.getAttributes().getNamedItem("variable").getNodeValue())
										, new Expression(innerNode.getAttributes().getNamedItem("value").getNodeValue()));
							}

						}
						if (outerNode.getAttributes().getNamedItem("units_external") != null &&
								outerNode.getAttributes().getNamedItem("units_external").getNodeValue() != null) {
							this.mIsUnitsExternal =
									outerNode.getAttributes().getNamedItem("units_external").getNodeValue().contentEquals("true");
						}
						if (outerNode.getAttributes().getNamedItem("simulation_steps") != null &&
								outerNode.getAttributes().getNamedItem("simulation_steps").getNodeValue() != null) {
							//if it matches a value declaration, e.g. 10ms (value:unit)
							if (outerNode.getAttributes().getNamedItem("simulation_steps").getNodeValue().matches("[0-9]+[a-zA-Z]+")) {
								String unit = outerNode.getAttributes().getNamedItem("simulation_steps").getNodeValue().replaceAll("[0-9]", "");
								mSimulationStepsUnit = new Unit(unit,HelperCollection.powerConverter(unit),new Dimension(HelperCollection.PREFIX_DIMENSION + unit, 0, 0, 1, 0, 0, 0, 0));
								mSimulationStepsLength = Double.parseDouble(outerNode.getAttributes().getNamedItem("simulation_steps").getNodeValue().replaceAll("[a-zA-Z]", ""));
								_container.addUnit(mSimulationStepsUnit);
								_container.addDimension(mSimulationStepsUnit.getDimension());
							}
						}
					}
				}
			}

		} catch (SAXException e) {
			System.err.println("Artifact skipped (invalid): " + mConfigPath);
		} catch (ParserConfigurationException e) {
			System.err.println("Artifact skipped (invalid): " + mConfigPath);
		} catch (IOException e) {
			System.err.println("Artifact skipped (not found): " + mConfigPath);
		} catch (Exception e){
			System.err.println("Artifact skipped (not found): " + mConfigPath);
		}
		//in the case that a correct artifact has been provided but without steps length and unit stated
		if (this.mSimulationStepsLength == -1 && this.mSimulationStepsUnit == null) {
			this.mSimulationStepsLength = 1;
			this.mSimulationStepsUnit = new Unit("ms",HelperCollection.powerConverter("ms"),new Dimension(HelperCollection.PREFIX_DIMENSION + "ms", 0, 0, 1, 0, 0, 0, 0));
		}

	}

	public boolean isUnitsExternal() {
		return mIsUnitsExternal;
	}

	public double getSimulationStepsLength() {
		return mSimulationStepsLength;
	}

	public String getSimulationStepsLengthAsString(){
		if(mSimulationStepsLength - (int) mSimulationStepsLength == 0){
			return String.valueOf((int) mSimulationStepsLength);
		}
		else{
			return String.valueOf(mSimulationStepsLength);
		}
	}

	public Unit getSimulationStepsUnit() {
		return mSimulationStepsUnit;
	}

}
