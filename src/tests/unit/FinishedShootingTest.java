package tests.unit;

import atlantis.battle.DuelMemory;
import atlantis.battle.BattleCommander;
import atlantis.game.A;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeBullet;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Finished shooting (owner): one of our observed projectiles is 1-2
 * frames old, so the damage is committed and the attacker is free -
 * the swing hold and the settle release the unit instead of riding
 * the animation out. No bullet, no latch (fail-closed): today's
 * holds keep working exactly as before.
 */
@Tag("combat")
public class FinishedShootingTest extends WorldStubForTests {

    @Test
    public void noBulletMeansNotFinished() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);

        world(2, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            if (A.now() != 1) return;

            assertFalse(DuelMemory.finishedShooting(dragoon),
                "no projectile observed: fail-closed, the holds stay");
        });
    }

    @Test
    public void firingFrameIsNotFinished() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);

        world(2, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            if (A.now() != 1) return;

            dragoon.setLastBullet(new FakeBullet(dragoon, zealot, true));
            assertFalse(DuelMemory.finishedShooting(dragoon),
                "age 0 is the firing frame itself, not the commit");
        });
    }

    @Test
    public void oneFrameOldBulletIsFinished() {
        assertFinishedAtAge(1, true);
    }

    @Test
    public void twoFramesOldBulletIsFinished() {
        assertFinishedAtAge(2, true);
    }

    @Test
    public void staleBulletIsNotFinished() {
        assertFinishedAtAge(3, false);
    }

    private void assertFinishedAtAge(int age, boolean expected) {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);

        world(age + 2, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            int now = A.now();
            if (now == 1) dragoon.setLastBullet(new FakeBullet(dragoon, zealot, true));
            if (now == 1 + age) {
                if (expected) {
                    assertTrue(DuelMemory.finishedShooting(dragoon),
                        "age " + age + ": projectile confirmed away, damage committed");
                }
                else {
                    assertFalse(DuelMemory.finishedShooting(dragoon),
                        "age " + age + ": previous cycle, not this shot");
                }
            }
        });
    }

    @Test
    public void finishedShotReleasesTheSwingHoldIntoAKiteStep() {
        // Dist 1.5, cooldown 25: the cooling rule kites, but the open
        // animation would hold the unit - until the age-1 projectile
        // releases both the swing hold and the settle.
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 31.5, 20);
        zealot.setEnemy();
        dragoon.cooldown = 25;
        atlantis.battle.BattleCommander.clearForTests();

        try {
            world(3, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
                int now = A.now();
                if (now == 1) {
                    dragoon.attackUnit(zealot);
                    dragoon.attackFrame = true;
                    dragoon.setLastBullet(new FakeBullet(dragoon, zealot, true));
                }
                if (now == 2) {
                    atlantis.util.log.EventLog.clearForTests();
                    BattleCommander.act(
                        dragoon, atlantis.battle.cluster.FightOrFlight.Tactic.FIGHT, null, dragoon);
                    assertTrue(
                        hasOrder(ordersOf(dragoon), dragoon.id(), "move"),
                        "bullet age 1: swing hold and settle release, the kite step issues: "
                            + ordersOf(dragoon));
                    assertTrue(
                        atlantis.util.log.EventLog.toJsonLines().contains("released-finished"),
                        "the release twins, so live logs show the latch firing: "
                            + atlantis.util.log.EventLog.toJsonLines());
                }
            });
        }
        finally {
            atlantis.battle.BattleCommander.clearForTests();
        }
    }

    @Test
    public void unfinishedSwingStillHoldsWithoutABullet() {
        // Same open animation, no projectile: nothing confirms the
        // shot, so the hold stays - the latch only releases, never
        // forces.
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 31.5, 20);
        zealot.setEnemy();
        dragoon.cooldown = 25;
        atlantis.battle.BattleCommander.clearForTests();

        try {
            world(2, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
                if (A.now() != 1) return;

                dragoon.attackUnit(zealot);
                dragoon.attackFrame = true;
                BattleCommander.act(
                    dragoon, atlantis.battle.cluster.FightOrFlight.Tactic.FIGHT, null, dragoon);
                assertFalse(
                    hasOrder(ordersOf(dragoon), dragoon.id(), "move"),
                    "mid-swing with no confirmed projectile: hold, do not step: "
                        + ordersOf(dragoon));
            });
        }
        finally {
            atlantis.battle.BattleCommander.clearForTests();
        }
    }

    private static boolean hasOrder(java.util.List<tests.fakes.FakeOrderSink.Record> orders,
                                    int unitId, String operation) {
        for (tests.fakes.FakeOrderSink.Record record : orders) {
            if (record.actorId == unitId && record.operation.equals(operation)) return true;
        }
        return false;
    }

    private static java.util.List<tests.fakes.FakeOrderSink.Record> ordersOf(FakeUnit unit) {
        return ((tests.fakes.FakeOrderSink) unit.orderSink()).orders();
    }
}
