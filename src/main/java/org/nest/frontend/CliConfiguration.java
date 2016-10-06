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
  private final Path configPath;
  private final boolean printUnitsExternal;
  private final double simSteps;

  public CliConfiguration(final Builder builder) {
    this.checkCoCos = builder.checkCoCos;
    this.inputBasePath = builder.inputBasePath;
    this.targetPath = builder.targetPath;
    this.configPath = builder.configFile;
    this.printUnitsExternal = builder.printUnitsExternal;
    this.simSteps = builder.simSteps;
  }

  boolean isCheckCoCos() {
    return checkCoCos;
  }

  Path getConfigPath(){
    return  configPath;
  }

  boolean isPrintUnitsExternal(){
    return this.printUnitsExternal;
  }

  double getSimSteps(){return this.simSteps;}

  Path getInputBase() {

      return inputBasePath;
  }

  Path getTargetPath() {
    return targetPath;
  }

  public static class Builder {
    private boolean checkCoCos = false;
    private Path inputBasePath;
    private Path targetPath;
    private Path configFile;
    private boolean printUnitsExternal = false;
    private double simSteps;

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
