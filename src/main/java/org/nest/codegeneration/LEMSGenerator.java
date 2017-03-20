package org.nest.codegeneration;

import static com.google.common.base.Preconditions.checkArgument;
import static de.se_rwth.commons.logging.Log.info;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import de.monticore.generating.GeneratorEngine;
import de.monticore.generating.GeneratorSetup;
import de.monticore.generating.templateengine.GlobalExtensionManagement;
import org.nest.codegeneration.helpers.LEMSElements.LEMSCollector;
import org.nest.codegeneration.helpers.LEMSElements.Dimension;
import org.nest.codegeneration.helpers.LEMSElements.SimulationConfiguration;
import org.nest.codegeneration.helpers.LEMSElements.Unit;
import org.nest.nestml._ast.ASTNESTMLCompilationUnit;
import org.nest.nestml._ast.ASTNeuron;
import org.nest.spl.prettyprinter.ExpressionsPrettyPrinter;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

/**
 * This class reads, processes and generates models from given NESTML-neurons. By usage of the parameter
 * "units_dimensions_external", the generator can be instructed to generate units and dimensions of all
 * neurons to single file or within each transformed model. The "outputDirectory" sets the target where
 * all artifacts are generated. "configPath" can be used to hand over an external artifact containing
 * additional elements which have to be added. "simSteps" indicates the length of a single simulation step
 * and is required whenever the steps() function call has to be processed.
 *
 * @author perun
 */
public class LEMSGenerator {
	public static final String LOG_NAME = LEMSGenerator.class.getName();

	private List<LEMSCollector> listOfNeurons = new ArrayList<>();//used for tests and debug

	/**
	 * Generates from a provided set of models the corresponding set of counter-pieces in LEMS.
	 *
	 * @param root            Root of source-models
	 * @param outputDirectory path to the output directory to which the models will be generated
	 * @param configPath      the source path where a component configuration  is stored
	 */
	public void generateLEMS(
			final ASTNESTMLCompilationUnit root,
			final Path outputDirectory,
			final Path configPath) {
		generateLEMSForNeurons(root.getNeurons(), outputDirectory, configPath);
	}

	/**
	 * Generates from a set of neurons the corresponding LEMS-models.
	 *
	 * @param neurons         set of neurons
	 * @param outputDirectory path to the output directory to which the models will be generated.
	 * @param configPath      the source path where a  component configuration  is stored
	 */
	private void generateLEMSForNeurons(
			final List<ASTNeuron> neurons,
			final Path outputDirectory,
			final Path configPath) {
		checkArgument(!neurons.isEmpty());
		//set the system language to english in order to avoid problems with "," instead of "."
		Locale.setDefault(Locale.ENGLISH);

		final GeneratorSetup setup = new GeneratorSetup(new File(outputDirectory.toString()));
		setup.setTracing(false);
		final GlobalExtensionManagement glex = getGlexConfiguration();
		setup.setGlex(glex);
		final GeneratorEngine generator = new GeneratorEngine(setup);
		Path makefileFile;

		Set<Unit> collectedUnits = new HashSet<>();//store units in order to print them to one file if required
		Set<Dimension> collectedDimension = new HashSet<>();//store dimensions in order to print them to one file if required

		SimulationConfiguration config = new SimulationConfiguration(configPath);

		LEMSModelPrettyPrinter LEMSPrettyPrinterInstance = new LEMSModelPrettyPrinter();


		LEMSCollector collector;
		//transform each given neuron
		for (ASTNeuron neuron : neurons) {
			collector = new LEMSCollector(neuron, config);//process the neuron
			glex.setGlobalValue("container", collector);
			if (config.isUnitsExternal()) {
				collectedUnits.addAll(collector.getUnitsSet());
				collectedDimension.addAll(collector.getDimensionsSet());
			}
			makefileFile = Paths.get(collector.getNeuronName() + ".xml");
			generator.generate(
					"org.nest.lems.componentType",
					makefileFile,
					neuron); // the current neuron
			info("Successfully generated LEMS model of " + neuron.getName() + " in " + outputDirectory, LOG_NAME);
			listOfNeurons.add(collector);
			LEMSPrettyPrinterInstance.prettyPrintModel(Paths.get(collector.getNeuronName() + ".xml"), outputDirectory);
		}
		//print units and dimensions externally if required
		if (config.isUnitsExternal()) {
			ArrayList<String> listOfNeuronsNames = new ArrayList<>();
			for(int i=0;i<neurons.size();i++){
				listOfNeuronsNames.add(neurons.get(i).getName());
			}
			makefileFile = Paths.get("units_dimensions.xml");
			glex.setGlobalValue("units", collectedUnits);
			glex.setGlobalValue("dimensions", collectedDimension);
			glex.setGlobalValue("global", true);
			glex.setGlobalValue("namesOfNeurons",listOfNeuronsNames);
			generator.generate(
					"org.nest.lems.units_dimensions",
					makefileFile,
					neurons.get(0));// an arbitrary neuron as placeholder
			info("Successfully generated LEMS units and dimensions.", LOG_NAME);
			LEMSPrettyPrinterInstance.prettyPrintModel(Paths.get("units_dimensions.xml"), outputDirectory);
		}
	}

	private GlobalExtensionManagement getGlexConfiguration() {
		final GlobalExtensionManagement glex = new GlobalExtensionManagement();
		glex.setGlobalValue("idemPrinter", new ExpressionsPrettyPrinter());
		return glex;
	}

	public List<LEMSCollector> getListOfNeurons() {
		return this.listOfNeurons;
	}


	/**
	 * This class implements a pretty printer for LEMS models, used to provide a more readable format of the
	 * generated file.
	 */
	private class LEMSModelPrettyPrinter {
		DocumentBuilderFactory dbFactory;
		DocumentBuilder dBuilder;
		Document original = null;
		StringWriter stringWriter = null;
		StreamResult xmlOutput = null;
		TransformerFactory tf = null;
		Transformer transformer = null;

		public LEMSModelPrettyPrinter() {
			dbFactory = DocumentBuilderFactory.newInstance();
			try {
				dBuilder = dbFactory.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				info("Could not create pretty print instance (LEMS)", LOG_NAME);
				e.printStackTrace();
			}
			tf = TransformerFactory.newInstance();
			try {
				transformer = tf.newTransformer();
			} catch (TransformerConfigurationException e) {
				e.printStackTrace();
			}
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC,"yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		}

		/**
		 * Reads in the file, normalizes and pretty prints it, and finally writes it back.
		 *
		 * @param modelPath a path to the LEMS model which shall be pretty printed.
		 */
		public void prettyPrintModel(Path modelPath, Path outPutDir) {
			try {
				stringWriter = new StringWriter();
				xmlOutput = new StreamResult(stringWriter);
				original = dBuilder.parse(new InputSource(new InputStreamReader(
						new FileInputStream(outPutDir.toAbsolutePath().toString() + "/" + modelPath.toString()))));
				original.getDocumentElement().normalize();
				XPathExpression xpath = XPathFactory.newInstance().//delete all "white" but ignore comments
						newXPath().compile("//text()[normalize-space(.) = '' and not(comment())]");
				NodeList blankTextNodes = (NodeList) xpath.evaluate(original, XPathConstants.NODESET);

				for (int i = 0; i < blankTextNodes.getLength(); i++) {
					blankTextNodes.item(i).getParentNode().removeChild(blankTextNodes.item(i));
				}
				xmlOutput = new StreamResult(stringWriter);
				transformer.transform(new DOMSource(original), xmlOutput);
				PrintWriter out = new PrintWriter(outPutDir.toAbsolutePath().toString() + "/" + modelPath.toString());
				out.println(xmlOutput.getWriter().toString());
				out.close();
			} catch (Exception ex) {
				info("Could not pretty print model (LEMS)", LOG_NAME);
				throw new RuntimeException("Error converting to String", ex);
			}
		}
	}
}