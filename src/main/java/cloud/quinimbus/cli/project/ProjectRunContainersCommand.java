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
        name = "run-containers",
        description = "Run docker images of this project as conmtainers.",
        showDefaultValues = true)
public class ProjectRunContainersCommand extends DockerCommand implements Callable<Integer> {

    private static final Pattern DOCKER_COMPOSE_CONTAINER_STARTED =
            Pattern.compile("^\\s+Container\\s+([\\w\\d\\.\\-]+)\\s+Started$");

    private final List<String> startedContainers = new ArrayList<>();

    @Override
    public Integer call() throws Exception {
        var res = this.composeUp();
        if (res != 0) {
            Logger.error("Failed to start all docker containers");
            return res;
        }
        if (startedContainers.isEmpty()) {
            Logger.warn("No containers started, please check verbose output.");
        } else {
            Logger.foot("Successfully started all containers");
            Logger.info("Started containers:\n\t" + startedContainers.stream().collect(Collectors.joining("\n\t")));
            Logger.info("To access the backend use http://localhost:8080/");
            Logger.info("To access the admin-ui use http://localhost:8081/");
            Logger.info("To access the Mongo DB use localhost:27017");
            Logger.info("To stop the containers run: quinimbus project stop-containers");
        }
        return 0;
    }

    private int composeUp() throws IOException {
        Logger.head("Starting all relevant docker containers");
        return this.runDocker(
                line -> {
                    var startedContainersMatcher = DOCKER_COMPOSE_CONTAINER_STARTED.matcher(line);
                    if (startedContainersMatcher.matches()) {
                        startedContainers.add(startedContainersMatcher.group(1));
                    }
                },
                "compose",
                "up",
                "--detach",
                "--quiet-pull",
                "--force-recreate");
    }
}
