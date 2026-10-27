package tests.unit;

import atlantis.map.position.APosition;
import atlantis.map.region.ARegion;
import atlantis.map.region.Regions;
import bwapi.TilePosition;
import bwapi.WalkPosition;
import bwem.Area;
import bwem.AreaId;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Region-center robustness (NEXT #177 parking endgame): on UMS maps
 * BWEM areas can carry sentinel bounding boxes the guard rejects, and
 * then every center reads null - the ghost chain (unexplored, sweep)
 * parks in silence with no log. The highest-altitude walk tile needs
 * no box, and a centerless region must be skipped, never exploded on.
 */
@Tag("map")
public class RegionCenterFallbackTest {

    @Test
    public void degenerateBoxFallsBackToHighestTile() {
        // Sentinel bounding box (guard rejects topLeft.x >= 2000000):
        // the center is the highest-altitude walk tile instead.
        Area area = mock(Area.class);
        when(area.getId()).thenReturn(AreaId.ZERO);
        when(area.getBoundingBoxSize()).thenReturn(new TilePosition(10, 10));
        when(area.getTopLeft()).thenReturn(new TilePosition(2500000, 0));
        when(area.getBottomRight()).thenReturn(new TilePosition(2500001, 1));
        when(area.getTop()).thenReturn(new WalkPosition(400, 300));

        assertEquals(
            APosition.createFromPixels(400 * 8, 300 * 8),
            ARegion.create(area).center(),
            "sentinel box: center is the highest walk tile, not null"
        );
    }

    @Test
    public void noBoxAndNoTopTileStaysNull() {
        Area area = mock(Area.class);
        when(area.getId()).thenReturn(AreaId.ZERO);
        when(area.getBoundingBoxSize()).thenReturn(new TilePosition(10, 10));
        when(area.getTopLeft()).thenReturn(new TilePosition(2500000, 0));
        when(area.getBottomRight()).thenReturn(new TilePosition(2500001, 1));
        when(area.getTop()).thenReturn(null);

        assertNull(
            ARegion.create(area).center(),
            "no box and no tile: honest null, never an exception"
        );
    }

    @Test
    public void unexploredSkipsCenterlessRegions() {
        ARegion home = mock(ARegion.class);
        ARegion broken = mock(ARegion.class);
        when(broken.center()).thenReturn(null);
        when(home.getReachableRegions())
            .thenReturn(java.util.Collections.singletonList(broken));

        Regions.useSource((tx, ty) -> home);
        try {
            assertNull(
                Regions.getNearestUnexploredRegion(APosition.create(20, 20)),
                "a region without a center is skipped, not exploded on"
            );
        } finally {
            tests.fakes.FakeRegion.installAsSource();
        }
    }
}
