package tests.unit;

import atlantis.debug.tweaker.TunerBatchManifest;
import atlantis.debug.tweaker.TunerObjective;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

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
    public void renderedRowsParseBack() {
        String text = TunerBatchManifest.render("1Drag_v_1Zeal.scm", "abc123",
            Arrays.asList(
                game("game-1", true, 8, 20, "step", 2.5, "margin", 1.0),
                game("game-2", false, 30, 45, "step", 3.0, "margin", 1.0)));

        java.util.List<TunerBatchManifest.Game> games = TunerBatchManifest.parseGames(text);
        assertEquals(2, games.size());
        assertEquals("game-1", games.get(0).dir);
        assertTrue(games.get(0).row.win());
        assertEquals(8, games.get(0).row.hitsTaken());
        assertEquals(20, games.get(0).row.seconds());
        assertEquals(2.5, games.get(0).vector.get("step"), 0.0001);
        assertEquals("game-2", games.get(1).dir);
        assertFalse(games.get(1).row.win());
    }

    @Test
    public void malformedRowsAreSkipped() {
        String text = "map=m\njar=j\ngames=3\n"
            + "game-1|WIN|8|20|abc|step=2.5\n"
            + "not a row\n"
            + "game-2|DRAW|8|20|abc|step=2.5\n"
            + "game-3|WIN|NaN|20|abc|step=\n";

        java.util.List<TunerBatchManifest.Game> games = TunerBatchManifest.parseGames(text);
        assertEquals(1, games.size(), "only the clean row survives");
        assertEquals("game-1", games.get(0).dir);
    }

    @Test
    public void parseMapReadsHeader() {
        String text = TunerBatchManifest.render("1Drag_v_1Zeal.scm", "abc123",
            Collections.singletonList(game("game-1", true, 8, 20, "step", 2.5, "margin", 1.0)));

        assertEquals("1Drag_v_1Zeal.scm", TunerBatchManifest.parseMap(text));
        assertNull(TunerBatchManifest.parseMap("garbage without header"));
        assertNull(TunerBatchManifest.parseMap(null));
    }

    @Test
    public void emptyBatchRendersHeaderOnly() {
        String text = TunerBatchManifest.render("m", "j", java.util.Collections.emptyList());

        assertTrue(text.contains("games=0"), text);
        assertTrue(TunerBatchManifest.gameDirs(text).isEmpty());
    }
}
