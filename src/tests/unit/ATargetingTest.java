package tests.unit;

import atlantis.combat.micro.avoid.AAvoidUnits;
import atlantis.combat.targeting.ATargeting;
import atlantis.game.AGame;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.FakeFoggedUnit;
import atlantis.units.select.BaseSelect;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class ATargetingTest extends AbstractTestWithUnits {

    public MockedStatic<AGame> aGame;
//    public MockedStatic<EnemyInfo> enemyInformation;
//    public MockedStatic<EnemyUnits> enemyUnitsMock;

    @Test
    public void targetsWorkers() {
        FakeUnit our = fake(AUnitType.Protoss_Dragoon, 10);
        FakeUnit drone, ling1, hydra, sunken, ling2;

        FakeUnit[] enemies = fakeEnemies(
                drone = fake(AUnitType.Zerg_Drone, 12),
                fake(AUnitType.Zerg_Larva, 11),
                fake(AUnitType.Zerg_Egg, 11),
                fake(AUnitType.Zerg_Hatchery, 11),
                fake(AUnitType.Zerg_Lurker_Egg, 11),
                fake(AUnitType.Zerg_Cocoon, 11),
//                fake(AUnitType.Zerg_Creep_Colony, 12),
                fake(AUnitType.Zerg_Spore_Colony, 12),
                fake(AUnitType.Zerg_Drone, 13),
                fake(AUnitType.Zerg_Drone, 14),
                fake(AUnitType.Zerg_Hydralisk, 18),
                fake(AUnitType.Zerg_Zergling, 19),
                fake(AUnitType.Zerg_Sunken_Colony, 28)
        );

        usingFakeOurAndFakeEnemies(our, enemies, () -> {
            assertEquals(drone, ATargeting.defineBestEnemyToAttackFor(our));
        });
    }

    @Test
    public void targetsSunken() {
        FakeUnit our = fake(AUnitType.Protoss_Dragoon, 10);
        FakeUnit drone, ling1, hydra, sunken, ling2;

        FakeUnit[] enemies = fakeEnemies(
                drone = fake(AUnitType.Zerg_Drone, 12),
                fake(AUnitType.Zerg_Larva, 11),
                fake(AUnitType.Zerg_Egg, 11),
                fake(AUnitType.Zerg_Hatchery, 11),
                fake(AUnitType.Zerg_Lurker_Egg, 11),
                fake(AUnitType.Zerg_Cocoon, 11),
                fake(AUnitType.Zerg_Creep_Colony, 12),
                fake(AUnitType.Zerg_Spore_Colony, 12),
                fake(AUnitType.Zerg_Drone, 13),
                fake(AUnitType.Zerg_Drone, 14),
                ling1 = fake(AUnitType.Zerg_Zergling, 12.5),
                hydra = fake(AUnitType.Zerg_Hydralisk, 14),
                sunken = fake(AUnitType.Zerg_Sunken_Colony, 16),
                fake(AUnitType.Zerg_Creep_Colony, 11),
                ling2 = fake(AUnitType.Zerg_Zergling, 12.9),
                fake(AUnitType.Zerg_Zergling, 13),
                fake(AUnitType.Zerg_Zergling, 14),
                fake(AUnitType.Zerg_Zergling, 15),
                fake(AUnitType.Zerg_Zergling, 16),
                fake(AUnitType.Zerg_Zergling, 17),
                fake(AUnitType.Zerg_Hydralisk, 18),
                fake(AUnitType.Zerg_Sunken_Colony, 28)
        );

        usingFakeOurAndFakeEnemies(our, enemies, () -> {
            assertEquals(sunken, ATargeting.defineBestEnemyToAttackFor(our));
        });
    }

    @Test
    public void targetsTargetsHighTemplars() {
        FakeUnit our = fake(AUnitType.Protoss_Dragoon, 10);
        FakeUnit templar;

        FakeUnit[] enemies = fakeEnemies(
            fake(AUnitType.Zerg_Larva, 11),
            fake(AUnitType.Zerg_Egg, 11),
            fake(AUnitType.Zerg_Lurker_Egg, 11),
            fake(AUnitType.Zerg_Cocoon, 11),
            fake(AUnitType.Zerg_Spore_Colony, 12),
            fake(AUnitType.Zerg_Greater_Spire, 17),
            fake(AUnitType.Zerg_Zergling, 16),
            fake(AUnitType.Zerg_Hydralisk, 17),
            templar = fake(AUnitType.Protoss_High_Templar, 17.5),
            fake(AUnitType.Protoss_Dragoon, 28),
            fake(AUnitType.Zerg_Sunken_Colony, 29)
        );

        usingFakeOurAndFakeEnemies(our, enemies, () -> {
//            System.out.println("defineBestEnemyToAttackFor = " + ATargeting.defineBestEnemyToAttackFor(our));
            assertEquals(templar, ATargeting.defineBestEnemyToAttackFor(our));
        });
    }

    @Test
    public void targetsDoesNotTargetTooFarHighTemplars() {
        FakeUnit our = fake(AUnitType.Protoss_Dragoon, 10);
        FakeUnit drone, ling1, hydra, sunken, ling2;

        FakeUnit[] enemies = fakeEnemies(
            fake(AUnitType.Zerg_Larva, 11),
            fake(AUnitType.Zerg_Egg, 11),
            fake(AUnitType.Zerg_Lurker_Egg, 11),
            fake(AUnitType.Zerg_Cocoon, 11),
            fake(AUnitType.Zerg_Spore_Colony, 12),
            fake(AUnitType.Zerg_Greater_Spire, 17),
            hydra = fake(AUnitType.Zerg_Hydralisk, 18),
            fake(AUnitType.Protoss_High_Templar, 21),
            fake(AUnitType.Protoss_Dragoon, 28),
            fake(AUnitType.Zerg_Sunken_Colony, 29)
        );

        usingFakeOurAndFakeEnemies(our, enemies, () -> {
            assertEquals(hydra, ATargeting.defineBestEnemyToAttackFor(our));
        });
    }

}