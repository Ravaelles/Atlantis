package tests.unit;

import atlantis.map.analysis.DefenseSpots;
import atlantis.map.position.APosition;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * M2 model slice: defense spots are ranked coverage over a tile grid - no
 * game, no BWEM. A straight horizontal choke with a flat grid pins the
 * ranking; a single free footprint pins the exact tile.
 */
@Tag("infra")
public class DefenseSpotsTest {

    private static DefenseSpots.TileGrid flat(final int w, final int h) {
        return new DefenseSpots.TileGrid() {
            @Override
            public int width() {
                return w;
            }

            @Override
            public int height() {
                return h;
            }

            @Override
            public boolean isFree(int tx, int ty) {
                return true;
            }
        };
    }

    @Test
    public void chokeCoveringSpotsRankFirst() {
        // Choke segment along y=320px (tile row 10), anchor far below it.
        APosition endA = APosition.createFromPixels(0, 320);
        APosition endB = APosition.createFromPixels(320, 320);
        APosition anchor = APosition.createFromPixels(160, 960);

        List<DefenseSpots.Spot> spots = DefenseSpots.ranked(
            endA, endB, anchor, 7, 2, 2, 12, flat(40, 40));

        assertFalse(spots.isEmpty(), "a flat map has spots");
        assertTrue(spots.get(0).coversChoke(), "the top spot covers the choke line");
        for (int i = 1; i < spots.size(); i++) {
            DefenseSpots.Spot prev = spots.get(i - 1);
            DefenseSpots.Spot spot = spots.get(i);
            assertTrue(
                !spot.coversChoke() || prev.coversChoke(),
                "no non-covering spot ranks above a covering one");
        }
    }

    @Test
    public void aSpotCoveringBothRanksAboveChokeOnly() {
        // Anchor ON the choke line: every choke-covering spot covers the
        // anchor too when the range allows - the ranking must prefer
        // nearer-to-choke among them, deterministically.
        APosition endA = APosition.createFromPixels(0, 320);
        APosition endB = APosition.createFromPixels(320, 320);
        APosition anchor = APosition.createFromPixels(160, 320);

        List<DefenseSpots.Spot> spots = DefenseSpots.ranked(
            endA, endB, anchor, 7, 2, 2, 12, flat(40, 40));

        assertFalse(spots.isEmpty());
        assertTrue(spots.get(0).coversChoke() && spots.get(0).coversAnchor());
    }

    @Test
    public void occupiedTilesNeverProduceSpots() {
        DefenseSpots.TileGrid none = new DefenseSpots.TileGrid() {
            @Override
            public int width() {
                return 40;
            }

            @Override
            public int height() {
                return 40;
            }

            @Override
            public boolean isFree(int tx, int ty) {
                return false;
            }
        };

        List<DefenseSpots.Spot> spots = DefenseSpots.ranked(
            APosition.createFromPixels(0, 320), APosition.createFromPixels(320, 320),
            APosition.createFromPixels(160, 960), 7, 2, 2, 12, none);

        assertEquals(0, spots.size());
    }

    @Test
    public void singleFreeFootprintPinsTheTile() {
        // Only tile (5,9)+(6,10) free: exactly one 2x2 footprint fits.
        DefenseSpots.TileGrid one = new DefenseSpots.TileGrid() {
            @Override
            public int width() {
                return 40;
            }

            @Override
            public int height() {
                return 40;
            }

            @Override
            public boolean isFree(int tx, int ty) {
                return tx >= 5 && tx <= 6 && ty >= 9 && ty <= 10;
            }
        };

        List<DefenseSpots.Spot> spots = DefenseSpots.ranked(
            APosition.createFromPixels(0, 320), APosition.createFromPixels(320, 320),
            APosition.createFromPixels(160, 960), 7, 2, 2, 12, one);

        assertEquals(1, spots.size());
        assertEquals(5, spots.get(0).tx());
        assertEquals(9, spots.get(0).ty());
    }
}
