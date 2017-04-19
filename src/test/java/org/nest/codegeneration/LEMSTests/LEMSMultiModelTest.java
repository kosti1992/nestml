package org.nest.codegeneration.LEMSTests;

import org.junit.Test;
import org.nest.base.ModelbasedTest;
import org.nest.frontend.NestmlFrontend;

/**
 * Created by perun on 19.04.2017.
 */
public class LEMSMultiModelTest extends ModelbasedTest {
	private static final String INPUT_DIRECTORY = "models";
	NestmlFrontend fe = new NestmlFrontend();

	@Test
	public void testGenerateLEMS() throws Exception {
		String args[] = {INPUT_DIRECTORY,"-lems","-targetbuild"};
		fe.start(args);
	}
}
