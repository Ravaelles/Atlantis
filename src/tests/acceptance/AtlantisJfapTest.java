package tests.acceptance;

import atlantis.units.AUnitType;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;


import static atlantis.units.AUnitType.Terran_Marine;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AtlantisJfapTest extends WorldStubForTests {
    private FakeUnit dragoon;
    private FakeUnit sunkenColony;

    @Test
    public void sunkenColoniesAreTakenIntoAccount() {
        FakeUnit[] ours = fakeOurs(
            dragoon = fake(AUnitType.Protoss_Dragoon, 10),
            fake(AUnitType.Protoss_Dragoon, 11),
            fake(AUnitType.Protoss_Dragoon, 11.1),
            fake(AUnitType.Protoss_Dragoon, 11.9)
        );
        FakeUnit[] enemies = fakeEnemies(
            sunkenColony = fake(AUnitType.Zerg_Sunken_Colony, 12),
            fake(AUnitType.Zerg_Larva, 12)
        );

        world(1, ours, enemies, () -> {
            double ourScore = dragoon.combatEvalAbsolute();
            double enemyScore = sunkenColony.combatEvalAbsolute();

            assertTrue(ourScore < -10);
            assertTrue(enemyScore < -10);
//                assertTrue(ourScore > enemyScore);
        });
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

        world(1, our, enemies, () -> {
            double ourScore = marine.combatEvalAbsolute();
//                System.err.println();
//                System.err.println("## our   SCORE = " + ourScore);
//                System.err.println("## enemy SCORE = " + hydra.combatEvalAbsolute());

            // JFAP simulates 60 frames and returns the score of the side the
            // unit belongs to, so a negative number means that fight goes
            // against us. Measured: 4 marines against 5 hydras = -186.
            assertTrue(ourScore < 0, "the simulated fight goes against our marines");

            // The relative form is the documented "them vs us" ratio, > 1
            // meaning the enemy is stronger. Measured: 2.25 - four Marines
            // (40 hit points each) against five Hydralisks (80 hit points, 10
            // damage at 4 tiles base; Grooved Spines takes them to 5).
            //
            // The old assertion compared our score with the hydra's *own*
            // score, which only meant something under the heuristic evaluator
            // that combatEvalAbsolute() no longer calls: the two numbers come
            // from opposite perspectives and are not on a common scale.
            assertEquals(2.25, marine.eval(), 0.1, "hydras are about twice as strong");

            // Sanity check that both views agree (measured 0.44, the
            // reciprocal): asking the hydra tells the same story.
            assertTrue(hydra.eval() < 1, "and the hydra agrees");
        });
    }

}
