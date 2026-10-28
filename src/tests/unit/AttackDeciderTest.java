package tests.unit;

import atlantis.combat.focus.AttackDecider;
import atlantis.combat.focus.AttackDecision;
import atlantis.combat.focus.AttackReason;
import atlantis.combat.focus.AttackSnapshot;
import atlantis.map.position.APosition;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * The attack decision as a pure function (owner: decisions testable in
 * StarEngine without issuing orders). No world, no units, no clock - a
 * hand-built snapshot in, target + reason out, branch by branch. The live
 * seam ({@code AttackFocusPoint}) only builds the snapshot from game
 * state; either way the same branches answer.
 */
@Tag("combat")
public class AttackDeciderTest {

    private static APosition at(int tx, int ty) {
        return APosition.create(tx, ty);
    }

    @Test
    public void knownBaseMarchesWithItsReason() {
        AttackDecision decision = AttackDecider.decide(new AttackSnapshot(
            at(100, 100), at(110, 110), at(20, 20), at(10, 10)));

        assertEquals(at(100, 100), decision.target());
        assertEquals(AttackReason.ENEMY_BASE_SEEN, decision.reason());
    }

    @Test
    public void knownMainIsNextWhenNoBaseIsSeen() {
        AttackDecision decision = AttackDecider.decide(new AttackSnapshot(
            null, at(110, 110), at(20, 20), at(10, 10)));

        assertEquals(at(110, 110), decision.target());
        assertEquals(AttackReason.ENEMY_MAIN_KNOWN, decision.reason());
    }

    @Test
    public void blindArmyMarchesTheUnexploredStart() {
        AttackDecision decision = AttackDecider.decide(new AttackSnapshot(
            null, null, at(20, 20), at(10, 10)));

        assertEquals(at(20, 20), decision.target());
        assertEquals(AttackReason.EXPLORE_UNEXPLORED_START, decision.reason());
    }

    @Test
    public void baselessBlindArmyFallsBackHome() {
        AttackDecision decision = AttackDecider.decide(new AttackSnapshot(
            null, null, null, at(10, 10)));

        assertEquals(at(10, 10), decision.target());
        assertEquals(AttackReason.FALLBACK_HOME, decision.reason());
    }

    @Test
    public void nothingKnownNowhereToStandIsNull() {
        AttackDecision decision = AttackDecider.decide(new AttackSnapshot(
            null, null, null, null));

        assertNull(decision.target(),
            "baseless map, nothing known - the old null path, named");
        assertEquals(AttackReason.FALLBACK_HOME, decision.reason());
    }
}
