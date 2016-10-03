package org.nest.codegeneration.LEMSTests;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;
import org.nest.base.ModelbasedTest;
import org.nest.codegeneration.LEMSGenerator;
import org.nest.codegeneration.helpers.LEMSElements.Dimension;
import org.nest.nestml._ast.ASTNESTMLCompilationUnit;

/**
 * Tests whether all dynamic elements have been generated correctly.
 * @author perun
 */
public class LEMSCollectorDynamicElementsTest extends ModelbasedTest{
  private static final Path OUTPUT_DIRECTORY = Paths.get("target", "LEMS");

  private static final String PSC_MODEL_WITH_ODE = "src/test/resources/codegeneration/LEMSTests/iaf_psc_alpha.nestml";
  private static final String INPUT_DIRECTORY = "src/test/resources/codegeneration/LEMSTests";
  @Test
  public void testGenerateLEMS() throws Exception {
    final ASTNESTMLCompilationUnit testModel = parseAndBuildSymboltable(PSC_MODEL_WITH_ODE);
    final LEMSGenerator testant = new LEMSGenerator();
    System.out.println("This test is not provided with an external artifact, message can be ignored!");
    testant.generateLEMS(testModel, OUTPUT_DIRECTORY,Paths.get(INPUT_DIRECTORY),false,0.1);
    //test state variables
    Assert.assertEquals(4,testant.getListOfNeurons().get(0).getStateVariablesList().size());
    Assert.assertEquals("V",testant.getListOfNeurons().get(0).getStateVariablesList().get(0).getName());
    Assert.assertEquals(Dimension.DimensionName.voltage,testant.getListOfNeurons().get(0).getStateVariablesList().get(0).getDimension());
    Assert.assertEquals("INITV",testant.getListOfNeurons().get(0).getStateVariablesList().get(0).getDefaultValue());
    //test default values
    Assert.assertTrue(testant.getListOfNeurons().get(0).getConstantsList().get(0).getName().equals("INITV"));
    Assert.assertTrue(testant.getListOfNeurons().get(0).getConstantsList().get(0).getValue().equals("0"));
    //test time derivative
    Assert.assertEquals(3,testant.getListOfNeurons().get(0).getEquations().size());
    Assert.assertTrue(testant.getListOfNeurons().get(0).getEquations().keySet().contains("I_shape_ex"));
    Assert.assertEquals("((exp(1)/tau_syn_ex)*t*exp(-1/tau_syn_ex*t))/CON1ms",
        testant.getListOfNeurons().get(0).getEquations().get("I_shape_ex"));
    Assert.assertTrue(testant.getListOfNeurons().get(0).getEquations().get("V").startsWith("not_supported"));
    //test conditional blocks
    Assert.assertTrue(testant.getListOfNeurons().get(0).getAutomaton().getConditionalBlocks().size()==3);
    Assert.assertTrue(testant.getListOfNeurons().get(0).getAutomaton().getConditionalBlocks().get(0).getInstructions().size()==1);



  }
}
