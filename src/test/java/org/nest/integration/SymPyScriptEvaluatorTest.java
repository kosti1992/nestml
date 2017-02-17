/*
 * Copyright (c) 2015 RWTH Aachen. All rights reserved.
 *
 * http://www.se-rwth.de/
 */
package org.nest.integration;

import org.junit.Ignore;
import org.junit.Test;
import org.nest.base.ModelbasedTest;
import org.nest.codegeneration.sympy.ODESolverGenerator;
import org.nest.codegeneration.sympy.SymPyScriptEvaluator;
import org.nest.codegeneration.sympy.TransformerBase;
import org.nest.nestml._ast.ASTNESTMLCompilationUnit;
import org.nest.nestml._symboltable.NESTMLScopeCreator;
import org.nest.utils.FilesHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.Assert.assertTrue;

/**
 * Test evaluation of the solver script. Depends on successful script generation.
 *
 * @author plonikov
 */
public class SymPyScriptEvaluatorTest extends ModelbasedTest {
  private static final String IAF_PSC_EXP = "models/iaf_psc_exp.nestml";
  private static final String IAF_PSC_ALPHA = "models/iaf_psc_alpha.nestml";
  private static final String PSC_MODEL_FILE = "models/iaf_neuron.nestml";
  private static final String COND_MODEL_FILE = "models/iaf_cond_alpha.nestml";

  private static final Path SYMPY_OUTPUT = Paths.get(OUTPUT_FOLDER.toString(), "sympy");

  @Test
  public void testPSC_ALPHA_MODEL() throws IOException {
    generateAndEvaluate(IAF_PSC_ALPHA);
  }

  @Test
  public void testPSC_EXP_MODEL() throws IOException {
    generateAndEvaluate(IAF_PSC_EXP);
  }

  @Test
  public void testIAF_NEURON() throws IOException {
    generateAndEvaluate(PSC_MODEL_FILE);
  }

  @Ignore
  @Test
  public void testIAF_COND_ALPHA() throws IOException {
    generateAndEvaluate(COND_MODEL_FILE);
  }

  private void generateAndEvaluate(final String pathToModel) throws IOException {
    final Optional<ASTNESTMLCompilationUnit> root = parser.parse(pathToModel);

    assertTrue(root.isPresent());

    final NESTMLScopeCreator nestmlScopeCreator = new NESTMLScopeCreator(TEST_MODEL_PATH);
    nestmlScopeCreator.runSymbolTableCreator(root.get());

    FilesHelper.deleteFilesInFolder(SYMPY_OUTPUT);

    final Optional<Path> generatedScript = ODESolverGenerator.generateSympyODEAnalyzer(
        root.get().getNeurons().get(0),
        SYMPY_OUTPUT);

    assertTrue(generatedScript.isPresent());
    final SymPyScriptEvaluator evaluator = new SymPyScriptEvaluator();

    assertTrue(evaluator.evaluateScript(generatedScript.get()));
    assertTrue(Files.exists(Paths.get(
        SYMPY_OUTPUT.toString(),
        root.get().getNeurons().get(0).getName() + "." + TransformerBase.SOLVER_TYPE)));
  }

}
