package cloud.quinimbus.cli;

import cloud.quinimbus.cli.self.SelfUpdateCommand;
import picocli.CommandLine;

@CommandLine.Command(
        name = "self",
        description = "Manage the CLI tool itself",
        subcommands = {SelfUpdateCommand.class})
public class SelfCommand {}
