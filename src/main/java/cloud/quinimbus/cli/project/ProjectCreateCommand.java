package cloud.quinimbus.cli.project;

import cloud.quinimbus.cli.Logger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.function.Failable;
import picocli.CommandLine;

@CommandLine.Command(name = "create", description = "Create a new QuiNimbus project", showDefaultValues = true)
public class ProjectCreateCommand implements Callable<Integer> {

    @CommandLine.Parameters(description = "The name of the project")
    private String projectName;

    @CommandLine.Option(
            names = {"-d", "--directory"},
            defaultValue = "<projectname>",
            description = "The directory to create the project into.")
    private String directory;

    @CommandLine.Option(
            names = {"-o", "--mvn-output"},
            defaultValue = "false",
            description = "Show the output of the invoked mvn commands, only visible in verbose mode.")
    private boolean showMavenOutput;

    @CommandLine.Option(
            names = {"--skip-archetype-installation"},
            defaultValue = "false",
            description = "Skip installation of archetype, for example to use a locally installed one.")
    private boolean skipArchetypeInstallation;

    @CommandLine.Option(
            names = {"-g", "--groupId"},
            defaultValue = "io.company.cloud",
            description = "The groupId used for the new project")
    private String projectGroupId;

    @CommandLine.Option(
            names = {"-a", "--artifactId"},
            defaultValue = "<projectname>",
            description = "The artifactId used for the new project")
    private String projectArtifactId;

    @CommandLine.Option(
            names = {"-V", "--projectVersion"},
            defaultValue = "0.1-SNAPSHOT",
            description = "The version used for the new project")
    private String projectVersion;

    private final Properties cliProperties;

    public ProjectCreateCommand(Properties cliProperties) {
        this.cliProperties = cliProperties;
    }

    @Override
    public Integer call() throws Exception {
        if (this.directory.equals("<projectname>")) {
            this.directory = this.projectName;
        }
        if (this.projectArtifactId.equals("<projectname>")) {
            this.projectArtifactId = this.projectName;
        }
        var rootDir = Paths.get(this.directory).toAbsolutePath();
        Logger.head("Creating a new QuiNimbus project in %s".formatted(rootDir));
        var res = this.setupProjectDir(rootDir);
        if (res != 0) {
            Logger.error(
                    "Failed to setup the project folder. You could try to run with -v to get more verbose logging.");
            return res;
        }
        this.installMvnw(rootDir);
        if (this.skipArchetypeInstallation) {
            Logger.warn("Skipping installation of the archetype.");
        } else {
            res = this.installArchetype(rootDir);
            if (res != 0) {
                Logger.error(
                        "Failed to install the archetype. You could try to run with -vo to get more verbose logging.");
                return res;
            }
        }
        res = this.createProject(rootDir);
        if (res != 0) {
            Logger.error("Failed to create the project. You could try to run with -vo to get more verbose logging.");
            return res;
        }
        Logger.foot("QuiNimbus project %s successfully created in %s".formatted(this.projectName, rootDir));
        Logger.foot("For a quick start run the following in the project folder: docker-compose up --build");
        return 0;
    }

    public Integer setupProjectDir(Path rootDir) throws IOException {
        Logger.head("Setting up the project folder");
        if (!Files.exists(rootDir)) {
            Logger.verbose("Creating directory %s".formatted(rootDir));
            Files.createDirectories(rootDir);
        } else {
            if (!Files.isDirectory(rootDir)) {
                Logger.error("%s already exists and is no directory".formatted(rootDir));
                return 1;
            }
        }
        return 0;
    }

    public void installMvnw(Path rootDir) throws IOException, URISyntaxException {
        Logger.head("Installing maven wrapper into project folder");
        if (Files.exists(rootDir.resolve("mvnw"))) {
            Logger.warn("%s is already present, skipping mvnw setup".formatted(rootDir.resolve("mvnw")));
            return;
        }
        Logger.verbose("Setup mvnw in %s".formatted(rootDir));
        var mvnwUrl = new URI(this.cliProperties.getProperty("mvnw.download.url")).toURL();
        var zipPath = Files.createTempFile("quinimbus-mvnw-", ".zip");
        Logger.verbose("Downloading %s to %s".formatted(mvnwUrl, zipPath));
        try (var is = mvnwUrl.openStream()) {
            Files.copy(is, zipPath, StandardCopyOption.REPLACE_EXISTING);
        }
        Logger.verbose("Extracting relevant files to %s".formatted(rootDir));
        try (var zipFS = FileSystems.newFileSystem(zipPath)) {
            Files.copy(
                    zipFS.getPath("mvnw"),
                    rootDir.resolve("mvnw"),
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.COPY_ATTRIBUTES);
            Files.copy(
                    zipFS.getPath("mvnw.cmd"),
                    rootDir.resolve("mvnw.cmd"),
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.COPY_ATTRIBUTES);
            var wrapperPath = Files.createDirectories(rootDir.resolve(".mvn/wrapper"));
            Files.copy(
                    zipFS.getPath(".mvn/wrapper/maven-wrapper.jar"),
                    wrapperPath.resolve("maven-wrapper.jar"),
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.COPY_ATTRIBUTES);
        }
        Files.setPosixFilePermissions(rootDir.resolve("mvnw"), PosixFilePermissions.fromString("rwxr--r--"));
        try (var writer = Files.newBufferedWriter(
                rootDir.resolve(".mvn/wrapper/maven-wrapper.properties"),
                Charset.forName("UTF-8"),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING)) {
            writer.write("distributionUrl=%s".formatted(this.cliProperties.get("mvnw.distribution.url")));
        }
        Logger.verbose("Removing %s".formatted(zipPath));
        Files.delete(zipPath);
    }

    private int installArchetype(Path rootDir) throws IOException {
        Logger.head("Installing the quinimbus archetype into the system");
        var version = this.cliProperties.getProperty("cli.version");
        var artifactId = this.cliProperties.getProperty("quinimbus.archetype.artifactId");
        var groupId = this.cliProperties.getProperty("quinimbus.archetype.groupId");
        var repository = this.cliProperties.getProperty("quinimbus.repository");
        var depGetResult = this.runMvn(
                rootDir,
                "-U",
                "dependency:get",
                "-Dartifact=%s:%s:%s".formatted(groupId, artifactId, version),
                "-DremoteRepositories=%s".formatted(repository));
        if (depGetResult != 0) {
            return depGetResult;
        }
        return this.runMvn(rootDir, "archetype:crawl");
    }

    private int createProject(Path rootDir) throws IOException {
        Logger.head("Installing maven wrapper into project folder");
        if (Files.exists(rootDir.resolve("pom.xml"))) {
            Logger.error("%s is already present, you cannot create a new project here"
                    .formatted(rootDir.resolve("pom.xml")));
            return 1;
        }
        Logger.head("Createing the project using the archetype");
        var archetypeVersion = this.cliProperties.getProperty("cli.version");
        var archetypeArtifactId = this.cliProperties.getProperty("quinimbus.archetype.artifactId");
        var archetypeGroupId = this.cliProperties.getProperty("quinimbus.archetype.groupId");
        var result = this.runMvn(
                rootDir,
                "archetype:generate",
                "-DarchetypeGroupId=%s".formatted(archetypeGroupId),
                "-DarchetypeArtifactId=%s".formatted(archetypeArtifactId),
                "-DarchetypeVersion=%s".formatted(archetypeVersion),
                "-DgroupId=%s".formatted(this.projectGroupId),
                "-DartifactId=%s".formatted(this.projectArtifactId),
                "-Dversion=%s".formatted(this.projectVersion),
                "-Dpackage=%s.%s".formatted(this.projectGroupId, this.projectArtifactId),
                "-DinteractiveMode=false");
        Files.newDirectoryStream(rootDir.resolve(this.projectArtifactId))
                .forEach(Failable.asConsumer(
                        p -> Files.move(p, rootDir.resolve(p.getFileName()), StandardCopyOption.REPLACE_EXISTING)));
        Files.delete(rootDir.resolve(this.projectArtifactId));
        return result;
    }

    private int runMvn(Path rootDir, String... args) throws IOException {
        var command = Stream.concat(Stream.of("./mvnw", "--no-transfer-progress"), Arrays.stream(args))
                .toArray(String[]::new);
        Logger.verbose("Running %s".formatted(Arrays.stream(command).collect(Collectors.joining(" "))));
        var process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .directory(rootDir.toFile())
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
}
