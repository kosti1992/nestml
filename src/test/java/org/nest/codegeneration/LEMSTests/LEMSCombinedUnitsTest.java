package org.nest.codegeneration.LEMSTests;

import org.junit.Assert;
import org.junit.Test;
import org.nest.base.ModelbasedTest;
import org.nest.codegeneration.LEMSGenerator;
import org.nest.nestml._ast.ASTNESTMLCompilationUnit;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by perun on 20.03.2017.
 */
public class LEMSCombinedUnitsTest extends ModelbasedTest {
	private static final Path OUTPUT_DIRECTORY = Paths.get("target", "LEMS");
	private static final String PSC_MODEL_WITH_ODE =
			"src/test/resources/codegeneration/LEMSTests/LEMSCombinedUnitsTest/izhikevich_comb_units.nestml";
	//an artifact is not required, but we still provide it to execute the test
	private static final String INPUT_DIRECTORY = "src/test/resources/codegeneration/LEMSTest/";

	@Test
	public void testGenerateLEMS() throws Exception {
		final ASTNESTMLCompilationUnit testModel = parseAndBuildSymboltable(PSC_MODEL_WITH_ODE);
		final LEMSGenerator testant = new LEMSGenerator();
		testant.generateLEMS(testModel, OUTPUT_DIRECTORY, Paths.get(INPUT_DIRECTORY + "/"));

		System.out.println(testant.getListOfNeurons().get(0).getConstantsList().get(6).getDimension());
		Assert.assertEquals("3", testant.getListOfNeurons().get(0).getConstantsList().get(4).getDimension());
	}


}
