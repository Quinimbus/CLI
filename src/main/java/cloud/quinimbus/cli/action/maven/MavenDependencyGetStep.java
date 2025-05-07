package cloud.quinimbus.cli.action.maven;

import java.io.IOException;
import java.nio.file.Path;

public class MavenDependencyGetStep extends MavenStep<MavenDependencyGetStep> {

    private final MavenArtifact artifact;

    public MavenDependencyGetStep(Path rootDir, MavenOptions mavenOptions, MavenArtifact artifact) {
        super(rootDir, mavenOptions, "dependency:get");
        this.artifact = artifact;
    }

    @Override
    public Integer call() throws IOException {
        this.withProperty(
                "artifact",
                "%s:%s:%s".formatted(this.artifact.groupId(), this.artifact.artifactId(), this.artifact.version()));
        return super.call();
    }
}
