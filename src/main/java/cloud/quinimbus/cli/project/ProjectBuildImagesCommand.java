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
        name = "build-images",
        description = "Build docker images of this project.",
        showDefaultValues = true)
public class ProjectBuildImagesCommand extends DockerCommand implements Callable<Integer> {

    private static final Pattern DOCKER_BAKE_EXTRACT_BUILT_IMAGE =
            Pattern.compile("^.*naming to ([\\w\\d\\./\\-:]+) done$");

    @CommandLine.Option(
            names = {"-c", "--clean"},
            defaultValue = "false",
            description = "Do not use cache and repull all images.")
    private boolean clean;

    private final List<String> builtImages = new ArrayList<>();

    @Override
    public Integer call() throws Exception {
        var res = this.buildBackendImage();
        if (res != 0) {
            Logger.error("Failed to build the backend image");
            return res;
        }
        res = this.buildAdminUiImage();
        if (res != 0) {
            Logger.error("Failed to build the admin-ui image");
            return res;
        }
        Logger.foot("Project images successfully built");
        if (builtImages.isEmpty()) {
            Logger.warn(
                    "No build images could be extracted from the output of docker bake, please check verbose output.");
        } else {
            Logger.info("Built images:\n\t" + builtImages.stream().collect(Collectors.joining("\n\t")));
        }
        Logger.info(
                "To test the created images run the following in the project folder: quinimbus project run-containers");
        return 0;
    }

    private int buildBackendImage() throws IOException {
        Logger.head("Building backend image");
        if (this.clean) {
            return this.runDocker("buildx", "bake", "-f", "docker-bake.hcl", "--no-cache", "--pull");
        } else {
            return this.runDocker("buildx", "bake", "-f", "docker-bake.hcl");
        }
    }

    private int buildAdminUiImage() throws IOException {
        Logger.head("Building admin-ui image");
        if (this.clean) {
            return this.runDocker("buildx", "bake", "-f", "docker-bake.hcl", "--no-cache", "--pull", "admin-ui");
        } else {
            return this.runDocker("buildx", "bake", "-f", "docker-bake.hcl", "admin-ui");
        }
    }

    private int runDocker(String... args) throws IOException {
        return this.runDocker(
                line -> {
                    var buildImageMatcher = DOCKER_BAKE_EXTRACT_BUILT_IMAGE.matcher(line);
                    if (buildImageMatcher.matches()) {
                        builtImages.add(buildImageMatcher.group(1));
                    }
                },
                args);
    }
}
