package cloud.quinimbus.cli.action.maven;

import io.soabase.recordbuilder.core.RecordBuilder;
import java.util.LinkedList;
import java.util.List;

@RecordBuilder
public record MavenOptions(boolean updateSnapshots, String settingsxml) {

    public List<String> toArgs() {
        var args = new LinkedList<String>();
        if (updateSnapshots) {
            args.add("-U");
        }
        if (settingsxml != null) {
            args.add("-s%s".formatted(settingsxml));
        }
        return args;
    }
}
