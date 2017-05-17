package org.nest.codegeneration.helpers.LEMSElements;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.*;

import de.monticore.emf._ast.ASTECNode;
import org.nest.codegeneration.helpers.Expressions.Expression;
import org.nest.codegeneration.helpers.Expressions.Function;
import org.nest.codegeneration.helpers.Expressions.LEMSSyntaxContainer;
import org.nest.codegeneration.helpers.Expressions.NumericalLiteral;
import org.nest.codegeneration.helpers.Expressions.Operator;
import org.nest.codegeneration.helpers.Expressions.Variable;
import org.nest.codegeneration.helpers.Names;
import org.nest.commons._ast.ASTExpr;
import org.nest.commons._ast.ASTFunctionCall;
import org.nest.commons._ast.ASTVariable;
import org.nest.nestml._ast.ASTDynamics;
import org.nest.spl._ast.*;
import org.nest.spl.prettyprinter.SPLPrettyPrinter;
import org.nest.spl.prettyprinter.SPLPrettyPrinterFactory;
import org.nest.units._ast.ASTUnitType;

/**
 * This class represents a transformed representation of the dynamic routine extracted from the source-model
 * in order to generate corresponding conditional mBlocks.
 *
 * @author perun
 */
public class DynamicRoutine {
    private List<ConditionalBlock> mBlocks;//A List of all states of the automaton.
    private LEMSCollector mContainer;//required in order to use the same pretty printer as in other parts of transformation

    public DynamicRoutine(List<ASTDynamics> input, LEMSCollector container) {
        this.mBlocks = new ArrayList<>();
        this.mContainer = container;
        //currently, only one dynamics block can be present, however, NESTML deals with it by means of a List
        for (ASTDynamics dyn : input) {//for all dynamic mBlocks in the model
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
            } else {//first check if it is a declaration where the ternary OP is used
                if (input.getSmall_Stmt().get().getDeclaration().isPresent() &&
                        input.getSmall_Stmt().get().getDeclaration().get().getExpr().isPresent() &&
                        input.getSmall_Stmt().get().getDeclaration().get().getExpr().get().conditionIsPresent()) {
                    //if it is a ternary OP we have to handle it in a special way
                    this.mBlocks.addAll(this.handleASTDeclarationTernaryOperator(input.getSmall_Stmt().get(), new Expression()));//the null is a big TODO
                } else {
                    tempList.addAll(handleSmallStatement(input.getSmall_Stmt().get()));
                }
            }
            //generate a description header
            SPLPrettyPrinter tempPrettyPrinter = SPLPrettyPrinterFactory.createDefaultPrettyPrinter();
            tempPrettyPrinter.print(input);
            String rawCodeTemp = this.buildHeader(input, tempPrettyPrinter.result());
            if (tempList.size() > 0) {//generate a new block which shall be always executed, thus cond=TRUE
                this.mBlocks.add(new ConditionalBlock(tempList, Expression.generateTrue(), rawCodeTemp));
            }
        } else if (input.compound_StmtIsPresent()) {
            handleCompoundStatement(input.getCompound_Stmt().get(), null);
        } else {
            System.err.println(
                    "LEMS Error (Line: "
                            + mContainer.getNeuronName() + "/"
                            + input.get_SourcePositionStart().getLine()
                            + "):"
                            + " Not supported type of statement.");
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

            if (condition != null) {//required in order to process nested mBlocks
                Operator tempOp = new Operator();
                tempOp.setLogicalAnd(true);
                Expression tempRhs = Expression.encapsulateInBrackets(new Expression(input.getIF_Stmt().get().getIF_Clause().getExpr()));
                tempCondition.replaceLhs(condition);
                tempCondition.replaceRhs(tempRhs);
                tempCondition.replaceOp(tempOp);
                tempCondition = HelperCollection.replaceBooleanAtomByExpression(mContainer, tempCondition);
                tempCondition = Expression.encapsulateInBrackets(tempCondition);
            } else {
                tempCondition = new Expression(input.getIF_Stmt().get().getIF_Clause().getExpr());
                tempCondition = HelperCollection.replaceBooleanAtomByExpression(mContainer, tempCondition);
                tempCondition = Expression.encapsulateInBrackets(tempCondition);
            }
            tempPrettyPrinter.print(input.getIF_Stmt().get().getIF_Clause());
            //store a new conditional block
            tempCondition = HelperCollection.replaceConstantsWithReferences(mContainer, tempCondition);
            tempCondition = HelperCollection.replaceResolutionByConstantReference(mContainer, tempCondition);
            tempCondition = HelperCollection.replaceNotByLogicalEquivalent(mContainer, tempCondition);
            tempCondition = HelperCollection.encapsulateExpressionInConditions(tempCondition);
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
            tempCondition = HelperCollection.replaceConstantsWithReferences(mContainer, tempCondition);
            tempCondition = HelperCollection.replaceResolutionByConstantReference(mContainer, tempCondition);
            tempCondition = HelperCollection.encapsulateExpressionInConditions(tempCondition);
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
            tempCondition = HelperCollection.replaceConstantsWithReferences(mContainer, tempCondition);
            tempCondition = HelperCollection.replaceResolutionByConstantReference(mContainer, tempCondition);
            tempCondition = HelperCollection.encapsulateExpressionInConditions(tempCondition);
            tempCondition = Expression.encapsulateInBrackets(tempCondition);
            handleASTBlock(input.getIF_Stmt().get().getELSE_Clause().get().getBlock(),
                    tempCondition, tempPrettyPrinter.result());
        }

        //TODO: are these mBlocks really not supported?
        else if (input.getWHILE_Stmt().isPresent()) {//the block is a while block-> not supported yet
            System.err.println(
                    "LEMS Error (Line: "
                            + mContainer.getNeuronName() + "/"
                            + input.getWHILE_Stmt().get().get_SourcePositionStart().getLine()
                            + "):"
                            + " WHILE mBlocks are not yet supported.");
        } else if (input.getFOR_Stmt().isPresent()) {//the block is a for block-> not supported yet
            System.err.println(
                    "LEMS Error (Line: "
                            + mContainer.getNeuronName() + "/"
                            + input.getFOR_Stmt().get().get_SourcePositionStart().getLine()
                            + "):"
                            + " FOR mBlocks are not yet supported.");
        }
    }

    /**
     * Handles a block of instructions consisting of e.g. assignments, function-calls and further mBlocks.
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
                    //tempInstruction = deactivateIntegration(tempInstruction);
                    this.mBlocks.add(new ConditionalBlock(tempInstruction, condition, rawCodeTemp));//add a new condition
                }
                tempInstruction = new ArrayList<>();//delete all processed statements in order to avoid duplicates
                handleCompoundStatement(stmt.getCompound_Stmt().get(), condition);
                //now the ternary operator requires extra handling by generating an intermediate cond. block
            } else if (stmt.small_StmtIsPresent() && stmt.getSmall_Stmt().get().assignmentIsPresent()
                    && stmt.getSmall_Stmt().get().getAssignment().get().getExpr().conditionIsPresent()) {
                if (tempInstruction.size() > 0) {
                    //tempInstruction = deactivateIntegration(tempInstruction);
                    this.mBlocks.add(new ConditionalBlock(tempInstruction, condition, rawCodeTemp));//add a new condition
                }
                tempInstruction = new ArrayList<>();
                this.mBlocks.addAll(this.handleTernaryOperator(stmt.getSmall_Stmt().get(), condition));

            } else if (stmt.small_StmtIsPresent()) {
                List<Instruction> notNullCheck = handleSmallStatement(stmt.getSmall_Stmt().get());
                if (!notNullCheck.isEmpty()) {
                    tempInstruction.addAll(notNullCheck);
                }
            } else {
                System.err.println(
                        "LEMS Error (Line: "
                                + mContainer.getNeuronName() + "/"
                                + stmt.get_SourcePositionStart().getLine()
                                + "):"
                                + " if-processing. Neither small nor compound statement found.");
            }
        }
        //if no "integrate" directives have been found in this block but there exist some local "integrates", we
        //have to deactivate it in this block in order to stop the integration
        //TODO:this is currently deactivated
        //tempInstruction = deactivateIntegration(tempInstruction);
        //mBlocks without any instructions can be skipped
        if (tempInstruction != null && tempInstruction.size() > 0) {
            this.mBlocks.add(new ConditionalBlock(tempInstruction, condition, rawCodeTemp));
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
            if (HelperCollection.containsFunctionCall(input.getAssignment().get().getExpr(), true, mContainer)) {
                //Generate a proper error message
                Messages.printNotSupportedFunctionInBlock(input, mContainer);
                //now generate a expression which indicates that it is not supported
                Expression tempExpression = new Expression(input.getAssignment().get().getExpr());
                tempExpression = tempExpression.setNotSupported();
                //return a corresponding assignment
                Assignment retAssignment = new Assignment(input.getAssignment().get().getLhsVarialbe().getName().toString(), tempExpression);
                retAssignment.replaceConstantsWithReferences(mContainer);
                retAssignment.replaceResolutionByConstantReference(mContainer);
                res.add(retAssignment);
                return res;
            } else {
                Expression tempExpression = new Expression(input.getAssignment().get().getExpr());
                tempExpression = HelperCollection.replaceResolutionByConstantReference(mContainer, tempExpression);
                Expression ret = new Expression();
                Variable tempVar = new Variable(input.getAssignment().get().getLhsVarialbe().getName().toString());
                Operator tempOp = new Operator();
                ret.replaceLhs(tempVar);
                ret.replaceRhs(tempExpression);
                Assignment retAssignment;
                String varName = Names.convertToCPPName(input.getAssignment().get().getLhsVarialbe().getName().toString());


                ASTVariable lhs = input.getAssignment().get().getLhsVarialbe();
                //first check the order of the equation is bigger 0, then reduce it by one
                if (lhs.getDifferentialOrder().size() > 0) {
                    List<String> diffList = lhs.getDifferentialOrder();
                    diffList.remove(diffList.size() - 1);
                    lhs.setDifferentialOrder(diffList);
                }
                varName = Names.convertToCPPName(lhs.toString());//now retrieve the name and replace it by a proper representation


                //in order to process assignments of type x-=y
                if (input.getAssignment().get().isCompoundMinus()) {
                    tempOp.setMinusOp(true);
                    ret.replaceOp(tempOp);
                    ret = HelperCollection.replacementRoutine(mContainer, ret);
                    retAssignment = new Assignment(varName, ret);
                }//in order to process assignments of type x*=y
                else if (input.getAssignment().get().isCompoundProduct()) {
                    tempOp.setTimesOp(true);
                    ret.replaceOp(tempOp);
                    ret = HelperCollection.replacementRoutine(mContainer, ret);
                    retAssignment = new Assignment(varName, ret);
                }//in order to process assignments of type x+=y
                else if (input.getAssignment().get().isCompoundSum()) {
                    tempOp.setPlusOp(true);
                    ret.replaceOp(tempOp);
                    ret = HelperCollection.replacementRoutine(mContainer, ret);
                    retAssignment = new Assignment(varName, ret);
                }//in order to process assignments of type x/=y
                else if (input.getAssignment().get().isCompoundQuotient()) {
                    tempOp.setDivOp(true);
                    ret.replaceOp(tempOp);
                    ret = HelperCollection.replacementRoutine(mContainer, ret);
                    retAssignment = new Assignment(varName, ret);
                } else if (input.getAssignment().get().getExpr().conditionIsPresent()) {
                    this.mBlocks.addAll(this.handleTernaryOperator(input, new Expression()));
                    return res;
                } else {
                    ret = HelperCollection.replacementRoutine(mContainer, new Expression(input.getAssignment().get().getExpr()));
                    retAssignment = new Assignment(varName, ret);
                }
                retAssignment.replaceResolutionByConstantReference(mContainer);
                res.add(retAssignment);
                return res;
            }
        }
        if (input.functionCallIsPresent()) {
            return handleFunctionCall(input.getFunctionCall().get());
        }
        if (input.declarationIsPresent()) {
            return handleASTDeclaration(input.getDeclaration().get(), this.mContainer);
        }
        Messages.printErrorInSmallStatement(input,mContainer);
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
                this.mContainer.addNotConverted("Not supported function call "
                        + functionCall.getName().toString() + " in lines "
                        + functionCall.get_SourcePositionStart() + " to " + functionCall.get_SourcePositionEnd()
                        + " in model " + this.mContainer.getNeuronName());
                return new ArrayList<>();
        }
    }


    /**
     * Deals with variable declarations inside the update block. This method generates state variables and instructions
     * to set variables to the values in each iteration.
     *
     * @param declaration a ASTDeclaration containing a declaration of a variable inside the update block
     * @param container   a LEMSCollector for adding the state variables
     * @return a list of instructions
     */
    private List<Instruction> handleASTDeclaration(ASTDeclaration declaration, LEMSCollector container) {
        List<Instruction> ret = new ArrayList<>();
        String dimension = HelperCollection.DIMENSION_NONE;
        Optional<String> unit = Optional.empty();
        //if a data type is present,we have to add this type to the mContainer
        if (declaration.getDatatype().getUnitType().isPresent()) {
            int[] dec = HelperCollection.convertTypeDeclToArray(
                    declaration.getDatatype().getUnitType().get().getSerializedUnit());
            Expression tempExr = HelperCollection.getExpressionFromUnitType(declaration.getDatatype().
                    getUnitType().get());
            Dimension tempDimension = new Dimension(HelperCollection.PREFIX_DIMENSION
                    + (HelperCollection.formatComplexUnit(tempExr.print())),
                    dec[2], dec[3], dec[1], dec[6], dec[0], dec[5], dec[4]);
            Unit tempUnit = new Unit(HelperCollection.formatComplexUnit(tempExr.print()), dec[7], tempDimension);
            container.addDimension(tempDimension);
            container.addUnit(tempUnit);
            dimension = tempDimension.getName();
            unit = Optional.of(tempUnit.getSymbol());
        }
        Expression tempExpression;
        //now check if a declaration is present
        if (declaration.getExpr().isPresent()) {
            tempExpression = new Expression(declaration.getExpr().get());
        } else {//if no (this case is quite absurd, but a handling should be present)
            if (declaration.getDatatype().unitTypeIsPresent()) {
                ASTUnitType tempType = new ASTUnitType();
                tempType.setUnit(declaration.getDatatype().getUnitType().get().getUnit().get());
                tempType.setSerializedUnit(declaration.getDatatype().getUnitType().get().getSerializedUnit());
                tempExpression = new NumericalLiteral(0, tempType);
            } else {
                tempExpression = new NumericalLiteral(0, null);
            }
        }
        tempExpression = HelperCollection.replacementRoutine(container, tempExpression);
        Instruction tempInstruction;
        for (String var : declaration.getVars()) {
            container.addStateVariable(new StateVariable(var, dimension, tempExpression, unit));
            tempInstruction = new Assignment(var, tempExpression);
            ret.add(tempInstruction);
        }
        return ret;
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
     * replaces it by means of two sub mBlocks with corresponding conditions.
     *
     * @param input     the input small statement with ternary operator
     * @param condition the condition, if one is present, of the super block containing the assignment
     * @return the list containing two conditions
     */
    private List<ConditionalBlock> handleTernaryOperator(ASTSmall_Stmt input, Expression condition) {
        List<ConditionalBlock> ret = new ArrayList<>();
        //first create the first part of the expression, namely the one which applies if condition is true
        Expression firstSubCondition;
        //if it is a boolean literal, e.g. true or false
        if (input.getAssignment().get().getExpr().getCondition().get().booleanLiteralIsPresent()) {
            if (input.getAssignment().get().getExpr().getCondition().get().getBooleanLiteral().get().getValue()) {
                firstSubCondition = Expression.generateTrue();
            } else {
                firstSubCondition = Expression.generateFalse();
            }
        } else {
            firstSubCondition = new Expression(input.getAssignment().get().getExpr().getCondition().get());
        }

        firstSubCondition = Expression.encapsulateInBrackets(firstSubCondition);
        Operator opFirst = new Operator();
        opFirst.setLogicalAnd(true);
        Expression firstCondition = new Expression();
        if (!condition.isEmpty() && !condition.isEmpty()) {
            firstCondition.replaceLhs(condition.deepClone());
            firstCondition.replaceOp(opFirst);
            firstCondition.replaceRhs(firstSubCondition);
            firstCondition = HelperCollection.replaceBooleanAtomByExpression(mContainer, firstCondition);
        } else {
            firstCondition = firstSubCondition;
        }
        firstCondition = HelperCollection.encapsulateExpressionInConditions(firstCondition);
        //now generate an assignment for the first half
        Expression firstAssignmentExpression = new Expression(input.getAssignment().get().getExpr().getIfTrue().get());
        firstAssignmentExpression = HelperCollection.replaceConstantsWithReferences(mContainer, firstAssignmentExpression);
        firstAssignmentExpression = HelperCollection.replaceResolutionByConstantReference(mContainer, firstAssignmentExpression);
        Assignment firstAssignment = new Assignment(input.getAssignment().get().getLhsVarialbe().getName().toString(),
                firstAssignmentExpression);
        ConditionalBlock firstBlock = new ConditionalBlock(firstAssignment, firstCondition, null);
        ret.add(firstBlock);

        //now create the second part which applies if the condition is not true
        Expression secondSubCondition = firstSubCondition.deepClone();
        secondSubCondition = HelperCollection.replaceBooleanAtomByExpression(mContainer, secondSubCondition);
        secondSubCondition.negateLogic();
        Operator opSecond = new Operator();
        opSecond.setLogicalAnd(true);
        Expression secondCondition = new Expression();
        if (!condition.isEmpty()) {
            secondCondition.replaceLhs(condition.deepClone());
            secondCondition.replaceOp(opSecond);
            secondCondition.replaceRhs(secondSubCondition);
        } else {
            secondCondition = secondSubCondition;
        }
        secondCondition = HelperCollection.encapsulateExpressionInConditions(secondCondition);
        //now generate an assignment for the second half
        Expression secondAssignmentExpression = new Expression(input.getAssignment().get().getExpr().getIfNot().get());
        secondAssignmentExpression = HelperCollection.replaceConstantsWithReferences(mContainer, secondAssignmentExpression);
        secondAssignmentExpression = HelperCollection.replaceResolutionByConstantReference(mContainer, secondAssignmentExpression);
        Assignment secondAssignment = new Assignment(input.getAssignment().get().getLhsVarialbe().getName().toString(),
                secondAssignmentExpression);
        ConditionalBlock secondBlock = new ConditionalBlock(secondAssignment, secondCondition, null);
        ret.add(secondBlock);
        return ret;
    }

    /**
     * For a given conditional block and an assignment inside which uses the ternary operator, this method
     * replaces it by means of two sub mBlocks with corresponding conditions.
     *
     * @param _smallStmt     the input small statement with ternary operator
     * @param _condition the condition, if one is present, of the super block containing the assignment
     * @return the list containing two conditions
     */
    private List<ConditionalBlock> handleASTDeclarationTernaryOperator(ASTSmall_Stmt _smallStmt, Expression _condition) {
        List<ConditionalBlock> ret = new ArrayList<>();
        //first create the first part of the expression, namely the one which applies if condition is true
        Expression firstSubCondition;
        //if it is a boolean literal, e.g. true or false
        if (_smallStmt.getDeclaration().get().getExpr().get().getCondition().get().booleanLiteralIsPresent()) {
            if (_smallStmt.getDeclaration().get().getExpr().get().getCondition().get().getBooleanLiteral().get().getValue()) {
                firstSubCondition = Expression.generateTrue();
            } else {
                firstSubCondition = Expression.generateFalse();
            }
        } else {
            firstSubCondition = new Expression(_smallStmt.getDeclaration().get().getExpr().get().getCondition().get());
        }

        String dimension = "";
        Optional<String> unit = Optional.empty();
        if (_smallStmt.getDeclaration().get().getDatatype().getUnitType().isPresent()) {
            int[] dec = HelperCollection.convertTypeDeclToArray(
                    _smallStmt.getDeclaration().get().getDatatype().getUnitType().get().getSerializedUnit());
            Expression tempExr = HelperCollection.getExpressionFromUnitType(_smallStmt.getDeclaration().get().getDatatype().
                    getUnitType().get());
            Dimension tempDimension = new Dimension(HelperCollection.PREFIX_DIMENSION
                    + (HelperCollection.formatComplexUnit(tempExr.print())),
                    dec[2], dec[3], dec[1], dec[6], dec[0], dec[5], dec[4]);
            Unit tempUnit = new Unit(HelperCollection.formatComplexUnit(tempExr.print()), dec[7], tempDimension);
            mContainer.addDimension(tempDimension);
            mContainer.addUnit(tempUnit);
            dimension = tempDimension.getName();
            unit = Optional.of(tempUnit.getSymbol());
        }
        firstSubCondition = Expression.encapsulateInBrackets(firstSubCondition);
        Operator opFirst = new Operator();
        opFirst.setLogicalAnd(true);
        Expression firstCondition = new Expression();
        if (!_condition.isEmpty() && !_condition.isEmpty()) {
            firstCondition.replaceLhs(_condition.deepClone());
            firstCondition.replaceOp(opFirst);
            firstCondition.replaceRhs(firstSubCondition);
            firstCondition = HelperCollection.replaceBooleanAtomByExpression(mContainer, firstCondition);
        } else {
            firstCondition = firstSubCondition;
        }
        firstCondition = HelperCollection.encapsulateExpressionInConditions(firstCondition);
        //now generate an assignment for the first half
        Expression firstAssignmentExpression = new Expression(_smallStmt.getDeclaration().get().getExpr().get().getIfTrue().get());
        firstAssignmentExpression = HelperCollection.replaceConstantsWithReferences(mContainer, firstAssignmentExpression);
        firstAssignmentExpression = HelperCollection.replaceResolutionByConstantReference(mContainer, firstAssignmentExpression);

        Assignment firstAssignment;
        ConditionalBlock firstBlock;
        for (String var : _smallStmt.getDeclaration().get().getVars()) {
            firstAssignment = new Assignment(var, firstAssignmentExpression);
            firstBlock = new ConditionalBlock(firstAssignment, firstCondition, null);
            ret.add(firstBlock);
        }

        //now create the second part which applies if the condition is not true
        Expression secondSubCondition = firstSubCondition.deepClone();
        secondSubCondition = HelperCollection.replaceBooleanAtomByExpression(mContainer, secondSubCondition);
        secondSubCondition.negateLogic();
        Operator opSecond = new Operator();
        opSecond.setLogicalAnd(true);
        Expression secondCondition = new Expression();
        if (!_condition.isEmpty()) {
            secondCondition.replaceLhs(_condition.deepClone());
            secondCondition.replaceOp(opSecond);
            secondCondition.replaceRhs(secondSubCondition);
        } else {
            secondCondition = secondSubCondition;
        }
        secondCondition = HelperCollection.encapsulateExpressionInConditions(secondCondition);
        //now generate an assignment for the second half
        Expression secondAssignmentExpression = new Expression(_smallStmt.getDeclaration().get().getExpr().get().getIfNot().get());
        secondAssignmentExpression = HelperCollection.replaceConstantsWithReferences(mContainer, secondAssignmentExpression);
        secondAssignmentExpression = HelperCollection.replaceResolutionByConstantReference(mContainer, secondAssignmentExpression);
        Assignment secondAssignment;
        ConditionalBlock secondBlock;
        for (String var : _smallStmt.getDeclaration().get().getVars()) {
            secondAssignment = new Assignment(var, secondAssignmentExpression);
            secondBlock = new ConditionalBlock(secondAssignment, secondCondition, null);
            ret.add(secondBlock);
        }

        Expression tempLiteral = new NumericalLiteral(0, null);
        if (_smallStmt.getDeclaration().get().getDatatype().unitTypeIsPresent()) {
            ((NumericalLiteral) tempLiteral).setType(Optional.of(_smallStmt.getDeclaration().get().getDatatype().getUnitType().get()));
        }
        tempLiteral = HelperCollection.replacementRoutine(mContainer, tempLiteral);
        for (String var : _smallStmt.getDeclaration().get().getVars()) {
            mContainer.addStateVariable(new StateVariable(var, dimension, tempLiteral, unit));
        }
        return ret;
    }


    /**
     * Checks a given set of instructions for existence of integrate directives. If non found,
     * a corresponding assignment which deactivates the integration in this step is generated.
     *
     * @param _listOfInstructions a list of instructions
     * @return a possibly modified list of instructions
     */
    private List<Instruction> deactivateIntegration(List<Instruction> _listOfInstructions) {
        if (_listOfInstructions.isEmpty()) {
            return _listOfInstructions;//in order to avoid cond mBlocks which only consists of the deactivate directive
        }
        boolean temp;
        //check all local dime derivatives
        for (Variable var : mContainer.getLocalTimeDerivative()) {
            temp = false;
            //for all elements in the list, check if an integration directive has been found
            for (Instruction call : _listOfInstructions) {
                if (call.getClass().equals(Assignment.class) &&
                        ((Assignment) call).printAssignedVariable().equals(HelperCollection.PREFIX_ACT + var)) {
                    temp = true;
                }
            }
            //add a deactivation assignment to the list of directives if no integrate directive has been found
            if (!temp) {
                NumericalLiteral tempLiteral = new NumericalLiteral(0, null);
                _listOfInstructions.add(new Assignment(HelperCollection.PREFIX_ACT + var.getVariable(), tempLiteral));
            }
        }
        return _listOfInstructions;
    }

    /**
     * Generates a "integrate" counter piece in the target modeling language by replacing it with assignments.
     *
     * @param _functionCall a integrate function call
     * @return an instruction list which represents the integrate directive consisting of a single instruction
     */
    private List<Instruction> handleIntegrate(ASTFunctionCall _functionCall) {
        //add a new state variable which symbolize that integration should be activated in necessary
        if (_functionCall.getArgs().size() != 1) {
            Messages.printIntegrateWronglyDeclared(_functionCall, mContainer);
            return new ArrayList<>();
        } else {
            for (StateVariable var : mContainer.getStateVariablesList()) {              //the integrate function call has exactly one argument
                if (var.getName().equals(HelperCollection.PREFIX_ACT + _functionCall.getArgs().get(0).getVariable().get().getName().toString())) {
                    ((NumericalLiteral) var.getDefaultValue().get()).setValue(0);
                }
            }
            //integrate the corresponding variable in this block
            NumericalLiteral tempLiteral = new NumericalLiteral(1, null);
            //the method requires a list of instructions rather than a single instruction
            List<Instruction> res = new ArrayList<>();
            res.add(new Assignment(HelperCollection.PREFIX_ACT +
                    _functionCall.getArgs().get(0).getVariable().get().getName().toString(), tempLiteral));
            return res;
        }
    }

    /**
     * Handles the integrate_odes() function calls and generates a set of activator assignments.
     *
     * @return a list of instructions
     */
    private List<Instruction> handleIntegrate_ode() {
        //since all variables have to be integrated, we create a list of integrate instructions
        List<Instruction> res = new ArrayList<>();
        NumericalLiteral tempLiteral;
        for (Variable var : mContainer.getEquations().keySet()) {
            //integrate the corresponding variable in this block
            tempLiteral = new NumericalLiteral(1, null);
            res.add(new Assignment(HelperCollection.PREFIX_ACT + var.getVariable(), tempLiteral));
            //moreover, since a integrate_odes function call has been found, we make all integrations local
            mContainer.addLocalTimeDerivative(var);
        }
        return res;
    }

    /**
     * For a given list of ASTExpressions, this method build an else condition by
     * negating all stated condition and combining them by AND-operator.
     *
     * @param _listOfExpressions a list of ASTExpressions
     * @return a Expression object representing the else condition
     */
    private Expression buildElseCondition(List<ASTExpr> _listOfExpressions) {
        if (!_listOfExpressions.isEmpty()) {
            Expression tempExpr = Expression.encapsulateInBrackets(new Expression(_listOfExpressions.get(0)));
            tempExpr.negateLogic();
            if (_listOfExpressions.size() <= 1) {
                return tempExpr;
            }
            Expression combExpr = new Expression();
            if (_listOfExpressions.size() > 1) {
                combExpr.replaceLhs(tempExpr);
                Operator tempOp = new Operator();
                tempOp.setLogicalAnd(true);
                combExpr.replaceOp(tempOp);
                _listOfExpressions.remove(0);
                combExpr.replaceRhs(this.buildElseCondition(_listOfExpressions));
            }
            return combExpr;
        }
        return new Expression();
    }

    /**
     * This method is called whenever it is required to generate a proper header
     * for a block of the dynamic routine.
     *
     * @param _astNode   a node whose code is processed
     * @param _rawSource the raw source code which will be printed
     * @return a string representing the header
     */
    private String buildHeader(ASTECNode _astNode, String _rawSource) {
        String rawCodeTemp =
                "Generated from source lines " + _astNode.get_SourcePositionStart().toString() +
                        " to " + _astNode.get_SourcePositionEnd().toString() + ".\n";
        rawCodeTemp = rawCodeTemp + _rawSource;
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
        FunctionCall functionCall = new FunctionCall("emit_spike", new ArrayList<>());
        ArrayList<Instruction> instructionArrayList = new ArrayList<>();
        instructionArrayList.add(functionCall);
        String rawCode = "This is an artificial EventOut which is never used,\n but required by LEMS to regard out-ports.";
        ConditionalBlock block = new ConditionalBlock(instructionArrayList, Expression.generateFalse(), rawCode);
        this.mBlocks.add(block);
    }

    /**
     * Returns a list of all instructions in all mBlocks.
     *
     * @return a list of instruction objects
     */
    public List<Instruction> getAllInstructions() {
        ArrayList<Instruction> ret = new ArrayList<>();
        for (ConditionalBlock block : this.mBlocks) {
            ret.addAll(block.getInstructions());
        }
        return ret;
    }

    public List<ConditionalBlock> getConditionalBlocks() {
        return this.mBlocks;
    }

    /**
     * This method casts a given instruction to a assignment call and is only used in the
     * template.
     */
    @SuppressWarnings("unused")//used in the template
    public Assignment getAssignmentFromInstruction(Instruction _instruction) {
        return (Assignment) _instruction;
    }

    /**
     * This method casts a given instruction to a function call and is only used in the template. This
     * method is required since there is no "instanceof" operation in the provided backend.
     */
    @SuppressWarnings("unused")//used in the template
    public FunctionCall getFunctionCallFromInstruction(Instruction _instruction) {
        return (FunctionCall) _instruction;
    }

    /**
     * An instruction superclass used required in order to store all types of instructions in a single list.
     */
    public abstract class Instruction {
        private String mClassIdentifier;//each instruction has to provide an identifier for the backend

        public abstract String getClassIdentifier();

    }

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

    /**
     * This class is used to store concrete instructions, namely function calls.
     */
    public class FunctionCall extends Instruction {
        private final String mClassIdentifier = "FunctionCall";//required by the backend
        private Function mFunctionCallExpr;

        public FunctionCall(ASTFunctionCall _astFunctionCall) {
            checkNotNull(_astFunctionCall);
            this.mFunctionCallExpr = new Function(_astFunctionCall);
        }

        public FunctionCall(String _functionName, List<Expression> _arguments) {
            checkNotNull(_functionName);
            checkNotNull(_arguments);
            this.mFunctionCallExpr = new Function(_functionName, _arguments);
        }

        @SuppressWarnings("unused")//used in the template
        public String printName() {
            return this.mFunctionCallExpr.getFunctionName();
        }

        @SuppressWarnings("unused")//used in the template
        public String printArgs() {
            StringBuilder newBuilder = new StringBuilder();
            for (Expression expr : this.mFunctionCallExpr.getArguments()) {
                newBuilder.append(expr.print(new LEMSSyntaxContainer()));
                newBuilder.append(",");
            }
            newBuilder.deleteCharAt(newBuilder.length() - 1);//delete the last "," before the end of the string
            return newBuilder.toString();
        }

        public String getClassIdentifier() {
            return mClassIdentifier;
        }

        public List<Expression> getArgs() {
            return mFunctionCallExpr.getArguments();
        }
    }
}