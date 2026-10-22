package tests.unit;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The bot's output must reach the IDE console (owner question, 2026-10-07:
 * "is there a way to see a println in the IntelliJ console?").
 *
 * <p>
 * The reason it did not: the bot is a <b>separate process</b> - a Windows JVM
 * under Wine, started by this script - so its {@code System.out} goes to the
 * script's redirected file, not to the IDE. On Windows it worked because the
 * bot
 * <em>was</em> the IDE process. The owner's prints were all present in
 * {@code out/wine/client.log} (13 524 lines of them) while the console showed
 * nothing, which reads as "the code never runs".
 * </p>
 *
 * <p>
 * The fix is a {@code tee}: the log file keeps everything (scripts and greps
 * read it) and the same lines are streamed to the script's stdout, which
 * {@code UnixChaosGameLauncher} shows live through
 * {@code ProcessBuilder.inheritIO()}. Since the stream split there are two
 * tees - game lines and diagnostics travel in separate files but still share
 * the live console.
 * </p>
 */
@Tag("infra")
public class BotOutputReachesConsoleTest {

        private static final Path WINE_FULL = Paths.get("scripts/run-wine-full.sh");
        private static final Path WINE_UMS = Paths.get("scripts/run-wine-ums.sh");
        private static final Path LAUNCHER = Paths.get("src/atlantis/config/launcher/UnixChaosGameLauncher.java");

        @Test
        public void botOutputIsStreamedToTheScriptStdout() throws IOException {
                String script = read(WINE_FULL);

                assertTrue(script.contains("tee \"${TEE_OUT[@]}\""),
                                "the bot's game output must be tee'd: the log file keeps everything and the"
                                                + " script's stdout (the IDE console) shows it live");
                assertTrue(script.contains("tee \"${TEE_ERR[@]}\""),
                                "diagnostics travel on a second live tee into their own file");
                assertTrue(script.contains("LIVE_OUTPUT"),
                                "streaming must have an off switch for a quiet console");
        }

        @Test
        public void theLogFileIsStillWrittenInBothModes() throws IOException {
                // Both branches must write the log: turning streaming off may not lose
                // the artefact that scripts and diagnosis depend on.
                String script = read(WINE_FULL);

                assertTrue(script.contains("TEE_OUT=(\"$CLIENT_LOG\")"),
                                "both branches tee game output starting at the client log");
                assertTrue(script.contains("TEE_ERR=(\"$LOG_DIR/WINE_DEBUG\")"),
                                "diagnostics start at their own file in both branches");
                assertTrue(script.contains("> /dev/null"),
                                "quiet mode must still write the log files while silencing the console");
        }

        @Test
        public void visibleWineDesktopIsTheDefault() throws IOException {
                // Owner 2026-10-21, second ruling the same day: IDE runs must
                // show the game. Visible is the default everywhere; --headless
                // is the opt-in for unattended runs (overnight batches etc.).
                // Owner 2026-10-22 speed directive supersedes the lifecycle
                // half: Xvfb holds no game state, so it persists across runs
                // (and starts before the box lock, so it never inherits the
                // lock fd - measured REFUSED with Xvfb as the sole holder).
                String script = read(WINE_FULL);
                String ums = read(WINE_UMS);

                assertTrue(script.contains("\"${HIDDEN:-0}\""),
                                "visible must be the default, never opt-in");
                assertTrue(script.contains("--headless)"),
                                "unattended runs opt into the hidden X server");
                assertTrue(script.contains("Xvfb :99"),
                                "headless mode runs its own X server");
                assertTrue(script.contains("export DISPLAY=:99"),
                                "headless mode points the whole session at it");
                assertTrue(script.contains("reused, not restarted"),
                                "the hidden server persists across runs instead of paying boot each game");
                assertFalse(script.contains("kill \"$XVFB_PID\""),
                                "nothing kills the reused server at teardown anymore");
                assertTrue(ums.contains("--headless"),
                                "single UMS games offer headless explicitly");
                assertTrue(ums.contains("--visible)"),
                                "single UMS games can still be forced visible");
        }

        @Test
        public void theLauncherShowsTheScriptOutputInTheIde() throws IOException {
                // The streaming only helps because the launcher inherits the script's
                // I/O; without inheritIO() the tee'd lines would go nowhere the owner
                // can see.
                String launcher = read(LAUNCHER);

                assertTrue(launcher.contains("inheritIO()"),
                                "UnixChaosGameLauncher must inherit the script's I/O, or streaming the"
                                                + " bot's output has no destination in the IDE");
                assertTrue(launcher.contains("run-wine-full.sh"),
                                "the launcher must run the script that does the streaming");
        }

        @Test
        public void theHeaderDocumentsHowToSeeOutput() throws IOException {
                // The owner asked how to see a println; the answer belongs where they
                // will look, not only in a commit message.
                String script = read(WINE_FULL);

                assertTrue(script.contains("Seeing the bot's output in the IDE console"),
                                "the script header must explain how bot output reaches the IDE");
                assertTrue(script.contains("LIVE_OUTPUT=0") && script.contains("BUILD=0"),
                                "the header must document both switches");
        }

        @Test
        public void theBotStillUsesOneLoggingPath() throws IOException {
                // A.println goes through the LogPort seam (AConsole -> SystemLogPort),
                // so it lands on System.out exactly like System.out.println. Pinning
                // that keeps "which call do I use" from becoming a question again.
                String port = read(Paths.get("src/atlantis/util/log/SystemLogPort.java"));
                assertTrue(port.contains("System.out"),
                                "SystemLogPort is the one place that writes to the console");

                String console = read(Paths.get("src/atlantis/util/AConsole.java"));
                assertTrue(console.contains("port.println"),
                                "AConsole.println must go through the port, so A.println and"
                                                + " System.out.println end up in the same stream");
        }

        @Test
        public void scriptParses() throws Exception {
                Process p = new ProcessBuilder("bash", "-n", WINE_FULL.toAbsolutePath().toString())
                                .redirectErrorStream(true).start();
                byte[] chunk = new byte[4096];
                while (p.getInputStream().read(chunk) != -1) {
                        // drain (Java 8: no readAllBytes)
                }
                assertEquals(0, p.waitFor(), "run-wine-full.sh must parse as bash");
        }

        @Test
        public void noStrayStreamingWhenDisabled() throws IOException {
                // LIVE_OUTPUT=0 must stay quiet on the console: the quiet branch
                // pipes through tee (so the log files still land) but throws
                // tee's own stdout away.
                String script = read(WINE_FULL);
                assertTrue(script.contains("> /dev/null"),
                                "the quiet branch must silence the console while still writing the log files");
        }

        @Test
        public void wineLogCopyStaysRetired() throws IOException {
                // WINE_LOG_COPY retired (owner): out/wine/ is the only
                // destination now (WINE_LOG game log, WINE_DEBUG
                // diagnostics, WINE_DECISIONS audit). The per-ENV live
                // copy with its resolver and tee plumbing is gone - this
                // pins the absence so it is not quietly re-added.
                String script = read(WINE_FULL);

                assertFalse(script.contains("WINE_LOG_COPY"),
                                "no per-ENV live copy mechanism in the runner");
                assertFalse(script.contains("resolve_wine_log_copy"),
                                "no live-copy resolver in the runner");
                assertFalse(script.contains("WINE_LIVE_"),
                                "no live-copy destinations in the runner");
        }

        private static String read(Path path) throws IOException {
                assertTrue(Files.exists(path), "missing file: " + path.toAbsolutePath());

                return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        }
}
