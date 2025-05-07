package cloud.quinimbus.cli.action.maven;

import io.soabase.recordbuilder.core.RecordBuilder;

@RecordBuilder
public record MavenArtifact(String groupId, String artifactId, String version) {}
