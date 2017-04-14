package org.nest.codegeneration.helpers.LEMSElements;

import static com.google.common.base.Preconditions.checkNotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import de.monticore.emf._ast.ASTECNode;
import de.monticore.types.types._ast.ASTQualifiedName;
import org.nest.codegeneration.helpers.Expressions.Expression;
import org.nest.codegeneration.helpers.Expressions.Function;
import org.nest.codegeneration.helpers.Expressions.LEMSSyntaxContainer;
import org.nest.codegeneration.helpers.Expressions.NumericalLiteral;
import org.nest.codegeneration.helpers.Expressions.Operator;
import org.nest.codegeneration.helpers.Expressions.Variable;
import org.nest.commons._ast.ASTExpr;
import org.nest.commons._ast.ASTFunctionCall;
import org.nest.nestml._ast.ASTDynamics;
import org.nest.nestml._ast.ASTFunction;
import org.nest.spl._ast.ASTBlock;
import org.nest.spl._ast.ASTCompound_Stmt;
import org.nest.spl._ast.ASTELIF_Clause;
import org.nest.spl._ast.ASTSmall_Stmt;
import org.nest.spl._ast.ASTStmt;
import org.nest.spl.prettyprinter.SPLPrettyPrinter;
import org.nest.spl.prettyprinter.SPLPrettyPrinterFactory;

/**
 * This class represents a transformed representation of the dynamic routine extracted from the source-model
 * in order to generate corresponding conditional blocks.
 *
 * @author perun
 */
public class DynamicRoutine {
	private List<ConditionalBlock> blocks;//A List of all states of the automaton.
	private LEMSCollector container;//required in order to use the same pretty printer as in other parts of transformation

	public DynamicRoutine(List<ASTDynamics> input, LEMSCollector container) {
		this.blocks = new ArrayList<>();
		this.container = container;
		//currently, only one dynamics block can be present, however, NESTML deals with it by means of a List
		for (ASTDynamics dyn : input) {//for all dynamic blocks in the model
			//for all outer statements in the model
			dyn.getBlock().getStmts().forEach(this::handleStatement);
		}
	}

	/**
	 * Handles a given statement by evoking an adequate subroutine.
	 *
	 * @param input the processed AST statement.
	 */
	private void handleStatement(ASTStmt input) {
		checkNotNull(input);
		if (input.small_StmtIsPresent()) {
			//a simple statement can be processed directly
			List<Instruction> tempList = new ArrayList<>();
			if (input.getSmall_Stmt().get().getFunctionCall().isPresent()) {
				List<Instruction> notNullCheck = handleFunctionCall(input.getSmall_Stmt().get().getFunctionCall().get());
				if (!notNullCheck.isEmpty()) {
					//some function calls do not generate an instruction, thus it is not required to add null to the list
					tempList.addAll(notNullCheck);
				}
			} else {
				tempList.addAll(handleSmallStatement(input.getSmall_Stmt().get()));
			}
			//generate a description header
			SPLPrettyPrinter tempPrettyPrinter = SPLPrettyPrinterFactory.createDefaultPrettyPrinter();
			tempPrettyPrinter.print(input);
			String rawCodeTemp = this.buildHeader(input, tempPrettyPrinter.result());
			if (tempList.size() > 0) {//generate a new block which shall be always executed, thus cond=TRUE
				this.blocks.add(new ConditionalBlock(tempList, Expression.generateTrue(), rawCodeTemp));
			}
		} else if (input.compound_StmtIsPresent()) {
			handleCompoundStatement(input.getCompound_Stmt().get(), null);
		} else {
			System.err.println("Not supported type of statement in line " + input.get_SourcePositionStart());
		}
	}

	/**
	 * Handles a given compound statement and converts it to a corresponding regime.
	 *
	 * @param input a compound statement, i.e a block
	 */
	private void handleCompoundStatement(ASTCompound_Stmt input, Expression condition) {
		List<ASTExpr> tempList = new ArrayList<>();//used for generating the final else block by concatenating all conditions
		//printer used to print a header
		SPLPrettyPrinter tempPrettyPrinter = SPLPrettyPrinterFactory.createDefaultPrettyPrinter();
		Expression tempCondition = new Expression();

		if (input.getIF_Stmt().isPresent()) {//this block is an if-block
			//process the if statement
			tempList.add(input.getIF_Stmt().get().getIF_Clause().getExpr());//store the if-condition

			if (condition != null) {//required in order to process nested blocks
				Operator tempOp = new Operator();
				tempOp.setLogicalAnd(true);
				Expression tempRhs = Expression.encapsulateInBrackets(new Expression(input.getIF_Stmt().get().getIF_Clause().getExpr()));
				tempCondition.replaceLhs(condition);
				tempCondition.replaceRhs(tempRhs);
				tempCondition.replaceOp(tempOp);
				tempCondition = container.getHelper().replaceBooleanAtomByExpression(container, tempCondition);
				tempCondition = Expression.encapsulateInBrackets(tempCondition);
			} else {
				tempCondition = new Expression(input.getIF_Stmt().get().getIF_Clause().getExpr());
				tempCondition = container.getHelper().replaceBooleanAtomByExpression(container, tempCondition);
				tempCondition = Expression.encapsulateInBrackets(tempCondition);
			}
			tempPrettyPrinter.print(input.getIF_Stmt().get().getIF_Clause());
			//store a new conditional block
			tempCondition = container.getHelper().replaceConstantsWithReferences(container, tempCondition);
			tempCondition = container.getHelper().replaceResolutionByConstantReference(container, tempCondition);
			tempCondition = container.getHelper().replaceNotByLogicalEquivalent(container, tempCondition);
			handleASTBlock(input.getIF_Stmt().get().getIF_Clause().getBlock(), tempCondition, tempPrettyPrinter.result());
		}
		//process all elif statements

		for (ASTELIF_Clause clause : input.getIF_Stmt().get().getELIF_Clauses()) {
			List<ASTExpr> copYofTempList = new ArrayList<>(tempList);
			tempList.add(clause.getExpr());//store each elif condition
			tempPrettyPrinter = SPLPrettyPrinterFactory.createDefaultPrettyPrinter();
			tempPrettyPrinter.print(clause);//collect raw code for the header
			tempCondition = Expression.encapsulateInBrackets(new Expression(clause.getExpr()));
			Expression tExpr = new Expression();
			Operator opr = new Operator();
			opr.setLogicalAnd(true);
			tExpr.replaceLhs(this.buildElseCondition(copYofTempList));
			tExpr.replaceOp(opr);
			tExpr.replaceRhs(tempCondition);
			tempCondition = tExpr;

			if (condition != null) {
				Expression tempExpr = new Expression();
				tempExpr.replaceLhs(condition);
				tempExpr.replaceRhs(tempCondition);
				Operator newOp = new Operator();
				newOp.setLogicalAnd(true);
				tempExpr.replaceOp(newOp);
				tempCondition = tempExpr;
			}
			tempCondition = container.getHelper().replaceConstantsWithReferences(container, tempCondition);
			tempCondition = container.getHelper().replaceResolutionByConstantReference(container, tempCondition);
			tempCondition = Expression.encapsulateInBrackets(tempCondition);//finally encapsulate everything in brackets
			handleASTBlock(clause.getBlock(), tempCondition, tempPrettyPrinter.result());
		}

		//process the else statement
		if (input.getIF_Stmt().get().getELSE_Clause().isPresent()) {
			tempPrettyPrinter = SPLPrettyPrinterFactory.createDefaultPrettyPrinter();
			//collect raw code for the header
			tempPrettyPrinter.print(input.getIF_Stmt().get().getELSE_Clause().get());
			//now generate a proper condition
			tempCondition = this.buildElseCondition(tempList);
			if (condition != null) {
				Expression tempExpr = new Expression();
				tempExpr.replaceLhs(condition);
				tempExpr.replaceRhs(tempCondition);
				Operator newOp = new Operator();
				newOp.setLogicalAnd(true);
				tempCondition = tempExpr;
			}
			//create the corresponding block
			tempCondition = container.getHelper().replaceConstantsWithReferences(container, tempCondition);
			tempCondition = container.getHelper().replaceResolutionByConstantReference(container, tempCondition);
			tempCondition = Expression.encapsulateInBrackets(tempCondition);
			handleASTBlock(input.getIF_Stmt().get().getELSE_Clause().get().getBlock(),
					tempCondition, tempPrettyPrinter.result());
		}

		//TODO: are these blocks really not supported?
		else if (input.getWHILE_Stmt().isPresent()) {//the block is a while block-> not supported yet
			System.err.println("WHILE blocks are not yet supported.");
		} else if (input.getFOR_Stmt().isPresent()) {//the block is a for block-> not supported yet
			System.err.println("FOR blocks are not yet supported.");
		}
	}

	/**
	 * Handles a block of instructions consisting of e.g. assignments, function-calls and further blocks.
	 *
	 * @param input block which will be processed
	 */
	private void handleASTBlock(ASTBlock input, Expression condition, String rawCode) {
		List<Instruction> tempInstruction = new ArrayList<>();
		List<ConditionalBlock> tempBlocks = null;
		//generate a description header
		String rawCodeTemp = this.buildHeader(input, rawCode);
		//iterate over all statements in the block
		for (ASTStmt stmt : input.getStmts()) {
			//if a compound block has been found, generate a cond. block for all previously found directives if required
			if (stmt.compound_StmtIsPresent()) {
				if (tempInstruction.size() > 0) {
					tempInstruction = deactivateIntegration(tempInstruction);
					this.blocks.add(new ConditionalBlock(tempInstruction, condition, rawCodeTemp));//add a new condition
				}
				tempInstruction = new ArrayList<>();//delete all processed statements in order to avoid duplicates
				handleCompoundStatement(stmt.getCompound_Stmt().get(), condition);
				//now the ternary operator requires extra handling by generating an intermediate cond. block
			} else if (stmt.small_StmtIsPresent() && stmt.getSmall_Stmt().get().assignmentIsPresent()
					&& stmt.getSmall_Stmt().get().getAssignment().get().getExpr().conditionIsPresent()) {
				if (tempInstruction.size() > 0) {
					tempInstruction = deactivateIntegration(tempInstruction);
					this.blocks.add(new ConditionalBlock(tempInstruction, condition, rawCodeTemp));//add a new condition
				}
				tempInstruction = new ArrayList<>();
				this.blocks.addAll(handleTernaryOperator(stmt.getSmall_Stmt().get(), condition));

			} else if (stmt.small_StmtIsPresent()) {
				List<Instruction> notNullCheck = handleSmallStatement(stmt.getSmall_Stmt().get());
				if (!notNullCheck.isEmpty()) {
					tempInstruction.addAll(notNullCheck);
				}
			} else {
				System.err.println("Error in if-processing. Neither small nor compound statement found.");
			}
		}
		//if no "integrate" directives have been found in this block but there exist some local "integrates", we
		//have to deactivate it in this block in order to stop the integration
		tempInstruction = deactivateIntegration(tempInstruction);
		//blocks without any instructions can be skipped
		if (tempInstruction != null && tempInstruction.size() > 0) {
			this.blocks.add(new ConditionalBlock(tempInstruction, condition, rawCodeTemp));
		}
	}

	/**
	 * Handles a simple statement, e.g assignment of function-call.
	 *
	 * @param input a statement
	 * @return a instruction encapsulated in a object
	 */
	private List<Instruction> handleSmallStatement(ASTSmall_Stmt input) {
		List<Instruction> res = new ArrayList<>();
		if (input.assignmentIsPresent()) {
			//check if not supported functions are part of the assignment
			if (container.getHelper().containsFunctionCall(input.getAssignment().get().getExpr(), true)) {
				//Generate a proper error message
				container.getHelper().printNotSupportedFunctionInBlock(input);
				//now generate a expression which indicates that it is not supported
				Expression tempExpression = new Expression(input.getAssignment().get().getExpr());
				tempExpression = tempExpression.setNotSupported();
				//return a corresponding assignment
				Assignment retAssignment = new Assignment(input.getAssignment().get().getLhsVarialbe().getName().toString(), tempExpression);
				retAssignment.replaceConstantsWithReferences(container);
				retAssignment.replaceResolutionByConstantReference(container);
				res.add(retAssignment);
				return res;
			} else {
				Expression tempExpression = new Expression(input.getAssignment().get().getExpr());
				tempExpression = container.getHelper().replaceResolutionByConstantReference(container, tempExpression);
				Expression ret = new Expression();
				Variable tempVar = new Variable(input.getAssignment().get().getLhsVarialbe().getName().toString());
				Operator tempOp = new Operator();
				ret.replaceRhs(tempVar);
				ret.replaceLhs(tempExpression);
				Assignment retAssignment;
				//in order to process assignments of type x-=y
				if (input.getAssignment().get().isCompoundMinus()) {
					tempOp.setMinusOp(true);
					ret.replaceOp(tempOp);
					ret = container.getHelper().replaceConstantsWithReferences(container, ret);
					retAssignment = new Assignment(input.getAssignment().get().getLhsVarialbe().getName().toString(), ret);
				}//in order to process assignments of type x*=y
				else if (input.getAssignment().get().isCompoundProduct()) {
					tempOp.setTimesOp(true);
					ret.replaceOp(tempOp);
					ret = container.getHelper().replaceConstantsWithReferences(container, ret);
					retAssignment = new Assignment(input.getAssignment().get().getLhsVarialbe().getName().toString(), ret);
				}//in order to process assignments of type x+=y
				else if (input.getAssignment().get().isCompoundSum()) {
					tempOp.setPlusOp(true);
					ret.replaceOp(tempOp);
					ret = container.getHelper().replaceConstantsWithReferences(container, ret);
					retAssignment = new Assignment(input.getAssignment().get().getLhsVarialbe().getName().toString(), ret);
				}//in order to process assignments of type x/=y
				else if (input.getAssignment().get().isCompoundQuotient()) {
					tempOp.setDivOp(true);
					ret.replaceOp(tempOp);
					ret = container.getHelper().replaceConstantsWithReferences(container, ret);
					retAssignment = new Assignment(input.getAssignment().get().getLhsVarialbe().getName().toString(), ret);
				} else {
					ret = container.getHelper().replaceConstantsWithReferences(container, new Expression(input.getAssignment().get().getExpr()));
					retAssignment = new Assignment(input.getAssignment().get().getLhsVarialbe().getName().toString(), ret);
				}
				retAssignment.replaceResolutionByConstantReference(container);
				res.add(retAssignment);
				return res;
			}
		}
		if (input.functionCallIsPresent()) {
			return handleFunctionCall(input.getFunctionCall().get());
		}
		System.err.println("Something went wrong in small statement processing." +
				" Position in source: " + input.get_SourcePositionStart());
		res.add(new Assignment("", null));
		return res;
	}

	/**
	 * This functions evokes further processing of a given function call. This method is a point of extension whenever
	 * new function processing subroutines have to be included.
	 *
	 * @param functionCall the function call.
	 * @return a instruction which states steps steps need to be done
	 */
	private List<Instruction> handleFunctionCall(ASTFunctionCall functionCall) {
		checkNotNull(functionCall);
		switch (functionCall.getName().toString()) {
			case "integrate":
				return this.handleIntegrate(functionCall);
			case "integrate_odes":
				return this.handleIntegrate_ode();
			case "emit_spike":
				return this.handleEmitSpike(functionCall);
			default:
				this.container.addNotConverted("Not supported function call "
						+ functionCall.getName().toString() + " in lines "
						+ functionCall.get_SourcePositionStart() + " to " + functionCall.get_SourcePositionEnd());
				//System.err.println("Not supported function call found.");
				return null;
		}
	}

	/**
	 * Emit spike calls are simply transformed to a new function call.
	 *
	 * @param functionCall an function call containing "emitSpike()"
	 * @return a new function call which imitates the spike emission
	 */
	private List<Instruction> handleEmitSpike(ASTFunctionCall functionCall) {
		List<Instruction> res = new ArrayList<>();
		res.add(new FunctionCall(functionCall));
		return res;
	}


	/**
	 * For a given conditional block and an assignment inside which uses the ternary operator, this method
	 * replaces it by means of two sub blocks with corresponding conditions.
	 *
	 * @param input     the input small statement with ternary operator
	 * @param condition the condition, if one is present, of the super block containing the assignment
	 * @return the list containing two conditions
	 */
	private List<ConditionalBlock> handleTernaryOperator(ASTSmall_Stmt input, Expression condition) {
		List<ConditionalBlock> ret = new ArrayList<>();
		//first create the first part of the expression, namely the one which applies if condition is true
		Expression firstSubCondition = new Expression(input.getAssignment().get().getExpr().getCondition().get());
		firstSubCondition = Expression.encapsulateInBrackets(firstSubCondition);
		Operator opFirst = new Operator();
		opFirst.setLogicalAnd(true);
		Expression firstCondition = new Expression();
		firstCondition.replaceLhs(condition.deepClone());
		firstCondition.replaceOp(opFirst);
		firstCondition.replaceRhs(firstSubCondition);
		firstCondition = container.getHelper().replaceBooleanAtomByExpression(container, firstCondition);
		//now generate an assignment for the first half
		Expression firstAssignmentExpression = new Expression(input.getAssignment().get().getExpr().getIfTrue().get());
		firstAssignmentExpression = container.getHelper().replaceConstantsWithReferences(container, firstAssignmentExpression);
		firstAssignmentExpression = container.getHelper().replaceResolutionByConstantReference(container, firstAssignmentExpression);
		Assignment firstAssignment = new Assignment(input.getAssignment().get().getLhsVarialbe().getName().toString(),
				firstAssignmentExpression);
		ConditionalBlock firstBlock = new ConditionalBlock(firstAssignment, firstCondition, null);
		ret.add(firstBlock);

		//now create the second part which applies if the condition is not true
		Expression secondSubCondition = new Expression(input.getAssignment().get().getExpr().getCondition().get());
		secondSubCondition = container.getHelper().replaceBooleanAtomByExpression(container, secondSubCondition);
		secondSubCondition.negateLogic();
		secondSubCondition = Expression.encapsulateInBrackets(secondSubCondition);
		Operator opSecond = new Operator();
		opSecond.setLogicalAnd(true);
		Expression secondCondition = new Expression();
		secondCondition.replaceLhs(condition.deepClone());
		secondCondition.replaceOp(opSecond);
		secondCondition.replaceRhs(secondSubCondition);
		//now generate an assignment for the second half
		Expression secondAssignmentExpression = new Expression(input.getAssignment().get().getExpr().getIfNot().get());
		secondAssignmentExpression = container.getHelper().replaceConstantsWithReferences(container, secondAssignmentExpression);
		secondAssignmentExpression = container.getHelper().replaceResolutionByConstantReference(container, secondAssignmentExpression);
		Assignment secondAssignment = new Assignment(input.getAssignment().get().getLhsVarialbe().getName().toString(),
				secondAssignmentExpression);
		ConditionalBlock secondBlock = new ConditionalBlock(secondAssignment, secondCondition, null);
		ret.add(secondBlock);
		return ret;
	}


	/**
	 * Checks a given set of instructions for existence of integrate directives. If non found,
	 * a corresponding assignment which deactivates the integration in this step is generated.
	 *
	 * @param list a list of instructions
	 * @return a possibly modified list of instructions
	 */
	private List<Instruction> deactivateIntegration(List<Instruction> list) {
		if (list.isEmpty()) {
			return list;//in order to avoid cond blocks which only consists of the deactivate directive
		}
		boolean temp;
		//check all local dime derivatives
		for (String var : container.getLocalTimeDerivative()) {
			temp = false;
			//for all elements in the list, check if an integration directive has been found
			for (Instruction call : list) {
				if (call.getClass().equals(Assignment.class) &&
						((Assignment) call).printAssignedVariable().equals(container.getHelper().PREFIX_ACT + var)) {
					temp = true;
				}
			}
			//add a deactivation assignment to the list of directives if no integrate directive has been found
			if (!temp) {
				NumericalLiteral tempLiteral = new NumericalLiteral(0, null);
				list.add(new Assignment(container.getHelper().PREFIX_ACT + var, tempLiteral));
			}
		}
		return list;
	}

	/**
	 * Generates a "integrate" counter piece in the target modeling language by replacing it with assignments.
	 *
	 * @param functionCall a integrate function call
	 * @return an instruction list which represents the integrate directive consisting of a single instruction
	 */
	private List<Instruction> handleIntegrate(ASTFunctionCall functionCall) {
		//add a new state variable which symbolize that integration should be activated in necessary
		if (functionCall.getArgs().size() != 1) {
			System.err.println("Integrate is wrongly declared, <>1 arguments provided!");
			return null;
		} else {
			for (StateVariable var : container.getStateVariablesList()) {
				if (var.getName().equals(container.getHelper().PREFIX_ACT + functionCall.getArgs().get(0).getVariable().get().getName().toString())) {
					((NumericalLiteral) var.getDefaultValue().get()).setValue(0);
				}
			}
			//integrate the corresponding variable in this block
			NumericalLiteral tempLiteral = new NumericalLiteral(1, null);
			//the method requires a list of instructions rather than a single instruction
			List<Instruction> res = new ArrayList<>();
			res.add(new Assignment(container.getHelper().PREFIX_ACT +
					functionCall.getArgs().get(0).getVariable().get().getName().toString(), tempLiteral));
			return res;
		}
	}

	/**
	 * Handles the integrate_odes() function calls and generates a set of activator assignments.
	 * @return a list of instructions
	 */
	private List<Instruction> handleIntegrate_ode() {
		//since all variables have to be integrated, we create a list of integrate instructions
		List<Instruction> res = new ArrayList<>();
		NumericalLiteral tempLiteral;
		for (String var : container.getEquations().keySet()) {
			//integrate the corresponding variable in this block
			tempLiteral = new NumericalLiteral(1, null);
			res.add(new Assignment(container.getHelper().PREFIX_ACT +var, tempLiteral));
			//moreover, since a integrate_odes function call has been found, we make all integrations local
			container.addLocalTimeDerivative(var);
		}
		return res;
	}

	/**
	 * For a given list of ASTExpressions, this method build an else condtion by
	 * negating all stated condition and combining them by AND-operator.
	 *
	 * @param list a list of ASTExpressions
	 * @return a Expression object representing the else condition
	 */
	private Expression buildElseCondition(List<ASTExpr> list) {
		if (!list.isEmpty()) {
			Expression tempExpr = Expression.encapsulateInBrackets(new Expression(list.get(0)));
			tempExpr.negateLogic();
			Expression combExpr = new Expression();
			if (list.size() <= 1) {
				return tempExpr;
			}
			if (list.size() > 1) {
				combExpr.replaceLhs(tempExpr);
				Operator tempOp = new Operator();
				tempOp.setLogicalAnd(true);
				combExpr.replaceOp(tempOp);
				list.remove(0);
				combExpr.replaceRhs(this.buildElseCondition(list));
			}
			return combExpr;
		}
		return new Expression();
	}

	/**
	 * This method is called whenever it is required to generate a proper header
	 * for a block of the dynamic routine.
	 *
	 * @param input   a node whose code is processed
	 * @param rawCode the raw source code which will be printed
	 * @return
	 */
	private String buildHeader(ASTECNode input, String rawCode) {
		String rawCodeTemp =
				"Generated from source lines " + input.get_SourcePositionStart().toString() +
						" to " + input.get_SourcePositionEnd().toString() + ".\n";
		rawCodeTemp = rawCodeTemp + rawCode;
		rawCodeTemp = rawCodeTemp.trim();
		rawCodeTemp = rawCodeTemp.replaceAll("\\n\\s*\\n", "\n");//kill empty lines
		return rawCodeTemp;
	}

	/**
	 * There is currently a bug with jLEMS which requires that each output port has to be stated with a
	 * corresponding EventOut directive. This method adds a new block which is never invoked but provides
	 * such an EventOut directive.
	 */
	public void addPortActivator() {
		FunctionCall functionCall = new FunctionCall("emit_spike", null);
		ArrayList<Instruction> instructionArrayList = new ArrayList<>();
		instructionArrayList.add(functionCall);
		String rawCode = "This is an artificial EventOut which is never used,\n but required by LEMS to regard out-ports.";
		ConditionalBlock block = new ConditionalBlock(instructionArrayList, Expression.generateFalse(), rawCode);
		this.blocks.add(block);
	}

	@SuppressWarnings("unused")//DebugMethod
	private void printHashMap(LinkedHashMap<String, String> input) {
		for (String key : input.keySet()) {
			System.out.println(key + "=" + input.get(key));
		}
	}

	/**
	 * Returns a list of all instructions in all blocks.
	 *
	 * @return a list of instruction objects
	 */
	public List<Instruction> getAllInstructions() {
		ArrayList<Instruction> ret = new ArrayList<>();
		for (ConditionalBlock block : this.blocks) {
			ret.addAll(block.getInstructions());
		}
		return ret;
	}

	@SuppressWarnings("unused")//DebugMethod
	private void printHashSet(HashSet<String> input) {
		for (String key : input) {
			System.out.println("Calls: " + key);
		}
	}

	public List<ConditionalBlock> getConditionalBlocks() {
		return this.blocks;
	}

	/**
	 * This method casts a given instruction to a assignment call and is only used in the
	 * template.
	 */
	@SuppressWarnings("unused")//used in the template
	public Assignment getAssignmentFromInstruction(Instruction instr) {
		return (Assignment) instr;
	}

	/**
	 * This method casts a given instruction to a function call and  is only used in the template.
	 */
	@SuppressWarnings("unused")//used in the template
	public FunctionCall getFunctionCallFromInstruction(Instruction instr) {
		return (FunctionCall) instr;
	}

	/**
	 * An instruction superclass used required in order to store all types of instructions in a single list.
	 */
	public abstract class Instruction {
		//an empty super class
	}

	/**
	 * This class stores a concrete instructions, namely an assignments.
	 */
	public class Assignment extends Instruction {
		private String assignedVariable = null;
		private Expression assignedValue = null;

		public Assignment(String assignedVariable, Expression assignedValue) {
			this.assignedVariable = assignedVariable;
			this.assignedValue = assignedValue;
		}

		@SuppressWarnings("unused")//used in the template
		public String printAssignedVariable() {
			return this.assignedVariable;
		}

		@SuppressWarnings("unused")//used in the template
		public Expression getAssignedValue() {
			return this.assignedValue;
		}

		@SuppressWarnings("unused")//used in the template
		public String printAssignedValue() {
			if (this.assignedValue != null) {
				return this.assignedValue.print(new LEMSSyntaxContainer());
			}
			return "";
		}

		public void replaceConstantsWithReferences(LEMSCollector container) {
			this.assignedValue = container.getHelper().replaceConstantsWithReferences(container, this.assignedValue);
		}

		public void replaceResolutionByConstantReference(LEMSCollector container) {
			this.assignedValue = container.getHelper().replaceResolutionByConstantReference(container, this.assignedValue);
		}


	}

	/**
	 * This class is used to store concrete instructions, namely function calls.
	 */
	public class FunctionCall extends Instruction {
		private Function functionCallExpr;

		public FunctionCall(ASTFunctionCall call) {
			this.functionCallExpr = new Function(call);
		}

		public FunctionCall(String call, List<Expression> args) {
			this.functionCallExpr = new Function(call, args);
		}

		@SuppressWarnings("unused")//used in the template
		public String printName() {
			return this.functionCallExpr.getFunctionName();
		}

		@SuppressWarnings("unused")//used in the template
		public String printArgs() {
			StringBuilder newBuilder = new StringBuilder();
			for (Expression expr : this.functionCallExpr.getArguments()) {
				newBuilder.append(expr.print(new LEMSSyntaxContainer()));
				newBuilder.append(",");
			}
			newBuilder.deleteCharAt(newBuilder.length() - 1);//delete the last "," before the end of the string
			return newBuilder.toString();
		}

		public List<Expression> getArgs() {
			return functionCallExpr.getArguments();
		}


	}

}