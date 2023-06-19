package tests.acceptance;

import atlantis.combat.eval.AtlantisJfap;
import atlantis.game.AGame;
import atlantis.units.AUnitType;
import org.junit.Test;
import org.mockito.MockedStatic;
import tests.unit.AbstractTestWithUnits;
import tests.unit.FakeUnit;

import static atlantis.units.AUnitType.Terran_Marine;
import static atlantis.units.AUnitType.Terran_Siege_Tank_Tank_Mode;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AtlantisJfapTest extends NonAbstractTestFakingGame {

    @Test
    public void sunkenColoniesAreTakenIntoAccount() {
        FakeUnit dragoon1;
        FakeUnit sunkenColony;

        FakeUnit[] our = fakeOurs(
            dragoon1 = fake(AUnitType.Protoss_Dragoon, 10),
            fake(AUnitType.Protoss_Dragoon, 11),
            fake(AUnitType.Protoss_Dragoon, 11.1),
            fake(AUnitType.Protoss_Dragoon, 11.9)
        );
        FakeUnit[] enemies = fakeEnemies(
            sunkenColony = fake(AUnitType.Zerg_Sunken_Colony, 12),
            fake(AUnitType.Zerg_Larva, 12)
        );

        createWorld(1, () -> {
                double ourScore = dragoon1.combatEvalAbsolute();
                double enemyScore = sunkenColony.combatEvalAbsolute();

//                System.out.println();
//                System.out.println("## our   SCORE = " + ourScore);
//                System.out.println("## enemy SCORE = " + enemyScore);

                assertTrue(ourScore < -10);
                assertTrue(enemyScore < -10);
//                assertTrue(ourScore > enemyScore);
            },
            () -> our,
            () -> enemies
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
//            fake(Terran_Siege_Tank_Tank_Mode, 12),
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

                System.out.println();
                System.out.println("## our   SCORE = " + ourScore);
                System.out.println("## enemy SCORE = " + enemyScore);

                assertTrue(ourScore < 0);
                assertTrue(ourScore < enemyScore);
            },
            () -> our,
            () -> enemies
        );
    }

}
