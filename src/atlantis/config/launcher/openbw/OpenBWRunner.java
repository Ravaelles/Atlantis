package atlantis.config.launcher.openbw;

import atlantis.Atlantis;
import atlantis.util.log.ErrorLog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Runs one headless OpenBW game end to end: host, attach, play, tear down,
 * verdict.
 *
 * <p>
 * This is the class that replaces the shell script's sequencing. It is
 * deliberately explicit - each step is a named method and each step is reported
 * -
 * because the whole point of the exercise is that a reader (and a test) can see
 * what happens and in what order.
 * </p>
 */
public final class OpenBWRunner {

    /** The line the client prints once it is in the game. */
    private static final String ATTACH_MARKER = "Connection successful";
    /** The line the bot prints once it is running its logic. */
    private static final String PLAYING_MARKER = "Atlantis is playing!";
    private static final Pattern FRAME = Pattern.compile("Frame (\\d+)");

    private final OpenBWConfig config;
    private final OpenBWHost host;

    public OpenBWRunner(OpenBWConfig config) {
        this.config = config;
        this.host = new OpenBWHost(config);
    }

    /**
     * The full lifecycle. Never throws: a failed run is a {@link OpenBWRunResult}
     * with {@code attached == false} and the reason, which is what a caller needs
     * to decide whether to retry or fail its test.
     */
    public OpenBWRunResult run() {
        int hostPid = host.host();
        if (hostPid < 0) {
            host.teardown();
            return OpenBWRunResult.hostDidNotStart(serverLogPath(), botLogPath());
        }

        ErrorLog.printPlusToFile("OpenBW hosted (pid " + hostPid + "), attaching the bot...");

        try {
            Atlantis.getInstance().run();
            // Atlantis.run() returns when the game ends (frame limit, timeout or
            // exit); the verdict below reads what actually happened from the log.
            return verdict();
        } catch (Throwable t) {
            // The client died: that is a result, not a crash of the test runner.
            ErrorLog.printPlusToFile("OpenBW: the bot session ended with " + t);
            return verdict();
        } finally {
            host.teardown();
        }
    }

    /** Reads the run back from the bot log - the same evidence a human would. */
    public OpenBWRunResult verdict() {
        String log = readLog(botLogPath());
        boolean attached = log != null && log.contains(ATTACH_MARKER);
        boolean playing = log != null && log.contains(PLAYING_MARKER);
        int lastFrame = lastFrame(log);

        // Attaching is not playing: a client that connects and then does nothing
        // is exactly the failure this whole API exists to catch, so the reason is
        // reported whenever the bot did not reach its game logic - not only when
        // the connection itself failed.
        String failure = (attached && playing) ? null : failureReason(log, attached);

        return new OpenBWRunResult(attached, playing, lastFrame, serverLogPath(), botLogPath(), failure);
    }

    private String failureReason(String log, boolean attached) {
        if (log == null) return "no bot log was written";
        if (log.contains("BUILD ORDER is NULL")) return "the bot had no build order";
        if (log.contains("Race is not set")) return "the bot had no race";
        if (log.contains("AtlantisRaceConfig")) return "the race/config check failed";
        if (!attached) return "the client never attached";
        return "the bot attached but never reached its game logic (no build order, no mission)";
    }

    private int lastFrame(String log) {
        if (log == null)
            return 0;
        Matcher matcher = FRAME.matcher(log);
        int last = 0;
        while (matcher.find())
            last = Integer.parseInt(matcher.group(1));
        return last;
    }

    private String readLog(String path) {
        try {
            File file = new File(path);
            if (!file.isFile())
                return null;
            List<String> lines = Files.readAllLines(file.toPath());
            StringBuilder builder = new StringBuilder();
            for (String line : lines)
                builder.append(line).append('\n');
            return builder.toString();
        } catch (IOException e) {
            return null;
        }
    }

    public String botLogPath() {
        return config.logDir() + "/bot.log";
    }

    public String serverLogPath() {
        return config.logDir() + "/server.log";
    }
}
