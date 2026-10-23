package um.feri.ipt.tids.project.parser.ares;

import java.io.PrintStream;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;

/** Standalone entry point; does not start Spring or a web server. */
public final class AresParserCli {
    private AresParserCli() { }

    public static void main(String[] args) {
        System.exit(run(args, System.out, System.err));
    }

    static int run(String[] args, PrintStream out, PrintStream err) {
        try {
            if (args.length == 1 && args[0].equals("--help")) {
                usage(out);
                return 0;
            }
            if (args.length == 0) throw new IllegalArgumentException("An IČO is required.");
            String ico = Ico.validate(args[0]);
            Path output = Path.of("data", "ares", ico + ".json");
            boolean headed = false;
            String channel = null;
            Duration timeout = Duration.ofSeconds(30);
            for (int i = 1; i < args.length; i++) {
                switch (args[i]) {
                    case "--headed" -> headed = true;
                    case "--output", "--channel", "--timeout-seconds" -> {
                        String option = args[i];
                        if (++i >= args.length) throw new IllegalArgumentException("Missing value for " + option);
                        switch (option) {
                            case "--output" -> output = Path.of(args[i]);
                            case "--channel" -> channel = args[i];
                            default -> timeout = Duration.ofSeconds(Long.parseLong(args[i]));
                        }
                    }
                    default -> throw new IllegalArgumentException("Unknown option: " + args[i]);
                }
            }
            AresResult result = new AresScraper(Clock.systemUTC(), timeout, headed, channel).scrape(ico);
            Path written = new AresJsonExporter().write(result, output);
            out.println(result.status() + ": " + written);
            return switch (result.status()) {
                case SUCCESS -> 0;
                case NOT_FOUND -> 3;
                case PARTIAL -> 4;
                case ERROR -> 1;
            };
        } catch (IllegalArgumentException e) {
            err.println(e.getMessage());
            usage(err);
            return 2;
        } catch (Exception e) {
            err.println("Unable to export ARES JSON: " + e.getMessage());
            return 1;
        }
    }

    private static void usage(PrintStream stream) {
        stream.println("Usage: AresParserCli ICO [--output FILE] [--headed] [--channel chrome|msedge] [--timeout-seconds 1..300]");
    }
}
