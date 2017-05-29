package org.nest.codegeneration.LEMSTests;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;
import org.nest.base.ModelbasedTest;
import org.nest.codegeneration.LEMSCodeGenerator;
import org.nest.codegeneration.helpers.LEMS.Expressions.Variable;
import org.nest.codegeneration.helpers.LEMS.Elements.*;
import org.nest.nestml._ast.ASTNESTMLCompilationUnit;

/**
 * This test checks whether duplicate elements are correctly recognized and not added to the corresponding list.
 * @author perun
 */
public class LEMSDublicateTest extends ModelbasedTest {
  private static final Path OUTPUT_DIRECTORY = Paths.get("target", "LEMS");
  private static final String PSC_MODEL_WITH_ODE = "src/test/resources/codegeneration/LEMSTests/izhikevichNested.nestml";
  private static final String INPUT_DIRECTORY = "src/test/resources/codegeneration/LEMSTests/config.xml";
  @Test
  public void testGenerateLEMS() throws Exception {
    System.out.println("-----------------------------Error messages can be ignored-----------------------------");
    int varCount = 0;
    final ASTNESTMLCompilationUnit testModel = parseAndBuildSymboltable(PSC_MODEL_WITH_ODE);
    final LEMSCodeGenerator testant = new LEMSCodeGenerator();
    testant.generateLEMS(testModel, OUTPUT_DIRECTORY,Paths.get(INPUT_DIRECTORY+"/"));
    if(true) {
      //--------------------------------Constant+Parameter---------------------------------------
      //add two constants and check whether duplicates have been recognized
      testant.getListOfNeurons().get(0).addConstant(new Constant("test", "none",new Variable("test"),true));
      varCount = testant.getListOfNeurons().get(0).getConstantsList().size();
      testant.getListOfNeurons().get(0).addConstant(new Constant("test", "none",new Variable("test"),true));
      Assert.assertTrue(varCount == testant.getListOfNeurons().get(0).getConstantsList().size());
      //now add a parameter and check if it is recognized as such
      testant.getListOfNeurons().get(0).addConstant(new Constant("test", "none",new Variable("test"),false));
      Assert.assertTrue((varCount + 1) == testant.getListOfNeurons().get(0).getConstantsList().size() );
      testant.getListOfNeurons().get(0).addConstant(new Constant("test", "none",new Variable("test"),false));
      Assert.assertTrue((varCount + 1) == testant.getListOfNeurons().get(0).getConstantsList().size() );
      //---------------------------------Attachments----------------------------------------------
      testant.getListOfNeurons().get(0).addAttachment(new Attachment("test","test"));
      varCount = testant.getListOfNeurons().get(0).getAttachments().size();
      testant.getListOfNeurons().get(0).addAttachment(new Attachment("test","test"));
      Assert.assertTrue(varCount == testant.getListOfNeurons().get(0).getAttachments().size() );
      //-----------------------------------
      testant.getListOfNeurons().get(0).addUnit(new Unit("test",0,
              new Dimension("test",1,1,1,1,1,1,1)));
      varCount = testant.getListOfNeurons().get(0).getUnitsSet().size();
      testant.getListOfNeurons().get(0).addUnit(new Unit("test",0,
              new Dimension("test",1,1,1,1,1,1,1)));
      Assert.assertTrue(varCount == testant.getListOfNeurons().get(0).getUnitsSet().size());





    }
    System.out.println("---------------------------------------------------------------------------------------");
  }
}
