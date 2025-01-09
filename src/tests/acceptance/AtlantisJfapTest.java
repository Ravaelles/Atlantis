package tests.acceptance;

import atlantis.units.AUnitType;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import java.util.concurrent.Callable;

import static atlantis.units.AUnitType.Terran_Marine;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AtlantisJfapTest extends WorldStubForTests {
    private FakeUnit dragoon;
    private FakeUnit sunkenColony;

    @Test
    public void sunkenColoniesAreTakenIntoAccount() {
        Callable ours = () -> fakeOurs(
            dragoon = fake(AUnitType.Protoss_Dragoon, 10),
            fake(AUnitType.Protoss_Dragoon, 11),
            fake(AUnitType.Protoss_Dragoon, 11.1),
            fake(AUnitType.Protoss_Dragoon, 11.9)
        );
        Callable enemies = () -> fakeEnemies(
            sunkenColony = fake(AUnitType.Zerg_Sunken_Colony, 12),
            fake(AUnitType.Zerg_Larva, 12)
        );

        createWorld(1, () -> {
                double ourScore = dragoon.combatEvalAbsolute();
                double enemyScore = sunkenColony.combatEvalAbsolute();

                assertTrue(ourScore < -10);
                assertTrue(enemyScore < -10);
//                assertTrue(ourScore > enemyScore);
            },
            ours,
            enemies
        );
    }

    @Test
    public void marinesVsHydras() {
        FakeUnit marine;
        FakeUnit sunkenColony;
        FakeUnit hydra;

        FakeUnit[] our = fakeOurs(
            marine = fake(Terran_Marine, 10),
            fake(Terran_Marine, 10.1),
            fake(Terran_Marine, 10.2),
            fake(Terran_Marine, 10.3)
//            fake(Terran_Siege_Tank_Tank_Mode, 11),
//            fake(Terran_Siege_Tank_Tank_Mode, 12),
//            fake(Terran_Siege_Tank_Tank_Mode, 13)
//            fake(Terran_Siege_Tank_Tank_Mode, 14),
//            fake(Terran_Siege_Tank_Tank_Mode, 14.1),
//            fake(Terran_Siege_Tank_Tank_Mode, 14.2),
//            fake(Terran_Siege_Tank_Tank_Mode, 14.3),
//            fake(Terran_Siege_Tank_Tank_Mode, 14.4),
//            fake(Terran_Siege_Tank_Tank_Mode, 15)
        );
        FakeUnit[] enemies = fakeEnemies(
//            sunkenColony = fake(AUnitType.Zerg_Sunken_Colony, 12),
            hydra = fake(AUnitType.Zerg_Hydralisk, 12.1),
//            fake(AUnitType.Zerg_Hydralisk, 12.2),
//            fake(AUnitType.Zerg_Hydralisk, 12.3),
//            fake(AUnitType.Zerg_Hydralisk, 12.4),
//            fake(AUnitType.Zerg_Hydralisk, 12.5),
//            fake(AUnitType.Zerg_Hydralisk, 12.6),
            fake(AUnitType.Zerg_Hydralisk, 12.7),
            fake(AUnitType.Zerg_Hydralisk, 12.8),
            fake(AUnitType.Zerg_Hydralisk, 12.9)
        );

        createWorld(1, () -> {
                double ourScore = marine.combatEvalAbsolute();
                double enemyScore = hydra.combatEvalAbsolute();

//                System.err.println();
//                System.err.println("## our   SCORE = " + ourScore);
//                System.err.println("## enemy SCORE = " + enemyScore);

                assertTrue(ourScore < 0);
                assertTrue(ourScore < enemyScore);
            },
            () -> our,
            () -> enemies
        );
    }

}
