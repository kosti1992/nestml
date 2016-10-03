package org.nest.codegeneration.LEMSTests;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.nest.base.ModelbasedTest;
import org.nest.codegeneration.LEMSGenerator;
import org.nest.codegeneration.helpers.LEMSElements.Unit;
import org.nest.nestml._ast.ASTNESTMLCompilationUnit;

/**
 * @author perun
 */
public class PhysicalUnitsTest extends ModelbasedTest {
  private static final Path OUTPUT_DIRECTORY = Paths.get("target", "LEMS");
  private static final String PSC_MODEL_WITH_ODE = "src/test/resources/codegeneration/aeif_cond_alpha.nestml";
  private static final String INPUT_DIRECTORY = "src/test/resources/codegeneration/";

  @Test
  public void testGenerateLEMS() throws Exception {
    final ASTNESTMLCompilationUnit testModel = parseAndBuildSymboltable(PSC_MODEL_WITH_ODE);
    final LEMSGenerator testant = new LEMSGenerator();
    System.out.println("This test is not provided with an external artifact, message can be ignored!");
    testant.generateLEMS(testModel, OUTPUT_DIRECTORY,Paths.get(INPUT_DIRECTORY),false,0.1);
  }
}
