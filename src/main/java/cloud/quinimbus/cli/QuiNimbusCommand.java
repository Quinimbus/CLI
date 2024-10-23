package cloud.quinimbus.cli;

import java.util.Properties;
import picocli.CommandLine;

@CommandLine.Command(
        name = "quinimbus",
        mixinStandardHelpOptions = true,
        description = "CLI tool to manage QuiNimbus projects",
        versionProvider = QuiNimbusCommand.class,
        subcommands = {ProjectCommand.class, SelfCommand.class})
public class QuiNimbusCommand implements CommandLine.IVersionProvider {

    private final Properties cliProperties;

    public QuiNimbusCommand(Properties cliProperties) {
        this.cliProperties = cliProperties;
    }

    @CommandLine.Option(
            names = {"-v", "--verbose"},
            defaultValue = "false",
            description = "Show verbose log output.",
            scope = CommandLine.ScopeType.INHERIT)
    private void setVerbose(boolean verbose) {
        Logger.setVerbose(verbose);
    }

    @Override
    public String[] getVersion() throws Exception {
        var version = this.cliProperties.getProperty("cli.version", "<unknown>");
        var timestamp = this.cliProperties.getProperty("cli.build.timestamp", "<unknown>");
        var gitCommitId = this.cliProperties.getProperty("git.commit.id.abbrev", "<unknown>");
        var gitCommitTime = this.cliProperties.getProperty("git.commit.time", "<unknown>");
        if (version.endsWith("-SNAPSHOT")) {
            return new String[] {
                "QuiNimbus CLI %s [%s]".formatted(version, timestamp),
                "  Git: %s [%s]".formatted(gitCommitId, gitCommitTime)
            };
        } else {
            return new String[] {"QuiNimbus CLI %s".formatted(version)};
        }
    }
}
