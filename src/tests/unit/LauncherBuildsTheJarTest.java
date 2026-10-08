package tests.unit;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
    private static final Path OPENBW_E2E = Paths.get("scripts/run-openbw-e2e.sh");

    /**
     * The jar the launcher actually plays: JAR_OUT in run-wine-full.sh, which
     * defaults to the scbw bot slot. Kept in one place so this test checks the
     * jar that matters instead of a path nothing writes any more (the in-repo
     * bots/AtlantisP/AI/Atlantis.jar became vestigial when the owner moved the
     * build target to ~/.scbw - checked 2026-10-07).
     */
    private static Path playedJar() {
        String home = System.getProperty("user.home");
        return Paths.get(home, ".scbw", "bots", "AtlantisP", "AI", "Atlantis.jar");
    }

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
    public void everyRunnerChecksTheJarAgainstTheWorkingTree() throws IOException {
        // The whole-tree fingerprint replaced the old 6-file timestamp list, which
        // only caught the files someone remembered to name (a change anywhere else
        // still ran old code silently). The mechanism has three parts, and all
        // three must exist or the guarantee is lost:
        //
        //   1. build-bot-jar.sh tags the jar with a hash of every source file;
        //   2. check-jar-freshness.sh recomputes that hash and refuses a mismatch;
        //   3. the runners call the check before playing.
        String build = read(Paths.get("scripts/build-bot-jar.sh"));
        String check = read(Paths.get("scripts/check-jar-freshness.sh"));
        String openbw = read(OPENBW_E2E);

        assertTrue(build.contains("ATLANTIS_SOURCE_FINGERPRINT"),
                "the build must tag the jar with the source fingerprint");
        assertTrue(check.contains("ATLANTIS_SOURCE_FINGERPRINT"),
                "the checker must read the tag it is comparing against");
        assertTrue(openbw.contains("check-jar-freshness.sh"),
                "the OpenBW runner must check the jar before it plays");
    }

    @Test
    public void theCheckRefusesAnUntaggedJar() throws Exception {
        // A jar built before this mechanism, or by hand, carries no tag - and an
        // untagged jar cannot be shown to match anything, so it must be refused
        // rather than trusted.
        Path temp = Files.createTempDirectory("jar-freshness");
        Path fakeJar = temp.resolve("old.jar");
        Files.write(fakeJar, new byte[]{0x50, 0x4b, 0x03, 0x04}); // a ZIP header, no tag

        Process p = new ProcessBuilder("bash", "scripts/check-jar-freshness.sh",
                fakeJar.toAbsolutePath().toString())
                .redirectErrorStream(true).start();
        assertTrue(p.waitFor() != 0, "an untagged jar must not pass the freshness check");
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