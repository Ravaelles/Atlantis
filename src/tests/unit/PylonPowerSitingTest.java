package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.v2.PylonPowerSiting;
import atlantis.production.v2.PylonPlacementScore.Tile;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Power-smart Pylon siting picks the validated tile that unlocks the most
 * unpowered room - never an unvalidated one (inventing candidates the
 * finder never checked is worse than the first validated tile).
 */
@Tag("production")
public class PylonPowerSitingTest {

    private static APosition at(int x, int y) {
        return APosition.create(x, y);
    }

    @Test
    public void singleCandidateStands() {
        APosition only = at(10, 10);

        assertEquals(
            only,
            PylonPowerSiting.best(
                Collections.singletonList(only),
                Collections.<Tile>emptyList(), Collections.<Tile>emptyList()),
            "nothing to choose between: the validated tile stands");
    }

    @Test
    public void emptyCandidatesChooseNothing() {
        assertNull(
            PylonPowerSiting.best(
                Collections.<APosition>emptyList(),
                Collections.<Tile>emptyList(), Collections.<Tile>emptyList()),
            "no validated tile, no choice");
    }

    @Test
    public void mostUnlockingTileWins() {
        APosition crowded = at(10, 10);
        APosition open = at(30, 30);

        APosition picked = PylonPowerSiting.best(
            Arrays.asList(crowded, open),
            Arrays.asList(new Tile(11, 10), new Tile(31, 30), new Tile(32, 31)),
            Arrays.asList(new Tile(11, 10)));

        assertEquals(open, picked,
            "the tile near one unpowered tile beats the tile near one already-powered tile");
    }

    @Test
    public void tiesKeepFinderOrder() {
        APosition first = at(10, 10);
        APosition second = at(30, 30);

        APosition picked = PylonPowerSiting.best(
            Arrays.asList(first, second),
            Arrays.asList(new Tile(11, 10), new Tile(31, 30)),
            Collections.<Tile>emptyList());

        assertEquals(first, picked,
            "equal scores keep the finder's preference, not the scorer's");
    }
}
