package tests.unit;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The launcher must rebuild the jar it is about to play.
 *
 * <p>
 * Measured failure this guards against (2026-10-07): the owner added prints,
 * ran a game through the IDE, and saw NONE of them - so the bot looked like it
 * never ran its commanders at all. The jar on disk was from 17:56 while the
 * sources were from 20:25: the launcher starts a jar and nothing rebuilt it, so
 * hours of changes were never in the game. A stale artifact is worse than a
 * broken build, because everything you check afterwards is a lie.
 * </p>
 *
 * <p>
 * This is a script-reading test (the launcher is shell, so it cannot be
 * executed
 * here) plus a real freshness check on the artifact itself when it exists.
 * </p>
 */
public class LauncherBuildsTheJarTest {

    private static final Path WINE_FULL = Paths.get("scripts/run-wine-full.sh");
    private static final Path JAR = Paths.get("bots/AtlantisP/AI/Atlantis.jar");

    @Test
    public void wineFullRebuildsTheJarBeforePlaying() throws IOException {
        String script = read(WINE_FULL);

        assertTrue(script.contains("build-bot-jar.sh"),
                "run-wine-full.sh must rebuild the jar it plays - a launcher that"
                        + " starts a stale jar makes every test of new code meaningless");
        assertTrue(script.contains("BUILD=0"),
                "there must be an escape hatch for a deliberately frozen jar");
        assertTrue(script.contains("Building the bot jar from source"),
                "the rebuild must be visible in the log, not silent");
    }

    @Test
    public void theRebuildHappensBeforeTheGameStarts() throws IOException {
        // Order matters: rebuilding after launching StarCraft would play the old
        // jar anyway, which is exactly the bug this file exists for.
        //
        // Match the LAUNCH line, not any mention of Chaoslauncher: the script
        // also `pkill`s a leftover Chaoslauncher.exe twice, and a naive
        // indexOf("Chaoslauncher.exe") matches the first cleanup instead of the
        // start (measured - this test failed on its own first version for
        // exactly that reason).
        String script = read(WINE_FULL);

        int rebuild = script.indexOf("build-bot-jar.sh");
        int launch = script.indexOf("wine chaoslauncher/Chaoslauncher.exe");

        assertTrue(rebuild > 0, "no rebuild step found");
        assertTrue(launch > 0, "no game launch found");
        assertTrue(rebuild < launch,
                "the jar must be rebuilt BEFORE StarCraft is started"
                        + " (rebuild at char " + rebuild + ", launch at " + launch + ")");
    }

    @Test
    public void theExistingJarIsNotOlderThanTheSources() throws IOException {
        // Only checks when a jar is present; a missing jar is the launcher's
        // problem (it builds one), not this test's.
        if (!Files.exists(JAR))
            return;

        long jarTime = Files.getLastModifiedTime(JAR).toMillis();

        // The sources that decide production behaviour. If any of these is newer
        // than the jar, whoever runs a game now is testing old code.
        List<Path> watched = new ArrayList<>();
        watched.add(Paths.get("src/atlantis/game/AtlantisGameCommander.java"));
        watched.add(Paths.get("src/atlantis/production/ProductionCommander.java"));
        watched.add(Paths.get("src/atlantis/util/We.java"));
        watched.add(Paths.get("src/atlantis/config/AtlantisConfigChanger.java"));
        watched.add(Paths.get("src/atlantis/Atlantis.java"));
        watched.add(Paths.get("src/main/Main.java"));

        List<String> stale = new ArrayList<>();
        for (Path source : watched) {
            if (!Files.exists(source))
                continue;
            long sourceTime = Files.getLastModifiedTime(source).toMillis();
            if (sourceTime > jarTime) {
                stale.add(source + " (" + (sourceTime - jarTime) / 1000 + "s newer)");
            }
        }

        assertTrue(stale.isEmpty(),
                "bots/AtlantisP/AI/Atlantis.jar is OLDER than these sources, so a game"
                        + " started now would run old code and hide the changes: " + stale
                        + " -- rebuild with scripts/build-bot-jar.sh (run-wine-full.sh"
                        + " now does it automatically)");
    }

    @Test
    public void scriptParses() throws Exception {
        Process p = new ProcessBuilder("bash", "-n", WINE_FULL.toAbsolutePath().toString())
                .redirectErrorStream(true).start();
        // Java 8: InputStream.readAllBytes() is Java 9+. Drain the stream by
        // hand - a Java 9+ API in a test breaks the GAME JAR build, because the
        // whole tree is compiled in one javac invocation (CONVENTIONS §16).
        byte[] chunk = new byte[4096];
        while (p.getInputStream().read(chunk) != -1) {
            // drain
        }
        assertEquals(0, p.waitFor(), "run-wine-full.sh must parse as bash");
    }

    private static String read(Path path) throws IOException {
        assertTrue(Files.exists(path), "missing file: " + path.toAbsolutePath());
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}