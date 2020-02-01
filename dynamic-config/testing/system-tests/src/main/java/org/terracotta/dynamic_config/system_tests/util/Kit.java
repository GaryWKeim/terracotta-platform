/*
 * Copyright (c) 2011-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in your License Agreement with Software AG.
 */
package org.terracotta.dynamic_config.system_tests.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terracotta.ipceventbus.proc.AnyProcess;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static java.lang.System.lineSeparator;

/**
 * @author Mathieu Carbou
 */
public class Kit {
  private static final Logger LOGGER = LoggerFactory.getLogger(Kit.class);

  // memoize results for subsequent test execution to fasten build feedback in case of error
  private static volatile Path kitPath;
  private static RuntimeException error;

  public static Optional<Path> getPath() {
    return Optional.ofNullable(System.getProperty("kitInstallationPath")).map(s -> Paths.get(s));
  }

  public static Path getOrCreatePath() {
    if (kitPath == null) {
      constructPath();
    }
    return kitPath;
  }

  private synchronized static void constructPath() {
    if (kitPath == null) {
      Path constructedKitPath = getPath().orElseGet(() -> {
        Path path = build();
        System.setProperty("kitInstallationPath", path.toAbsolutePath().toString());
        return path;
      });

      // copy custom logback-ext
      try {
        Path dest = constructedKitPath.resolve("server").resolve("lib").resolve("logback-ext.xml");
        try (InputStream input = Kit.class.getResourceAsStream("/logback-ext.xml");
             OutputStream output = Files.newOutputStream(dest)) {
          byte[] data = new byte[4096]; // xml file will fit inside
          int read = input.read(data, 0, data.length);
          if (read == data.length) {
            throw new AssertionError("Log file has become too big, please increase the buffer");
          }
          output.write(data, 0, read);
        }
      } catch (IOException e) {
        //log an exception and continue, because not being able to copy logback-ext doesn't harm us in any way
        LOGGER.error("Caught exception during logback-ext.xml copy", e);
      }

      kitPath = constructedKitPath;
    }
  }

  private static synchronized Path build() {
    if (kitPath != null) {
      return kitPath;
    }
    if (error != null) {
      throw error;
    }
    try {
      Path rootPath = Env.getProjectRootPath();
      System.out.println("Building KIT...");
      AnyProcess process = AnyProcess.newBuilder()
          .workingDir(rootPath.toFile())
          .command(rootPath.resolve(Env.isWindows() ? "gradlew.bat" : "gradlew").toString(), ":dynamic-config-system-tests:unzipKit")
          .recordStdout()
          .redirectStderr()
          .build();
      if (process.waitFor() != 0) {
        throw new IllegalStateException("Failed building KIT. See logs below..." + lineSeparator() + process.getRecordedStdoutText());
      }
      File parent = Env.getModulePath().resolve("build").resolve("tc-db-kit").toFile();
      File[] children = parent.listFiles();
      if (children == null || children.length != 1) {
        throw new IllegalStateException("Kit directory not found in " + parent);
      }
      kitPath = children[0].toPath();
      return kitPath;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      error = new IllegalStateException(e);
      throw error;
    } catch (RuntimeException e) {
      error = e;
      throw error;
    }
  }

}
