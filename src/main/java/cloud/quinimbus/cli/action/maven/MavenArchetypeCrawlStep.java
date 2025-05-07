package cloud.quinimbus.cli.action.maven;

import java.nio.file.Path;

public class MavenArchetypeCrawlStep extends MavenStep<MavenArchetypeCrawlStep> {

    public MavenArchetypeCrawlStep(Path rootDir, MavenOptions mavenOptions) {
        super(rootDir, mavenOptions, "archetype:crawl");
    }
}
