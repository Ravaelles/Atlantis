package tests.unit;

import atlantis.combat.targeting.AcquireTarget;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Shoot-now targeting (owner 2026-10-26): a dragoon that walks past an
 * in-range target to chase a squadmate's kill 10 tiles out takes 5-6
 * hits and wastes its cooldown. A valid target inside our own weapon
 * range beats every sharing rule, and an out-of-range commit with an
 * imminent shot (cooldown <= 6) is refused - the approach march owns
 * the unit until something enters range.
 */
@Tag("combat")
public class AcquireTargetTest extends WorldStubForTests {

    @Test
    public void inRangeBeatsEverything() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit near = fake(AUnitType.Protoss_Zealot, 32, 20);
        near.setEnemy();
        near.hp = 100;
        FakeUnit far = fake(AUnitType.Protoss_Zealot, 38, 20);
        far.setEnemy();
        far.hp = 30;
        dragoon.cooldown = 20;

        world(2, fakeOurs(dragoon), fakeEnemies(near, far), () -> {
            if (atlantis.game.A.now() != 1) return;

            // The old chain picks the weakest in 9 (the far one) and
            // walks past the healthy in-range target - the reported bug.
            assertEquals(near, AcquireTarget.acquire(dragoon),
                "in-range target wins over the far shared one, no walk-past");
        });
    }

    @Test
    public void currentTargetKeptWhileInRange() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit current = fake(AUnitType.Protoss_Zealot, 33, 20);
        current.setEnemy();
        FakeUnit closer = fake(AUnitType.Protoss_Zealot, 31, 20);
        closer.setEnemy();
        dragoon.cooldown = 20;

        world(2, fakeOurs(dragoon), fakeEnemies(current, closer), () -> {
            if (atlantis.game.A.now() != 1) return;

            dragoon.attackUnit(current);
            assertEquals(current, AcquireTarget.acquire(dragoon),
                "no re-pick churn while the committed target stays in range");
        });
    }

    @Test
    public void imminentShotDoesNotHideTheTarget() {
        // The acquisition never hides what it sees: the no-chase rule
        // lives in the order funnel (rearmAttack refuses the
        // attackUnit walk with an imminent shot), not in target choice -
        // hiding the target starved the ghost march and froze the fight.
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit far = fake(AUnitType.Protoss_Zealot, 36, 20);
        far.setEnemy();
        dragoon.cooldown = 5;

        world(2, fakeOurs(dragoon), fakeEnemies(far), () -> {
            if (atlantis.game.A.now() != 1) return;

            assertEquals(far, AcquireTarget.acquire(dragoon),
                "seen is seen: the chase discipline belongs to the orders, not the target");
        });
    }

    @Test
    public void nearlyDeadIsFinishedFirst() {
        // Owner report: dragoons shot the healthiest chewer while nearly
        // dead ones were unselectable (old hp<=15 ban). Finishing is
        // always right: one shot removes a gun from the board, and the
        // weakest-in-range focus already names exactly this target.
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit dying = fake(AUnitType.Protoss_Zealot, 32, 20);
        dying.setEnemy();
        dying.hp = 8;
        FakeUnit healthy = fake(AUnitType.Protoss_Zealot, 31, 20);
        healthy.setEnemy();
        healthy.hp = 100;
        dragoon.cooldown = 20;

        world(2, fakeOurs(dragoon), fakeEnemies(dying, healthy), () -> {
            if (atlantis.game.A.now() != 1) return;

            assertEquals(dying, AcquireTarget.acquire(dragoon),
                "the 8hp chewer is finished first, not ignored for the healthy one");
        });
    }

    @Test
    public void woundedTargetStaysKeepable() {
        // Keepable must not drop a target just because it got wounded:
        // dropping it mid-finish is churn that costs the shot (the old
        // ban un-kept anything at or under 15 hp mid-trade).
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit current = fake(AUnitType.Protoss_Zealot, 33, 20);
        current.setEnemy();
        current.hp = 100;
        dragoon.cooldown = 20;

        world(2, fakeOurs(dragoon), fakeEnemies(current), () -> {
            if (atlantis.game.A.now() != 1) return;

            dragoon.attackUnit(current);
            current.hp = 8;
            assertEquals(current, AcquireTarget.acquire(dragoon),
                "wounded to 8 hp in range: the committed target holds, no churn");
        });
    }

    @Test
    public void coolingWeaponStillCloses() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit far = fake(AUnitType.Protoss_Zealot, 36, 20);
        far.setEnemy();
        dragoon.cooldown = 20;

        world(2, fakeOurs(dragoon), fakeEnemies(far), () -> {
            if (atlantis.game.A.now() != 1) return;

            assertNotNull(AcquireTarget.acquire(dragoon), "cooling weapon closes the distance so the shot lands on time");
        });
    }
}
