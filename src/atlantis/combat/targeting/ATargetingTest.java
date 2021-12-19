package atlantis.combat.targeting;

import atlantis.tests.unit.FakeUnit;
import atlantis.units.AUnitType;
import atlantis.tests.unit.AbstractTestWithUnits;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ATargetingTest extends AbstractTestWithUnits {

    @Test
    public void zerglingsOverDrones() {
        FakeUnit our = fake(AUnitType.Terran_Marine, 10);
        FakeUnit expectedTarget;

        FakeUnit[] enemies = fakeUnits(
                fake(AUnitType.Zerg_Drone, 11),
                fake(AUnitType.Zerg_Drone, 12),
                expectedTarget = fake(AUnitType.Zerg_Zergling, 13)
        );

        usingFakeOurAndFakeEnemies(our, enemies, () -> {
            assertEquals(expectedTarget, ATargeting.defineBestEnemyToAttackFor(our));
        });
    }

    @Test
    public void sunkensOverCreepColonies() {
        FakeUnit our = fake(AUnitType.Terran_Marine, 10);
        FakeUnit expectedTarget;

        FakeUnit[] enemies = fakeUnits(
                fake(AUnitType.Zerg_Overlord, 11),
                fake(AUnitType.Zerg_Spawning_Pool, 11),
                fake(AUnitType.Zerg_Creep_Colony, 12),
                expectedTarget = fake(AUnitType.Zerg_Sunken_Colony, 13)
        );

        usingFakeOurAndFakeEnemies(our, enemies, () -> {
            assertEquals(expectedTarget, ATargeting.defineBestEnemyToAttackFor(our));
        });
    }

    @Test
    public void overlords() {
        FakeUnit our = fake(AUnitType.Terran_Marine, 10);
        FakeUnit expectedTarget;

        FakeUnit[] enemies = fakeUnits(
                fake(AUnitType.Zerg_Hatchery, 11),
                expectedTarget = fake(AUnitType.Zerg_Overlord, 12),
                fake(AUnitType.Zerg_Zergling, 16)
        );

        usingFakeOurAndFakeEnemies(our, enemies, () -> {
            assertEquals(expectedTarget, ATargeting.defineBestEnemyToAttackFor(our));
        });
    }

}
