package org.nest.codegeneration.helpers.LEMSElements;


import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.nest.codegeneration.helpers.Expressions.Expression;
import org.nest.codegeneration.helpers.Expressions.LEMSSyntaxContainer;

/**
 * This class stores a block which will be evoked during the simulation on a certain condition. A condtional block is
 * a part of the dynamic routine.
 *
 * @author perun
 */
public class ConditionalBlock {
	//a conditional block consists of instructions
	private List<DynamicRoutine.Instruction> instructions;
	private Expression condition;
	private String rawCode;//stores a header printed next to the block

	public ConditionalBlock(List<DynamicRoutine.Instruction> instructions, Expression condition, String rawCode) {
		this.instructions = instructions;
		this.condition = condition;
		this.rawCode = rawCode;
	}

	public ConditionalBlock(DynamicRoutine.Instruction instruction,Expression condition, String rawCode){
		this.instructions = new ArrayList<>();
		this.instructions.add(instruction);
		this.condition = condition;
		this.rawCode = rawCode;
	}


	/**
	 * Returns the header of a conditional block, i.e. a string representation of the code as stated in the source model.
	 * The header is not a part of the actual mode, but benefits the overall readability of the model.
	 * @return an array String objects which represent line by line the source code
	 */
	@SuppressWarnings("unused")//used in the template
	public Object[] getInitialCode() {
		if(rawCode == null){
			return new String[0];
		}
		BufferedReader bufReader = new BufferedReader(new StringReader(rawCode));
		return bufReader.lines().toArray();
	}

	@SuppressWarnings("unused")//used in the template
	public Expression getCondition() {
		return this.condition;
	}

	@SuppressWarnings("unused")//used in the template
	public String printCondition() {
		return condition.print(new LEMSSyntaxContainer());
	}

	@SuppressWarnings("unused")//used in the template
	public List<DynamicRoutine.Instruction> getInstructions() {
		return this.instructions;
	}

	@SuppressWarnings("unused")//used in the template
	public String getInstructionType(DynamicRoutine.Instruction instr) {
		if (instr.getClass().equals(DynamicRoutine.Assignment.class)) {
			return "Assignment";
		}
		if (instr.getClass().equals(DynamicRoutine.FunctionCall.class)) {
			return "FunctionCall";
		} else {
			return "Class not defined";
		}

	}
}
