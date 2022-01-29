package atlantis.tests.acceptance;

import atlantis.combat.eval.ACombatEvaluator;
import atlantis.enemy.EnemyInformation;
import atlantis.tests.unit.FakeUnit;
import atlantis.units.AUnitType;
import org.junit.Test;

import static atlantis.units.AUnitType.Protoss_Photon_Cannon;
import static atlantis.units.AUnitType.Protoss_Zealot;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CombatEvaluatorTest extends AbstractTestFakingGame {

    private FakeUnit marine;
    private FakeUnit wraith;

    @Test
    public void returnsRelativeAndAbsoluteValuesThatMakeSense() {
        createWorld(1, () -> {
            FakeUnit enemy = nearestEnemy(marine);

            double ourEval = marine.combatEvalAbsolute();
            double enemyEval = enemy.combatEvalAbsolute();

//            System.out.println("ourEval = " + ourEval);
//            System.out.println("enemyEval = " + enemyEval);

            assertTrue(ourEval < enemyEval);
            assertTrue(ourEval * 3.6 > enemyEval);

            ourEval = marine.combatEvalRelative();
            enemyEval = enemy.combatEvalRelative();

//            System.out.println("ourEval = " + ourEval);
//            System.out.println("enemyEval = " + enemyEval);

            assertTrue(Math.abs(ourEval - 0.3) < 0.1);
            assertTrue(Math.abs(enemyEval - 3.1) < 0.1);
        });
    }

    @Test
    public void takesIntoAccountFoggedUnits() {
        createWorld(1, () -> {
            EnemyInformation.weDiscoveredEnemyUnit(fake(Protoss_Photon_Cannon, 92));
            EnemyInformation.weDiscoveredEnemyUnit(fake(Protoss_Photon_Cannon, 93));

            FakeUnit enemy = nearestEnemy(wraith);

            double ourEval = wraith.combatEvalAbsolute();
            double enemyEval = enemy.combatEvalAbsolute();

//            System.out.println("ourEval = " + ourEval);
//            System.out.println("enemyEval = " + enemyEval);

            assertTrue(ourEval < enemyEval);
            assertTrue(ourEval * 8 > enemyEval);
            assertEquals(2, ACombatEvaluator.opposingUnits(wraith).size());
        });
    }

    @Test
    public void consistentlyEvaluatesFoggedUnits() {
        FakeUnit cannon1 = fakeEnemy(Protoss_Photon_Cannon, 92);
        FakeUnit cannon2 = fakeEnemy(Protoss_Photon_Cannon, 93);
        createWorld(1, () -> {
                    EnemyInformation.weDiscoveredEnemyUnit(cannon1);
                    EnemyInformation.weDiscoveredEnemyUnit(cannon2);

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
                fakeEnemy(Protoss_Zealot, 91)
        );
    }

}
