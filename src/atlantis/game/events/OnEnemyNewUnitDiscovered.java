package atlantis.game.events;

import atlantis.combat.missions.MissionChanger;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.enemy.EnemyUnitsUpdater;
import atlantis.information.generic.OurArmy;
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
        if (A.minerals() >= 460) return;
        if (!weAreMuchWeaker()) return;

        if (A.seconds() <= 700) {
            CancelNotStartedBases.cancelNotStartedOrEarlyBases(null);
        }
    }

    private static boolean weAreMuchWeaker() {
        if (OurArmy.strength() >= 0.9) return false;

        if (Enemy.protoss()) {
            if (Count.ourCombatUnits() <= 15 && EnemyUnits.discovered().combatUnits().atLeast(20)) return true;
        }

        int n = (Enemy.protoss() ? 4 : 7) - Count.ourCombatUnits();
        if (EnemyUnits.discovered().combatUnits().atMost(n)) return false;

        return true;
    }
}
