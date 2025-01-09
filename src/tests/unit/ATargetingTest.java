package tests.unit;

import atlantis.combat.targeting.generic.ATargeting;
import atlantis.debug.DebugFlags;
import atlantis.game.AGame;
import atlantis.units.AUnitType;
import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ATargetingTest extends WorldStubForTests {
    public MockedStatic<AGame> aGame;

    @Override
    public void init() {
        DebugFlags.DEBUG_TARGETING = true;

        super.init();
    }

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
//            fake(AUnitType.Zerg_Creep_Colony, 12),
            fake(AUnitType.Zerg_Drone, 13),
            fake(AUnitType.Zerg_Drone, 14),
            fake(AUnitType.Zerg_Hydralisk, 18),
            fake(AUnitType.Zerg_Zergling, 19),
            fake(AUnitType.Zerg_Sunken_Colony, 28)
        );

        createWorld(1,
            () -> {
                assertEquals(drone, ATargeting.defineBestEnemyToAttack(our));
            }, () -> fakeOurs(our), () -> enemies
        );
    }

    @Test
    public void targetsSunken() {
        FakeUnit our = fake(AUnitType.Protoss_Dragoon, 10);
        FakeUnit drone, ling1, hydra, sunken, ling2;

        FakeUnit[] enemies = fakeEnemies(
//                drone = fake(AUnitType.Zerg_Drone, 12),
            fake(AUnitType.Zerg_Larva, 11),
            fake(AUnitType.Zerg_Egg, 11.1),
            fake(AUnitType.Zerg_Hatchery, 11.2),
            fake(AUnitType.Zerg_Lurker_Egg, 11.3),
            fake(AUnitType.Zerg_Creep_Colony, 11.4),
            fake(AUnitType.Zerg_Cocoon, 11.5),
            fake(AUnitType.Zerg_Creep_Colony, 12.6),
//                fake(AUnitType.Zerg_Spore_Colony, 12),
//                fake(AUnitType.Zerg_Drone, 13),
//                ling1 = fake(AUnitType.Zerg_Zergling, 12.5),
            fake(AUnitType.Zerg_Drone, 13.6),
            fake(AUnitType.Zerg_Hatchery, 13.7),
            sunken = fake(AUnitType.Zerg_Sunken_Colony, 13.9),
//                ling2 = fake(AUnitType.Zerg_Zergling, 12.9),
//                fake(AUnitType.Zerg_Zergling, 13),
//                fake(AUnitType.Zerg_Zergling, 14),
            fake(AUnitType.Zerg_Zergling, 15),
            fake(AUnitType.Zerg_Drone, 15.8),
            fake(AUnitType.Zerg_Zergling, 15.9),
            hydra = fake(AUnitType.Zerg_Hydralisk, 16.2),
            fake(AUnitType.Zerg_Zergling, 17),
            fake(AUnitType.Zerg_Hydralisk, 18),
            fake(AUnitType.Zerg_Sunken_Colony, 28)
        );

        createWorld(1,
            () -> {
                assertEquals(sunken, ATargeting.defineBestEnemyToAttack(our));
            }, () -> fakeOurs(our), () -> enemies
        );
    }

    @Test
    public void targetsUnfinishedSunkenOverBaseOrDrones() {
        FakeUnit our = fake(AUnitType.Protoss_Dragoon, 10);
        FakeUnit drone, ling1, hydra, sunken, ling2;

        FakeUnit[] enemies = fakeEnemies(
//                drone = fake(AUnitType.Zerg_Drone, 12),
            fake(AUnitType.Zerg_Larva, 11),
            fake(AUnitType.Zerg_Egg, 11.1),
            fake(AUnitType.Zerg_Hatchery, 11.2),
            fake(AUnitType.Zerg_Lurker_Egg, 11.3),
            fake(AUnitType.Zerg_Creep_Colony, 11.4),
            fake(AUnitType.Zerg_Cocoon, 11.5),
            fake(AUnitType.Zerg_Drone, 13.6),
            fake(AUnitType.Zerg_Hatchery, 13.7),
            fake(AUnitType.Zerg_Creep_Colony, 14.6),
            sunken = fake(AUnitType.Zerg_Sunken_Colony, 14.9).setCompleted(false),
            fake(AUnitType.Zerg_Zergling, 15),
            fake(AUnitType.Zerg_Drone, 15.8),
            fake(AUnitType.Zerg_Zergling, 15.9),
            hydra = fake(AUnitType.Zerg_Hydralisk, 16.2),
            fake(AUnitType.Zerg_Zergling, 17),
            fake(AUnitType.Zerg_Hydralisk, 18),
            fake(AUnitType.Zerg_Sunken_Colony, 28)
        );

        createWorld(1,
            () -> {
                assertEquals(sunken, ATargeting.defineBestEnemyToAttack(our));
            }, () -> fakeOurs(our), () -> enemies
        );
    }

    @Test
    public void targetsCreepOverBaseOrDrones() {
        FakeUnit our = fake(AUnitType.Protoss_Dragoon, 10);
        FakeUnit drone, ling1, hydra, colony, ling2;

        FakeUnit[] enemies = fakeEnemies(
//                drone = fake(AUnitType.Zerg_Drone, 12),
            fake(AUnitType.Zerg_Larva, 11),
            fake(AUnitType.Zerg_Egg, 11.1),
            fake(AUnitType.Zerg_Hatchery, 11.2),
            fake(AUnitType.Zerg_Lurker_Egg, 11.3),
            fake(AUnitType.Zerg_Cocoon, 11.5),
            fake(AUnitType.Zerg_Drone, 13.6),
            fake(AUnitType.Zerg_Hatchery, 13.7),
            colony = fake(AUnitType.Zerg_Creep_Colony, 14.9),
            fake(AUnitType.Zerg_Drone, 15.8),
            hydra = fake(AUnitType.Zerg_Hydralisk, 18.2),
            fake(AUnitType.Zerg_Hydralisk, 18.3),
            fake(AUnitType.Zerg_Zergling, 18.9),
            fake(AUnitType.Zerg_Sunken_Colony, 28)
        );

        createWorld(1,
            () -> {
                assertEquals(colony, ATargeting.defineBestEnemyToAttack(our));
            }, () -> fakeOurs(our), () -> enemies
        );
    }

    @Test
    public void targetsSuperCloseDronesOverCreep() {
        FakeUnit our = fake(AUnitType.Terran_Marine, 10);
        FakeUnit drone, ling1, hydra, colony, ling2;

        FakeUnit[] enemies = fakeEnemies(
//                drone = fake(AUnitType.Zerg_Drone, 12),
            drone = fake(AUnitType.Zerg_Drone, 11),
            fake(AUnitType.Zerg_Larva, 11.1),
            fake(AUnitType.Zerg_Egg, 11.2),
            fake(AUnitType.Zerg_Hatchery, 11.2),
            fake(AUnitType.Zerg_Lurker_Egg, 11.3),
            fake(AUnitType.Zerg_Cocoon, 11.5),
            fake(AUnitType.Zerg_Hatchery, 13.7),
            colony = fake(AUnitType.Zerg_Creep_Colony, 14.9),
            fake(AUnitType.Zerg_Drone, 15.8),
            hydra = fake(AUnitType.Zerg_Hydralisk, 18.2),
            fake(AUnitType.Zerg_Hydralisk, 18.3),
            fake(AUnitType.Zerg_Zergling, 18.9),
            fake(AUnitType.Zerg_Sunken_Colony, 28)
        );

        createWorld(1,
            () -> {
                assertEquals(drone, ATargeting.defineBestEnemyToAttack(our));
            }, () -> fakeOurs(our), () -> enemies
        );
    }

    @Test
    public void targetsUnfinishedSunken() {
        FakeUnit our = fake(AUnitType.Protoss_Dragoon, 10);
        FakeUnit drone, ling1, hydra, sunken, ling2;

        FakeUnit[] enemies = fakeEnemies(
//                drone = fake(AUnitType.Zerg_Drone, 12),
            fake(AUnitType.Zerg_Larva, 11),
            fake(AUnitType.Zerg_Egg, 11.1),
            fake(AUnitType.Zerg_Hatchery, 11.2),
            fake(AUnitType.Zerg_Lurker_Egg, 11.3),
            fake(AUnitType.Zerg_Cocoon, 11.4),
//            fake(AUnitType.Zerg_Creep_Colony, 12),
//                fake(AUnitType.Zerg_Spore_Colony, 12),
//                fake(AUnitType.Zerg_Drone, 13),
//                ling1 = fake(AUnitType.Zerg_Zergling, 12.5),
            fake(AUnitType.Zerg_Creep_Colony, 11.5),
            sunken = fake(AUnitType.Zerg_Sunken_Colony, 13.9).setCompleted(false),
//            sunken = fake(AUnitType.Zerg_Sunken_Colony, 13.9),
//                ling2 = fake(AUnitType.Zerg_Zergling, 12.9),
//                fake(AUnitType.Zerg_Zergling, 13),
//                fake(AUnitType.Zerg_Zergling, 14),
            fake(AUnitType.Zerg_Zergling, 15),
            fake(AUnitType.Zerg_Drone, 16),
            fake(AUnitType.Zerg_Zergling, 16.1),
            fake(AUnitType.Zerg_Zergling, 17),
            fake(AUnitType.Zerg_Hydralisk, 18),
            fake(AUnitType.Zerg_Sunken_Colony, 28)
        );

        createWorld(1,
            () -> {
                assertEquals(sunken, ATargeting.defineBestEnemyToAttack(our));
            }, () -> fakeOurs(our), () -> enemies
        );
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
            templar = fake(AUnitType.Protoss_High_Templar, 14.5),
            fake(AUnitType.Protoss_High_Templar, 16.5),
            fake(AUnitType.Zerg_Zergling, 18),
            fake(AUnitType.Zerg_Greater_Spire, 17),
            fake(AUnitType.Zerg_Hydralisk, 17),
            fake(AUnitType.Zerg_Spore_Colony, 27),
            fake(AUnitType.Protoss_Dragoon, 28),
            fake(AUnitType.Zerg_Sunken_Colony, 29)
        );

        createWorld(1,
            () -> {
                assertEquals(templar, ATargeting.defineBestEnemyToAttack(our));
            }, () -> fakeOurs(our), () -> enemies
        );
    }

    @Test
    public void targetsScoutsOverGroundUnits() {
        FakeUnit our = fake(AUnitType.Terran_Marine, 10);
        FakeUnit scout;

        FakeUnit[] enemies = fakeEnemies(
            fake(AUnitType.Protoss_Zealot, 13.8),
            scout = fake(AUnitType.Protoss_Scout, 14),
            fake(AUnitType.Protoss_Dragoon, 21),
            fake(AUnitType.Protoss_Zealot, 22)
        );

        createWorld(1,
            () -> {
                assertEquals(scout, ATargeting.defineBestEnemyToAttack(our));
            }, () -> fakeOurs(our), () -> enemies
        );
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

        createWorld(1,
            () -> {
                assertEquals(scout, ATargeting.defineBestEnemyToAttack(our));
            }, () -> fakeOurs(our), () -> enemies
        );
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

        createWorld(1,
            () -> {
                assertEquals(cannon, ATargeting.defineBestEnemyToAttack(our));
            }, () -> fakeOurs(our), () -> enemies
        );
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

        createWorld(1,
            () -> {
                assertEquals(building, ATargeting.defineBestEnemyToAttack(our));
            }, () -> fakeOurs(our), () -> enemies
        );
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

        createWorld(1,
            () -> {
                assertEquals(spore, ATargeting.defineBestEnemyToAttack(our));
            }, () -> fakeOurs(our), () -> enemies
        );
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

        createWorld(1,
            () -> {
                assertEquals(target, ATargeting.defineBestEnemyToAttack(our));
            }, () -> fakeOurs(our), () -> enemies
        );
    }

    @Test
    public void targetsSunkensOverZerglingsWhenSiegingZerg() {
        FakeUnit our = fake(AUnitType.Protoss_Dragoon, 10);
        FakeUnit target;

        FakeUnit[] enemies = fakeEnemies(
            fake(AUnitType.Zerg_Larva, 10.8),
            fake(AUnitType.Zerg_Egg, 10.9),
            fake(AUnitType.Zerg_Lurker_Egg, 10.95),
            fake(AUnitType.Zerg_Zergling, 14.9),
            target = fake(AUnitType.Zerg_Sunken_Colony, 13.8),
            fake(AUnitType.Zerg_Sunken_Colony, 29)
        );

        createWorld(1,
            () -> {
                assertEquals(target, ATargeting.defineBestEnemyToAttack(our));
            }, () -> fakeOurs(our), () -> enemies
        );
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

        createWorld(1,
            () -> {
                assertEquals(expectedTarget, ATargeting.defineBestEnemyToAttack(our));
            }, () -> fakeOurs(our), () -> enemies
        );
    }

    @Test
    public void nearHydrasOverWounded() {
        FakeUnit our = fake(AUnitType.Protoss_Dragoon, 10);
        FakeUnit expectedTarget;

        FakeUnit[] enemies = fakeEnemies(
            expectedTarget = fake(AUnitType.Zerg_Hydralisk, 16.1),
            fake(AUnitType.Zerg_Hydralisk, 17).setHp(30),
            fake(AUnitType.Zerg_Hydralisk, 18)
        );

        createWorld(1,
            () -> {
                assertEquals(expectedTarget, ATargeting.defineBestEnemyToAttack(our));
            }, () -> fakeOurs(our), () -> enemies
        );
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

        createWorld(1,
            () -> {
                assertEquals(expectedTarget, ATargeting.defineBestEnemyToAttack(our));
            }, () -> fakeOurs(our), () -> enemies
        );
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

        createWorld(1,
            () -> {
                assertEquals(expectedTarget, ATargeting.defineBestEnemyToAttack(our));
            }, () -> fakeOurs(our), () -> enemies
        );
    }

    @Test
    public void doesNotTargetOverlords() {
        FakeUnit our = fake(AUnitType.Terran_Marine, 10);
        FakeUnit expectedTarget;

        FakeUnit[] enemies = fakeEnemies(
            fake(AUnitType.Zerg_Hydralisk_Den, 13),
            fake(AUnitType.Zerg_Overlord, 12),
            expectedTarget = fake(AUnitType.Zerg_Zergling, 16)
        );

        createWorld(1,
            () -> {
                assertEquals(expectedTarget, ATargeting.defineBestEnemyToAttack(our));
            }, () -> fakeOurs(our), () -> enemies
        );
    }

    @Test
    public void itAllowsTargetingOverlords() {
        FakeUnit our = fake(AUnitType.Protoss_Dragoon, 10);
        FakeUnit expectedTarget;

        FakeUnit[] enemies = fakeEnemies(
            fake(AUnitType.Zerg_Hydralisk_Den, 13),
            expectedTarget = fake(AUnitType.Zerg_Overlord, 12),
            fake(AUnitType.Zerg_Zergling, 20)
        );

        createWorld(1,
            () -> {
                assertEquals(expectedTarget, ATargeting.defineBestEnemyToAttack(our));
            }, () -> fakeOurs(our), () -> enemies
        );
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

        createWorld(1,
            () -> {
                assertEquals(expectedTarget, ATargeting.defineBestEnemyToAttack(our));
            }, () -> fakeOurs(our), () -> enemies
        );
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

        createWorld(1,
            () -> {
                assertEquals(expectedTarget, ATargeting.defineBestEnemyToAttack(our));
            }, () -> fakeOurs(our), () -> enemies
        );
    }

    @Test
    public void targetsMarinesOverBunkerYup() {
        FakeUnit our = fake(AUnitType.Protoss_Dragoon, 10);
        FakeUnit expectedTarget;

        FakeUnit[] enemies = fakeEnemies(
//            fake(AUnitType.Terran_Marine, 12.9),
            fake(AUnitType.Terran_Bunker, 13.1),
            expectedTarget = fake(AUnitType.Terran_Marine, 13.2)
        );

        createWorld(1,
            () -> {
                assertEquals(expectedTarget, ATargeting.defineBestEnemyToAttack(our));
            }, () -> fakeOurs(our), () -> enemies
        );
    }

    @Test
    @Ignore
    public void targetsMostWoundedMarineOverBunker() {
        FakeUnit our = fake(AUnitType.Protoss_Dragoon, 10);
        FakeUnit expectedTarget;

        FakeUnit[] enemies = fakeEnemies(
            fake(AUnitType.Terran_Marine, 11.1),
            expectedTarget = fake(AUnitType.Terran_Marine, 12.1).setHp(11),
            fake(AUnitType.Terran_Bunker, 13.1),
            fake(AUnitType.Terran_Marine, 13.2)
        );

        createWorld(1,
            () -> {
//                Select.enemy().print();

                assertEquals(expectedTarget, ATargeting.defineBestEnemyToAttack(our));
            }, () -> fakeOurs(our), () -> enemies
        );
    }

    @Test
    public void targetsCannonOverOtherUnits() {
        FakeUnit our = fake(AUnitType.Protoss_Zealot, 10);
        FakeUnit expectedTarget;
        FakeUnit gate;

        FakeUnit[] enemies = fakeEnemies(
            fake(AUnitType.Protoss_Gateway, 10.1),
            fake(AUnitType.Protoss_Pylon, 10.2),
            fake(AUnitType.Protoss_Cybernetics_Core, 10.3),
            gate = fake(AUnitType.Protoss_Gateway, 13.1),
            fake(AUnitType.Protoss_Pylon, 13.2),
            fake(AUnitType.Protoss_Templar_Archives, 13.5),
            fake(AUnitType.Protoss_Photon_Cannon, 13.6).setHp(66),
            expectedTarget = fake(AUnitType.Protoss_Photon_Cannon, 13.7).setHp(11),
            fake(AUnitType.Protoss_Nexus, 13.9)
        );

//        our.attackUnit(gate);

        createWorld(1,
            () -> {
                assertEquals(expectedTarget, ATargeting.defineBestEnemyToAttack(our));
            }, () -> fakeOurs(our), () -> enemies
        );
    }
}
