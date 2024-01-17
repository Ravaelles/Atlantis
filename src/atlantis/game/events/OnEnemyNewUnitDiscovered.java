package atlantis.game.events;

import atlantis.combat.missions.MissionChanger;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnitsUpdater;
import atlantis.units.AUnit;

public class OnEnemyNewUnitDiscovered {
    public static void update(AUnit unit) {
        // Enemy unit
        if (unit.isEnemy()) {
            handleForEnemy(unit);
        }

        else if (unit.isOur()) {
            // Won't trigger... right?
        }

        else {
            if (!unit.isRealUnit() && !unit.type().isInvincible()) {
//                if (A.isUms()) {
//                    SpecialActionsCommander.NEW_NEUTRAL_THAT_WILL_RENEGADE_TO_US = unit;
//                }
            }
        }
    }

    protected static void handleForEnemy(AUnit unit) {
        EnemyUnitsUpdater.weDiscoveredEnemyUnit(unit);

        if (A.seconds() <= 600) MissionChanger.forceEvaluateGlobalMission();
    }
}
