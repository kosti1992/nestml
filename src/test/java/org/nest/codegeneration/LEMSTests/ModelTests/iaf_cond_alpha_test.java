package org.nest.codegeneration.LEMSTests.ModelTests;

import org.junit.Test;
import org.nest.base.ModelbasedTest;
import org.nest.frontend.NestmlFrontend;


/**
 * Created by kperun on 10.05.17.
 */
public class iaf_cond_alpha_test extends ModelbasedTest {
    private static final String INPUT_DIRECTORY = "models";
    NestmlFrontend fe = new NestmlFrontend();

    @Test
    public void testGenerateLEMS() throws Exception {
        String args[] = {INPUT_DIRECTORY+"/iaf_cond_alpha.nestml","-lems","-targetbuild"};
        fe.start(args);
    }

}
