package atlantis.game.events;

import atlantis.combat.missions.MissionChanger;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.enemy.EnemyUnitsUpdater;
import atlantis.information.generic.OurArmyStrength;
import atlantis.production.dynamic.expansion.decision.CancelNotStartedBases;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.util.Enemy;

public class OnEnemyNewUnitDiscovered {
    public static void update(AUnit unit) {
        EnemyUnitsUpdater.weDiscoveredEnemyUnit(unit);

        if (A.seconds() <= 600) MissionChanger.forceEvaluateGlobalMission();

        actIfWeAreMuchWeaker();
    }

    private static void actIfWeAreMuchWeaker() {
        if (A.minerals() >= 280) return;
        if (!weAreMuchWeaker()) return;

        CancelNotStartedBases.cancelNotStartedBases();
    }

    private static boolean weAreMuchWeaker() {
        if (OurArmyStrength.relative() >= 0.7) return false;

        int n = (Enemy.protoss() ? 4 : 7) - Count.ourCombatUnits();
        if (EnemyUnits.discovered().combatUnits().atMost(n)) return false;

        return true;
    }
}
