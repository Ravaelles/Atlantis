package atlantis.config.launcher.openbw;

/**
 * The verdict of one headless OpenBW run: the facts a caller asserts on.
 *
 * <p>
 * An exit code is not evidence that a game happened - a bot that never attached
 * exits just as quietly as one that played (measured 2026-10-08). So the
 * verdict
 * is read from the log: did the client attach, did the bot actually start
 * playing, how far did the game get, and if it failed, why.
 * </p>
 */
public final class OpenBWRunResult {

    private final boolean attached;
    private final boolean playing;
    private final int lastFrame;
    private final String serverLogPath;
    private final String botLogPath;
    private final String failureReason;

    OpenBWRunResult(boolean attached, boolean playing, int lastFrame,
            String serverLogPath, String botLogPath, String failureReason) {
        this.attached = attached;
        this.playing = playing;
        this.lastFrame = lastFrame;
        this.serverLogPath = serverLogPath;
        this.botLogPath = botLogPath;
        this.failureReason = failureReason;
    }

    public static OpenBWRunResult hostDidNotStart(String serverLogPath, String botLogPath) {
        return new OpenBWRunResult(false, false, 0, serverLogPath, botLogPath,
                "the OpenBW host never came up");
    }

    public boolean attached() {
        return attached;
    }

    /** True when the bot reached its own game logic, not merely the connection. */
    public boolean playing() {
        return playing;
    }

    public int lastFrame() {
        return lastFrame;
    }

    public String serverLogPath() {
        return serverLogPath;
    }

    public String botLogPath() {
        return botLogPath;
    }

    /** Null on success; the reason on failure. */
    public String failureReason() {
        return failureReason;
    }

    public boolean isSuccess() {
        return attached && playing;
    }

    @Override
    public String toString() {
        if (isSuccess()) {
            return "OpenBW run OK (attached, playing, last frame " + lastFrame + ") - logs: " + botLogPath;
        }
        return "OpenBW run FAILED: " + failureReason + " (logs: " + botLogPath + ")";
    }
}
