package cloud.quinimbus.cli;

import static org.fusesource.jansi.Ansi.ansi;

public class Logger {

    private static boolean verbose;

    public static void setVerbose(boolean verbose) {
        Logger.verbose = verbose;
    }

    public static void verbose(String msg) {
        if (verbose) {
            System.out.println(ansi().fgBrightDefault().a(msg));
        }
    }

    public static void info(String msg) {
        System.out.println(msg);
    }

    public static void head(String msg) {
        System.out.println(ansi().bold().a(msg).reset());
    }

    public static void foot(String msg) {
        System.out.println(ansi().bold().a(msg).reset());
    }

    public static void error(String msg) {
        System.out.println(ansi().bold().fgRed().a("ERROR: ").a(msg).reset());
    }

    public static void warn(String msg) {
        System.out.println(ansi().bold().fgYellow().a("WARNING: ").a(msg).reset());
    }
}
