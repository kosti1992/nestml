package org.nest.codegeneration;

import static com.google.common.base.Preconditions.checkArgument;
import static de.se_rwth.commons.logging.Log.info;
import static org.nest.utils.AstUtils.deepClone;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import com.google.common.io.Files;
import de.monticore.generating.GeneratorEngine;
import de.monticore.generating.GeneratorSetup;
import de.monticore.generating.templateengine.GlobalExtensionManagement;
import de.se_rwth.commons.logging.Log;
import org.nest.codegeneration.helpers.LEMS.Elements.LEMSCollector;
import org.nest.codegeneration.helpers.LEMS.Elements.Dimension;
import org.nest.codegeneration.helpers.LEMS.Elements.SimulationConfiguration;
import org.nest.codegeneration.helpers.LEMS.Elements.Unit;
import org.nest.codegeneration.sympy.OdeProcessor;
import org.nest.codegeneration.sympy.TransformerBase;
import org.nest.nestml._ast.ASTBody;
import org.nest.nestml._ast.ASTNESTMLCompilationUnit;
import org.nest.nestml._ast.ASTNeuron;
import org.nest.nestml._symboltable.NESTMLScopeCreator;
import org.nest.ode._ast.ASTOdeDeclaration;
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
public class LEMSCodeGenerator {
	public static final String LOG_NAME = LEMSCodeGenerator.class.getName();
    private OdeProcessor odeProcessor;
    private boolean enableTracing;
    private NESTMLScopeCreator scopeCreator;
	private List<LEMSCollector> listOfNeurons;//used for tests and debug

    /**
     * This constructor is only used for testing the framework without to involve the frontend.
     */
    public LEMSCodeGenerator(){
        enableTracing = false;
        odeProcessor = new OdeProcessor();
        listOfNeurons = new ArrayList<>();
    }

    /**
     * Generates a new code generator object as used to generate LEMS models.
     * @param scopeCreator a scope creator object
     * @param enableTracing states whether processing shall be traced or not
     * @param odeProcessor an ode processor to solve odes
     */
    public LEMSCodeGenerator(final NESTMLScopeCreator scopeCreator,boolean enableTracing,final OdeProcessor odeProcessor){
        this.odeProcessor = odeProcessor;
        this.listOfNeurons = new ArrayList<>();
        this.enableTracing = enableTracing;
        this.scopeCreator = scopeCreator;
    }

    /**
     * Generates a new code generator object as used to generate LEMS models.
     * @param scopeCreator a scope creator object
     * @param enableTracing states whether processing shall be traced or not
     */
    public LEMSCodeGenerator(final NESTMLScopeCreator scopeCreator,boolean enableTracing){
        this.odeProcessor = new OdeProcessor();
        this.listOfNeurons = new ArrayList<>();
        this.enableTracing = enableTracing;
        this.scopeCreator = scopeCreator;
    }


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
		generateLEMSForNeurons(root, outputDirectory, configPath);
	}

	/**
	 * Generates from a set of neurons the corresponding LEMS-models.
	 *
	 * @param root         the root of the file containing all models
	 * @param outputDirectory path to the output directory to which the models will be generated.
	 * @param configPath      the source path where a  component configuration  is stored
	 */
	private void generateLEMSForNeurons(
			final ASTNESTMLCompilationUnit root,
			final Path outputDirectory,
			final Path configPath) {
		checkArgument(!root.getNeurons().isEmpty());

		//set the system language to english in order to avoid problems with "," instead of "."
		Locale.setDefault(Locale.ENGLISH);

		final GeneratorSetup setup = new GeneratorSetup(new File(outputDirectory.toString()));
		setup.setTracing(this.enableTracing);
		final GlobalExtensionManagement glex = getGlexConfiguration();
		setup.setGlex(glex);
		final GeneratorEngine generator = new GeneratorEngine(setup);
		Path makefileFile;

		Set<Unit> collectedUnits = new HashSet<>();//store units in order to print them to one file if required
		Set<Dimension> collectedDimension = new HashSet<>();//store dimensions in order to print them to one file if required

		SimulationConfiguration config = new SimulationConfiguration(configPath);

		LEMSModelBeautifier LEMSPrettyPrinterInstance = new LEMSModelBeautifier();

		//first some preprocessing is required
        ASTNESTMLCompilationUnit workingVersion = root;
        if(scopeCreator!=null) {//odes shall only be solved if a scope creator has been handed over
            for (int i = 0; i < root.getNeurons().size(); ++i) {
                final ASTNeuron solvedNeuron = solveODESInNeuron(root.getNeurons().get(i), root, outputDirectory);
                root.getNeurons().set(i, solvedNeuron);
            }

            workingVersion = deepClone(workingVersion, scopeCreator, outputDirectory);
        }
        List<ASTNeuron> neurons = workingVersion.getNeurons();

		LEMSCollector collector;
		//transform each given neuron
		for (ASTNeuron neuron : neurons) {
			System.out.println("-------------------------------------------------");
			System.out.println("Starting processing of: " + neuron.getName());
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
			System.out.println("Successfully generated LEMS model of " + neuron.getName() + " in " + outputDirectory);
			listOfNeurons.add(collector);
			LEMSPrettyPrinterInstance.beautifyOutput(Paths.get(collector.getNeuronName() + ".xml"), outputDirectory);
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
			System.out.println("Successfully generated LEMS units and dimensions.");
			LEMSPrettyPrinterInstance.beautifyOutput(Paths.get("units_dimensions.xml"), outputDirectory);
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
     * Solves all ODEs in the model and generates a processable format as input for LEMS genrator.
     * @param astNeuron the ast node of a neuron
     * @param artifactRoot the root folder of the artifact
     * @param outputBase the folder where generated models shall be stored
     * @return a solved ASTNeuron object
     * @author plotnikov
     */
    private ASTNeuron solveODESInNeuron(
            final ASTNeuron astNeuron,
            final ASTNESTMLCompilationUnit artifactRoot,
            final Path outputBase) {
        final ASTBody astBody = astNeuron.getBody();
        final Optional<ASTOdeDeclaration> odesBlock = astBody.getODEBlock();
        if (odesBlock.isPresent()) {
            if (odesBlock.get().getShapes().size() == 0) {
                info("The model will be solved numerically with GSL solver.", LOG_NAME);
                markNumericSolver(astNeuron.getName(), outputBase);
                return astNeuron;
            }
            else {
                info("The model will be analysed.", LOG_NAME);
                return odeProcessor.solveODE(astNeuron, artifactRoot, outputBase);
            }

        }
        else {
            return astNeuron;
        }

    }


    private void markNumericSolver(final String neuronName, final Path outputBase) {
        try {
            Files.write("numeric",
                    Paths.get(outputBase.toString(), neuronName + "." + TransformerBase.SOLVER_TYPE).toFile(),
                    Charset.defaultCharset());
        }
        catch (IOException e) {
            Log.error("Cannot write status file. Check you permissions.", e);
        }
    }


	/**
	 * This class implements a beautifier for LEMS models, used to provide a more readable format of the
	 * generated file.
	 */
	private class LEMSModelBeautifier {
		DocumentBuilderFactory dbFactory;
		DocumentBuilder dBuilder;
		Document original = null;
		StringWriter stringWriter = null;
		StreamResult xmlOutput = null;
		TransformerFactory tf = null;
		Transformer transformer = null;

		public LEMSModelBeautifier() {
			dbFactory = DocumentBuilderFactory.newInstance();
			try {
				dBuilder = dbFactory.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				info("Could not create pretty printer instance (LEMS)", LOG_NAME);
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
		public void beautifyOutput(Path modelPath, Path outPutDir) {
			try {
				stringWriter = new StringWriter();
				xmlOutput = new StreamResult(stringWriter);
				System.out.println(outPutDir.toAbsolutePath().toString() + "/" + modelPath.toString());
				original = dBuilder.parse(new InputSource(new InputStreamReader(
						new FileInputStream(outPutDir.toAbsolutePath().toString() + "/" + modelPath.toString()))));
				original.getDocumentElement().normalize();
				XPathExpression xpath = XPathFactory.newInstance().//delete all "white" lines but ignore comments
						newXPath().compile("//text()[normalize-space(.) = ''] ");
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
				info("Could not beautify model (LEMS)", LOG_NAME);
			}
		}
	}
}