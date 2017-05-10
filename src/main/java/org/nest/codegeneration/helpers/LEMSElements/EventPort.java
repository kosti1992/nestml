package org.nest.codegeneration.helpers.LEMSElements;

import org.nest.nestml._ast.ASTInputLine;
import org.nest.nestml._ast.ASTOutput;
import org.w3c.dom.Node;

/**
 * This class represents an input or output-port, an interface derived from buffers stated in the source-model.
 * This class stores only abstract interfaces without any values. A concrete buffer is transformed to a externally
 * derived variable.
 *
 * @author perun
 */
public class EventPort extends LEMSElement{
  private String mName;
  private Direction mDir;

  /**
   * Creates a port from an input-buffer of the source-model.
   *
   * @param _variable an input-buffer
   */
  public EventPort(ASTInputLine _variable) {
    this.mName = _variable.getName();
    this.mDir = Direction.in;
  }


	/**
     * This method can be used to generate an event port from a xml node.
     * @param _xmlNode the event port xml node.
     */
  public EventPort(Node _xmlNode){
    this.mName = _xmlNode.getAttributes().getNamedItem("name").getNodeValue();
    if(_xmlNode.getAttributes().getNamedItem("direction").getNodeValue().equals("in")){
      this.mDir = Direction.in;
    }
    else {
      this.mDir = Direction.out;
    }
  }

  /**
   * Creates a port from an output-buffer of the source-model.
   *
   * @param _variable an output-buffer
   */
  public EventPort(ASTOutput _variable) {
    if (_variable.isCurrent()) {
      this.mName = "current";
    }
    else {
      this.mName = "spike";
    }
    mDir = Direction.out;
  }

  public String getName() {
    return this.mName;
  }

  public Direction getDirection() {
    return this.mDir;
  }

  public enum Direction {
    in, out
  }
}
