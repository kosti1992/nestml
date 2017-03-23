package org.nest.codegeneration.LEMSTests;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;
import org.nest.base.ModelbasedTest;
import org.nest.codegeneration.LEMSCodeGenerator;
import org.nest.codegeneration.helpers.LEMSElements.EventPort;
import org.nest.nestml._ast.ASTNESTMLCompilationUnit;

/**
 * Tests whether an external artifact is read-in correctly and without any errors.
 * @author perun
 */
public class LEMSArtifactTest extends ModelbasedTest{
  private static final Path OUTPUT_DIRECTORY = Paths.get("target", "LEMS");
  private static final String PSC_MODEL_WITH_ODE = "src/test/resources/codegeneration/LEMSTests/izhikevich.nestml";
  private static final String CONFIG_PATH = "src/test/resources/codegeneration/LEMSTests/artifact.xml";
  @Test
  public void testGenerateLEMS() throws Exception {
    final ASTNESTMLCompilationUnit testModel = parseAndBuildSymboltable(PSC_MODEL_WITH_ODE);
    final LEMSCodeGenerator testant = new LEMSCodeGenerator();
    testant.generateLEMS(testModel, OUTPUT_DIRECTORY, Paths.get(CONFIG_PATH));
    //check if Attachments are read in correctly
    Assert.assertEquals("testAttachment",testant.getListOfNeurons().get(0).getAttachments().get(0).getBindName());
    Assert.assertEquals("testAttachmentType",testant.getListOfNeurons().get(0).getAttachments().get(0).getBindType());
    //check if DerivedVariables are read in correctly
    Assert.assertEquals("testDerivedVariable",testant.getListOfNeurons().get(0).getDerivedElementList().get(0).getName());
    Assert.assertEquals("testDimension",testant.getListOfNeurons().get(0).getDerivedElementList().get(0).getDimension());
    Assert.assertEquals("testSelect",testant.getListOfNeurons().get(0).
            getDerivedElementList().get(0).getDerivationInstruction().print());
    Assert.assertEquals("testReduce",testant.getListOfNeurons().get(0).getDerivedElementList().get(0).getReduce());
    //check if parameter are read in correctly
    Assert.assertEquals("testParameter",testant.getListOfNeurons().get(0).getConstantsList().get(8).getName());
    Assert.assertEquals("testDimension",testant.getListOfNeurons().get(0).getConstantsList().get(8).getDimension());
    //check if constants are read in correctly
    Assert.assertEquals("testConst",testant.getListOfNeurons().get(0).getConstantsList().get(9).getName());
    Assert.assertEquals("testDimension",testant.getListOfNeurons().get(0).getConstantsList().get(9).getDimension());
    Assert.assertEquals("testValue",testant.getListOfNeurons().get(0).getConstantsList().get(9).getValue().print());
    //check if event port is read in correctly
    Assert.assertEquals("testEventPort",testant.getListOfNeurons().get(0).getPortsList().get(3).getName());
    Assert.assertEquals(EventPort.Direction.out,testant.getListOfNeurons().get(0).getPortsList().get(3).getDirection());
    //check if state variable is read in correctly
    Assert.assertEquals("testStateVariable",testant.getListOfNeurons().get(0).getStateVariablesList().get(4).getName());
    Assert.assertEquals("testDimension",testant.getListOfNeurons().get(0).getStateVariablesList().get(4).getDimension());
    //check if time derivative is read in correctly
    Assert.assertEquals("testEquation",testant.getListOfNeurons().get(0).getEquations().get("testTimeDerivative").print());
}
}
