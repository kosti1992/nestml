package org.nest.codegeneration.helpers.Expressions;

import org.nest.commons._ast.ASTExpr;

/**
 * @author perun
 */
public class Operator extends Expression{
  private boolean inf;
  private boolean logicalOr;
  private boolean logicalAnd;
  private boolean logicalNot;
  private boolean gt;
  private boolean ge;
  private boolean ne;
  private boolean eq;
  private boolean le;
  private boolean lt;
  private boolean bitOr;
  private boolean bitXor;
  private boolean bitAnd;
  private boolean shiftRight;
  private boolean shiftLeft;
  private boolean minusOp;
  private boolean plusOp;
  private boolean moduloOp;
  private boolean timesOp;
  private boolean divOp;
  private boolean unaryTilde;
  private boolean unaryMinus;
  private boolean unaryPlus;
  private boolean power;
  private boolean leftParentheses;
  private boolean rightParentheses;


  public Operator(ASTExpr expr){
    if(expr.isInf()){
      this.inf=true;
    }
    else if(expr.isLogicalOr()){
      this.logicalOr = true;
    }
    else if(expr.isLogicalAnd()){
      this.logicalAnd = true;
    }
    else if(expr.isLogicalNot()){
      this.logicalNot = true;
    }
    else if(expr.isGt()){
      this.gt = true;
    }
    else if(expr.isGe()){
      this.ge= true;
    }
    else if(expr.isNe()){
      this.ne = true;
    }
    else if(expr.isEq()){
      this.eq = true;
    }
    else if(expr.isLe()){
      this.le = true;
    }
    else if(expr.isLt()){
      this.lt = true;
    }
    else if(expr.isBitOr()){
      this.bitOr = true;
    }
    else if(expr.isBitXor()){
      this.bitXor = true;
    }
    else if(expr.isBitAnd()){
      this.bitAnd = true;
    }
    else if(expr.isShiftRight()){
      this.shiftRight = true;
    }
    else if(expr.isShiftLeft()){
      this.shiftLeft = true;
    }
    else if(expr.isMinusOp()){
      this.minusOp = true;
    }
    else if(expr.isPlusOp()){
      this.plusOp = true;
    }
    else if(expr.isModuloOp()){
      this.moduloOp = true;
    }
    else if(expr.isDivOp()){
      this.divOp = true;
    }
    else if(expr.isTimesOp()){
      this.timesOp = true;
    }
    else if(expr.isUnaryTilde()){
      this.unaryTilde = true;
    }
    else if(expr.isUnaryPlus()){
      this.unaryPlus = true;
    }
    else if(expr.isUnaryMinus()){
      this.unaryMinus = true;
    }
    else if(expr.isPow()){
      this.power = true;
    }
    else if(expr.isLeftParentheses()){
      this.leftParentheses = true;
    }
    else if(expr.isRightParentheses()){
      this.rightParentheses = true;
    }

  }

  public boolean isInf() {
    return inf;
  }

  public boolean isLogicalOr() {
    return logicalOr;
  }

  public boolean isLogicalAnd() {
    return logicalAnd;
  }

  public boolean isLogicalNot() {
    return logicalNot;
  }

  public boolean isGt() {
    return gt;
  }

  public boolean isGe() {
    return ge;
  }

  public boolean isNe() {
    return ne;
  }

  public boolean isEq() {
    return eq;
  }

  public boolean isLe() {
    return le;
  }

  public boolean isLt() {
    return lt;
  }

  public boolean isBitOr() {
    return bitOr;
  }

  public boolean isBitXor() {
    return bitXor;
  }

  public boolean isBitAnd() {
    return bitAnd;
  }

  public boolean isShiftRight() {
    return shiftRight;
  }

  public boolean isShiftLeft() {
    return shiftLeft;
  }

  public boolean isMinusOp() {
    return minusOp;
  }

  public boolean isPlusOp() {
    return plusOp;
  }

  public boolean isModuloOp() {
    return moduloOp;
  }

  public boolean isTimesOp() {
    return timesOp;
  }

  public boolean isDivOp() {
    return divOp;
  }

  public boolean isUnaryTilde() {
    return unaryTilde;
  }

  public boolean isUnaryMinus() {
    return unaryMinus;
  }

  public boolean isUnaryPlus() {
    return unaryPlus;
  }

  public boolean isPower() {
    return power;
  }

  public boolean isLeftParentheses() {
    return leftParentheses;
  }

  public boolean isRightParentheses() {
    return rightParentheses;
  }

  public void negate(){
    if(this.logicalAnd){
      this.logicalAnd = false;
      this.logicalOr = true;
    }
    else if(this.logicalOr){
      this.logicalOr = false;
      this.logicalAnd = true;
    }
    else if(this.logicalNot){
      //this case is quite interesting, since no log. op would be active
      this.logicalNot = false;
    }
  }
}
