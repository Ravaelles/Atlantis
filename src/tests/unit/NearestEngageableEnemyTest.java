package tests.unit;

import atlantis.battle.targeting.ASquadTargeting;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * The cluster walked past enemies to the anchor because nothing ever set
 * a target (owner 2026-10-19, Wine: army ignores enemy units). The march
 * now stops for the nearest damageable enemy inside 9 tiles (the same
 * radius the squad-target path uses) and remembers it as the squad's
 * last target. Engagement only - focus exclusions (lings, workers,
 * ranged mirrors) stay in the squad-target path, untouched.
 */
@Tag("infra")
public class NearestEngageableEnemyTest extends AbstractTestWithWorld {

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Test
    public void engagesNearestEnemyInsideNineTiles() {
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 30, 20);
        FakeUnit ling = fake(AUnitType.Zerg_Zergling, 32, 20);
        FakeUnit marine = fake(AUnitType.Terran_Marine, 34, 20);

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            zealot
        ), fakeEnemies(ling, marine), () -> {
            if (atlantis.game.A.now() != 3) return;

            AUnit target = ASquadTargeting.nearestEngageableEnemy(zealot, 9);
            assertTrue(target != null, "a Zealot with enemies at 2-4 tiles must engage");
            assertSame(ling, target, "nearest first - the squad-target path keeps focus order");
        });
    }

    @Test
    public void ignoresEnemiesBeyondNineTiles() {
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 30, 20);
        FakeUnit ling = fake(AUnitType.Zerg_Zergling, 50, 20);

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            zealot
        ), fakeEnemies(ling), () -> {
            if (atlantis.game.A.now() != 3) return;

            assertNull(
                ASquadTargeting.nearestEngageableEnemy(zealot, 9),
                "20 tiles away is march territory - no cross-map chase");
        });
    }

    @Test
    public void noEnemyMeansNoTarget() {
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 30, 20);

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            zealot
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            assertNull(
                ASquadTargeting.nearestEngageableEnemy(zealot, 9),
                "empty battlefield keeps the anchor march");
        });
    }
}
