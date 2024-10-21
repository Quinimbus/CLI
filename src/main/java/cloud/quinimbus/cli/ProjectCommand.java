package cloud.quinimbus.cli;

import cloud.quinimbus.cli.project.ProjectBuildImageCommand;
import cloud.quinimbus.cli.project.ProjectCreateCommand;
import picocli.CommandLine;

@CommandLine.Command(
        name = "project",
        description = "Create and manage projects",
        subcommands = {ProjectCreateCommand.class, ProjectBuildImageCommand.class})
public class ProjectCommand {}
