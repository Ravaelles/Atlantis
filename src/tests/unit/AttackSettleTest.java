package tests.unit;

import atlantis.combat.FightTarget;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Post-swing settle (PurpleWave AttackDelay, owner 2026-10-26): a move
 * or target switch landing right after the attack animation sticks the
 * unit. Follow-up orders wait until animation start + observed
 * duration + 2, latency-aware. Calibrates per unit from observed
 * swings - no hand-written stop frames.
 */
@Tag("combat")
public class AttackSettleTest extends WorldStubForTests {

    @Test
    public void nothingSettlesBeforeAnySwing() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);

        world(2, fakeOurs(dragoon), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 1) return;

            assertFalse(FightTarget.attackSettleHold(dragoon, null),
                "no swing ever seen: nothing to settle");
        });
    }

    @Test
    public void animationHoldsAndReleaseFollowsCalibration() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 33, 20);
        zealot.setEnemy();
        dragoon.cooldown = 0;

        world(40, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            int now = atlantis.game.A.now();
            if (now < 1) return;

            if (now == 1) dragoon.attackUnit(zealot);
            // 2-frame swing, frames 5-6; the gate is consulted every
            // frame exactly like fight() does, so the calibration
            // tracks the animation instead of guessing it. Observed
            // settle is 2 + 2 = 4 (safe arrival >= 5 + 4 = 9 with
            // stub latency 3) - the pre-calibration default 6 would
            // still hold at frame 7, the calibrated window releases.
            dragoon.attackFrame = now == 5 || now == 6;
            boolean held = FightTarget.attackSettleHold(dragoon, zealot);

            if (now == 5 || now == 6) {
                assertTrue(held, "mid-swing holds at frame " + now);
            }
            if (now == 7 || now == 12) {
                assertFalse(held, "calibrated settle released at frame " + now);
            }
        });
    }
}
