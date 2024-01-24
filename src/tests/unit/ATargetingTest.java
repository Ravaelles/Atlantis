package tests.unit;

import atlantis.combat.targeting.ATargeting;
import atlantis.game.AGame;
import atlantis.units.AUnitType;
import org.junit.Test;
import org.mockito.MockedStatic;

import static org.junit.Assert.*;

public class ATargetingTest extends AbstractTestWithUnits {
    public MockedStatic<AGame> aGame;

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
//                fake(AUnitType.Zerg_Spore_Colony, 12),
            fake(AUnitType.Zerg_Creep_Colony, 12),
            fake(AUnitType.Zerg_Drone, 13),
            fake(AUnitType.Zerg_Drone, 14),
            fake(AUnitType.Zerg_Hydralisk, 18),
            fake(AUnitType.Zerg_Zergling, 19),
            fake(AUnitType.Zerg_Sunken_Colony, 28)
        );

        usingFakeOurAndFakeEnemies(our, enemies, () -> {
            assertEquals(drone, ATargeting.defineBestEnemyToAttack(our));
        });
    }

    @Test
    public void targetsSunken() {
        FakeUnit our = fake(AUnitType.Protoss_Dragoon, 10);
        FakeUnit drone, ling1, hydra, sunken, ling2;

        FakeUnit[] enemies = fakeEnemies(
//                drone = fake(AUnitType.Zerg_Drone, 12),
            fake(AUnitType.Zerg_Larva, 11),
            fake(AUnitType.Zerg_Egg, 11),
            fake(AUnitType.Zerg_Hatchery, 11),
            fake(AUnitType.Zerg_Lurker_Egg, 11),
            fake(AUnitType.Zerg_Cocoon, 11),
            fake(AUnitType.Zerg_Creep_Colony, 12),
//                fake(AUnitType.Zerg_Spore_Colony, 12),
//                fake(AUnitType.Zerg_Drone, 13),
            fake(AUnitType.Zerg_Drone, 16),
//                ling1 = fake(AUnitType.Zerg_Zergling, 12.5),
            hydra = fake(AUnitType.Zerg_Hydralisk, 14),
            sunken = fake(AUnitType.Zerg_Sunken_Colony, 13.9),
            fake(AUnitType.Zerg_Creep_Colony, 11),
//                ling2 = fake(AUnitType.Zerg_Zergling, 12.9),
//                fake(AUnitType.Zerg_Zergling, 13),
//                fake(AUnitType.Zerg_Zergling, 14),
            fake(AUnitType.Zerg_Zergling, 15),
            fake(AUnitType.Zerg_Zergling, 16),
            fake(AUnitType.Zerg_Zergling, 17),
            fake(AUnitType.Zerg_Hydralisk, 18),
            fake(AUnitType.Zerg_Sunken_Colony, 28)
        );

        usingFakeOurAndFakeEnemies(our, enemies, () -> {
            assertEquals(sunken, ATargeting.defineBestEnemyToAttack(our));
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
            templar = fake(AUnitType.Protoss_High_Templar, 14.5),
            fake(AUnitType.Protoss_High_Templar, 16.5),
            fake(AUnitType.Zerg_Zergling, 18),
            fake(AUnitType.Zerg_Greater_Spire, 17),
            fake(AUnitType.Zerg_Hydralisk, 17),
            fake(AUnitType.Protoss_Dragoon, 28),
            fake(AUnitType.Zerg_Sunken_Colony, 29)
        );

        usingFakeOurAndFakeEnemies(our, enemies, () -> {
//            System.err.println("defineBestEnemyToAttackFor = " + ATargeting.defineBestEnemyToAttackFor(our));
            assertEquals(templar, ATargeting.defineBestEnemyToAttack(our));
        });
    }

    @Test
    public void targetsScoutsOverCannons() {
        FakeUnit our = fake(AUnitType.Terran_Marine, 10);
        FakeUnit scout;

        FakeUnit[] enemies = fakeEnemies(
            fake(AUnitType.Protoss_Photon_Cannon, 13.8),
            scout = fake(AUnitType.Protoss_Scout, 14),
            fake(AUnitType.Protoss_Dragoon, 21),
            fake(AUnitType.Protoss_Zealot, 22)
        );

        usingFakeOurAndFakeEnemies(our, enemies, () -> {
            assertEquals(scout, ATargeting.defineBestEnemyToAttack(our));
        });
    }

    @Test
    public void targetsCannonOverScoutsIfCannonIsNear() {
        FakeUnit our = fake(AUnitType.Terran_Marine, 10);
        FakeUnit scout;

        FakeUnit[] enemies = fakeEnemies(
            scout = fake(AUnitType.Protoss_Scout, 14),
            fake(AUnitType.Protoss_Photon_Cannon, 15),
            fake(AUnitType.Protoss_Dragoon, 21),
            fake(AUnitType.Protoss_Zealot, 22)
        );

        usingFakeOurAndFakeEnemies(our, enemies, () -> {
            assertEquals(scout, ATargeting.defineBestEnemyToAttack(our));
        });
    }

    @Test
    public void targetsCannonOverOtherBuildingsAndWorkers() {
        FakeUnit our = fake(AUnitType.Protoss_Dragoon, 10);
        FakeUnit cannon;

        FakeUnit[] enemies = fakeEnemies(
            fake(AUnitType.Protoss_Pylon, 11),
            fake(AUnitType.Protoss_Gateway, 12),
            fake(AUnitType.Protoss_Fleet_Beacon, 13),
            fake(AUnitType.Protoss_Nexus, 14),
            cannon = fake(AUnitType.Protoss_Photon_Cannon, 15),
            fake(AUnitType.Protoss_Pylon, 16),
            fake(AUnitType.Protoss_Pylon, 17),
            fake(AUnitType.Protoss_Dragoon, 21),
            fake(AUnitType.Protoss_Zealot, 22)
        );

        usingFakeOurAndFakeEnemies(our, enemies, () -> {
            assertEquals(cannon, ATargeting.defineBestEnemyToAttack(our));
        });
    }

    @Test
    public void doesNotTargetLarvas() {
        FakeUnit our = fake(AUnitType.Protoss_Dragoon, 10);
        FakeUnit building;

        FakeUnit[] enemies = fakeEnemies(
            fake(AUnitType.Zerg_Larva, 11),
            fake(AUnitType.Zerg_Egg, 11.2),
            fake(AUnitType.Zerg_Lurker_Egg, 11.4),
            fake(AUnitType.Zerg_Cocoon, 12),
            building = fake(AUnitType.Zerg_Hydralisk_Den, 17)
        );

        usingFakeOurAndFakeEnemies(our, enemies, () -> {
            assertEquals(building, ATargeting.defineBestEnemyToAttack(our));
        });
    }

    @Test
    public void targetsDoesNotTargetTooFarHighTemplars() {
        FakeUnit our = fake(AUnitType.Protoss_Dragoon, 10);
        FakeUnit spore;

        FakeUnit[] enemies = fakeEnemies(
            fake(AUnitType.Zerg_Larva, 11),
            fake(AUnitType.Zerg_Egg, 11),
            fake(AUnitType.Zerg_Lurker_Egg, 11),
            fake(AUnitType.Zerg_Cocoon, 11),
            spore = fake(AUnitType.Zerg_Spore_Colony, 12),
            fake(AUnitType.Zerg_Greater_Spire, 17),
            fake(AUnitType.Zerg_Hydralisk, 18),
            fake(AUnitType.Protoss_High_Templar, 21),
            fake(AUnitType.Protoss_Dragoon, 28),
            fake(AUnitType.Zerg_Sunken_Colony, 29)
        );

        usingFakeOurAndFakeEnemies(our, enemies, () -> {
            assertEquals(spore, ATargeting.defineBestEnemyToAttack(our));
        });
    }

    @Test
    public void targetsZerglingsOverSunkensWhenSiegingZerg() {
        FakeUnit our = fake(AUnitType.Protoss_Dragoon, 10);
        FakeUnit target;

        FakeUnit[] enemies = fakeEnemies(
            fake(AUnitType.Zerg_Larva, 10.8),
            fake(AUnitType.Zerg_Egg, 10.9),
            fake(AUnitType.Zerg_Lurker_Egg, 10.95),
            target = fake(AUnitType.Zerg_Zergling, 11.5),
            fake(AUnitType.Zerg_Sunken_Colony, 13.8),
            fake(AUnitType.Zerg_Sunken_Colony, 29)
        );

        usingFakeOurAndFakeEnemies(our, enemies, () -> {
            assertEquals(target, ATargeting.defineBestEnemyToAttack(our));
        });
    }

    @Test
    public void targetsSunkensOverZerglingsWhenSiegingZerg() {
        FakeUnit our = fake(AUnitType.Protoss_Dragoon, 10);
        FakeUnit target;

        FakeUnit[] enemies = fakeEnemies(
            fake(AUnitType.Zerg_Larva, 10.8),
            fake(AUnitType.Zerg_Egg, 10.9),
            fake(AUnitType.Zerg_Lurker_Egg, 10.95),
            fake(AUnitType.Zerg_Zergling, 13.5),
            target = fake(AUnitType.Zerg_Sunken_Colony, 13.8),
            fake(AUnitType.Zerg_Sunken_Colony, 29)
        );

        usingFakeOurAndFakeEnemies(our, enemies, () -> {
            assertEquals(target, ATargeting.defineBestEnemyToAttack(our));
        });
    }

    @Test
    public void zerglingsOverDrones() {
        FakeUnit our = fake(AUnitType.Terran_Marine, 10);
        FakeUnit expectedTarget;

        FakeUnit[] enemies = fakeEnemies(
            fake(AUnitType.Zerg_Drone, 11),
            fake(AUnitType.Zerg_Drone, 12),
            expectedTarget = fake(AUnitType.Zerg_Zergling, 13)
        );

        usingFakeOurAndFakeEnemies(our, enemies, () -> {
            assertEquals(expectedTarget, ATargeting.defineBestEnemyToAttack(our));
        });
    }

    @Test
    public void sunkensOverCreepColonies() {
        FakeUnit our = fake(AUnitType.Terran_Marine, 10);
        FakeUnit expectedTarget;

        FakeUnit[] enemies = fakeEnemies(
            fake(AUnitType.Zerg_Overlord, 11),
            fake(AUnitType.Zerg_Spawning_Pool, 11),
            fake(AUnitType.Zerg_Creep_Colony, 12),
            expectedTarget = fake(AUnitType.Zerg_Sunken_Colony, 13)
        );

        usingFakeOurAndFakeEnemies(our, enemies, () -> {
            assertEquals(expectedTarget, ATargeting.defineBestEnemyToAttack(our));
        });
    }

    @Test
    public void spawningPools() {
        FakeUnit our = fake(AUnitType.Terran_Marine, 10);
        FakeUnit expectedTarget;

        FakeUnit[] enemies = fakeEnemies(
            fake(AUnitType.Zerg_Hydralisk_Den, 11),
            fake(AUnitType.Zerg_Egg, 12),
            expectedTarget = fake(AUnitType.Zerg_Spawning_Pool, 13),
            fake(AUnitType.Zerg_Evolution_Chamber, 13)
        );

        usingFakeOurAndFakeEnemies(our, enemies, () -> {
            assertEquals(expectedTarget, ATargeting.defineBestEnemyToAttack(our));
        });
    }

    @Test
    public void overlords() {
        FakeUnit our = fake(AUnitType.Terran_Marine, 10);
        FakeUnit expectedTarget;

        FakeUnit[] enemies = fakeEnemies(
            fake(AUnitType.Zerg_Hydralisk_Den, 13),
            expectedTarget = fake(AUnitType.Zerg_Overlord, 12),
            fake(AUnitType.Zerg_Zergling, 19)
        );

        usingFakeOurAndFakeEnemies(our, enemies, () -> {
            assertEquals(expectedTarget, ATargeting.defineBestEnemyToAttack(our));
        });
    }

    @Test
    public void guardians() {
        FakeUnit our = fake(AUnitType.Terran_Marine, 10);
        FakeUnit expectedTarget;

        FakeUnit[] enemies = fakeEnemies(
            fake(AUnitType.Zerg_Hydralisk_Den, 11),
            fake(AUnitType.Zerg_Drone, 12),
            expectedTarget = fake(AUnitType.Zerg_Guardian, 12.5),
            fake(AUnitType.Zerg_Zergling, 19)
        );

        usingFakeOurAndFakeEnemies(our, enemies, () -> {
            assertEquals(expectedTarget, ATargeting.defineBestEnemyToAttack(our));
        });
    }

    @Test
    public void targetsMarinesOverBunker() {
        FakeUnit our = fake(AUnitType.Protoss_Dragoon, 10);
        FakeUnit expectedTarget;

        FakeUnit[] enemies = fakeEnemies(
            expectedTarget = fake(AUnitType.Terran_Marine, 11.1),
            fake(AUnitType.Terran_Marine, 12.1),
            fake(AUnitType.Terran_Bunker, 13.1),
            fake(AUnitType.Terran_Marine, 13.2)
        );

        usingFakeOurAndFakeEnemies(our, enemies, () -> {
            assertEquals(expectedTarget, ATargeting.defineBestEnemyToAttack(our));
        });
    }

    @Test
    public void targetsMostWoundedMarineOverBunker() {
        FakeUnit our = fake(AUnitType.Protoss_Dragoon, 10);
        FakeUnit expectedTarget;

        FakeUnit[] enemies = fakeEnemies(
            fake(AUnitType.Terran_Marine, 11.1),
            expectedTarget = fake(AUnitType.Terran_Marine, 12.1).setHp(11),
            fake(AUnitType.Terran_Bunker, 13.1),
            fake(AUnitType.Terran_Marine, 13.2)
        );

        usingFakeOurAndFakeEnemies(our, enemies, () -> {
            assertEquals(expectedTarget, ATargeting.defineBestEnemyToAttack(our));
        });
    }
}
