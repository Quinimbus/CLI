package cloud.quinimbus.cli.self;

import cloud.quinimbus.cli.Logger;
import java.util.concurrent.Callable;
import picocli.CommandLine;

@CommandLine.Command(name = "update", description = "Update the QuiNimbus CLI tool")
public class SelfUpdateCommand implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        Logger.error("NOT YET IMPLEMENTED");
        return 1;
    }
}
