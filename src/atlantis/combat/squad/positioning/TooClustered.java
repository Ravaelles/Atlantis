package atlantis.combat.squad.positioning;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class TooClustered {

    public static boolean handleTooClustered(AUnit unit) {
        if (shouldSkip(unit)) {
            return false;
        }

        Selection ourCombatUnits = Select.ourCombatUnits().inRadius(5, unit);
        AUnit nearestBuddy = ourCombatUnits.clone().nearestTo(unit);
        double minDistBetweenUnits = minDistBetweenUnits(unit);

        if (tooClustered(unit, ourCombatUnits, nearestBuddy, minDistBetweenUnits)) {
            return unit.moveAwayFrom(
                nearestBuddy, 0.3, "SpreadOut", Actions.MOVE_FORMATION
            );
        }

        return false;
    }

    // =========================================================

    private static boolean shouldSkip(AUnit unit) {
//        if (unit.isAir()) {
//            return true;
//        }

//        if (unit.mission() != null && unit.mission().isMissionAttack()) {
//            return true;
//        }

        if (unit.squad().size() <= 1 || unit.isMoving()) {
            return true;
        }

        return false;
    }

    private static double minDistBetweenUnits(AUnit unit) {
        double baseDist = 0.4;
        int enemiesNear = unit.enemiesNearInRadius(4);

        if (enemiesNear <= 1 || unit.noCooldown()) {
            int highTemplars = unit.enemiesNear().ofType(AUnitType.Protoss_High_Templar).havingEnergy(75).count();
            if (highTemplars > 0) {
                return baseDist + 0.7 * highTemplars;
            }
        }

        if (enemiesNear <= 2 || unit.noCooldown()) {
            int lurkers = unit.enemiesNear().ofType(AUnitType.Zerg_Lurker).count();
            if (lurkers > 0) {
                return baseDist + 0.1 * lurkers;
            }
        }

        return baseDist;
    }

    private static boolean tooClustered(
        AUnit unit,
        Selection ourCombatUnits,
        AUnit nearestBuddy,
        double minDistBetweenUnits
    ) {
        return nearestBuddy != null
            && ourCombatUnits.size() >= 5
            && nearestBuddy.distToLessThan(unit, minDistBetweenUnits);
    }
}
