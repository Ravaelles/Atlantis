package tests.unit;

import java.util.Arrays;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import atlantis.combat.targeting.AcquireTarget;
import atlantis.combat.targeting.TargetLadder;
import atlantis.units.AUnitType;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * The targeting ladder is an explicit ordered behavior list (owner
 * direction, NEXT #184/#185): reordering two adjacent rules is a
 * behavior change, so the order pins here and the top of the ladder
 * pins behaviorally (chewer beats keep-current beats weakest).
 * Swapping branches without updating these names the intended change
 * or fails - never silently.
 */
@Tag("combat")
public class TargetLadderTest extends WorldStubForTests {

    @Test
    public void orderIsExplicitAndStable() {
        assertEquals(
            Arrays.asList("chewer", "keepCurrent", "weakestInRange", "sticky",
                "shared", "cover", "squad", "nearest", "breach"),
            TargetLadder.order(),
            "the ladder order lives once, visibly - reorder only with the pins");
    }

    @Test
    public void chewerBeatsKeepCurrent() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit z = fake(AUnitType.Protoss_Zealot, 30, 20);
        z.hp = 100;
        z.cooldown = 0;
        FakeUnit h = fake(AUnitType.Protoss_Zealot, 30 + 0.4, 20);
        h.setEnemy();
        h.hp = 60;
        FakeUnit w = fake(AUnitType.Protoss_Zealot, 30 - 0.3, 20);
        w.setEnemy();
        w.hp = 3;

        world(3, fakeOurs(nexus, z), fakeEnemies(h, w), () -> {
            if (atlantis.game.A.now() != 1) return;
            z.attackUnit(h);

            assertEquals(w.id(), idOf(AcquireTarget.acquire(z)),
                "shot ready and chewed: weakest in range beats the committed target");
            assertEquals(w.id(), idOf(AcquireTarget.acquire(z)),
                "same scene, same pick - the rule does not oscillate");
        });
    }

    @Test
    public void keepCurrentBeatsWeakestWhenCooling() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit z = fake(AUnitType.Protoss_Zealot, 30, 20);
        z.hp = 100;
        z.cooldown = 12;
        FakeUnit h = fake(AUnitType.Protoss_Zealot, 30 + 0.4, 20);
        h.setEnemy();
        h.hp = 60;
        FakeUnit w = fake(AUnitType.Protoss_Zealot, 30 - 0.3, 20);
        w.setEnemy();
        w.hp = 3;

        world(3, fakeOurs(nexus, z), fakeEnemies(h, w), () -> {
            if (atlantis.game.A.now() != 1) return;
            z.attackUnit(h);

            assertEquals(h.id(), idOf(AcquireTarget.acquire(z)),
                "cooling down: the chewer rule sleeps, the committed shot is kept");
        });
    }

    @Test
    public void stickyHoldsCommittedTargetInChaseBand() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit d = fake(AUnitType.Protoss_Dragoon, 30, 20);
        d.hp = 100;
        d.cooldown = 0;
        FakeUnit a = fake(AUnitType.Protoss_Zealot, 30 + 4.5, 20);
        a.setEnemy();
        a.hp = 60;

        world(3, fakeOurs(nexus, d), fakeEnemies(a), () -> {
            if (atlantis.game.A.now() != 1) return;
            d.attackUnit(a);

            assertEquals(a.id(), idOf(AcquireTarget.acquire(d)),
                "committed target past weapon range but inside the chase band"
                    + " (4.5 vs 4 + 1.0) stays: keep-current slept, weakest"
                    + " found nothing in range, sticky holds");
        });
    }

    @Test
    public void weakestTakesOverWhenEnemyEntersRange() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit d = fake(AUnitType.Protoss_Dragoon, 30, 20);
        d.hp = 100;
        d.cooldown = 0;
        FakeUnit a = fake(AUnitType.Protoss_Zealot, 30 + 4.5, 20);
        a.setEnemy();
        a.hp = 60;
        FakeUnit b = fake(AUnitType.Protoss_Zealot, 30 + 2.5, 20);
        b.setEnemy();
        b.hp = 3;

        world(3, fakeOurs(nexus, d), fakeEnemies(a, b), () -> {
            if (atlantis.game.A.now() != 1) return;
            d.attackUnit(a);

            assertEquals(b.id(), idOf(AcquireTarget.acquire(d)),
                "an in-range enemy hands the pick back to rule 3 even while"
                    + " the committed target is still inside the chase band");
        });
    }

    private static int idOf(atlantis.units.AUnit u) {
        return u == null ? -1 : u.id();
    }
}
