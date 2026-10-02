package tests.unit.helpers.protoss;

import atlantis.architecture.Manager;
import atlantis.combat.missions.Mission;
import atlantis.combat.missions.MissionChanger;
import atlantis.combat.missions.Missions;
import atlantis.combat.retreating.protoss.ProtossRetreat;
import atlantis.units.AliveEnemies;
import atlantis.units.select.BaseSelect;
import atlantis.units.select.Select;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import bwapi.Race;

public class RetreatScenarioTest extends WorldStubForTests {

    /**
     * ProtossRetreat.applies() is {@code We.protoss()} and nothing else, so the
     * race is not a detail of this helper: with any other race the whole retreat
     * is switched off and every scenario here reports "no retreat".
     */
    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    public double ourCombatEvalRelative = -666;
    public double enemyCombatEvalRelative = -666;
    public boolean retreatManagerApplied;
    private static Mission globalMission = null;
    private static Mission oldGlobalMission = null;

    public double eval() {
        return ourCombatEvalRelative;
    }

    public static RetreatScenarioTest testWith(FakeUnit[] ours, FakeUnit[] enemies, Mission globalMission) {
        RetreatScenarioTest.oldGlobalMission = Missions.globalMission();
        RetreatScenarioTest.globalMission = globalMission;
        RetreatScenarioTest test = new RetreatScenarioTest(ours, enemies);
        RetreatScenarioTest.globalMission = oldGlobalMission;

        return test;
    }

    public RetreatScenarioTest(FakeUnit[] ours, FakeUnit[] enemies) {
        FakeUnit our, enemy;

//        ClearAllCaches.clearAll();
//        Cache.nukeAllCaches();

        our = ours[0];
        enemy = enemies[0];

        our.clearAUnitCache();
        BaseSelect.clearCache();
        Select.clearCache();
        AliveEnemies.clearCache();

//        our.enemiesNear().print("our.enemiesNear()");
//        Select.enemy().print("Select.enemy()");

//        System.err.println(ours.length);
//        System.err.println(enemies.length);
//        System.err.println(ours[0]);
//        System.err.println(enemies[0]);
//        our.clearCache();
//        enemy.clearCache();

//        world(1, ours, enemies, () -> {
        world(1, ours, enemies, () -> {
            if (globalMission != null) {
                MissionChanger.setGlobalMissionTo(globalMission, "Forced in tests");
            }

//                Select.our().print("Our");
//                Select.enemy().print("Enemy");

            // Bunch of integrity tests that were failing before
            assertEquals(BaseSelect.ourUnitsWithUnfinishedList().size(), Select.our().size());
            assertEquals(BaseSelect.enemyUnits().size(), Select.enemy().size());
            assertEquals(1 + enemy.friendsNear().count(), Select.enemy().count());
            assertEquals(enemy.enemiesNear().count(), Select.our().count());
            assertEquals(1 + our.friendsNear().count(), Select.our().count());

//                System.out.println("===================================================");
//                Select.our().print("Our");
//                our.enemiesNear().print("enemiesNear()");
//                Select.enemy().print("Select.enemy()");
//                EnemyUnits.discovered().print("EnemyUnits.discovered()");
//                EnemyUnits.freshDiscovered().print("EnemyUnits.freshDiscovered()");
//                AliveEnemies.get().print("AliveEnemies.get()");

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

            if (oldGlobalMission != null) {
                MissionChanger.setGlobalMissionTo(oldGlobalMission, "Force restore in tests");
            }
        });
    }
}
