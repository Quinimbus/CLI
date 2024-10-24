package cloud.quinimbus.cli.project;

import cloud.quinimbus.cli.Logger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import picocli.CommandLine;

@CommandLine.Command(
        name = "build-image",
        description = "Build docker images of this project.",
        showDefaultValues = true)
public class ProjectBuildImageCommand implements Callable<Integer> {

    private static Pattern DOCKER_BAKE_EXTRACT_BUILT_IMAGE = Pattern.compile("^.*naming to ([\\w\\d\\./\\-:]+) done$");

    @CommandLine.Option(
            names = {"-o", "--docker-output"},
            defaultValue = "false",
            description = "Show the output of the invoked docker commands, only visible in verbose mode.")
    private boolean showDockerOutput;

    private List<String> builtImages = new ArrayList<>();

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
        return 0;
    }

    private int buildBackendImage() throws IOException {
        Logger.head("Building backend image");
        return this.runDocker("buildx", "bake", "-f", "docker-bake.hcl");
    }

    private int buildAdminUiImage() throws IOException {
        Logger.head("Building admin-ui image");
        return this.runDocker("buildx", "bake", "-f", "docker-bake.hcl", "admin-ui");
    }

    private int runDocker(String... args) throws IOException {
        var command = Stream.concat(Stream.of("docker"), Arrays.stream(args)).toArray(String[]::new);
        Logger.verbose("Running %s".formatted(Arrays.stream(command).collect(Collectors.joining(" "))));
        var process = new ProcessBuilder(command).redirectErrorStream(true).start();
        while (process.isAlive()) {
            try (var reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.forName("UTF-8")))) {
                String line;
                do {
                    line = reader.readLine();
                    if (line != null) {
                        if (this.showDockerOutput) {
                            Logger.verbose(line);
                        }
                        var buildImageMatcher = DOCKER_BAKE_EXTRACT_BUILT_IMAGE.matcher(line);
                        if (buildImageMatcher.matches()) {
                            builtImages.add(buildImageMatcher.group(1));
                        }
                    }
                } while (line != null);
            }
            try {
                Thread.sleep(Duration.ofMillis(100));
            } catch (InterruptedException ex) {
            }
        }
        return process.exitValue();
    }
}
