package tests.unit;

import atlantis.combat.targeting.AcquireTarget;
import atlantis.combat.targeting.ASquadTargeting;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * Undetected units (cloaked Observer with no detector near) are not
 * shootable: the engine refuses the order, so naming one as the target
 * only spams "Trying to attack not detected unit" every frame. The
 * ladder must skip undetected enemies instead of picking them -
 * weakest-first makes an undetected low-hp unit the most attractive
 * pick, exactly the wrong answer.
 */
@Tag("combat")
public class UndetectedTargetTest extends WorldStubForTests {

    @Test
    public void validityRefusesUndetected() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 30);
        FakeUnit observer = fake(AUnitType.Protoss_Observer, 31, 30);
        observer.setEnemy();
        observer.setDetected(false);
        observer.hp = 20;

        world(3, fakeOurs(nexus, dragoon), fakeEnemies(observer), () -> {
            if (atlantis.game.A.now() != 1) return;

            assertFalse(
                ASquadTargeting.isValidTarget(dragoon, observer),
                "undetected observer is not a valid target - the engine refuses the order");
        });
    }

    @Test
    public void ladderSkipsUndetectedObserverForVisibleZealot() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 30);
        FakeUnit observer = fake(AUnitType.Protoss_Observer, 31, 30);
        observer.setEnemy();
        observer.setDetected(false);
        observer.hp = 20;
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 30);
        zealot.setEnemy();
        zealot.hp = 100;

        world(3, fakeOurs(nexus, dragoon), fakeEnemies(observer, zealot), () -> {
            if (atlantis.game.A.now() != 1) return;

            assertEquals(
                zealot.id(), idOf(AcquireTarget.acquire(dragoon)),
                "ladder answers the shootable zealot, never the undetected observer");
        });
    }

    @Test
    public void nearestEngageableSkipsUndetected() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 30);
        FakeUnit observer = fake(AUnitType.Protoss_Observer, 31, 30);
        observer.setEnemy();
        observer.setDetected(false);
        observer.hp = 20;

        world(3, fakeOurs(nexus, dragoon), fakeEnemies(observer), () -> {
            if (atlantis.game.A.now() != 1) return;

            assertEquals(
                null, ASquadTargeting.nearestEngageableEnemy(dragoon, 9),
                "no engageable enemy when the only near one is undetected");
        });
    }

    @Test
    public void validityAndRangeAgreeOnUndetected() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 30);
        FakeUnit observer = fake(AUnitType.Protoss_Observer, 31, 30);
        observer.setEnemy();
        observer.setDetected(false);
        observer.hp = 20;

        world(3, fakeOurs(nexus, dragoon), fakeEnemies(observer), () -> {
            if (atlantis.game.A.now() != 1) return;

            assertFalse(
                ASquadTargeting.isValidTarget(dragoon, observer),
                "validity refuses undetected");
            assertFalse(
                dragoon.hasWeaponRangeToAttack(observer, 0),
                "range refuses undetected");
        });
    }

    private static int idOf(atlantis.units.AUnit u) {
        return u == null ? -1 : u.id();
    }
}
