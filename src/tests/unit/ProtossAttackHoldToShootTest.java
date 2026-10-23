package tests.unit;

import atlantis.battle.strategy.ProtossAttackHoldToShoot;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * NEXT #186: the hold-early race, pinned without a game. Given the
 * effective range R* (foe reach + chaser cover during our windup -
 * our braking glide), the weapon must be ready when the enemy reaches
 * R* (plus a hold margin for order-issue slop): stand and wait.
 * Otherwise keep kiting.
 */
public class ProtossAttackHoldToShootTest {

    @Test
    public void readyOnArrivalHolds() {
        assertTrue(ProtossAttackHoldToShoot.shouldHoldEarly(200, 100, 10, 3, 4.0, 2),
            "enemy reaches R* in 25f, weapon ready in 13f: stand and wait, no ground to buy");
    }

    @Test
    public void coolingThroughArrivalRuns() {
        assertFalse(ProtossAttackHoldToShoot.shouldHoldEarly(200, 100, 25, 3, 4.0, 2),
            "enemy reaches R* in 25f, weapon ready in 28f: standing eats the arrival unarmed");
    }

    @Test
    public void boundaryHoldsWithoutMargin() {
        assertTrue(ProtossAttackHoldToShoot.shouldHoldEarly(200, 100, 22, 3, 4.0, 0),
            "exact tie (25 vs 25) favors the stand: the order is already in flight");
    }

    @Test
    public void marginCoversOrderIssueSlop() {
        assertFalse(ProtossAttackHoldToShoot.shouldHoldEarly(200, 100, 22, 3, 4.0, 2),
            "same geometry as the tie, but the 2f slop (same cover as the duel gate's +2) keeps the kite");
    }

    @Test
    public void runnerNeverHolds() {
        assertFalse(ProtossAttackHoldToShoot.shouldHoldEarly(200, 100, 30, 3, -2.0, 2),
            "opening gap: the chase rule owns runners, never the stand");
        assertFalse(ProtossAttackHoldToShoot.shouldHoldEarly(200, 100, 30, 3, 0.0, 2),
            "static enemy: nothing arrives, nothing to wait for");
    }

    @Test
    public void insideEffectiveRangeWithCoolingWeaponRuns() {
        assertFalse(ProtossAttackHoldToShoot.shouldHoldEarly(90, 100, 5, 3, 4.0, 2),
            "already inside R* with 8f to go: open ground instead");
    }

    @Test
    public void insideEffectiveRangeWithReadyWeaponHolds() {
        assertTrue(ProtossAttackHoldToShoot.shouldHoldEarly(90, 100, 0, 0, 4.0, 2),
            "already inside R* with the weapon up: fire now");
    }

    @Test
    public void lateCooldownHoldAtKitingDistance() {
        assertTrue(ProtossAttackHoldToShoot.shouldHoldEarly(100, 42, 8, 3, 4.0, 2),
            "R* 42px, gap 100px, cd 8: arrival in 14.5f vs ready in 11f+2 slop - stand, this is the new behavior");
    }

    @Test
    public void gateEdgeStillYields() {
        assertFalse(ProtossAttackHoldToShoot.shouldHoldEarly(80, 42, 6, 3, 4.0, 2),
            "R* 42px, gap 80px (2.5 tiles), cd 6: arrival in 9.5f vs ready in 9f+2 slop - yield, the owner's gate edge holds");
    }

    @Test
    public void effectiveRangeCoversWindupMinusBraking() {
        assertEquals(34.0,
            ProtossAttackHoldToShoot.effectiveRange(16.0, 4.0, 6, 6.0), 0.001,
            "foe reach + chaser cover during windup - our braking glide");
    }

    @Test
    public void effectiveRangeFloorsAtZero() {
        assertEquals(0.0,
            ProtossAttackHoldToShoot.effectiveRange(2.0, 1.0, 2, 6.0), 0.001,
            "braking longer than the whole exchange: the stop is free");
    }
}
