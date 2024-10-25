package cloud.quinimbus.cli;

import cloud.quinimbus.cli.project.ProjectBuildImagesCommand;
import cloud.quinimbus.cli.project.ProjectCreateCommand;
import cloud.quinimbus.cli.project.ProjectRunContainersCommand;
import cloud.quinimbus.cli.project.ProjectStopContainersCommand;
import picocli.CommandLine;

@CommandLine.Command(
        name = "project",
        description = "Create and manage projects",
        subcommands = {
            ProjectCreateCommand.class,
            ProjectBuildImagesCommand.class,
            ProjectRunContainersCommand.class,
            ProjectStopContainersCommand.class
        })
public class ProjectCommand {}
