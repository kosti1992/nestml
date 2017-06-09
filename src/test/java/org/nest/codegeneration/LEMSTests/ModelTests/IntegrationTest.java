package org.nest.codegeneration.LEMSTests.ModelTests;

import org.junit.Test;
import org.nest.base.ModelbasedTest;
import org.nest.frontend.NestmlFrontend;

/**
 * Created by kperun on 07.06.17.
 */
public class IntegrationTest extends ModelbasedTest {
    private static final String INPUT_DIRECTORY = "lemsmodels";
    NestmlFrontend fe = new NestmlFrontend();

    @Test
    public void testGenerateLEMS() throws Exception {
        String args[] = {INPUT_DIRECTORY,"-lems","-targetbuild"};
        fe.start(args);
    }
}
