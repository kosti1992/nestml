/*
 * Copyright (c)  RWTH Aachen. All rights reserved.
 *
 * http://www.se-rwth.de/
 */
package org.nest.frontend;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Data class to store the tool's configuration
 *
 * @author plotnikov
 */
public class CliConfiguration {
  private final boolean checkCoCos;
  private final Path inputBasePath;
  private final Path targetPath;
  private boolean isTracing;

  //the following attributes are part of the NESTML2LEMS framework
  private final Path configPath;

  public CliConfiguration(final Builder builder) {
    this.checkCoCos = builder.checkCoCos;
    this.inputBasePath = builder.inputBasePath;
    this.targetPath = builder.targetPath;
    this.isTracing = builder.isTracing;

    this.configPath = builder.configFile;//Part of the NESTML2LEMS framework
}

  boolean isCheckCoCos() {
    return checkCoCos;
  }

  Path getConfigPath(){
    return  configPath;
  }

  Path getInputBase() {

      return inputBasePath;
  }

  Path getTargetPath() {
    return targetPath;
  }

  public boolean isTracing() {
    return isTracing;
  }

  public static class Builder {
    private boolean checkCoCos = false;
    private Path inputBasePath;
    private Path targetPath;
    private Path configFile;
    private boolean isTracing = false;

    private boolean printUnitsExternal = false;//part of the NESTML2LEMS Framework
    private double simSteps;//part of the NESTML2LEMS Framework

    Builder withCoCos() {
      this.checkCoCos = true;
      return this;
    }

    Builder withCoCos(boolean checkCoCos) {
      this.checkCoCos = checkCoCos;
      return this;
    }

    Builder withInputBasePath(final String inputBasePath) {
      this.inputBasePath = Paths.get(inputBasePath);
      return this;
    }

    Builder withInputBasePath(final Path inputBasePath) {
      this.inputBasePath = inputBasePath;
      return this;
    }

    Builder withTargetPath(final String targetPath) {
      this.targetPath = Paths.get(targetPath);
      return this;
    }

    Builder withTargetPath(final Path targetPath) {
      this.targetPath = targetPath;
      return this;
    }


    Builder withUnitsExternal(){
      this.printUnitsExternal = true;
      return this;
    }
    Builder withUnitsExternal(boolean printUnitsExternal){
      this.printUnitsExternal = printUnitsExternal;
      return this;
    }


    Builder withConfigPath(final String configPath){
      this.configFile = Paths.get(configPath);
      return this;
    }
    Builder withConfigPath(final Path configPath){
      this.configFile = configPath;
      return this;
    }

    Builder withSimSteps(final double simSteps){
      this.simSteps = simSteps;
      return this;
    }


    public CliConfiguration build() {
      return new CliConfiguration(this);
    }

  }

}
