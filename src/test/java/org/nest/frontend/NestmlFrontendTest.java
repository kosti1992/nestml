/*
 * Copyright (c)  RWTH Aachen. All rights reserved.
 *
 * http://www.se-rwth.de/
 */
package org.nest.frontend;

import org.apache.commons.cli.CommandLine;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests various modis of the {@code NestmlFrontend} class. For this, several combinations of
 * CLI parameters also invalid combination are provided to the frontend.
 *
 * @author plotnikov
 */
public class NestmlFrontendTest {
  private final NestmlFrontend nestmlFrontend = new NestmlFrontend();

  @Test
  public void testCreationOfConfiguration() throws Exception {

    final Path testInputModelsPath = Paths.get("testInputModelsPath");
    final Path targetPath = Paths.get("targetPath");

    final CliConfiguration testant = nestmlFrontend.createCLIConfiguration(new String[] {
        testInputModelsPath.toString(),
        "--target", targetPath.toString()
    });

    assertTrue(testant.isCheckCoCos());
    assertEquals(testInputModelsPath, testant.getInputBase());
    assertEquals(targetPath, testant.getTargetPath());
  }

  @Test
  public void testInputPath() throws Exception {
    final String inputModelsPath = "./testTargetPath";
    CommandLine cliArguments = nestmlFrontend.parseCLIArguments(
        new String[] { "--target",  inputModelsPath});
    final String testant = nestmlFrontend.interpretTargetPathArgument(cliArguments);
    assertEquals(inputModelsPath, testant);

  }

  @Test
  public void testUnparsableModels() {
    nestmlFrontend.start(new String[] {
        "src/test/resources/cli_unparsable",
        "--target", Paths.get("target", "cli_unparsable").toString()});
  }

  @Test
  public void testInvalidPath() {
    nestmlFrontend.start(new String[] {
        "//bla/blu",
        "--target", Paths.get("target", "cli_unparsable").toString()});

    nestmlFrontend.start(new String[] {});
  }
}
