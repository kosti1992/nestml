/*
 * Copyright (c)  RWTH Aachen. All rights reserved.
 *
 * http://www.se-rwth.de/
 */
package org.nest.nestml._parser;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import de.se_rwth.commons.Names;
import de.se_rwth.commons.logging.Finding;
import de.se_rwth.commons.logging.Log;
import org.antlr.v4.runtime.RecognitionException;
import org.nest.nestml._ast.ASTNESTMLCompilationUnit;
import org.nest.nestml._ast.ASTNeuron;
import org.nest.spl._ast.ASTDeclaration;
import org.nest.units._visitor.UnitsSIVisitor;
import org.nest.utils.AstUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
 * HW parser that also is able
 *
 * @author plotnikov
 */
public class NESTMLParser extends NESTMLParserTOP {

  private final List<String> sourceText = Lists.newArrayList();
  private final Optional<Path> modelPath;

  public NESTMLParser() {
    modelPath = Optional.empty();
  }

  public NESTMLParser(final Path modelPath) {
    this.modelPath = Optional.of(modelPath);
  }


  @Override
  public Optional<ASTNESTMLCompilationUnit> parseNESTMLCompilationUnit(final String filename)
      throws IOException, RecognitionException {
    final Path pathToModel = Paths.get(filename).normalize();
    if (pathToModel.getParent() == null) {
      final String msg = String.format("The model '%s' must be stored in a folder. Structuring "
          + "models improves maintenance.", filename);
      Log.error(msg);
      return Optional.empty();
    }

    final Optional<ASTNESTMLCompilationUnit> res = super.parseNESTMLCompilationUnit(filename);

    if (res.isPresent()) {
      setModelPackage(filename, res.get());
      // in case of no importstatements the first comment, that should belong to neuron, is interpreted as artifact
      // //comment
      forwardModelComment(res.get());

      List<Finding> typeFindings = UnitsSIVisitor.convertSiUnitsToSignature(res.get());
      if (!typeFindings.isEmpty()) {
        return Optional.empty();
      }

      // store model text as list of strings
      sourceText.addAll(Files.readLines(pathToModel.toFile(), Charset.defaultCharset()));
      final List<ASTDeclaration> declarations = AstUtils.getAll(res.get(), ASTDeclaration.class);

      for (final ASTDeclaration astDeclaration:declarations) {
        int line = astDeclaration.get_SourcePositionStart().getLine();
        final List<String> variableComments = extractComments(sourceText, line - 1);
        variableComments.forEach(astDeclaration::addComment);
      }

    }

    return res;
  }

  /**
   * Extracts comments starting from the `line` backwards
   */
  private List<String> extractComments(final List<String> sourceText, int lineIndex) {
    final List<String> result = Lists.newArrayList();
    if (sourceText.get(lineIndex).contains("#")) {
      result.add(sourceText.get(lineIndex).substring(sourceText.get(lineIndex).indexOf("#") + 1).trim());
    }

    while (lineIndex > 0) {
      --lineIndex;
      if (sourceText.get(lineIndex).trim().startsWith("#")) {
        result.add(0, sourceText.get(lineIndex).substring(sourceText.get(lineIndex).indexOf("#") + 1).trim());
      }
      else {
        break;
      }
    }

    return result;
  }

  /**
   * Through the grammar structure it is not possible to distinguish between module comment and a comment on first
   * neuron. As a workaround put this comment also to the first neuron.
   */
  private void forwardModelComment(final ASTNESTMLCompilationUnit root) {
    if (!root.get_PreComments().isEmpty() && !root.getNeurons().isEmpty()) {
      final ASTNeuron astNeuron = root.getNeurons().get(0);
      astNeuron.set_PreComments(Lists.newArrayList(root.get_PreComments()));
      // copy of the list was necessary, since otherwise the list would be cleared in both nodes!
      root.get_PreComments().clear();

    }

  }

  private void setModelPackage(final String filename, final ASTNESTMLCompilationUnit root) {
    if (modelPath.isPresent()) {
      final Optional<String> packageName = computePackageName(Paths.get(filename), modelPath.get());
      final String artifactName = computeArtifactName(Paths.get(filename));

      packageName.ifPresent(root::setPackageName);
      root.setArtifactName(artifactName);
    }
    else {
      throw new RuntimeException("The parser must be instantiated with a model path.");
    }
  }

  Optional<String> computePackageName(Path artifactPath, Path modelPath) {
    artifactPath = artifactPath.normalize().toAbsolutePath();
    modelPath = modelPath.normalize().toAbsolutePath();

    final Path directParent = modelPath.relativize(artifactPath).getParent();
    if (directParent == null) {
      return Optional.empty();
    }
    else {
      final String pathAsString = directParent.toString();
      return Optional.of(Names.getPackageFromPath(pathAsString));
    }

  }

  String computeArtifactName(final Path artifactPath) {
    final String filename = artifactPath.getFileName().getName(0).toString();
    if (filename.endsWith(".nestml")) {
      return filename.substring(0, filename.indexOf(".nestml"));
    } else {
      return filename;
    }

  }

}
