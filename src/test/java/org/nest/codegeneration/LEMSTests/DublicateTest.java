package org.nest.codegeneration.LEMSTests;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;
import org.nest.base.ModelbasedTest;
import org.nest.codegeneration.LEMSGenerator;
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
public class DublicateTest extends ModelbasedTest {
  private static final Path OUTPUT_DIRECTORY = Paths.get("target", "LEMS");

  private static final String PSC_MODEL_WITH_ODE = "src/test/resources/codegeneration/LEMSTests/izhikevichNested.nestml";
  private static final String INPUT_DIRECTORY = "src/test/resources/codegeneration/LEMSTests/config";
  @Test
  public void testGenerateLEMS() throws Exception {
    int varCount = 0;
    final ASTNESTMLCompilationUnit testModel = parseAndBuildSymboltable(PSC_MODEL_WITH_ODE);
    final LEMSGenerator testant = new LEMSGenerator();
    testant.generateLEMS(testModel, OUTPUT_DIRECTORY,Paths.get(INPUT_DIRECTORY+"/"),false,0.1);
    //add two constants and check whether duplicates have been recognized
    testant.getListOfNeurons().get(0).addConstant(new Constant("test", "none","test","test",true));
    varCount = testant.getListOfNeurons().get(0).getConstantsList().size();
    testant.getListOfNeurons().get(0).addConstant(new Constant("test", "none","test","test",true));
    Assert.assertTrue(varCount==testant.getListOfNeurons().get(0).getConstantsList().size());
    //add two derived elements and check whether duplicates have been recognized
    //testant.getListOfNeurons().get(0).addDerivedElement(new DerivedElement("test",
    //    Dimension.DimensionName.current,"test",false,false));
    varCount = testant.getListOfNeurons().get(0).getDerivedParametersList().size();
    //testant.getListOfNeurons().get(0).addDerivedElement(new DerivedElement("test",
    //    Dimension.DimensionName.current,"test",false,false));
    Assert.assertTrue(varCount==testant.getListOfNeurons().get(0).getDerivedParametersList().size());
    //add two units and check whether duplicates have been recognized
    testant.getListOfNeurons().get(0).addUnit(new Unit(new TypeSymbol("mV",TypeSymbol.Type.UNIT)));
    varCount = testant.getListOfNeurons().get(0).getUnitsSet().size();
    int dimCount = testant.getListOfNeurons().get(0).getDimensionsSet().size();
    testant.getListOfNeurons().get(0).addUnit(new Unit(new TypeSymbol("mV",TypeSymbol.Type.UNIT)));
    Assert.assertTrue(varCount==testant.getListOfNeurons().get(0).getUnitsSet().size());
    Assert.assertTrue(dimCount==testant.getListOfNeurons().get(0).getDimensionsSet().size());
    //dimensions are made implicitly -> check whether two dimensions are equal
    //add two units and check whether duplicates have been recognized
    //testant.getListOfNeurons().get(0).addStateVariable(
    //    new StateVariable("test", Dimension.DimensionName.none,"0","mV",testant.getListOfNeurons().get(0)));
    varCount = testant.getListOfNeurons().get(0).getStateVariablesList().size();
    //testant.getListOfNeurons().get(0).addStateVariable(
    //    new StateVariable("test", Dimension.DimensionName.none,"0","mV",testant.getListOfNeurons().get(0)));
    Assert.assertTrue(varCount==testant.getListOfNeurons().get(0).getStateVariablesList().size());
  }
}
