package org.nest.codegeneration.LEMSTests.ModelTests;

import org.junit.Test;
import org.nest.base.ModelbasedTest;
import org.nest.frontend.NestmlFrontend;

/**
 * Created by kperun on 06.06.17.
 */
public class aeif_cond_alpha_neuron_test extends ModelbasedTest{
    private static final String INPUT_DIRECTORY = "models";
    NestmlFrontend fe = new NestmlFrontend();

    @Test
    public void testGenerateLEMS() throws Exception {
        String args[] = {INPUT_DIRECTORY+"/aeif_cond_alpha.nestml","-lems","-targetbuild"};
        fe.start(args);
    }
}
