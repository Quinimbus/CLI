package cloud.quinimbus.cli.project;

import cloud.quinimbus.cli.Logger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import picocli.CommandLine;

public class DockerCommand {

    @CommandLine.Option(
            names = {"-o", "--docker-output"},
            defaultValue = "false",
            description = "Show the output of the invoked docker commands, only visible in verbose mode.")
    private boolean showDockerOutput;

    protected int runDocker(Consumer<String> lineReader, String... args) throws IOException {
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
                        lineReader.accept(line);
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
