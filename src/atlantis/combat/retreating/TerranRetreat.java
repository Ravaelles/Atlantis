package atlantis.combat.retreating;

import atlantis.units.AUnit;

public class TerranRetreat {

    public static boolean shouldNotRetreat(AUnit unit) {
        if (!unit.isTerran()) {
            return false;
        }

        if (unit.isTank() && unit.woundPercentMax(60) && unit.cooldownRemaining() <= 0) {
            unit.setTooltip("BraveTank");
            return true;
        }

        if (unit.kitingUnit() && unit.isHealthy()) {
            unit.setTooltip("BraveKite");
            return true;
        }

        if (unit.isStimmed() && (unit.hp() >= 17 || unit.noCooldown()) && unit.enemiesNearInRadius(1.8) <= 2) {
            unit.setTooltip("BraveStim");
            return true;
        }

//        if (unit.friendsInRadius(4).count() >= 8) {
//            unit.setTooltip("BraveRetard");
//            return true;
//        }

        return false;
    }

    public static boolean shouldRetreat(AUnit unit) {
        if (unit.isTerranInfantry()) {
            if (!unit.mission().isMissionDefend()) {
                if (unit.enemiesNear().ranged().notEmpty() && unit.friendsNear().atMost(4) && unit.combatEvalRelative() <= 2) {
                    unit.setTooltipTactical("BewareRanged");
                    return true;
                }
            }
        }

        return false;
    }
}
