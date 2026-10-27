package tests.unit;

import atlantis.debug.tweaker.TunerDriver;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NEXT #160, step 6: the driver moves files and writes the next env
 * fragment - cold start writes nothing (batch randomizes), a played
 * batch yields a full in-bounds vector.
 */
@Tag("infra")
public class TunerDriverTest {

    private static String manifest(String map, String game, boolean win, int hits, int seconds) {
        return "map=" + map + "\njar=j\n"
            + "games=1\n"
            + game + "|" + (win ? "WIN" : "LOSS") + "|" + hits + "|" + seconds + "|d|"
            + "KiteStepTiles=2.5,KiteGlueTiles=0.6\n";
    }

    @Test
    public void singleRowProposesAroundChampion() throws Exception {
        Path dir = Files.createTempDirectory("tuner-driver-cold");
        Path manifest = dir.resolve("manifest.txt");
        Files.write(manifest, manifest("1Drag_v_1Zeal.scm", "game-1", true, 10, 30).getBytes(StandardCharsets.UTF_8));
        Path out = dir.resolve("fragment.env");

        TunerDriver.main(new String[]{
            "--hof", dir.resolve("hof.txt").toString(),
            "--manifest", manifest.toString(),
            "--out", out.toString(),
            "--step", "0.05", "--radius", "0", "--seed", "7"});

        // One row is not a history: jitter radius is 0 as well, so the
        // proposal rides at champion values - still a full fragment.
        assertTrue(Files.exists(out), "single row still proposes around the champion");
        List<String> lines = Files.readAllLines(out, StandardCharsets.UTF_8);
        assertEquals(33, lines.size(), "full vector even from one row");
        assertTrue(lines.contains("KiteStepTiles=2.5"), "champion value rides through");
    }

    @Test
    public void emptyArchiveMeansNoProposal() throws Exception {
        Path dir = Files.createTempDirectory("tuner-driver-empty");
        Path manifest = dir.resolve("manifest.txt");
        Files.write(manifest, "map=m\njar=j\ngames=0\n".getBytes(StandardCharsets.UTF_8));

        TunerDriver.main(new String[]{
            "--hof", dir.resolve("hof.txt").toString(),
            "--manifest", manifest.toString(),
            "--out", dir.resolve("fragment.env").toString(),
            "--step", "0.05", "--radius", "0.1", "--seed", "7"});

        assertFalse(Files.exists(dir.resolve("fragment.env")), "cold start: batch randomizes");
        assertTrue(Files.exists(dir.resolve("hof.txt")), "archive file still created");
    }

}
