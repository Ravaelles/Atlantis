package atlantis.tests.acceptance;

import atlantis.tests.unit.FakeUnit;
import atlantis.units.AUnitType;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class CombatEvaluatorMeleeTest extends AbstractTestFakingGame {

    @Test
    public void evaluatesMeleeUnits() {
        createWorld(1, () -> {
            FakeUnit our = ourFirst;
            FakeUnit enemy = nearestEnemy(our);

            double ourEval = our.combatEvalAbsolute();
            double enemyEval = enemy.combatEvalAbsolute();

//            System.out.println("ourEval = " + ourEval);
//            System.out.println("enemyEval = " + enemyEval);

            assertTrue(ourEval < enemyEval);
            assertTrue(ourEval * 4 > enemyEval);

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
        int enemyTy = 16;
        return fakeEnemies(
                fake(AUnitType.Protoss_Zealot, enemyTy),
                fake(AUnitType.Protoss_Zealot, enemyTy + 1)
        );
    }

}
