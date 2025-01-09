package tests.acceptance;

import atlantis.combat.running.RunToPositionFinder;
import atlantis.combat.running.any_direction.RunInAnyDirection;
import atlantis.game.AGame;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import tests.fakes.FakeUnit;

public class RunInAnyDirectionTest extends WorldStubForTests {
    public MockedStatic<AGame> aGame;

    @Test
    public void hello() {
        FakeUnit marine = fake(AUnitType.Terran_Marine, 10);
        FakeUnit[] ours = fakeOurs(marine);

        FakeUnit drone, zergling, hydra, sunken, ling2;

        FakeUnit[] enemies = fakeEnemies(
            zergling = fake(AUnitType.Zerg_Zergling, 10.9),
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
            hydra = fake(AUnitType.Zerg_Hydralisk, 14),
            sunken = fake(AUnitType.Zerg_Sunken_Colony, 16),
            fake(AUnitType.Zerg_Creep_Colony, 11),
            ling2 = fake(AUnitType.Zerg_Zergling, 12.9),
            fake(AUnitType.Zerg_Zergling, 13),
            fake(AUnitType.Zerg_Zergling, 14),
            fake(AUnitType.Zerg_Zergling, 15),
            fake(AUnitType.Zerg_Zergling, 16),
            fake(AUnitType.Zerg_Zergling, 17),
            fake(AUnitType.Zerg_Hydralisk, 19),
            fake(AUnitType.Zerg_Sunken_Colony, 28)
        );

        usingFakeOursAndFakeEnemies(ours, enemies, () -> {
            RunInAnyDirection runInAnyDirection = (new RunInAnyDirection(
                new RunToPositionFinder(
                    marine.runningManager()
                )
            ));

            System.out.println(runInAnyDirection.runInAnyDirection(zergling));

//            assertContainsAll(
//                new FakeUnit[]{drone, zergling, hydra, sunken},
//                (new EnemyUnitsToAvoid(marine)).enemiesDangerouslyClose().array()
//            );
        });
    }
}
