package tests.unit;

import atlantis.units.CombatEvalScale;
import org.junit.jupiter.api.Test;

import static atlantis.units.CombatEvalScale.FLOOR;
import static atlantis.units.CombatEvalScale.OUR_SIDE_HEDGE;
import static atlantis.units.CombatEvalScale.hedgedForOurSide;
import static atlantis.units.CombatEvalScale.signSafe;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The floor, as arithmetic rather than as a world.
 *
 * <p>The interesting half - the Protoss tweaks pushing a real ratio through zero -
 * is {@code ProtossCombatEvalScaleTest}. This one only asks what the guarantee
 * means: nothing that goes in comes out negative, everything else comes out
 * untouched.</p>
 */
public class CombatEvalScaleTest {

    @Test
    public void aNegativeRatioIsFlooredInsteadOfSurviving() {
        assertEquals(FLOOR, signSafe(-0.3789), 0.000001,
            "the measured Wraith-against-two-cannons ratio from _AI/BUGS.md B-2");
        assertEquals(FLOOR, signSafe(-9874.0), 0.000001, "however negative it gets");
        assertEquals(FLOOR, signSafe(0.0), 0.000001, "zero is not a fight either");
    }

    @Test
    public void aPositiveRatioIsLeftAlone() {
        assertEquals(0.05, signSafe(0.05), 0.000001);
        assertEquals(1.0, signSafe(1.0), 0.000001, "an even fight");
        assertEquals(47.5030, signSafe(47.5030), 0.000001,
            "the biggest measured reading (cannon side) passes through");
        assertEquals(9874.0, signSafe(9874.0), 0.000001,
            "including the 'nothing to fight' shortcut, which must stay huge");
    }

    @Test
    public void ourSideReadingPaysTheHedgeAndTheFloor() {
        // The doctrine in one line: raw 1.0 reads 0.7, so only a raw 1.3 is "even".
        assertEquals(0.3, OUR_SIDE_HEDGE, 0.000001, "the hedge the owner asked for");
        assertEquals(0.7, hedgedForOurSide(1.0), 0.000001);
        assertEquals(1.0, hedgedForOurSide(1.3), 0.000001, "only 1.3 gives 1.0");
        assertEquals(0.6999, hedgedForOurSide(0.9999), 0.000001, "and the same just under");

        // The hedge is subtractive, so it bites hardest around parity - which is
        // where the decisions are - and least on the huge "no threat" reading.
        assertEquals(5.0 - OUR_SIDE_HEDGE, hedgedForOurSide(5.0), 0.000001);
        assertEquals(9874.0 - OUR_SIDE_HEDGE, hedgedForOurSide(9874.0), 0.000001,
            "the no-threat shortcut still reads as nothing to fight here");

        // And it never produces a number no threshold can read.
        assertEquals(FLOOR, hedgedForOurSide(0.05), 0.000001, "a losing fight stays floored");
        assertEquals(FLOOR, hedgedForOurSide(-0.3789), 0.000001);
    }

    @Test
    public void theFloorIsInertForEveryThresholdProductionUses() {
        // Not an accident, so it is a test: the smallest number any production guard
        // compares eval() against is 0.3 (measured 2026-10-04 over all 236 eval()
        // call sites in src/atlantis, 34 distinct literals from 0.3 to 10). So the
        // floor only ever changes the sign...
        assertTrue(FLOOR < 0.3, "below the smallest threshold in production");

        // ...except that it also bounds the domain from below: a tweaked ratio
        // below 0.01 cannot survive, and the smallest measured Terran-side raw
        // is 0.0211 (wraith vs fogged cannons), which passes through untouched.
        // The floor engages only the Protoss-tweaked negatives, pinned in
        // aNegativeRatioIsFlooredInsteadOfSurviving.
        assertEquals(0.0211, signSafe(0.0211), 0.000001,
            "the smallest measured Terran-side raw passes through");
    }

    @Test
    public void theFloorIsPositiveAndTiny() {
        assertTrue(FLOOR > 0, "a negative number is what we are fixing");
        assertTrue(FLOOR < 0.1, "and it has to stay below any threshold production compares against");
    }
}