package org.nest.codegeneration.LEMSTests;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;
import org.nest.base.ModelbasedTest;
import org.nest.codegeneration.LEMSGenerator;
import org.nest.codegeneration.helpers.Expressions.LEMSSyntaxContainer;
import org.nest.nestml._ast.ASTNESTMLCompilationUnit;

/**
 * Tests whether an external artifact is read-in correctly and without any errors.
 * @author perun
 */
public class ArtifactTest extends ModelbasedTest{
  private static final Path OUTPUT_DIRECTORY = Paths.get("target", "LEMS");

  //private static final String PSC_MODEL_WITH_ODE = "src/test/resources/codegeneration/LEMSTests/izhikevich.nestml";
  //private static final String CONFIG_PATH = "src/test/resources/codegeneration/LEMSTests/config.xml";
  private static final String PSC_MODEL_WITH_ODE = "src/test/resources/codegeneration/LEMSTests/izhikevich_neuron.nestml";
  private static final String CONFIG_PATH = "src/test/resources/codegeneration/LEMSTests/config2.xml";
  @Test
  public void testGenerateLEMS() throws Exception {
    final ASTNESTMLCompilationUnit testModel = parseAndBuildSymboltable(PSC_MODEL_WITH_ODE);
    final LEMSGenerator testant = new LEMSGenerator();
    testant.generateLEMS(testModel, OUTPUT_DIRECTORY, Paths.get(CONFIG_PATH), false, 0.1);
    //Assert.assertEquals("INITu", testant.getListOfNeurons().get(0).getDerivedElementList().get(0).getName());
    //Assert.assertEquals("none", testant.getListOfNeurons().get(0).getDerivedElementList().get(0).getDimension());
    //Assert.assertEquals("pulseGeneratorDL[*]/I", testant.getListOfNeurons().get(0).getDerivedElementList().get(1).getDerivationInstruction().print(new LEMSSyntaxContainer()));
    //test whether the attachment has been read in properly
    //Assert.assertTrue(testant.getListOfNeurons().get(0).getAttachments().size() == 1);

}
}
