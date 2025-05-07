package cloud.quinimbus.cli.action.maven;

import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class MavenStepTest {

    private class TestMavenStep extends MavenStep {
        public TestMavenStep(Path rootDir, MavenOptions mavenOptions, String goal) {
            super(rootDir, mavenOptions, goal);
        }
    }

    @Test
    public void testToCommand() {
        var rootDir = Path.of("/tmp/project");
        var step = new TestMavenStep(
                        rootDir,
                        MavenOptionsBuilder.builder()
                                .updateSnapshots(true)
                                .settingsxml("settings.xml")
                                .build(),
                        "clean install")
                .withProperty("prop1", "val1")
                .withProperty("prop2", "val2");
        var command = step.toCommand();
        assertArrayEquals(
                new String[] {
                    "./mvnw",
                    "--no-transfer-progress",
                    "-U",
                    "-ssettings.xml",
                    "-Dprop1=val1",
                    "-Dprop2=val2",
                    "clean install"
                },
                command);
    }
}
