package org.nest.codegeneration.helpers.LEMSElements;

import org.nest.nestml._ast.ASTInputLine;
import org.nest.nestml._ast.ASTOutput;

/**
 * This class represents an input or output-port, an interface derived from buffers stated in the source-model.
 * This class has to be altered if the concept of current or spike-buffers became supported by LEMS.
 *
 * @author perun
 */
public class EventPort {
  private String name;

  private Direction dir;

  /**
   * Creates a port from an input-buffer of the source-model.
   *
   * @param variable an input-buffer
   */
  public EventPort(ASTInputLine variable) {
    name = variable.getName();
    dir = Direction.in;
  }

  /**
   * Creates a port from an output-buffer of the source-model.
   *
   * @param variable an output-buffer
   */
  public EventPort(ASTOutput variable) {
    if (variable.isCurrent()) {
      this.name = "current";
    }
    else {
      this.name = "spike";
    }
    dir = Direction.out;
  }

  public String getName() {
    return name;
  }

  public Direction getDirection() {
    return dir;
  }

  public enum Direction {
    in, out
  }
}
