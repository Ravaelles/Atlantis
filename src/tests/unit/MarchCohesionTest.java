package tests.unit;

import atlantis.combat.formation.MarchCohesion;
import atlantis.map.position.APosition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The march travels abreast, never in single file (owner order
 * 2026-11-04): a line strung out is the worst shape, so the march
 * measures spread as the stddev of distances to the next choke and the
 * front holds while the rear dresses. The rear never steps back, so the
 * spread only shrinks - no yo-yo. Hysteresis keeps the boundary from
 * dithering (4+ to hold, below 2.5 to release).
 *
 * <p>Pure over numbers: no world, no engine, milliseconds.</p>
 */
public class MarchCohesionTest {

    @BeforeEach
    public void clearLatch() {
        MarchCohesion.clearForTests();
    }

    @AfterEach
    public void cleanLatch() {
        MarchCohesion.clearForTests();
    }

    @Test
    public void fileHasBigSpreadAbreastLineHasSmall() {
        // Single file along the march axis: 10 tiles apart each.
        List<Double> file = Arrays.asList(0.0, 10.0, 20.0, 30.0);
        // Line abreast: same distance, small offsets.
        List<Double> line = Arrays.asList(19.5, 20.0, 20.5, 21.0);

        assertTrue(MarchCohesion.spreadOf(file) > 4.0, "a snake reads wide: " + MarchCohesion.spreadOf(file));
        assertTrue(MarchCohesion.spreadOf(line) < 1.0, "a dressed line reads tight: " + MarchCohesion.spreadOf(line));
    }

    @Test
    public void spreadNeedsTwoUnits() {
        assertEquals(0.0, MarchCohesion.spreadOf(java.util.Collections.singletonList(5.0)));
        assertEquals(0.0, MarchCohesion.spreadOf(java.util.Collections.emptyList()));
    }

    @Test
    public void scatteredLineHoldsDressedLineAdvances() {
        assertEquals(MarchCohesion.Verdict.REGROUP,
            MarchCohesion.decide(8.0, false), "wide spread holds");
        assertEquals(MarchCohesion.Verdict.ADVANCE,
            MarchCohesion.decide(1.0, false), "tight line advances");
    }

    @Test
    public void boundaryHasHysteresis() {
        assertEquals(MarchCohesion.Verdict.REGROUP,
            MarchCohesion.decide(3.0, true), "holding through the band, no dither");
        assertEquals(MarchCohesion.Verdict.ADVANCE,
            MarchCohesion.decide(3.0, false), "advancing through the band, no dither");
        assertEquals(MarchCohesion.Verdict.ADVANCE,
            MarchCohesion.decide(2.0, true), "dressed line releases the hold");
    }

    @Test
    public void frontHoldsRearAdvances() {
        double mean = 20.0;
        double scattered = 8.0;

        assertTrue(MarchCohesion.shouldHold(12.0, mean, scattered, false),
            "ahead of the pack waits");
        assertFalse(MarchCohesion.shouldHold(28.0, mean, scattered, false),
            "the rear dresses instead of stepping back");
        assertFalse(MarchCohesion.shouldHold(12.0, mean, 1.0, false),
            "a dressed line advances as one");
    }

    @Test
    public void fallbackStepsTwoBackFromTheChoke() {
        APosition choke = APosition.create(100, 100);
        APosition enemy = APosition.create(100, 120);

        APosition back = MarchCohesion.fallbackPoint(choke, enemy);

        assertTrue(Math.abs(back.distTo(choke) - 2.0) < 0.6,
            "owner: nextChoke.moveAway(2), not a march home: " + back.distTo(choke));
        assertTrue(back.distTo(enemy) > choke.distTo(enemy),
            "back means away from the enemy");
    }
}
