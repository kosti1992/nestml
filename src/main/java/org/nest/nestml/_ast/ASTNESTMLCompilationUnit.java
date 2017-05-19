/*
 * Copyright (c)  RWTH Aachen. All rights reserved.
 *
 * http://www.se-rwth.de/
 */
package org.nest.nestml._ast;

import java.util.Objects;
import java.util.Optional;

/**
 * HC Class that encapsulates several comfort features to work with package definition in nestml.
 *
 * @author plotnikov
 */
public class ASTNESTMLCompilationUnit extends ASTNESTMLCompilationUnitTOP {
  private Optional<String> packageName = Optional.empty();
  private String artifactName = "";

  public void setPackageName(final String packageName) {
    Objects.requireNonNull(packageName);
    this.packageName = !packageName.isEmpty()?Optional.of(packageName):Optional.empty();
  }

  public void setArtifactName(final String artifactName) {
    this.artifactName = artifactName;
  }

  protected ASTNESTMLCompilationUnit () {
    // used in the MC generated code
  }

  protected ASTNESTMLCompilationUnit (
      java.util.List<org.nest.nestml._ast.ASTNeuron> neurons
      ,
      java.util.List<String> nEWLINEs

  ) {
    super(neurons, nEWLINEs);
  }

  public String getArtifactName() {
    return artifactName;
  }

  public Optional<String> getPackageName() {
    return packageName;
  }

  public String getFullName() {
    if (getPackageName().isPresent()) {
      return getPackageName().get() + "." + getArtifactName();
    }
    else {
      return  getArtifactName();
    }

  }

}
