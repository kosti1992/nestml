package org.nest.codegeneration;

import static com.google.common.base.Preconditions.checkArgument;
import static de.se_rwth.commons.logging.Log.info;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import de.monticore.generating.GeneratorEngine;
import de.monticore.generating.GeneratorSetup;
import de.monticore.generating.templateengine.GlobalExtensionManagement;
import org.nest.codegeneration.helpers.LEMSCollector;
import org.nest.codegeneration.helpers.LEMSElements.Dimension;
import org.nest.codegeneration.helpers.LEMSElements.SimulationConfiguration;
import org.nest.codegeneration.helpers.LEMSElements.Unit;
import org.nest.nestml._ast.ASTNESTMLCompilationUnit;
import org.nest.nestml._ast.ASTNeuron;
import org.nest.spl.prettyprinter.ExpressionsPrettyPrinter;

/**
 * This class reads, processes and generates models from given NESTML-neurons. By usage of the parameter
 * "units_dimensions_external", the generator can be instructed to generate units and dimensions of all
 * neurons to single file or within each transformed model. The "outputDirectory" sets the target where
 * all artifacts are generated. "configPath" can be used to hand over an external artifact containing
 * additional elements which have to be added. "simSteps" indicates the length of a single simulation step
 * and is required whenever the steps() function call has to be processed.
 * @author perun
 */
public class LEMSGenerator {
  private final String LOG_NAME = LEMSGenerator.class.getName();

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
      final Path configPath,
      final boolean unitsExternal,
      final double simSteps) {
    generateLEMSForNeurons(root.getNeurons(), outputDirectory, configPath,unitsExternal,simSteps);
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
      final Path configPath,
      final boolean unitsExternal,
      final double simSteps) {
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

    SimulationConfiguration config = new SimulationConfiguration(configPath,unitsExternal,simSteps);

    LEMSCollector collect = null;
    //transform each given neuron
    for (ASTNeuron neuron : neurons) {
      collect = new LEMSCollector(neuron, config);//process the neuron
      glex.setGlobalValue("container", collect);
      if (config.isUnitsExternal()) {
        collectedUnits.addAll(collect.getUnitsSet());
        collectedDimension.addAll(collect.getDimensionsSet());
      }
      makefileFile = Paths.get(collect.getNeuronName() + ".xml");
      generator.generate(
          "org.nest.lems.componentType",
          makefileFile,
          neuron); // the current neuron
      info("Successfully generated LEMS model of " + neuron.getName() + " in " + outputDirectory, LOG_NAME);
      listOfNeurons.add(collect);
    }
    //print units and dimensions externally if required
    if (config.isUnitsExternal()) {
      makefileFile = Paths.get("units_dimensions.xml");
      glex.setGlobalValue("units", collectedUnits);
      glex.setGlobalValue("dimensions", collectedDimension);
      glex.setGlobalValue("global", true);
      generator.generate(
          "org.nest.lems.units_dimensions",
          makefileFile,
          neurons.get(0));// an arbitrary neuron as placeholder
      info("Successfully generated LEMS units and dimensions.", LOG_NAME);
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
}
