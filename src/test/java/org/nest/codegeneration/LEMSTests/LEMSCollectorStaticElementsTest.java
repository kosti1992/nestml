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
 * Tests whether elements outside the dynamics block are correctly stored.
 * @author perun
 */
public class LEMSCollectorStaticElementsTest extends ModelbasedTest{
  private static final Path OUTPUT_DIRECTORY = Paths.get("target", "LEMS");

  private static final String PSC_MODEL_WITH_ODE = "src/test/resources/codegeneration/LEMSTests/iaf_psc_alpha.nestml";
  private static final String INPUT_DIRECTORY = "src/test/resources/codegeneration/LEMSTests";
  @Test
  public void testGenerateLEMS() throws Exception {
    final ASTNESTMLCompilationUnit testModel = parseAndBuildSymboltable(PSC_MODEL_WITH_ODE);
    final LEMSGenerator testant = new LEMSGenerator();
    System.out.println("This test is not provided with an external artifact, message can be ignored!");
    testant.generateLEMS(testModel, OUTPUT_DIRECTORY,Paths.get(INPUT_DIRECTORY));
    //test constants
    if(true) {
      Assert.assertEquals(12, testant.getListOfNeurons().get(0).getConstantsList().size());
      Assert.assertEquals("INITV", testant.getListOfNeurons().get(0).getConstantsList().get(0).getName());
      Assert.assertEquals("0mV", testant.getListOfNeurons().get(0).getConstantsList().get(0).getValueUnit());
      Assert.assertEquals("DimensionOf_mV", testant.getListOfNeurons().get(0).getConstantsList().get(0).getDimension());
      Assert.assertEquals("55mV", testant.getListOfNeurons().get(0).getConstantsList().get(10).getValueUnit());

      //test derived parameter
      Assert.assertEquals(3, testant.getListOfNeurons().get(0).getDerivedParametersList().size());
      Assert.assertEquals("V_reset", testant.getListOfNeurons().get(0).getDerivedParametersList().get(0).getName());
      Assert.assertEquals("DimensionOf_mV", testant.getListOfNeurons().get(0).getDerivedParametersList().get(0).getDimension());
      Assert.assertEquals("-CON70.0mV-E_L", testant.getListOfNeurons().get(0).getDerivedParametersList().get(0).getDerivationInstruction().print());
      //not supported elements found
      //Assert.assertTrue(testant.getListOfNeurons().get(0).getNotConvertedElements().size() > 0);
      //test event ports
      Assert.assertEquals(4, testant.getListOfNeurons().get(0).getPortsList().size());
      Assert.assertEquals("ex_spikes", testant.getListOfNeurons().get(0).getPortsList().get(0).getName());
      Assert.assertEquals(EventPort.Direction.in, testant.getListOfNeurons().get(0).getPortsList().get(0).getDirection());
    }
  }
}
