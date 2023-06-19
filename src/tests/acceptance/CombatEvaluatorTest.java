package tests.acceptance;

import atlantis.information.enemy.EnemyUnitsUpdater;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import org.junit.Test;
import tests.unit.FakeUnit;

import static atlantis.units.AUnitType.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CombatEvaluatorTest extends AbstractTestFakingGame {

    private FakeUnit marine;
    private FakeUnit wraith;
    private FakeUnit sunken;

    @Test
    public void returnsRelativeValuesThatMakeSense() {
        createWorld(1, () -> {
            FakeUnit enemy = nearestEnemy(marine);

//            System.out.println("Test print:");
//            Select.our().print();
//            Select.enemy().print();

            double ourEval;
            double enemyEval;

            ourEval = marine.combatEvalAbsolute();
            enemyEval = enemy.combatEvalAbsolute();

//            System.out.println("ourEval = " + ourEval);
//            System.out.println("enemyEval = " + enemyEval);

            assertTrue(ourEval < 0);
            assertTrue(enemyEval < 0);
            assertTrue(valueAround(-88, ourEval));
            assertTrue(valueAround(-1, enemyEval));

            ourEval = marine.combatEvalRelative();
            enemyEval = enemy.combatEvalRelative();

//            System.out.println("-- ourEval = " + ourEval);
//            System.out.println("-- enemyEval = " + enemyEval);

            assertTrue(valueAround(0.011, ourEval));
            assertTrue(valueAround(88, enemyEval));
        });
    }

    @Test
    public void marineVsSunken() {
        createWorld(1, () -> {
                FakeUnit enemy = nearestEnemy(marine);

                double ourEval;
                double enemyEval;

                ourEval = marine.combatEvalRelative();
                enemyEval = enemy.combatEvalRelative();

//                System.out.println("-- ourEval = " + ourEval);
//                System.out.println("-- enemyEval = " + enemyEval);

                assertTrue(valueAround(0.65, ourEval));
                assertTrue(valueAround(1.5, enemyEval));
            },
            () -> fakeOurs(
                fake(AUnitType.Terran_Marine, 10),
                fake(AUnitType.Terran_Marine, 11),
                marine = fake(AUnitType.Terran_Marine, 11.5),
                fake(AUnitType.Terran_Marine, 12)
            ),
            () -> fakeEnemies(
                sunken = fake(AUnitType.Zerg_Sunken_Colony, 13)
            )
        );
    }

    @Test
    public void combatEvalAgainstNoEnemy() {
        createWorld(1, () -> {
                double ourEvalRelative = marine.combatEvalRelative();
                double ourEvalAbsolute = marine.combatEvalAbsolute();
//                double enemyEval = enemy.combatEvalRelative();

//                System.out.println("ourEvalRelative = " + ourEvalRelative);
//                System.out.println("ourEvalAbsolute = " + ourEvalAbsolute);

                assertTrue(marine.combatEvalRelative() > 0);
                assertTrue(marine.combatEvalAbsolute() < 0);
            },
            () -> fakeOurs(
                marine = fake(AUnitType.Terran_Marine, 10)
            ),
            () -> fakeEnemies()
        );
    }

    @Test
    public void combatEvalAgainstWeakEnemy() {
        FakeUnit enemy = fake(Zerg_Zergling, 13);
        FakeUnit[] enemyUnits = fakeEnemies(
            enemy
        );

        createWorld(1, () -> {
                double ourEvalRelative = marine.combatEvalRelative();
                double ourEvalAbsolute = marine.combatEvalAbsolute();
                double enemyEvalRelative = enemy.combatEvalRelative();
                double enemyEvalAbsolute = enemy.combatEvalAbsolute();

//                System.err.println("ourEvalRelative = " + ourEvalRelative);
//                System.err.println("ourEvalAbsolute = " + ourEvalAbsolute);
//
//                System.err.println("enemyEvalRelative = " + enemyEvalRelative);
//                System.err.println("enemyEvalAbsolute = " + enemyEvalAbsolute);

                assertTrue(marine.combatEvalRelative() > 0);
                assertTrue(marine.combatEvalAbsolute() < 0);
                assertTrue(enemy.combatEvalRelative() > 0);
                assertTrue(enemy.combatEvalAbsolute() < 0);

                assertTrue(valueAround(marine.combatEvalRelative(), 1.66));
                assertTrue(valueAround(enemy.combatEvalRelative(), 0.6));
            },
            () -> fakeOurs(
                fake(AUnitType.Terran_Marine, 10),
                fake(AUnitType.Terran_Marine, 11),
                marine = fake(AUnitType.Terran_Marine, 11.5),
                fake(AUnitType.Terran_Marine, 12)
            ),
            () -> enemyUnits
        );
    }

    @Test
    public void testOneMarinevsOneMargine() {
        final FakeUnit enemyMarine = fakeEnemy(Terran_Marine, 7);

        createWorld(1, () -> {
                double ourEval = marine.combatEvalAbsolute();
                double enemyEval = enemyMarine.combatEvalAbsolute();

//                System.out.println("ourEval = " + ourEval);
//                System.out.println("enemyEval = " + enemyEval);

                assertTrue(ourEval < 0);
                assertTrue(ourEval == enemyEval);
            },
            () -> this.generateOur(),
            () -> fakeEnemies(enemyMarine)
        );
    }

    @Test
    public void takesIntoAccountFoggedUnits() {
        createWorld(1, () -> {
            FakeUnit cannon;
            EnemyUnitsUpdater.weDiscoveredEnemyUnit(cannon = fakeEnemy(Protoss_Photon_Cannon, 92));
            EnemyUnitsUpdater.weDiscoveredEnemyUnit(fakeEnemy(Protoss_Gateway, 93));

//            System.out.println("wraith = " + wraith);
//            System.out.println("cannon = " + cannon);
//
//            wraith.enemiesNear().print("Enemies of " + wraith);
//            cannon.enemiesNear().print("Enemies of " + cannon);

            assertEquals(2, wraith.enemiesNear().size());
            assertEquals(1, cannon.enemiesNear().size());

//            assertEquals(1, OldUnusedCombatEvaluator.opposingUnits(wraith).size());
//            assertEquals(1, OldUnusedCombatEvaluator.opposingUnits(cannon).size());

            double ourEval = wraith.combatEvalRelative();
            double enemyEval = cannon.combatEvalRelative();

//            System.out.println("ourEval = " + ourEval);
//            System.out.println("enemyEval = " + enemyEval);

            assertTrue(ourEval > 0);
            assertTrue(enemyEval > 0);
            assertTrue(ourEval < enemyEval);
            assertTrue(ourEval * 800 > enemyEval);

//            double ourEval = wraith.combatEvalAbsolute();
//            double enemyEval = cannon.combatEvalAbsolute();
//
//            System.out.println("ourEval = " + ourEval);
//            System.out.println("enemyEval = " + enemyEval);
//
//            assertTrue(ourEval > 0);
//            assertTrue(enemyEval > 0);
//            assertTrue(ourEval < enemyEval);
//            assertTrue(ourEval * 8 > enemyEval);
        });
    }

    @Test
    public void doesNotTakeIntoAccountLockedDownUnitsOrStasised() {
        createWorld(1, () -> {
            FakeUnit dragoon1 = (FakeUnit) Select.enemies(Protoss_Dragoon).first();
            FakeUnit dragoon2 = (FakeUnit) Select.enemies(Protoss_Dragoon).second();

//            System.out.println("Dragoon isLockedDown = " + dragoon1.isLockedDown());
//            System.out.println("Dragoon isStasised = " + dragoon2.isStasised());
//
//            Select.our().print("Our");
//            System.out.println("wraith = " + wraith);
//            wraith.enemiesNear().print("Enemies near");

            assertEquals(2, wraith.enemiesNear().size());
            assertEquals(1, dragoon1.enemiesNear().size());

            double ourEval, enemyEval;

            ourEval = wraith.combatEvalAbsolute();
            enemyEval = dragoon1.combatEvalAbsolute();

//            System.out.println("ourEval = " + ourEval);
//            System.out.println("enemyEval = " + enemyEval);

            assertTrue(ourEval == -1);
            assertTrue(enemyEval < -300);

            ourEval = wraith.combatEvalRelative();
            enemyEval = dragoon1.combatEvalRelative();

//            System.out.println("ourEval = " + ourEval);
//            System.out.println("enemyEval = " + enemyEval);

            assertTrue(ourEval >= 9800);
            assertTrue(enemyEval < 0.4);
        }, () -> this.generateOur(), () -> this.generateEnemiesWithStasisesAndLockedDown());
    }

    @Test
    public void consistentlyEvaluatesFoggedUnits() {
        FakeUnit cannon1 = fakeEnemy(Protoss_Photon_Cannon, 92);
        FakeUnit cannon2 = fakeEnemy(Protoss_Photon_Cannon, 93);
        createWorld(1, () -> {
                EnemyUnitsUpdater.weDiscoveredEnemyUnit(cannon1);
                EnemyUnitsUpdater.weDiscoveredEnemyUnit(cannon2);

                FakeUnit enemy = cannon1;

//                Select.our().print("Our");
//                wraith.enemiesNear().print("Enemies near " + wraith);
//                System.out.println("cannon1 = " + cannon1);
//                cannon1.enemiesNear().print("Cannon enemies nearby");

                double ourEval = wraith.combatEvalRelative();
                double enemyEval = enemy.combatEvalRelative();

//                System.out.println("ourEval = " + ourEval);
//                System.out.println("enemyEval = " + enemyEval);
//
                assertEquals(2, wraith.enemiesNear().size());
                assertTrue(ourEval < enemyEval);
                assertTrue(ourEval * 1300 > enemyEval);
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

    private FakeUnit[] generateEnemiesWithStasisesAndLockedDown() {
        int enemyTy = 16;
        return fakeEnemies(
            fakeEnemy(AUnitType.Zerg_Hydralisk, enemyTy),
            fakeEnemy(AUnitType.Zerg_Hydralisk, enemyTy + 1),
            fakeEnemy(Protoss_Zealot, 11),
//            fakeEnemy(Protoss_Dragoon, 92),
//            fakeEnemy(Protoss_Dragoon, 93)
            fakeEnemy(Protoss_Dragoon, 92).setLockedDown(true),
            fakeEnemy(Protoss_Dragoon, 93).setStasised(true)
        );
    }

}
