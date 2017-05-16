package org.nest.codegeneration.LEMSTests;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;
import org.nest.base.ModelbasedTest;
import org.nest.codegeneration.LEMSCodeGenerator;
import org.nest.codegeneration.helpers.Expressions.Variable;
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
    final LEMSCodeGenerator testant = new LEMSCodeGenerator();
    if(true){
      System.out.println("This test is not provided with an external artifact, message can be ignored!");
      testant.generateLEMS(testModel, OUTPUT_DIRECTORY, Paths.get(INPUT_DIRECTORY));
      //test state variables
      Assert.assertEquals(3, testant.getListOfNeurons().get(0).getStateVariablesList().size());
      Assert.assertEquals("V", testant.getListOfNeurons().get(0).getStateVariablesList().get(0).getName());
      Assert.assertEquals("DIM_mV", testant.getListOfNeurons().get(0).getStateVariablesList().get(0).getDimension());
      Assert.assertEquals("INIT_V", testant.getListOfNeurons().get(0).getStateVariablesList().get(0).getDefaultValue().get().print());
      //test time derivative
      //only time derivative represent equations, shapes are stored as derived variables
      Assert.assertEquals(1, testant.getListOfNeurons().get(0).getEquations().size());
      Assert.assertTrue(testant.getListOfNeurons().get(0).getEquations().keySet().contains(new Variable("V")));
      Assert.assertEquals("(ACT_V*(-1/Tau*V+1/C_m))/CON_1_ms",testant.getListOfNeurons().get(0).getEquations().get(new Variable("V")).print());
      //shapes test
      Assert.assertTrue(testant.getListOfNeurons().get(0).getDerivedElementList().get(1).getName().equals("I_shape_in"));
      //Assert.assertTrue(testant.getListOfNeurons().get(0).getEquations().get("V").startsWith("not_supported"));
      //test conditional blocks
      Assert.assertTrue(testant.getListOfNeurons().get(0).getAutomaton().getConditionalBlocks().size() == 3);
      Assert.assertTrue(testant.getListOfNeurons().get(0).getAutomaton().getConditionalBlocks().get(0).getInstructions().size() == 1);
    }


  }
}
