package atlantis.combat.micro.managers;

import atlantis.combat.micro.AAttackEnemyUnit;
import atlantis.combat.missions.focus.AFocusPoint;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.We;

public class AdvanceUnitsManager {

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
        if (focusPoint == null) {
            unit.addLog("NoFocusPoint");
            return false;
        }

        if (unit.friendsNearCount() <= 10 && unit.enemiesNear().combatUnits().notEmpty()) {
            if (
                unit.isMoving()
                    && !unit.isUnitAction(Actions.MOVE_FORMATION)
                    && !unit.isRunning()
//                    && unit.lastActionMoreThanAgo(15)
                    && unit.distToSquadCenter() >= 5
            ) {
                unit.addLog("TooFast");
                return unit.move(unit.squadCenter(), Actions.MOVE_FORMATION, "TooFast", false);
            }
            return false;
        }

        // =========================================================

//        if (
//            unit.isMoving()
//                && !unit.isUnitAction(Actions.MOVE_FORMATION)
//                && !unit.isRunning()
//                && unit.lastActionMoreThanAgo(10, Actions.MOVE_ENGAGE)
//        ) {
////        if (!unit.isStopped() && unit.lastActionMoreThanAgo(7, Actions.MOVE_ENGAGE)) {
//            unit.stop("TooFast", false);
//            return true;
//        }

        double optimalDist = optimalDistFromFocusPoint(unit, focusPoint);
        double distToFocusPoint = unit.distTo(focusPoint);
        double margin = Math.max(2.5, unit.squadSize() / 7.0);
        boolean result;

        if (We.terran() && handleTerranAdvance(unit)) {
            return true;
        }

        // =========================================================

//        if (AAttackEnemyUnit.handleAttackNearEnemyUnits(unit)) {
//            unit.setTooltip("Adv:Attack", true);
//            return true;
//        }

        // =========================================================

        // Too close
        if (
                allowTooClose
                && distToFocusPoint <= optimalDist - margin
                && (result = unit.moveAwayFrom(
                        focusPoint,
                        2.5,
                        "#Adv:TooClose(" + (int) distToFocusPoint + ")",
                        Actions.MOVE_FORMATION
                ))
        ) {
            return result;
        }

        // Close enough
        else if (allowCloseEnough && distToFocusPoint <= optimalDist + margin) {
            if (unit.isMoving() && unit.lastActionMoreThanAgo(5)) {
                unit.stop("#Adv:Good(" + (int) distToFocusPoint + ")", true);
            }
            else {
                unit.setTooltip("Adv:Ok", true);
            }
            return true;
        }

        // Too far
        else if (distToFocusPoint > optimalDist + margin) {
            if (unit.isMoving() && unit.lastActionLessThanAgo(20, Actions.MOVE_ENGAGE)) {
                return true;
            }

            return unit.move(focusPoint, Actions.MOVE_ENGAGE, "#Adv:Back(" + (int) distToFocusPoint + ")", true);
        }

//        System.out.println("Target = " + ATargeting.defineBestEnemyToAttackFor(unit, 40) + " // " +
//                unit.enemiesNear().inRadius(10, unit).count());

        // =========================================================

        if (distToFocusPoint > 6) {
//            if (unit.isMoving() && unit.lastActionLessThanAgo(20, Actions.MOVE_ENGAGE)) {
//                return true;
//            }

            if (!unit.isMoving()) {
                String canAttack = AAttackEnemyUnit.canAttackEnemiesNowString(unit);
                unit.move(focusPoint, Actions.MOVE_ENGAGE, "Advance" + canAttack, true);
            }
            return true;
        }

        if (!unit.hasTooltip()) {
            unit.setTooltip("#Adv", true);
        }
        return false;
    }

    private static double optimalDistFromFocusPoint(AUnit unit, APosition focusPoint) {
        return 8
                - unit.hpPercent() / 66.0
                + (unit.isTank() ? 2 : 0)
                + (unit.isMedic() ? -1.2 : 0);
    }

    // === Terran ======================================================

    private static boolean handleTerranAdvance(AUnit unit) {
        if (unit.isTerranInfantry() && unit.isWounded() && !unit.isMedic() && Count.medics() >= 1) {
            AUnit medic = Select.ourOfType(AUnitType.Terran_Medic).havingEnergy(20).nearestTo(unit);
//            if (medic != null && medic.distToMoreThan(unit, maxDistToMedic(unit))) {
            if (medic != null) {
//                if (Select.ourCombatUnits().inRadius(5, unit).atMost(5)) {
                return unit.move(medic, Actions.MOVE_FOCUS, "ToMedic", false);
//                }
            }
        }

        return false;
    }

    private static double maxDistToMedic(AUnit unit) {
        if (unit.isMarine()) {
            return 8;
        }
        else if (unit.isFirebat()) {
            return 1.9;
        }

        return 8;
    }

    // =========================================================

}