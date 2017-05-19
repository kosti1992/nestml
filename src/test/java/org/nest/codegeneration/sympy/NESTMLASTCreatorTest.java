/*
 * Copyright (c) 2015 RWTH Aachen. All rights reserved.
 *
 * http://www.se-rwth.de/
 */
package org.nest.codegeneration.sympy;

import org.junit.Test;
import org.nest.spl._ast.ASTDeclaration;

import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * Tests how the plain text is converted into the NESTML ast
 *
 * @author plotnikov
 */
public class NESTMLASTCreatorTest {

  private static final String P_30 = "P30";

  private final static String P30_FILE = "src/test/resources/codegeneration/sympy/psc/iaf_psc_alpha_neuron.P30.tmp";

  @Test
  public void testConvertToDeclaration() throws Exception {
    final ASTDeclaration testant = NESTMLASTCreator.createDeclarations(Paths.get(P30_FILE)).get(0);
    assertEquals(testant.getVars().get(0), P_30);
    assertTrue(testant.getExpr().isPresent());

  }

  @Test
  public void testConvertString2Alias() {
    final String testExpr = "P30 real = -Tau*tau_in*(Tau*h*exp(h/Tau) + Tau*tau_in*exp(h/Tau) - Tau*tau_in*exp"
        + "(h/tau_in) - "
        + "h*tau_in*exp(h/Tau))*exp(-h/tau_in - h/Tau)/(C*(Tau**2 - 2*Tau*tau_in + tau_in**2)) # PXX";
    final ASTDeclaration testant = NESTMLASTCreator.createDeclaration(testExpr);
    assertNotNull(testant);
    assertEquals(1, testant.getVars().size());
    assertEquals(P_30, testant.getVars().get(0));
  }


}
