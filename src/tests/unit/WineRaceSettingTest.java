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
 * Pins how the Wine launcher learns our race (owner report, 2026-10-07: "I
 * changed Main.ourRace to Protoss but the bot still starts as Terran").
 *
 * <p>
 * The race is owned by the client ({@code Main.ourRace()}) but it is <b>read by
 * StarCraft from {@code bwapi.ini}</b> at game start, and the launcher script
 * is
 * the only thing that rewrites the live ini. The bug was simply that nothing
 * rewrote {@code race=}: the file kept whatever it had, so the client's choice
 * never reached the game.
 * </p>
 *
 * <p>
 * The script now reads the race out of {@code Main.ourRace()} rather than
 * duplicating it, and the interesting failure is subtle: that method commits a
 * race by leaving the chosen {@code return} uncommented above the alternatives,
 * so a naive grep matches a <b>commented-out</b> line and reports the wrong
 * race. That exact mistake happened while writing this (it read "Terran" while
 * the active line said "Protoss"), which is why the extraction is exercised
 * here as a shell snippet against the real source file instead of being
 * inspected by eye.
 * </p>
 */
public class WineRaceSettingTest {

    private static final Path SCRIPT = Paths.get("scripts/run-wine-full.sh");
    private static final Path MAIN = Paths.get("src/main/Main.java");

    /** The extraction as the script performs it, run against the real source. */
    private static final String EXTRACT = "sed -n '/public static String ourRace/,/^    }/p' " + MAIN + " " +
            "| grep -vE '^\\s*//' " +
            "| grep -oE 'return \"(Protoss|Terran|Zerg)\"' | head -1 " +
            "| sed -E 's/.*\"(.*)\".*/\\1/'";

    @Test
    public void raceIsReadFromTheClientNotHardcodedInTheScript() throws IOException, InterruptedException {
        String script = read(SCRIPT);

        assertTrue(script.contains("CLIENT_RACE="),
                "run-wine-full.sh must read the race from the client");
        assertTrue(script.contains("Setting race to $CLIENT_RACE"),
                "the race must actually be written into bwapi.ini");
        assertTrue(script.contains("Main.java"),
                "the race must come from Main.ourRace(), the client's source of truth");
    }

    @Test
    public void extractionSkipsCommentedOutRaces() throws Exception {
        String detected = run(EXTRACT);

        String source = read(MAIN);
        int header = source.indexOf("public static String ourRace");
        String method = source.substring(header, Math.min(source.length(), header + 600));

        // The active (uncommented) return is what must win. Derive the expected
        // value from the same file so this test cannot rot when the owner
        // switches race: it asserts the extraction agrees with the FIRST
        // uncommented return, not with a literal.
        String expected = null;
        for (String line : method.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("//"))
                continue;
            if (trimmed.startsWith("return \"")) {
                expected = trimmed.replace("return \"", "").replaceAll("\".*", "");
                break;
            }
        }

        assertEquals(expected, detected,
                "the race must be the first UNCOMMENTED return in ourRace();"
                        + " matching a commented line would silently pick a different race");
    }

    @Test
    public void scriptStillParses() throws Exception {
        // The working directory of the JUnit run is the repo root (run-tests.sh
        // cds there), which is also how the other script-reading tests work.
        assertEquals(0, runExitCode("bash -n " + SCRIPT.toAbsolutePath()),
                "a broken README-adjacent edit here would only show up as a failed launch");
    }

    // ---- helpers -----------------------------------------------------------

    private static int runExitCode(String command) throws Exception {
        Process p = new ProcessBuilder("bash", "-c", command)
                .redirectErrorStream(true)
                .start();
        readAll(p);
        return p.waitFor();
    }

    private static String run(String command) throws Exception {
        Process p = new ProcessBuilder("bash", "-c", command)
                .redirectErrorStream(false)
                .start();
        String out = new String(readAll(p), StandardCharsets.UTF_8).trim();
        p.waitFor();
        return out;
    }

    private static byte[] readAll(Process p) throws IOException {
        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        byte[] chunk = new byte[4096];
        int read;
        while ((read = p.getInputStream().read(chunk)) != -1) {
            buffer.write(chunk, 0, read);
        }
        return buffer.toByteArray();
    }

    private static String read(Path path) throws IOException {
        assertTrue(Files.exists(path), "missing file: " + path.toAbsolutePath());
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
