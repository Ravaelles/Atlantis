package atlantis.tests.acceptance;

import atlantis.combat.ACombatUnitManager;
import atlantis.tests.unit.FakeUnit;
import atlantis.units.AUnitType;
import atlantis.util.A;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class CombatEvaluatorTest extends AbstractTestFakingGame {

    @Test
    public void combatEvaluatorReturnsRelativeAndAbsoluteValuesThatMakeSense() {
        createWorld(0, () -> {
            FakeUnit our = ourFirst;
            FakeUnit enemy = nearestEnemy(our);

            double ourEval = our.combatEvalAbsolute();
            double enemyEval = enemy.combatEvalAbsolute();

            assertTrue(ourEval < enemyEval);
            assertTrue(ourEval * 5 < enemyEval);

            ourEval = our.combatEvalRelative();
            enemyEval = enemy.combatEvalRelative();

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
