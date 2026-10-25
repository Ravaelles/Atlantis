package tests.unit;

import atlantis.combat.focus.TrappedByMassZealots;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * NEXT #197: the trapped case owns one predicate. The numbers live with
 * the case they gate (sim-bracketed mass line, live-proven old-changer
 * floor and window), not re-spelled at the call site.
 */
@Tag("combat")
public class TrappedByMassZealotsTest {

    @Test
    public void boxedLineAgainstMassZealotsMarchesOut() {
        assertTrue(TrappedByMassZealots.applies(4, 2, 6, 100));
        assertTrue(TrappedByMassZealots.applies(12, 5, 20, 600));
    }

    @Test
    public void everyBoundHolds() {
        assertFalse(TrappedByMassZealots.applies(3, 2, 6, 100), "too few to kite with");
        assertFalse(TrappedByMassZealots.applies(4, 1, 6, 100), "a lone dragoon holds no line");
        assertFalse(TrappedByMassZealots.applies(4, 2, 5, 100), "below the sim-bracketed mass line");
        assertFalse(TrappedByMassZealots.applies(4, 2, 6, 651), "late game has answers, not this one");
    }
}
