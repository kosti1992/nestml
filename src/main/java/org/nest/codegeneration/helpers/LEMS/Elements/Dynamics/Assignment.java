package org.nest.codegeneration.helpers.LEMS.Elements.Dynamics;


import org.nest.codegeneration.helpers.LEMS.Elements.HelperCollection;
import org.nest.codegeneration.helpers.LEMS.Elements.LEMSCollector;
import org.nest.codegeneration.helpers.LEMS.Expressions.Expression;
import org.nest.codegeneration.helpers.LEMS.Expressions.LEMSSyntaxContainer;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This class stores a concrete instructions, namely an assignments.
 */
public class Assignment extends Instruction {
    private final String mClassIdentifier = "Assignment";//required by the backend
    private String mAssignedVariable = null;
    private Expression mAssignedValue = null;

    public Assignment(String assignedVariable, Expression assignedValue) {
        checkNotNull(assignedValue);
        checkNotNull(assignedVariable);
        this.mAssignedVariable = assignedVariable;
        this.mAssignedValue = assignedValue;
    }

    @SuppressWarnings("unused")//used in the template
    public String printAssignedVariable() {
        return this.mAssignedVariable;
    }

    @SuppressWarnings("unused")//used in the template
    public Expression getAssignedValue() {
        return this.mAssignedValue;
    }

    @SuppressWarnings("unused")//used in the template
    public String printAssignedValue() {
        if (this.mAssignedValue != null) {
            return this.mAssignedValue.print(new LEMSSyntaxContainer());
        }
        return "";
    }

    public void replaceConstantsWithReferences(LEMSCollector container) {
        this.mAssignedValue = HelperCollection.replaceConstantsWithReferences(container, this.mAssignedValue);
    }

    public void replaceResolutionByConstantReference(LEMSCollector container) {
        this.mAssignedValue = HelperCollection.replaceResolutionByConstantReference(container, this.mAssignedValue);
    }

    public String getClassIdentifier() {
        return mClassIdentifier;
    }
}