package tests.unit;

import atlantis.combat.eval.CombatEvalScale;
import org.junit.jupiter.api.Test;

import static atlantis.combat.eval.CombatEvalScale.FLOOR;
import static atlantis.combat.eval.CombatEvalScale.signSafe;
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
        assertEquals(152.03, signSafe(152.03), 0.000001, "and the huge values");
        assertEquals(9874.0, signSafe(9874.0), 0.000001,
            "including the 'nothing to fight' shortcut, which must stay huge");
    }

    @Test
    public void theFloorIsInertForEveryThresholdProductionUses() {
        // Not an accident, so it is a test: the smallest number any production guard
        // compares eval() against is 0.3 (measured 2026-10-04 over all 236 eval()
        // call sites in src/atlantis, 34 distinct literals from 0.3 to 10). So the
        // floor only ever changes the sign...
        assertTrue(FLOOR < 0.3, "below the smallest threshold in production");

        // ...except that it also clamps genuine extreme ratios: the same Wraith
        // fight scores 0.0066 as Terran, and the floor reports that as 0.01. Nothing
        // can tell the two apart, so the clamp is harmless today - but it is a real
        // clamp, and this is where it is written down.
        assertEquals(FLOOR, signSafe(0.0066), 0.000001);
    }

    @Test
    public void theFloorIsPositiveAndTiny() {
        assertTrue(FLOOR > 0, "a negative number is what we are fixing");
        assertTrue(FLOOR < 0.1, "and it has to stay below any threshold production compares against");
    }
}