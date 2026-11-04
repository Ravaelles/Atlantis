package tests.unit;

import atlantis.production.constructions.position.RefusedTiles;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Map-edge refusals are structural, not transient (owner order
 * 2026-11-04, Tomas Cere game): a Nexus parked at [120,0] from 5:52
 * was engine-refused at 5:57, TTL-forgiven, re-served at 7:37 and
 * parked another 105 s. The border never becomes buildable, so edge
 * tiles stay refused for the game once the engine says no - while
 * interior tiles keep the 2400-frame forgiveness (scouted late,
 * transient blockers).
 *
 * <p>Pure boundary table, no game: the predicate owns the numbers,
 * the TTL path owns the clock.
 */
@Tag("placement")
public class RefusedTileEdgeTest {

    @Test
    public void cornerTilesAreStructural() {
        assertTrue(RefusedTiles.isMapEdgeTile(0, 0, 4, 3, 128, 128), "corner");
        assertTrue(RefusedTiles.isMapEdgeTile(120, 0, 4, 3, 128, 128), "Tomas Nexus tile, y=0");
        assertTrue(RefusedTiles.isMapEdgeTile(116, 0, 4, 3, 128, 128), "second Tomas Nexus tile, y=0");
        assertTrue(RefusedTiles.isMapEdgeTile(126, 21, 2, 2, 128, 128), "right edge Pylon footprint");
    }

    @Test
    public void interiorTilesAreNotStructural() {
        assertFalse(RefusedTiles.isMapEdgeTile(10, 10, 4, 3, 128, 128), "open ground");
        assertFalse(RefusedTiles.isMapEdgeTile(104, 111, 3, 2, 128, 128), "played Core site");
        assertFalse(RefusedTiles.isMapEdgeTile(92, 103, 4, 3, 128, 128), "recovered Gateway site");
    }

    @Test
    public void marginIsFourTilesEachSide() {
        assertFalse(RefusedTiles.isMapEdgeTile(4, 4, 1, 1, 128, 128), "boundary is exclusive");
        assertTrue(RefusedTiles.isMapEdgeTile(3, 50, 1, 1, 128, 128), "one tile inside the margin");
        // Far corner: footprint crossing the far margin counts.
        assertFalse(RefusedTiles.isMapEdgeTile(123, 100, 1, 1, 128, 128), "1x1 clear of the far margin");
        assertTrue(RefusedTiles.isMapEdgeTile(123, 100, 2, 2, 128, 128), "2x2 crossing the far margin");
    }

    @Test
    public void unknownMapFailsOpen() {
        // No game attached: no dimensions, no structural verdict - the
        // TTL path still applies instead of refusing the world.
        assertFalse(
            RefusedTiles.isMapEdgeTile(0, 0, atlantis.units.AUnitType.Protoss_Nexus),
            "without map data nothing is structural");
    }
}
