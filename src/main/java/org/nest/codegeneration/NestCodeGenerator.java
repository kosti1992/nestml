/*
 * Copyright (c) 2015 RWTH Aachen. All rights reserved.
 *
 * http://www.se-rwth.de/
 */
package org.nest.codegeneration;

import com.google.common.io.Files;
import de.monticore.generating.GeneratorEngine;
import de.monticore.generating.GeneratorSetup;
import de.monticore.generating.templateengine.GlobalExtensionManagement;
import de.se_rwth.commons.logging.Log;
import org.nest.codegeneration.converters.*;
import org.nest.codegeneration.helpers.*;
import org.nest.codegeneration.sympy.OdeTransformer;
import org.nest.codegeneration.sympy.OdeProcessor;
import org.nest.codegeneration.sympy.TransformerBase;
import org.nest.nestml._ast.ASTBody;
import org.nest.nestml._ast.ASTNESTMLCompilationUnit;
import org.nest.nestml._ast.ASTNeuron;
import org.nest.nestml._symboltable.NESTMLScopeCreator;
import org.nest.ode._ast.ASTOdeDeclaration;
import org.nest.spl.prettyprinter.ExpressionsPrettyPrinter;
import org.nest.spl.prettyprinter.IReferenceConverter;
import org.nest.symboltable.NestmlSymbols;
import org.nest.utils.AstUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static de.se_rwth.commons.logging.Log.info;
import static org.nest.utils.AstUtils.deepClone;
import static org.nest.utils.AstUtils.getAllNeurons;

/**
 * Generates C++ implementation and model integration code for NEST.
 *
 * @author plotnikov
 */
public class NestCodeGenerator {
  private final String LOG_NAME = NestCodeGenerator.class.getName();
  private final OdeProcessor odeProcessor;
  private final NESTMLScopeCreator scopeCreator;
  private final Boolean enableTracing ;

  public NestCodeGenerator(final NESTMLScopeCreator scopeCreator,
                           final OdeProcessor odeProcessor,
                           boolean enableTracing) {
    this.scopeCreator = scopeCreator;
    this.odeProcessor= odeProcessor;
    this.enableTracing = enableTracing;
  }

  public NestCodeGenerator(final NESTMLScopeCreator scopeCreator,
                           boolean enableTracing) {
    this.scopeCreator = scopeCreator;
    this.odeProcessor= new OdeProcessor();
    this.enableTracing = enableTracing;
  }

  public void analyseAndGenerate(
      final ASTNESTMLCompilationUnit root,
      final Path outputBase) {
    info("Starts processing of the model: " + root.getFullName(), LOG_NAME);

    ASTNESTMLCompilationUnit workingVersion = root;
    for (int i = 0; i < root.getNeurons().size(); ++i) {
      final ASTNeuron solvedNeuron = solveODESInNeuron(root.getNeurons().get(i), root, outputBase);
      root.getNeurons().set(i, solvedNeuron);
    }

    workingVersion = deepClone(workingVersion, scopeCreator, outputBase);
    workingVersion
        .getNeurons()
        .forEach(astNeuron -> generateNestCode(astNeuron, outputBase));

    final String msg = "Successfully generated NEST code for: '" + root.getFullName() + "' in: '"
        + outputBase.toAbsolutePath().toString() + "'";
    info(msg, LOG_NAME);
  }

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

  private void generateNestCode(
      final ASTNeuron astNeuron,
      final Path outputBase) {

    final GlobalExtensionManagement glex = getGlexConfiguration();
    setNeuronGenerationParameter(glex, astNeuron);
    generateHeader(astNeuron, outputBase, glex);
    generateClassImplementation(astNeuron, outputBase, glex);
  }

  private void generateHeader(
      final ASTNeuron astNeuron,
      final Path outputFolder,
      final GlobalExtensionManagement glex) {
    final GeneratorSetup setup = new GeneratorSetup(new File(outputFolder.toString()));
    setup.setGlex(glex);
    setup.setTracing(enableTracing);
    final GeneratorEngine generator = new GeneratorEngine(setup);
    final Path outputFile = Paths.get(astNeuron.getName() + ".h");
    generator.generate("org.nest.nestml.neuron.NeuronHeader", outputFile, astNeuron);
  }

  private void generateClassImplementation(
      final ASTNeuron astNeuron,
      final Path outputFolder,
      final GlobalExtensionManagement glex) {
    final GeneratorSetup setup = new GeneratorSetup(new File(outputFolder.toString()));
    setup.setGlex(glex);
    setup.setTracing(enableTracing);
    final GeneratorEngine generator = new GeneratorEngine(setup);

    final Path classImplementationFile = Paths.get(astNeuron.getName() + ".cpp");
    generator.generate(
        "org.nest.nestml.neuron.NeuronClass",
        classImplementationFile,
        astNeuron);

  }

  /**
   * Generates code that is necessary to integrate neuron models into the NEST infrastructure.
   * @param modelRoots List with neurons
   * @param moduleName The name of the nest module, which is then used in nest.Install(moduleName)
   * @param outputDirectory Directory to write the output
   */
  public void generateNESTModuleCode(
      final List<ASTNESTMLCompilationUnit> modelRoots,
      final String moduleName,
      final Path outputDirectory) {
    final List<ASTNeuron> neurons = getAllNeurons(modelRoots);
    final GeneratorSetup setup = new GeneratorSetup(new File(outputDirectory.toString()));
    setup.setTracing(false);

    final GlobalExtensionManagement glex = getGlexConfiguration();
    glex.setGlobalValue("neurons", neurons);
    glex.setGlobalValue("moduleName", moduleName);

    setup.setGlex(glex);
    setup.setTracing(false); // must be disabled
    final GeneratorEngine generator = new GeneratorEngine(setup);

    final Path cmakeLists = Paths.get("CMakeLists.txt");
    generator.generate(
        "org.nest.nestml.module.CMakeLists",
        cmakeLists,
        neurons.get(0)); // an arbitrary AST to match the signature

    final Path cmakeModuleHeader = Paths.get(moduleName + ".h");
    generator.generate(
        "org.nest.nestml.module.ModuleHeader",
        cmakeModuleHeader,
        neurons.get(0)); // an arbitrary AST to match the signature

    final Path cmakeModuleClass = Paths.get(moduleName + ".cpp");
    generator.generate(
        "org.nest.nestml.module.ModuleClass",
        cmakeModuleClass,
        neurons.get(0)); // an arbitrary AST to match the signature

    final Path initSLI = Paths.get("sli", moduleName + "-init.sli");
    generator.generate(
        "org.nest.nestml.module.SLI_Init",
        initSLI,
        neurons.get(0)); // an arbitrary AST to match the signature

    info("Successfully generated NEST module code in " + outputDirectory, LOG_NAME);
  }

  private GlobalExtensionManagement getGlexConfiguration() {
    final GlobalExtensionManagement glex = new GlobalExtensionManagement();
    final NESTReferenceConverter converter = new NESTReferenceConverter();
    final ExpressionsPrettyPrinter expressionsPrinter  = new ExpressionsPrettyPrinter(converter);

    final IReferenceConverter parameterBlockConverter = new NESTParameterBlockReferenceConverter();
    final ExpressionsPrettyPrinter parameterBlockPrinter = new ExpressionsPrettyPrinter(parameterBlockConverter);

    final IReferenceConverter stateBlockReferenceConverter = new NESTStateBlockReferenceConverter();
    final ExpressionsPrettyPrinter stateBlockPrettyPrinter = new ExpressionsPrettyPrinter(stateBlockReferenceConverter);

    glex.setGlobalValue("expressionsPrinter", expressionsPrinter);
    glex.setGlobalValue("functionCallConverter", converter);
    glex.setGlobalValue("idemPrinter", new ExpressionsPrettyPrinter());
    // this printer is used in one of the variable blocks. there, S_, V_, B_ structs are not defined and getters
    // setters must be used instead.
    glex.setGlobalValue("printerWithGetters", parameterBlockPrinter);
    glex.setGlobalValue("stateBlockPrettyPrinter", stateBlockPrettyPrinter);
    return glex;
  }


  private void setNeuronGenerationParameter(
      final GlobalExtensionManagement glex,
      final ASTNeuron neuron) {
    checkArgument(neuron.getSymbol().isPresent());
    glex.setGlobalValue("names", new Names());
    glex.setGlobalValue("statusNames", new Names());
    defineSolverType(glex, neuron); // potentially, overrides names with gsl name provider. the order is important

    final String guard = (neuron.getName()).replace(".", "_");
    glex.setGlobalValue("guard", guard);
    glex.setGlobalValue("neuronName", neuron.getName());
    glex.setGlobalValue("neuronSymbol", neuron.getSymbol().get());

    final NESTFunctionPrinter functionPrinter = new NESTFunctionPrinter();
    glex.setGlobalValue("declarations", new ASTDeclarations() );
    glex.setGlobalValue("assignments", new ASTAssignments());
    glex.setGlobalValue("functionPrinter", functionPrinter);
    glex.setGlobalValue("functions", new SPLFunctionCalls());
    glex.setGlobalValue("bufferHelper", new ASTBuffers());
    glex.setGlobalValue("variableHelper", new VariableHelper());
    glex.setGlobalValue("odeTransformer", new OdeTransformer());

    glex.setGlobalValue("outputEvent", ASTOutputs.printOutputEvent(neuron.getBody()));
    glex.setGlobalValue("isOutputEventPresent", ASTOutputs.isOutputEventPresent(neuron.getBody()));
    glex.setGlobalValue("isSpikeInput", ASTInputs.isSpikeInput(neuron));
    glex.setGlobalValue("isCurrentInput", ASTInputs.isCurrentInput(neuron));
    glex.setGlobalValue("body", neuron.getBody());

    final GslReferenceConverter converter = new GslReferenceConverter();
    final ExpressionsPrettyPrinter expressionsPrinter = new ExpressionsPrettyPrinter(converter);
    glex.setGlobalValue("expressionsPrinterForGSL", expressionsPrinter);
    glex.setGlobalValue("nestmlSymbols", new NestmlSymbols());
    glex.setGlobalValue("astUtils", new AstUtils());
  }


  private void defineSolverType(final GlobalExtensionManagement glex, final ASTNeuron neuron) {
    final ASTBody astBody = neuron.getBody();
    glex.setGlobalValue("useGSL", false);

    if (astBody.getODEBlock().isPresent()) {
      if (astBody.getODEBlock().get().getShapes().size() == 0) {
        glex.setGlobalValue("names", new GslNames());
        glex.setGlobalValue("useGSL", true);

        final IReferenceConverter converter = new NESTArrayStateReferenceConverter();
        final ExpressionsPrettyPrinter expressionsPrinter = new ExpressionsPrettyPrinter(converter);
        glex.setGlobalValue("expressionsPrinter", expressionsPrinter);
      }

    }

  }

}
