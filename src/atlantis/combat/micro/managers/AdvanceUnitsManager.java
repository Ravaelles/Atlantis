package atlantis.combat.micro.managers;

import atlantis.combat.micro.AAttackEnemyUnit;
import atlantis.combat.missions.AFocusPoint;
import atlantis.combat.missions.MissionUnitManager;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.UnitAction;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.actions.UnitActions;
import atlantis.util.We;

public class AdvanceUnitsManager extends MissionUnitManager {

//    private Mission mission;
//
//    public boolean updateUnit(AUnit unit) {
//        unit.setTooltip("#Adv");
//
//        if (unit.distanceTo(mission.focusPoint()) > 6) {
//            unit.move(mission.focusPoint(), UnitActions.MOVE_TO_ENGAGE, "#MA:Forward!");
//            return true;
//        }
//
//        return false;
//    }

    public static boolean attackMoveToFocusPoint(AUnit unit, AFocusPoint focusPoint) {
        return moveToFocusPoint(unit, focusPoint, false, false);
    }

//    public static boolean moveToFocusPoint(AUnit unit, AFocusPoint focusPoint) {
//        return moveToFocusPoint(unit, focusPoint, true, true);
//    }

    // =========================================================

    private static boolean moveToFocusPoint(
            AUnit unit, AFocusPoint focusPoint, boolean allowTooClose, boolean allowCloseEnough
    ) {
        double optimalDist = optimalDistFromFocusPoint(unit, focusPoint);
        double distToFocusPoint = unit.distTo(focusPoint);
        double margin = Math.max(2.5, unit.squadSize() / 7.0);
        boolean result;

        if (We.terran() && handleTerranAdvance(unit)) {
            return true;
        }

        // =========================================================

        if (AAttackEnemyUnit.handleAttackNearbyEnemyUnits(unit)) {
            return true;
        }

        // =========================================================

        // Too close
        if (
                allowTooClose
                && distToFocusPoint <= optimalDist - margin
                && (result = unit.moveAwayFrom(focusPoint, 2.5, "#Adv:TooClose(" + (int) distToFocusPoint + ")"))
        ) {
            return result;
        }

        // Close enough
        else if (allowCloseEnough && distToFocusPoint <= optimalDist + margin) {
            if (unit.isMoving()) {
                unit.stop("#Adv:Good(" + (int) distToFocusPoint + ")");
            }
            return true;
        }

        // Too far
        else if (distToFocusPoint > optimalDist + margin) {
            return unit.move(focusPoint, UnitActions.MOVE_TO_ENGAGE, "#Adv(" + (int) distToFocusPoint + ")");
        }

        return false;
    }

    private static double optimalDistFromFocusPoint(AUnit unit, APosition focusPoint) {
        return 8
                - unit.hpPercent() / 66.0
                + (unit.isTank() ? 2 : 0)
                + (unit.isMedic() ? -1.2 : 0);
    }

    // =========================================================

    private static boolean handleTerranAdvance(AUnit unit) {
        if (unit.isInfantry() && !unit.isMedic() && Count.medics() >= 4) {
//            if (Select.enemyCombatUnits().inRadius(7, unit).isEmpty()) {
//                return false;
//            }

            AUnit medic = Select.ourOfType(AUnitType.Terran_Medic).havingEnergy(30).nearestTo(unit);
            if (medic != null && medic.distToMoreThan(unit, maxDistToMedic(unit))) {
                if (Select.ourCombatUnits().inRadius(5, unit).atMost(5)) {
                    return unit.move(medic, UnitActions.MOVE, "ToMedic");
                }
            }
        }

        return false;

//        if (Select.our().tanks().isEmpty()) {
//            return false;
//        }
//
//        if (unit.isTank()) {
//            return false;
//        }
//
////        double maxRadiusFromTank = 4 + Math.sqrt(Count.ourCombatUnits());
//        AUnit nearestTank = Select.our().tanks().nearestTo(unit);
//
//        if (nearestTank >= 1) {
//            return false;
//        }
//
//        unit.move(
//                unit.translatePercentTowards(Select.our().tanks().nearestTo(unit), 30),
//                UnitActions.MOVE_TO_FOCUS,
//                "ToTank"
//        );
//        return true;
    }

    private static double maxDistToMedic(AUnit unit) {
        if (unit.isMarine()) {
            return 8;
        }
        else if (unit.isFirebat()) {
            return 2.6;
        }

        return 8;
    }

}