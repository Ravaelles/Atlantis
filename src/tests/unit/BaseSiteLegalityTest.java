package tests.unit;

import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.placement.engine.CataloguePlacementPlanner;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Expansion anchor discipline (owner order 2026-11-04, Tomas Cere
 * game): a Nexus parked at [120,0] - an edge tile with no base on it -
 * sat 105 s while the natural never came online. A base tile is legal
 * only next to a real base location (within 2 tiles) and away from
 * our standing or planned bases (15+ tiles). Pure geometry over tile
 * lists, so the boundary pins without a game.
 */
@Tag("placement")
public class BaseSiteLegalityTest {

    private static HasPosition at(int tx, int ty) {
        return APosition.create(tx, ty);
    }

    @Test
    public void tileNextToABaseLocationWithRoomIsLegal() {
        assertTrue(CataloguePlacementPlanner.isLegalBaseSite(
            101, 101,
            Arrays.asList(at(100, 100)),
            Collections.singletonList(at(130, 130))),
            "2 tiles from a base, far from home: servable");
    }

    @Test
    public void parkedNexusTileIsRefused() {
        // The Tomas Cere shape: y=0 edge tile, nearest map bases ~9
        // tiles away (main) and farther.
        assertFalse(CataloguePlacementPlanner.isLegalBaseSite(
            120, 0,
            Arrays.asList(at(117, 9), at(93, 118), at(7, 44)),
            Collections.singletonList(at(117, 9))),
            "no base within 2 tiles of [120,0]");
        assertFalse(CataloguePlacementPlanner.isLegalBaseSite(
            116, 0,
            Arrays.asList(at(117, 9), at(93, 118), at(7, 44)),
            Collections.singletonList(at(117, 9))),
            "no base within 2 tiles of [116,0] either");
    }

    @Test
    public void tileOnTopOfOwnBaseIsRefused() {
        assertFalse(CataloguePlacementPlanner.isLegalBaseSite(
            101, 101,
            Arrays.asList(at(100, 100)),
            Collections.singletonList(at(105, 105))),
            "5 tiles from our own base is stacking, not expanding");
    }

    @Test
    public void boundaryIsExclusiveForBasesInclusiveForDistance() {
        assertTrue(CataloguePlacementPlanner.isLegalBaseSite(
            102, 100,
            Arrays.asList(at(100, 100)),
            Collections.singletonList(at(200, 200))),
            "exactly 2 tiles from a base still counts as next to it");
        assertFalse(CataloguePlacementPlanner.isLegalBaseSite(
            114, 100,
            Arrays.asList(at(100, 100)),
            Collections.singletonList(at(100, 100))),
            "14 tiles from our own base is too close");
        assertTrue(CataloguePlacementPlanner.isLegalBaseSite(
            115, 100,
            Arrays.asList(at(113, 100), at(100, 100)),
            Collections.singletonList(at(100, 100))),
            "2 tiles from the expansion base and 15 from our own is a new base");
    }

    @Test
    public void noMapTruthFailsOpen() {
        // Tests and sims without BWTA list no bases: keep serving (the
        // old behavior) instead of bricking every expansion.
        assertTrue(CataloguePlacementPlanner.isLegalBaseSite(
            120, 0, Collections.<HasPosition>emptyList(), null),
            "empty base list must not veto");
    }
}
