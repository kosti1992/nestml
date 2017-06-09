package org.nest.codegeneration.helpers.LEMS.Elements.Dynamics;

/**
 * An instruction superclass used required in order to store all types of instructions in a single list.
 */
public abstract class Instruction {
    private String mClassIdentifier;//each instruction has to provide an identifier for the backend

    public abstract String getClassIdentifier();

}
