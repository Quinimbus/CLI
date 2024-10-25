package cloud.quinimbus.cli.project;

import cloud.quinimbus.cli.Logger;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import picocli.CommandLine;

@CommandLine.Command(
        name = "stop-containers",
        description = "Stop docker containers of this project.",
        showDefaultValues = true)
public class ProjectStopContainersCommand extends DockerCommand implements Callable<Integer> {

    private static final Pattern DOCKER_COMPOSE_CONTAINER_STOPPED =
            Pattern.compile("^\\s+Container\\s+([\\w\\d\\.\\-]+)\\s+Stopped$");

    @CommandLine.Option(
            names = {"-c", "--clear"},
            defaultValue = "false",
            description = "")
    private boolean clear;

    private final List<String> stoppedContainers = new ArrayList<>();

    @Override
    public Integer call() throws Exception {
        var res = this.composeDown();
        if (res != 0) {
            Logger.error("Failed to stop all docker containers");
            return res;
        }
        if (stoppedContainers.isEmpty()) {
            Logger.warn("No containers stopped, please check verbose output.");
        } else {
            Logger.foot("Successfully stopped all containers");
            Logger.info("Stopped containers:\n\t" + stoppedContainers.stream().collect(Collectors.joining("\n\t")));
        }
        return 0;
    }

    private int composeDown() throws IOException {
        Logger.head("Stopping all relevant docker containers");
        String[] args;
        if (this.clear) {
            args = new String[] {"compose", "down", "--volumes"};
        } else {
            args = new String[] {"compose", "down"};
        }
        return this.runDocker(
                line -> {
                    var stoppedContainersMatcher = DOCKER_COMPOSE_CONTAINER_STOPPED.matcher(line);
                    if (stoppedContainersMatcher.matches()) {
                        stoppedContainers.add(stoppedContainersMatcher.group(1));
                    }
                },
                args);
    }
}
