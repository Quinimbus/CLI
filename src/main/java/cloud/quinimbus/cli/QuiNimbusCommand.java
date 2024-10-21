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
        return new String[] {"%s [%s]".formatted(version, timestamp)};
    }
}
