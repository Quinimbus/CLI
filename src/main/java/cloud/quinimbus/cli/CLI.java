package cloud.quinimbus.cli;

import java.io.IOException;
import java.util.Properties;
import org.fusesource.jansi.AnsiConsole;
import picocli.CommandLine;

public class CLI {

    public static void main(String[] args) throws IOException {
        AnsiConsole.systemInstall();
        try (var is = QuiNimbusCommand.class.getResourceAsStream("/cli.properties")) {
            var p = new Properties();
            p.load(is);
            CommandLine.IFactory factory = new IFactoryImpl(p);
            var exitCode = new CommandLine(new QuiNimbusCommand(p), factory)
                    .setUsageHelpWidth(120)
                    .setUsageHelpLongOptionsMaxWidth(36)
                    .execute(args);
            System.exit(exitCode);
        } finally {
            AnsiConsole.systemUninstall();
        }
    }

    private static class IFactoryImpl implements CommandLine.IFactory {

        private final Properties properties;

        public IFactoryImpl(Properties properties) {
            this.properties = properties;
        }

        @Override
        public <K> K create(Class<K> cls) throws Exception {
            try {
                var propertiesBasedConstructor = cls.getConstructor(Properties.class);
                return propertiesBasedConstructor.newInstance(properties);
            } catch (NoSuchMethodException e) {
                return CommandLine.defaultFactory().create(cls); // fallback if missing
            }
        }
    }
}
