package org.nest.codegeneration.helpers.Expressions;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.nest.commons._ast.ASTExpr;
import org.nest.symboltable.symbols.VariableSymbol;

/**
 * @author perun
 */
public class Expression {
  private Optional<Expression> lhs = Optional.empty();
  private Optional<Operator> operator = Optional.empty();
  private Optional<Expression> rhs = Optional.empty();

  public Expression(){}

  public Expression(ASTExpr expr){
    checkNotNull(expr);
    this.handleExpression(expr);
  }

  public Expression(VariableSymbol expr){
    checkNotNull(expr);
    if(expr.getDeclaringExpression().isPresent()){
      this.handleExpression(expr.getDeclaringExpression().get());
    }
  }

  private void handleExpression(ASTExpr expr){
    if (expr.getTerm().isPresent()) {
      this.operator = Optional.of(new Operator(expr));
      if (expr.getTerm().get().nESTMLNumericLiteralIsPresent()) {
        this.rhs = Optional.of(new NumericalLiteral(expr.getTerm().get().getNESTMLNumericLiteral().get()));
      }
      else if(expr.getTerm().get().variableIsPresent()){
        this.rhs = Optional.of(new Variable(expr.getTerm().get().getVariable().get().getName().toString()));
      }
      else if(expr.getTerm().get().functionCallIsPresent()){
        this.rhs = Optional.of(new Function(expr.getTerm().get().getFunctionCall().get()));
      }
    }
    else if(expr.nESTMLNumericLiteralIsPresent()){
      this.rhs = Optional.of(new NumericalLiteral(expr.getNESTMLNumericLiteral().get()));
    }
    else if(expr.variableIsPresent()){
      this.rhs = Optional.of(new Variable(expr.getVariable().get().getName().toString()));
    }
    else if(expr.functionCallIsPresent()){
      this.rhs = Optional.of(new Function(expr.getFunctionCall().get()));
    }
    else{
      this.lhs = Optional.of(new Expression(expr.getLeft().get()));
      this.operator = Optional.of(new Operator(expr));
      this.rhs = Optional.of(new Expression(expr.getRight().get()));
    }
  }

  public List<Expression> getOperators(){
    List<Expression> resOps = new ArrayList<>();
    if(this.getClass().equals(Operator.class)){
      resOps.add(this);
    }
    if(this.lhs.isPresent()){
      resOps.addAll(lhs.get().getOperators());
    }
    if(this.rhs.isPresent()){
      resOps.addAll(rhs.get().getOperators());
    }
    return resOps;
  }

  public List<Expression> getFunctions(){
    List<Expression> resFunc = new ArrayList<>();
    if(this.getClass().equals(Function.class)){
      resFunc.add(this);
    }
    if(this.lhs.isPresent()){
      resFunc.addAll(lhs.get().getFunctions());
    }
    if(this.rhs.isPresent()){
      resFunc.addAll(rhs.get().getFunctions());
    }
    return resFunc;
  }

  public List<Expression> getVariables(){
    List<Expression> resVars = new ArrayList<>();
    if(this.getClass().equals(Variable.class)){
      resVars.add(this);
    }
    if(this.lhs.isPresent()){
      resVars.addAll(lhs.get().getVariables());
    }
    if(this.rhs.isPresent()){
      resVars.addAll(rhs.get().getVariables());
    }
    return resVars;
  }

  public List<Expression> getNumericals(){
    List<Expression> resNums = new ArrayList<>();
    if(this.getClass().equals(NumericalLiteral.class)){
      resNums.add(this);
    }
    if(this.lhs.isPresent()){
      resNums.addAll(lhs.get().getNumericals());
    }
    if(this.rhs.isPresent()){
      resNums.addAll(rhs.get().getNumericals());
    }
    return resNums;
  }

  public String printExpression(SyntaxContainer container){
    String ret = "";
    if(this.lhs.isPresent()){
      if(this.lhs.get().getClass().equals(Expression.class)){
        ret = ret+this.lhs.get().printExpression(container);
      }
      else{
        ret = ret+container.print(this.lhs.get());
      }

    }
    if(this.operator.isPresent()){
      ret = ret+container.print(this.operator.get());
    }
    if(this.rhs.isPresent()){
      if(this.rhs.get().getClass().equals(Expression.class)){
        ret = ret+this.rhs.get().printExpression(container);
      }
      else{
        ret = ret+container.print(this.rhs.get());
      }
    }
    return ret;
  }



}
