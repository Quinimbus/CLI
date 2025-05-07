package cloud.quinimbus.cli.action.maven;

import cloud.quinimbus.cli.Logger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class MavenStep<S extends MavenStep> implements Callable<Integer> {

    private final Path rootDir;
    private final MavenOptions mavenOptions;
    private final String goal;
    private final Map<String, String> properties;

    private boolean showMavenOutput;

    public MavenStep(Path rootDir, MavenOptions mavenOptions, String goal) {
        this.showMavenOutput = false;
        this.rootDir = rootDir;
        this.mavenOptions = mavenOptions;
        this.goal = goal;
        this.properties = new LinkedHashMap<>();
    }

    public S showMavenOutput(boolean show) {
        this.showMavenOutput = show;
        return (S) this;
    }

    public S withProperty(String key, String value) {
        this.properties.put(key, value);
        return (S) this;
    }

    public String[] toCommand() {
        var args = Stream.concat(
                        this.mavenOptions.toArgs().stream(),
                        Stream.concat(this.propertiesToArgs(this.properties).stream(), Stream.of(this.goal)))
                .toArray(String[]::new);
        return Stream.concat(Stream.of("./mvnw", "--no-transfer-progress"), Arrays.stream(args))
                .toArray(String[]::new);
    }

    public Integer call() throws IOException {
        var command = this.toCommand();
        Logger.verbose("Running %s".formatted(Arrays.stream(command).collect(Collectors.joining(" "))));
        var process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .directory(this.rootDir.toFile())
                .start();
        while (process.isAlive()) {
            try (var reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.forName("UTF-8")))) {
                String line;
                do {
                    line = reader.readLine();
                    if (line != null) {
                        if (this.showMavenOutput) {
                            Logger.verbose(line);
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

    private List<String> propertiesToArgs(Map<String, String> properties) {
        return properties.entrySet().stream()
                .map(entry -> "-D%s=%s".formatted(entry.getKey(), entry.getValue()))
                .toList();
    }
}
