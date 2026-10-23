package tests.unit;

import atlantis.battle.strategy.ProtossAttackHoldToShoot;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * NEXT #186: the hold-early race, pinned without a game. Given the
 * effective range R* (nominal range minus what the enemy covers during
 * our windup/latency plus our braking glide - computed by a later
 * slice), the weapon must be ready when the enemy reaches R*.
 * All units are explicit: pixels for space, frames for time.
 */
public class ProtossAttackHoldToShootTest {

    @Test
    public void readyOnArrivalHolds() {
        assertTrue(ProtossAttackHoldToShoot.shouldHoldEarly(200, 100, 10, 3, 4.0),
            "enemy reaches R* in 25f, weapon ready in 13f: stand and wait, no ground to buy");
    }

    @Test
    public void coolingThroughArrivalRuns() {
        assertFalse(ProtossAttackHoldToShoot.shouldHoldEarly(200, 100, 25, 3, 4.0),
            "enemy reaches R* in 25f, weapon ready in 28f: standing means eating the arrival unarmed");
    }

    @Test
    public void boundaryHolds() {
        assertTrue(ProtossAttackHoldToShoot.shouldHoldEarly(200, 100, 22, 3, 4.0),
            "exact tie (25 vs 25) favors the stand: the order is already in flight");
    }

    @Test
    public void runnerNeverHolds() {
        assertFalse(ProtossAttackHoldToShoot.shouldHoldEarly(200, 100, 30, 3, -2.0),
            "opening gap: the chase rule owns runners, never the stand");
        assertFalse(ProtossAttackHoldToShoot.shouldHoldEarly(200, 100, 30, 3, 0.0),
            " static enemy: nothing arrives, nothing to wait for");
    }

    @Test
    public void insideEffectiveRangeWithCoolingWeaponRuns() {
        assertFalse(ProtossAttackHoldToShoot.shouldHoldEarly(90, 100, 5, 3, 4.0),
            "already inside R* with 8f to go: standing eats the close-down, open ground instead");
    }

    @Test
    public void insideEffectiveRangeWithReadyWeaponHolds() {
        assertTrue(ProtossAttackHoldToShoot.shouldHoldEarly(90, 100, 0, 0, 4.0),
            "already inside R* with the weapon up: fire now");
    }
}
