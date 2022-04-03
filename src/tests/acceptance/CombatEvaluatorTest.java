package tests.acceptance;

import atlantis.combat.eval.ACombatEvaluator;
import atlantis.information.enemy.EnemyUnitsUpdater;
import atlantis.units.AUnitType;
import org.junit.Test;
import tests.unit.FakeUnit;

import static atlantis.units.AUnitType.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CombatEvaluatorTest extends AbstractTestFakingGame {

    private FakeUnit marine;
    private FakeUnit wraith;

    @Test
    public void returnsRelativeAndAbsoluteValuesThatMakeSense() {
        createWorld(1, () -> {
            FakeUnit enemy = nearestEnemy(marine);

//            Select.our().print();
//            Select.enemy().print();

            double ourEval = marine.combatEvalAbsolute();
            double enemyEval = enemy.combatEvalAbsolute();

//            System.out.println("ourEval = " + ourEval);
//            System.out.println("enemyEval = " + enemyEval);

            assertTrue(ourEval < enemyEval);
            assertTrue(ourEval * 7 > enemyEval);

            ourEval = marine.combatEvalRelative();
            enemyEval = enemy.combatEvalRelative();

//            System.out.println("ourEval = " + ourEval);
//            System.out.println("enemyEval = " + enemyEval);

            assertTrue(ourEval < 0.2);
            assertTrue(enemyEval > 2);

//            assertTrue(Math.abs(ourEval - 0.3) < 0.1);
//            assertTrue(Math.abs(enemyEval - 3.1) < 0.1);
        });
    }

    @Test
    public void takesIntoAccountFoggedUnits() {
        createWorld(1, () -> {
            FakeUnit cannon;
            EnemyUnitsUpdater.weDiscoveredEnemyUnit(cannon = fakeEnemy(Protoss_Photon_Cannon, 92));
            EnemyUnitsUpdater.weDiscoveredEnemyUnit(fakeEnemy(Protoss_Gateway, 93));

//            Select.our().print("Our");
//            System.out.println("wraith = " + wraith);
//            System.out.println("cannon = " + cannon);

            assertEquals(1, ACombatEvaluator.opposingUnits(wraith).size());
            assertEquals(1, ACombatEvaluator.opposingUnits(cannon).size());

            double ourEval = wraith.combatEvalAbsolute();
            double enemyEval = cannon.combatEvalAbsolute();

//            System.out.println("ourEval = " + ourEval);
//            System.out.println("enemyEval = " + enemyEval);

            assertTrue(ourEval > 0);
            assertTrue(enemyEval > 0);
            assertTrue(ourEval < enemyEval);
            assertTrue(ourEval * 8 > enemyEval);
        });
    }

    @Test
    public void consistentlyEvaluatesFoggedUnits() {
        FakeUnit cannon1 = fakeEnemy(Protoss_Photon_Cannon, 92);
        FakeUnit cannon2 = fakeEnemy(Protoss_Photon_Cannon, 93);
        createWorld(1, () -> {
                EnemyUnitsUpdater.weDiscoveredEnemyUnit(cannon1);
                EnemyUnitsUpdater.weDiscoveredEnemyUnit(cannon2);

                FakeUnit enemy = cannon1;

                double ourEval = wraith.combatEvalAbsolute();
                double enemyEval = enemy.combatEvalAbsolute();

                //            System.out.println("ourEval = " + ourEval);
                //            System.out.println("enemyEval = " + enemyEval);

                assertTrue(ourEval < enemyEval);
                assertTrue(ourEval * 8 > enemyEval);
                assertEquals(2, ACombatEvaluator.opposingUnits(wraith).size());
            },
            () -> this.generateOur(),
            () -> fakeEnemies()
        );
    }

    // =========================================================

    protected FakeUnit[] generateOur() {
        return fakeOurs(
            marine = fake(AUnitType.Terran_Marine, 10),
            wraith = fake(AUnitType.Terran_Wraith, 90)
        );
    }

    protected FakeUnit[] generateEnemies() {
        int enemyTy = 16;
        return fakeEnemies(
            fakeEnemy(AUnitType.Zerg_Hydralisk, enemyTy),
            fakeEnemy(AUnitType.Zerg_Hydralisk, enemyTy + 1),
            fakeEnemy(Protoss_Zealot, 11)
        );
    }

}
