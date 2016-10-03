package org.nest.codegeneration.LEMSTests;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;
import org.nest.base.ModelbasedTest;
import org.nest.codegeneration.LEMSGenerator;
import org.nest.codegeneration.helpers.LEMSElements.Dimension;
import org.nest.codegeneration.helpers.LEMSElements.EventPort;
import org.nest.nestml._ast.ASTNESTMLCompilationUnit;

/**
 * Tests whether an external artifact is read-in correctly and without any errors.
 * @author perun
 */
public class ArtifactTest extends ModelbasedTest{
  private static final Path OUTPUT_DIRECTORY = Paths.get("target", "LEMS");

  private static final String PSC_MODEL_WITH_ODE = "src/test/resources/codegeneration/LEMSTests/izhikevich.nestml";
  private static final String INPUT_DIRECTORY = "src/test/resources/codegeneration/LEMSTests/config";
  @Test
  public void testGenerateLEMS() throws Exception {
    final ASTNESTMLCompilationUnit testModel = parseAndBuildSymboltable(PSC_MODEL_WITH_ODE);
    final LEMSGenerator testant = new LEMSGenerator();
    testant.generateLEMS(testModel, OUTPUT_DIRECTORY,Paths.get(INPUT_DIRECTORY+"/"),false,0.1);
    //test whether the artifact has been correctly read in
    Assert.assertTrue(testant.getListOfNeurons().get(0).getConfig().getInstructions().size()>0);
    //test if the derived variable has been read in properly
    Assert.assertEquals("I",testant.getListOfNeurons().get(0).getDerivedElementList().get(0).getName());
    Assert.assertEquals(Dimension.DimensionName.none,
        testant.getListOfNeurons().get(0).getDerivedElementList().get(0).getDimension());
    Assert.assertEquals("pulseGeneratorDL[*]/I",
        testant.getListOfNeurons().get(0).getDerivedElementList().get(0).getDerivationInstruction());
    //test whether the attachment has been read in properly
    Assert.assertTrue(testant.getListOfNeurons().get(0).getAttachments().size()==1);
    Assert.assertTrue(testant.getListOfNeurons().get(0).
        getAttachments().get("pulseGeneratorDL").equals("pulseGeneratorDL"));
  }
}
