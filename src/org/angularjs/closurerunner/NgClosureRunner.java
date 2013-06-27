package org.angularjs.closurerunner;

import com.google.javascript.jscomp.CommandLineRunner;
import com.google.javascript.jscomp.CompilerOptions;

public class NgClosureRunner extends CommandLineRunner {

  protected NgClosureRunner(String[] args) {
    super(args);
  }

  @Override
  protected CompilerOptions createOptions() {
    CompilerOptions options = super.createOptions();

    // enable additional options here

    return options;
  }

  public static void main(String[] args) {
    NgClosureRunner runner = new NgClosureRunner(args);
    if (runner.shouldRunCompiler()) {
      runner.run();
    } else {
      System.exit(-1);
    }
  }
}