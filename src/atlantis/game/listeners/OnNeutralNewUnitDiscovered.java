package atlantis.game.listeners;

import atlantis.combat.missions.MissionChanger;
import atlantis.game.A;
import atlantis.game.neutral.NeutralUnits;
import atlantis.information.enemy.EnemyUnitsUpdater;
import atlantis.information.generic.Army;
import atlantis.production.dynamic.expansion.decision.CancelNotStartedBases;
import atlantis.units.AUnit;

public class OnNeutralNewUnitDiscovered {
    public static void update(AUnit unit) {
        NeutralUnits.weDiscoveredNewUnit(unit);
    }
}
