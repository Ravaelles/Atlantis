package atlantis.combat.retreating;

import atlantis.AGame;
import atlantis.combat.eval.ACombatEvaluator;
import atlantis.combat.missions.MissionChanger;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.Units;

import java.util.Objects;

public class RetreatManager {

    public static int GLOBAL_RETREAT_COUNTER = 0;

//    public static boolean shouldNotRetreat(AUnit unit, Units enemies) {
//        return shouldRetreat(unit, enemies);
//    }

    /**
     * If chances to win the skirmish with the nearby enemy units aren't favorable, avoid fight and retreat.
     */
    public static boolean shouldRetreat(AUnit unit, Units enemies) {
        if (shouldNotConsiderRetreatingNow(unit)) {
            return false;
        }

//        boolean isNewFight = (unit.getUnitAction() != null && !unit.getUnitAction().isRunningOrRetreating());
        boolean isSituationFavorable = ACombatEvaluator.isSituationFavorable(unit);

        // If situation is unfavorable, retreat
        if (!isSituationFavorable) {
            unit._lastRetreat = AGame.now();
            GLOBAL_RETREAT_COUNTER++;
            unit.setTooltip("Retreat");
            MissionChanger.notifyThatUnitRetreated(unit);
            return unit.runningManager().runFrom(enemies.average(), 3.5);
        }

        if (Objects.equals(unit.getTooltip(), "Retreat")) {
            unit.removeTooltip();
        }

        return false;
    }

    // =========================================================

    protected static boolean shouldNotConsiderRetreatingNow(AUnit unit) {
        if (unit.type().isReaver()) {
            return unit.enemiesNearby().isEmpty() && unit.cooldownRemaining() <= 7;
        }

        return false;
    }

}
