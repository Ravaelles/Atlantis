package tests.unit;

import atlantis.map.position.APosition;
import atlantis.map.scout.ScoutFallbackEnemyBases;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeMapTiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * An idle scout always has somewhere to go: the fallback visits the first
 * unexplored enemy expansion - natural, third, fourth - instead of standing
 * around (measured: "Scout idle: chain found nothing to do" 7x in one long
 * game). All explored means coverage is complete, and only then may the
 * chain stay silent.
 */
@Tag("map")
public class ScoutFallbackEnemyBasesTest {

    private static final APosition NATURAL = APosition.create(100, 20);
    private static final APosition THIRD = APosition.create(60, 60);
    private static final APosition FOURTH = APosition.create(20, 100);

    @BeforeEach
    public void stubTiles() {
        FakeMapTiles.EXPLORED = false;
        FakeMapTiles.installAsSource();
    }

    @Test
    public void naturalFirstThenThirdThenFourth() {
        assertEquals(
            NATURAL, ScoutFallbackEnemyBases.firstUnexplored(NATURAL, THIRD, FOURTH));
        assertEquals(
            THIRD, ScoutFallbackEnemyBases.firstUnexplored(null, THIRD, FOURTH));
        assertEquals(
            FOURTH, ScoutFallbackEnemyBases.firstUnexplored(null, null, FOURTH));
    }

    @Test
    public void allExploredOrMissingIsNull() {
        assertNull(ScoutFallbackEnemyBases.firstUnexplored(null, null, null));

        FakeMapTiles.EXPLORED = true;
        try {
            assertNull(
                ScoutFallbackEnemyBases.firstUnexplored(NATURAL, THIRD, FOURTH),
                "everything explored means coverage is complete - the idle "
                    + "line keeps that one remaining meaning");
        } finally {
            FakeMapTiles.EXPLORED = false;
        }
    }
}
