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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the game-type rule of the Wine launcher (owner report, 2026-10-07: "the
 * game stops at the map selection screen").
 *
 * <p>
 * StarCraft needs different `game_type` values for the two map families, and
 * the wrong one does not error - it just leaves the game waiting for a human at
 * the map screen, which reads as "the bot does not start":
 * </p>
 *
 * <ul>
 * <li>`sscai/...` are real melee maps -> `MELEE`, which also lets the game
 * start by itself;</li>
 * <li>`ums/...` are scenarios with their own rules -> `USE_MAP_SETTINGS`.</li>
 * </ul>
 *
 * <p>
 * The scripts are shell, so this test reads them rather than executing Wine: it
 * guards the mapping and, more importantly, that the scripts stopped hardcoding
 * `USE_MAP_SETTINGS` for every map. That hardcoded value was the bug.
 * </p>
 */
public class WineMapGameTypeTest {

    private static final Path WINE_GAME = Paths.get("scripts/run-wine-game.sh");
    private static final Path WINE_FULL = Paths.get("scripts/run-wine-full.sh");

    @Test
    public void wineGameMapsSscaiToMeleeAndUmsToMapSettings() throws IOException {
        String script = read(WINE_GAME);

        assertTrue(script.contains("sscai/*|*/sscai/*) GAME_TYPE=\"MELEE\""),
                "run-wine-game.sh must select MELEE for the sscai/ map family -"
                        + " USE_MAP_SETTINGS leaves a melee map waiting at the selection screen");
        assertTrue(script.contains("GAME_TYPE=\"USE_MAP_SETTINGS\""),
                "UMS scenarios must keep their own map settings");
        assertTrue(script.contains("game_type = $GAME_TYPE"),
                "the ini must be written from the resolved game type, not a constant");
        assertFalse(script.contains("\ngame_type = USE_MAP_SETTINGS"),
                "run-wine-game.sh must not hardcode USE_MAP_SETTINGS any more");
    }

    @Test
    public void wineFullSetsGameTypeAlongsideTheMap() throws IOException {
        String script = read(WINE_FULL);

        // run-wine-full.sh owns the map line at runtime; it must own the game
        // type in the same breath, or the two drift apart.
        assertTrue(script.contains("game_type=$GAME_TYPE"),
                "run-wine-full.sh must set game_type when it rewrites the map");
        assertTrue(script.contains("*sscai*) GAME_TYPE=\"MELEE\""),
                "run-wine-full.sh must key MELEE off the resolved sscai path");
        assertTrue(script.contains("map=$RESOLVED_MAP"),
                "the map line must still be written (regression guard)");
    }

    @Test
    public void neitherScriptHardcodesAMapFamilyGameType() throws IOException {
        // Java 8: no String.lines() (Java 11) and no stream collection here -
        // this tree compiles with --release 8 as one unit, tests included, so a
        // Java 9+ API in a test breaks the GAME JAR build, not just the test.
        for (Path path : SCRIPTS) {
            List<String> hardcoded = new ArrayList<>();
            for (String line : read(path).split("\\n")) {
                String trimmed = line.trim();
                if (trimmed.startsWith("game_type") && !trimmed.contains("$GAME_TYPE")) {
                    hardcoded.add(trimmed);
                }
            }

            assertTrue(hardcoded.isEmpty(),
                    path + " still hardcodes a game_type: " + hardcoded);
        }
    }

    @Test
    public void bothScriptsAreSyntacticallyUsable() throws IOException {
        // The mapping is worthless if the script does not parse. bash -n is the
        // cheapest guard, and it would have caught a broken sed/quote here.
        for (Path path : SCRIPTS) {
            assertEquals(0, bashSyntaxCheck(path), path + " does not parse as bash");
        }
    }

    /** The scripts this rule covers. A field, because Java 8 has no List.of(). */
    private static final List<Path> SCRIPTS = new ArrayList<>();

    static {
        SCRIPTS.add(WINE_GAME);
        SCRIPTS.add(WINE_FULL);
    }

    private static int bashSyntaxCheck(Path path) throws IOException {
        try {
            Process p = new ProcessBuilder("bash", "-n", path.toString())
                    .redirectErrorStream(true)
                    .start();
            p.waitFor();
            return p.exitValue();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return -1;
        }
    }

    private static String read(Path path) throws IOException {
        assertTrue(Files.exists(path), "missing script: " + path.toAbsolutePath());
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
