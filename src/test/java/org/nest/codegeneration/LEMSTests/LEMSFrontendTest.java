package org.nest.codegeneration.LEMSTests;

import org.junit.Test;
import org.nest.base.ModelbasedTest;
import org.nest.frontend.NestmlFrontend;

/**
 * Tests whether model can be generated without any critical errors (e.g. exceptions) and whether
 * the frontend works correctly.
 * @author perun
 */
public class LEMSFrontendTest extends ModelbasedTest {
  private static final String INPUT_DIRECTORY = "src/test/resources/codegeneration/LEMSTests";
  NestmlFrontend fe = new NestmlFrontend();

  @Test
  public void testGenerateLEMS() throws Exception {
    String args[] = {INPUT_DIRECTORY+"/izhikevich.nestml","-lems","-targetbuild",
        "-config"+INPUT_DIRECTORY+"/config.xml"};
    //String args[] = {INPUT_DIRECTORY+"/izhikevich.nestml","-lems","-targetbuild"};
    fe.start(args);
  }
}
