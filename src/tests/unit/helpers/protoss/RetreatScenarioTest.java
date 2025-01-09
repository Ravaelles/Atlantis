package tests.unit.helpers.protoss;

import atlantis.architecture.Manager;
import atlantis.combat.retreating.protoss.ProtossRetreat;
import atlantis.units.select.BaseSelect;
import atlantis.units.select.Select;
import atlantis.util.cache.Cache;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

//public class RetreatScenarioTest extends AbstractTestWithUnits {
public class RetreatScenarioTest extends WorldStubForTests {
    public double ourCombatEvalRelative = -666;
    public double enemyCombatEvalRelative = -666;
    public boolean retreatManagerApplied;

    public double eval() {
        return ourCombatEvalRelative;
    }

    public RetreatScenarioTest(FakeUnit[] ours, FakeUnit[] enemies) {
        FakeUnit our, enemy;

        our = ours[0];
        enemy = enemies[0];

//        System.err.println(ours.length);
//        System.err.println(enemies.length);
//        System.err.println(ours[0]);
//        System.err.println(enemies[0]);
//        our.clearCache();
//        enemy.clearCache();

//        usingFakeOursAndFakeEnemies(ours, enemies, () -> {
        createWorld(1,
            () -> {
//                Select.our().print("Our");
//                Select.enemy().print("Enemy");

                Cache.nukeAllCaches();
                BaseSelect.clearCache();
                Select.clearCache();

                // Bunch of integrity tests that were failing before
                assertEquals(BaseSelect.ourUnitsWithUnfinishedList().size(), Select.our().size());
                assertEquals(BaseSelect.enemyUnits().size(), Select.enemy().size());
                assertEquals(1 + enemy.friendsNear().count(), Select.enemy().count());
                assertEquals(enemy.enemiesNear().count(), Select.our().count());
                assertEquals(1 + our.friendsNear().count(), Select.our().count());

//                System.out.println("our.enemiesNear().count() = " + our.enemiesNear().count());
//                our.enemiesNear().print();
//                System.out.println("Select.enemy().count() = " + Select.enemy().count());
//                Select.enemy().print();
                
                assertEquals(0, our.enemiesNear().count() - Select.enemy().count());

                double ourCombatEvalRelative = our.eval();
                double enemyCombatEvalRelative = enemy.eval();

//                System.out.println("Our " + our.typeWithUnitId() + " eval  : "
//                    + A.digit(ourCombatEvalRelative) + " / " + our.combatEvalAbsolute());
//                System.out.println("Enemy " + enemy.typeWithUnitId() + " eval: "
//                    + A.digit(enemyCombatEvalRelative) + " / " + enemy.combatEvalAbsolute());

                this.ourCombatEvalRelative = ourCombatEvalRelative;
                this.enemyCombatEvalRelative = enemyCombatEvalRelative;

                Manager manager = (new ProtossRetreat(our)).invokeFrom(null);
//            boolean applies = (new ProtossRetreat(our)).applies();

                this.retreatManagerApplied = manager != null;

//            assertEquals(true, (new ProtossShouldRetreat(our).forceHandle());
            }, () -> ours, () -> enemies);
    }
}
