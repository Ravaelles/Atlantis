package tests.unit;

import atlantis.map.position.APosition;
import atlantis.placement.core.BuildLocation;
import atlantis.placement.engine.CoverageCannonRanker;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * M2 consumer slice: cannon tiles rank by weapon coverage of the choke,
 * not by distance to it. A near tile that cannot shoot the line loses to a
 * farther tile that can - the exact inversion the distance ranker got wrong
 * on wide chokes.
 */
public class CoverageCannonRankerTest {

    private static BuildLocation at(int tx, int ty) {
        return new BuildLocation(tx, ty, 2, 2, 0, 0, 0, false);
    }

    @Test
    public void coveringTileBeatsNearerNonCoveringTile() {
        // Choke segment along y=320px; range 2 tiles = 64px.
        APosition endA = APosition.createFromPixels(0, 320);
        APosition endB = APosition.createFromPixels(320, 320);
        CoverageCannonRanker ranker = new CoverageCannonRanker(endA, endB, null, 2);

        // (5,11): center (192,384), 64px below the line - exactly at range.
        // (5,14): center (192,480), 160px below - nearer the map, cannot shoot.
        BuildLocation covering = at(5, 11);
        BuildLocation nearer = at(5, 14);

        assertTrue(ranker.compare(covering, nearer) < 0,
                "a tile covering the choke outranks a nearer tile that cannot shoot it");
        assertTrue(ranker.compare(nearer, covering) > 0);
    }

    @Test
    public void coverageVerdictIsExposedForTheTwin() {
        APosition endA = APosition.createFromPixels(0, 320);
        APosition endB = APosition.createFromPixels(320, 320);
        CoverageCannonRanker ranker = new CoverageCannonRanker(endA, endB, null, 2);

        atlantis.placement.core.CoverageVerdict covering = ranker.coverageOf(at(5, 11));
        atlantis.placement.core.CoverageVerdict distant = ranker.coverageOf(at(5, 14));

        assertTrue(covering.choke(), "the twin must see what the ranking decided");
        assertTrue(!distant.choke());
    }

    @Test
    public void availabilityStillOrdersFirst() {        APosition endA = APosition.createFromPixels(0, 320);
        APosition endB = APosition.createFromPixels(320, 320);
        CoverageCannonRanker ranker = new CoverageCannonRanker(endA, endB, null, 7);

        BuildLocation powered = new BuildLocation(5, 11, 2, 2, 0, 0, 0, false);
        BuildLocation waiting = new BuildLocation(5, 11, 2, 2, 0, 500, 0, false);

        assertTrue(ranker.compare(powered, waiting) < 0,
                "a tile powered now beats the same tile waiting for power");
    }
}
