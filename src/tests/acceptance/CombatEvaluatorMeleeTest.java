package tests.acceptance;

import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import org.junit.Test;
import tests.unit.FakeUnit;

import static org.junit.Assert.assertTrue;

public class CombatEvaluatorMeleeTest extends AbstractTestFakingGame {

    @Test
    public void evaluatesMeleeUnits() {
        createWorld(1, () -> {
//            Select.our().print("Our units");
//            Select.enemy().print("Enemy units");

            FakeUnit our = ourFirst;
            FakeUnit enemy = nearestEnemy(our);

            double ourEval = our.combatEvalAbsolute();
            double enemyEval = enemy.combatEvalAbsolute();

//            System.out.println("ourEval = " + ourEval);
//            System.out.println("enemyEval = " + enemyEval);

            /**
             * It only works for Atlantis heuristic model (ACombatEvaluator)
             * For JFAP solution this will be negative, but more = better
             *
             * @see AUnit::combatEvalAbsolute
             */

            // ACombatEvaluator
            if (ourEval > 0) {
                assertTrue(ourEval < enemyEval);
                assertTrue(ourEval * 4 > enemyEval);
            }
            // JFAP
            else {
                assertTrue(ourEval < enemyEval);
            }

            ourEval = our.combatEvalRelative();
            enemyEval = enemy.combatEvalRelative();

//            System.out.println("ourEval = " + ourEval);
//            System.out.println("enemyEval = " + enemyEval);

            assertTrue(ourEval > 0);
            assertTrue(enemyEval > 0);
        });
    }

    // =========================================================

    protected FakeUnit[] generateOur() {
        return fakeOurs(
                fake(AUnitType.Terran_Firebat, 10),
                fake(AUnitType.Terran_Firebat, 11),
                fake(AUnitType.Terran_Firebat, 10)
        );
    }

    protected FakeUnit[] generateEnemies() {
        int enemyTy = 13;
        return fakeEnemies(
                fake(AUnitType.Protoss_Zealot, enemyTy),
                fake(AUnitType.Protoss_Zealot, enemyTy + 1)
        );
    }

}
