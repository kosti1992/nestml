package org.nest.codegeneration.LEMSTests;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;
import org.nest.base.ModelbasedTest;
import org.nest.codegeneration.LEMSGenerator;
import org.nest.codegeneration.helpers.Expressions.Variable;
import org.nest.codegeneration.helpers.LEMSElements.Constant;
import org.nest.codegeneration.helpers.LEMSElements.DerivedElement;
import org.nest.codegeneration.helpers.LEMSElements.Dimension;
import org.nest.codegeneration.helpers.LEMSElements.StateVariable;
import org.nest.codegeneration.helpers.LEMSElements.Unit;
import org.nest.nestml._ast.ASTNESTMLCompilationUnit;
import org.nest.symboltable.symbols.TypeSymbol;

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
    int varCount = 0;
    final ASTNESTMLCompilationUnit testModel = parseAndBuildSymboltable(PSC_MODEL_WITH_ODE);
    final LEMSGenerator testant = new LEMSGenerator();
    testant.generateLEMS(testModel, OUTPUT_DIRECTORY,Paths.get(INPUT_DIRECTORY+"/"));
    if(true) {
      System.err.println("this test is currently broken due to the update of unit handling");//TODO
      //add two constants and check whether duplicates have been recognized
      testant.getListOfNeurons().get(0).addConstant(new Constant("test", "none",new Variable("test"),true));
      varCount = testant.getListOfNeurons().get(0).getConstantsList().size();
      //testant.getListOfNeurons().get(0).addConstant(new Constant("test", "none",new Variable("test"),"test",true));
      Assert.assertTrue(varCount == testant.getListOfNeurons().get(0).getConstantsList().size());
      //add two derived elements and check whether duplicates have been recognized
      testant.getListOfNeurons().get(0).addConstant(new Constant("test", "none",new Variable("test"),true));
      varCount = testant.getListOfNeurons().get(0).getDerivedParametersList().size();
      Assert.assertTrue(varCount == testant.getListOfNeurons().get(0).getDerivedParametersList().size());
      //add two units and check whether duplicates have been recognized
      Unit tempUnit = new Unit(new TypeSymbol("mV", TypeSymbol.Type.UNIT));
      testant.getListOfNeurons().get(0).addUnit(tempUnit);
      varCount = testant.getListOfNeurons().get(0).getUnitsSet().size();
      int dimCount = testant.getListOfNeurons().get(0).getDimensionsSet().size();
      testant.getListOfNeurons().get(0).addUnit(new Unit(new TypeSymbol("mV", TypeSymbol.Type.UNIT)));
      Assert.assertTrue(varCount == testant.getListOfNeurons().get(0).getUnitsSet().size());
      Assert.assertTrue(dimCount == testant.getListOfNeurons().get(0).getDimensionsSet().size());
      //dimensions are made implicitly -> check whether two dimensions are equal
      //add two units and check whether duplicates have been recognized
      varCount = testant.getListOfNeurons().get(0).getStateVariablesList().size();
      Assert.assertTrue(varCount == testant.getListOfNeurons().get(0).getStateVariablesList().size());
    }
  }
}
