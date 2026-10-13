package atlantis.game.listeners;

import atlantis.combat.missions.MissionChanger;
import atlantis.game.A;
import atlantis.game.listeners.protoss.AsProtossWhenDarkTemplarDetected;
import atlantis.information.enemy.EnemyUnitsUpdater;
import atlantis.information.generic.Army;
import atlantis.production.dynamic.expansion.decision.CancelNotStartedBases;
import atlantis.units.AUnit;
import atlantis.util.We;

public class OnEnemyNewUnitDiscovered {
    public static void update(AUnit unit) {
        EnemyUnitsUpdater.weDiscoveredEnemyUnit(unit);

        if (A.seconds() <= 600) MissionChanger.forceEvaluateGlobalMission();

        actIfWeAreMuchWeaker();

        if (unit.isDarkTemplar()) {
            if (We.protoss()) AsProtossWhenDarkTemplarDetected.update(unit);
        }
    }

    private static void actIfWeAreMuchWeaker() {
        if (A.minerals() >= 460) return;

        if (A.seconds() <= 700 && Army.strength() <= 80 && !A.hasMinerals(250)) {
            CancelNotStartedBases.cancelNotStartedOrEarlyBases(null, "Cancel base - much weaker");
        }
    }

}
