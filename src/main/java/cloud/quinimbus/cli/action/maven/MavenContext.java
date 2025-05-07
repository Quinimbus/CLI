package cloud.quinimbus.cli.action.maven;

import java.nio.file.Path;

public class MavenContext {

    private final Path rootDir;
    private final MavenOptions defaultOptions;
    private final boolean showMavenOutput;

    public MavenContext(Path rootDir, MavenOptions defaultOptions, boolean showMavenOutput) {
        this.rootDir = rootDir;
        this.defaultOptions = defaultOptions;
        this.showMavenOutput = showMavenOutput;
    }

    public Path getRootDir() {
        return rootDir;
    }

    public MavenDependencyGetStep dependencyGet(MavenArtifact artifact) {
        return new MavenDependencyGetStep(this.rootDir, this.defaultOptions, artifact).showMavenOutput(showMavenOutput);
    }

    public MavenArchetypeCrawlStep archetypeCrawl() {
        return new MavenArchetypeCrawlStep(this.rootDir, this.defaultOptions);
    }

    public MavenArchetypeGenerateStep archetypeGenerate(MavenArtifact archetypeArtifact) {
        return new MavenArchetypeGenerateStep(this.rootDir, this.defaultOptions, archetypeArtifact);
    }
}
