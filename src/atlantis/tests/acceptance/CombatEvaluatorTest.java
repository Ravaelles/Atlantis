package atlantis.tests.acceptance;

import atlantis.combat.ACombatUnitManager;
import atlantis.tests.unit.FakeUnit;
import atlantis.units.AUnitType;
import atlantis.util.A;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class CombatEvaluatorTest extends AbstractTestFakingGame {

    @Test
    public void oneMarineVsTwoHydras() {
        createWorld(0, () -> {
            FakeUnit our = ourFirst;
            FakeUnit enemy = nearestEnemy(our);

            double ourEval = our.combatEval(false);
            double enemyEval = enemy.combatEval(false);

            assertTrue(ourEval < enemyEval);
            assertTrue(ourEval * 5 < enemyEval);

            ourEval = our.combatEval(true);
            enemyEval = enemy.combatEval(true);

//            System.out.println("ourEval = " + ourEval);
//            System.out.println("enemyEval = " + enemyEval);

            assertTrue(Math.abs(ourEval - 0.18) < 0.1);
            assertTrue(Math.abs(enemyEval - 5.29) < 0.1);
        });
    }

    // =========================================================

    protected FakeUnit[] generateOur() {
        return fakeOurs(
                fake(AUnitType.Terran_Marine, 10)
        );
    }

    protected FakeUnit[] generateEnemies() {
        int enemyTy = 16;
        return fakeEnemies(
                fake(AUnitType.Zerg_Hydralisk, enemyTy),
                fake(AUnitType.Zerg_Hydralisk, enemyTy + 1)
        );
    }

}
