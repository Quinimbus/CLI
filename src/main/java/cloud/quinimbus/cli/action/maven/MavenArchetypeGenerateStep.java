package cloud.quinimbus.cli.action.maven;

import java.io.IOException;
import java.nio.file.Path;

/*
        var result = this.runMvn(
                rootDir,
                "archetype:generate",
                "-ssettings.xml",
                "-DarchetypeGroupId=%s".formatted(archetypeGroupId),
                "-DarchetypeArtifactId=%s".formatted(archetypeArtifactId),
                "-DarchetypeVersion=%s".formatted(archetypeVersion),
                "-DgroupId=%s".formatted(this.projectGroupId),
                "-DartifactId=%s".formatted(this.projectArtifactId),
                "-Dversion=%s".formatted(this.projectVersion),
                "-Dpackage=%s.%s".formatted(this.projectGroupId, this.projectArtifactId),
                "-DinteractiveMode=false");
*/

public class MavenArchetypeGenerateStep extends MavenStep<MavenArchetypeGenerateStep> {

    private final MavenArtifact archetypeArtifact;

    public MavenArchetypeGenerateStep(Path rootDir, MavenOptions mavenOptions, MavenArtifact archetypeArtifact) {
        super(rootDir, mavenOptions, "archetype:generate");
        this.archetypeArtifact = archetypeArtifact;
    }

    @Override
    public Integer call() throws IOException {
        this.withProperty("interactiveMode", "false")
                .withProperty("archetypeGroupId", this.archetypeArtifact.groupId())
                .withProperty("archetypeArtifactId", this.archetypeArtifact.artifactId())
                .withProperty("archetypeVersion", this.archetypeArtifact.version());
        return super.call();
    }
}
