package tests.unit;

import atlantis.debug.tweaker.TunerBatchManifest;
import atlantis.debug.tweaker.TunerObjective;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * NEXT #188: the batch manifest is deterministic and round-trips.
 */
@Tag("infra")
public class TunerBatchManifestTest {

    private static TunerBatchManifest.Game game(String dir, boolean win, int hits, int seconds,
            String k1, double v1, String k2, double v2) {
        Map<String, Double> vector = new HashMap<>();
        vector.put(k1, v1);
        vector.put(k2, v2);
        return new TunerBatchManifest.Game(dir, new TunerObjective.Row("1Drag", win, hits, seconds), vector);
    }

    @Test
    public void digestIsDeterministicAndOrderIndependent() {
        Map<String, Double> a = new HashMap<>();
        a.put("x", 1.5);
        a.put("y", 2.5);
        Map<String, Double> b = new HashMap<>();
        b.put("y", 2.5);
        b.put("x", 1.5);

        assertEquals(TunerBatchManifest.digest(a), TunerBatchManifest.digest(b));
        assertEquals(32, TunerBatchManifest.digest(a).length(), "md5 hex");
    }

    @Test
    public void manifestRoundTripsGameDirs() {
        String text = TunerBatchManifest.render("1Drag_v_1Zeal.scm", "abc123",
            Arrays.asList(
                game("game-1", true, 8, 20, "step", 2.5, "margin", 1.0),
                game("game-2", false, 30, 45, "step", 3.0, "margin", 1.0)));

        assertTrue(text.contains("map=1Drag_v_1Zeal.scm"), text);
        assertTrue(text.contains("jar=abc123"), text);
        assertTrue(text.contains("game-1|WIN|8|20|"), text);
        assertTrue(text.contains("game-2|LOSS|30|45|"), text);
        assertEquals(Arrays.asList("game-1", "game-2"), TunerBatchManifest.gameDirs(text));
    }

    @Test
    public void emptyBatchRendersHeaderOnly() {
        String text = TunerBatchManifest.render("m", "j", java.util.Collections.emptyList());

        assertTrue(text.contains("games=0"), text);
        assertTrue(TunerBatchManifest.gameDirs(text).isEmpty());
    }
}
