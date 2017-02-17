/*
 * Copyright (c) 2015 RWTH Aachen. All rights reserved.
 *
 * http://www.se-rwth.de/
 */
package org.nest.nestml._cocos;

import de.se_rwth.commons.logging.Log;
import org.nest.nestml._ast.ASTBody;
import org.nest.nestml._ast.ASTNeuron;
import org.nest.nestml._ast.ASTOutput;

import java.util.List;

/**
 * Prohibits multiple output blocks
 *
 * @author (last commit) ippen, plotnikov
 * @since 0.0.1
 */
public class NeuronWithMultipleOrNoOutput implements NESTMLASTNeuronCoCo {

  public void check(ASTNeuron neuron) {
    ASTBody bodyDecorator = (neuron.getBody());
    final List<ASTOutput> outputs = bodyDecorator.getOutputs();

    if (outputs.size() == 0) {
      final String msg = NestmlErrorStrings.errorNoOutput(this);
      Log.error(msg, neuron.get_SourcePositionStart());

    }
    else if (outputs.size() >  1) {
      final String msg = NestmlErrorStrings.errorMultipleOutputs(this);
      Log.error(msg, neuron.get_SourcePositionStart());

    }

  }

}
