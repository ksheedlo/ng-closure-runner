package org.angularjs.closurerunner;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import com.google.javascript.jscomp.AbstractCompiler;
import com.google.javascript.jscomp.CommandLineRunner;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.CompilerPass;
import com.google.javascript.jscomp.CustomPassExecutionTime;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;
import org.kohsuke.args4j.spi.StringOptionHandler;

import java.io.IOException;
import java.io.PrintStream;

import java.util.Set;

public class NgClosureRunner extends CommandLineRunner {

  protected NgClosureRunner(String[] args) {
    super(args);
  }

  @Option(name = "--minerr_pass",
    handler = BooleanOptionHandler.class,
    usage = "Strip error messages from calls to minErr instances")
  private boolean minerrPass = false;

  @Option(name = "--minerr_errors",
    handler = StringOptionHandler.class,
    usage = "Output stripped error messages to a file")
  private String minerrErrors = "errors.json";

  @Option(name = "--minerr_url",
    handler = StringOptionHandler.class,
    usage = "MinErr error messages will log links with this url prefix")
  private String minerrUrl = null;

  private CompilerPass createMinerrPass() throws IOException {
    AbstractCompiler compiler = createCompiler();
    PrintStream output = new PrintStream(minerrErrors);

    if (minerrUrl != null) {
      return new MinerrPass(compiler, output, MinerrPass.substituteInSource(minerrUrl));
    }
    return new MinerrPass(compiler, output);
  }

  @Override
  protected CompilerOptions createOptions() {
    CompilerOptions options = super.createOptions();

    if (minerrPass) {
      if (options.customPasses == null) {
        options.customPasses = ArrayListMultimap.create();
      }
      try {
        options.customPasses.put(CustomPassExecutionTime.BEFORE_OPTIMIZATIONS,
          createMinerrPass());
      } catch (IOException e) {
        System.err.println(e);
        System.exit(1);
      }
    }
    return options;
  }

  public static class BooleanOptionHandler extends OptionHandler<Boolean> {
    private static final Set<String> TRUES =
        Sets.newHashSet("true", "on", "yes", "1");
    private static final Set<String> FALSES =
        Sets.newHashSet("false", "off", "no", "0");
    public BooleanOptionHandler(
        CmdLineParser parser, OptionDef option,
        Setter<? super Boolean> setter) {
      super(parser, option, setter);
    }
    @Override
    public int parseArguments(Parameters params) throws CmdLineException {
      String param = null;
      try {
        param = params.getParameter(0);
      } catch (CmdLineException e) {
        param = null; // to stop linter complaints
      }
      if (param == null) {
        setter.addValue(true);
        return 0;
      } else {
        String lowerParam = param.toLowerCase();
        if (TRUES.contains(lowerParam)) {
          setter.addValue(true);
        } else if (FALSES.contains(lowerParam)) {
          setter.addValue(false);
        } else {
          setter.addValue(true);
          return 0;
        }
        return 1;
      }
    }
    @Override
    public String getDefaultMetaVariable() {
      return null;
    }
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