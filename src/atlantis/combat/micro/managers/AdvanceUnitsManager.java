//package atlantis.combat.micro.managers;
//
//import atlantis.combat.micro.attack.AttackNearbyEnemies;
//import atlantis.combat.advance.focus.AFocusPoint;
//import atlantis.map.position.APosition;
//import atlantis.units.AUnit;
//import atlantis.units.AUnitType;
//import atlantis.units.actions.Actions;
//import atlantis.units.select.Count;
//import atlantis.units.select.Select;
//import atlantis.util.We;
//
//public class AdvanceUnitsManager {
//
////    private Mission mission;
////
////    public boolean updateUnit() {
////        unit.setTooltip("#Adv");
////
////        if (unit.distanceTo(mission.focusPoint()) > 6) {
////            unit.move(mission.focusPoint(), UnitActions.MOVE_TO_ENGAGE, "#MA:Forward!");
////            return true;
////        }
////
////        return false;
////    }
//
//    public  boolean moveToFocusPoint(AFocusPoint focusPoint) {
//        return moveToFocusPoint(unit, focusPoint, false, false);
//    }
//
////    public  boolean moveToFocusPoint(AFocusPoint focusPoint) {
////        return moveToFocusPoint(unit, focusPoint, true, true);
////    }
//
//    // =========================================================
//
//    private  boolean moveToFocusPoint(
//            , AFocusPoint focusPoint, boolean allowTooClose, boolean allowCloseEnough
//    ) {
//        // @Check
////        if (unit.isAir()) {
////            return false;
////        }
//
//        if (focusPoint == null) {
//            unit.addLog("NoFocusPoint");
//            return false;
//        }
//
//        if (unit.friendsInRadiusCount(6) <= 10 && unit.enemiesNear().combatUnits().notEmpty()) {
//            if (
//                unit.isMoving()
//                    && !unit.isAction(Actions.MOVE_FORMATION)
//                    && !unit.isRunning()
////                    && unit.lastActionMoreThanAgo(15)
//                    && unit.distToSquadCenter() >= 5
//            ) {
//                String t = "TooFast";
//                unit.addLog(t);
//                if (unit.move(unit.squadCenter(), Actions.MOVE_FORMATION, t, false)) {
//                    return true;
//                }
//            }
//        }
//
//        // =========================================================
//
////        if (
////            unit.isMoving()
////                && !unit.isUnitAction(Actions.MOVE_FORMATION)
////                && !unit.isRunning()
////                && unit.lastActionMoreThanAgo(10, Actions.MOVE_ENGAGE)
////        ) {
//////        if (!unit.isStopped() && unit.lastActionMoreThanAgo(7, Actions.MOVE_ENGAGE)) {
////            unit.stop("TooFast", false);
////            return true;
////        }
//
//        double optimalDist = optimalDistFromFocusPoint(unit, focusPoint);
//        double distToFocusPoint = unit.distTo(focusPoint);
//        double margin = Math.max(2.5, unit.squadSize() / 7.0);
//
//        if (We.terran() && handledTerranAdvance()) {
//            return true;
//        }
//
//        // =========================================================
//
////        if (AttackNearbyEnemies.handleAttackNearEnemyUnits()) {
////            unit.setTooltip("Adv:Attack", true);
////            return true;
////        }
//
//        // =========================================================
//
//        // Too close
//        if (
//                allowTooClose
//                && distToFocusPoint <= optimalDist - margin
//                && unit.moveAwayFrom(
//                    focusPoint,
//                    2.5,
//                    "#Adv:TooClose(" + (int) distToFocusPoint + ")",
//                    Actions.MOVE_FORMATION
//                )
//        ) {
//            return true;
//        }
//
//        // Close enough
//        else if (allowCloseEnough && distToFocusPoint <= optimalDist + margin) {
//            if (unit.isMoving() && unit.lastActionMoreThanAgo(5)) {
//                if (unit.stop("#Adv:Good(" + (int) distToFocusPoint + ")", true)) {
//                    return true;
//                }
//            }
//            else {
//                unit.setTooltip("Adv:Ok", true);
//                return true;
//            }
//        }
//
//        // Too far
//        else if (distToFocusPoint > optimalDist + margin) {
//            if (unit.isMoving() && unit.lastActionLessThanAgo(40, Actions.MOVE_ENGAGE)) {
//                unit.setTooltip("Engaging");
//                return true;
//            }
//
//            if (unit.isMissionAttack() && unit.friendsInRadius(2).count() >= 3) {
//                return false;
//            }
//
//            if (unit.move(
//                focusPoint, Actions.MOVE_ENGAGE, "#Adv:Back(" + (int) distToFocusPoint + ")", true)
//            ) {
//                return true;
//            }
//        }
//
////        System.out.println("Target = " + ATargeting.defineBestEnemyToAttackFor(unit, 40) + " // " +
////                unit.enemiesNear().inRadius(10, unit).count());
//
//        // =========================================================
//
//        if (distToFocusPoint > 6) {
////            if (unit.isMoving() && unit.lastActionLessThanAgo(20, Actions.MOVE_ENGAGE)) {
////                return true;
////            }
//
//            if (!unit.isMoving()) {
//                String canAttack = AttackNearbyEnemies.canAttackEnemiesNowString();
//                if (unit.move(focusPoint, Actions.MOVE_ENGAGE, "Advance" + canAttack, true)) {
//                    return true;
//                }
//            }
//        }
//
//        if (!unit.hasTooltip()) {
//            unit.setTooltip("#Adv", true);
//        }
//        return false;
//    }
//
//    private  double optimalDistFromFocusPoint(APosition focusPoint) {
//        return 8
//                - unit.hpPercent() / 66.0
//                + (unit.isTank() ? 2 : 0)
//                + (unit.isMedic() ? -1.2 : 0);
//    }
//
//    // === Terran ======================================================
//
//    private  boolean handledTerranAdvance() {
//        if (unit.isTerranInfantry() && unit.isWounded() && !unit.isMedic() && Count.medics() >= 1) {
//            AUnit medic = Select.ourOfType(AUnitType.Terran_Medic)
//                .havingEnergy(20)
//                .inRadius(15, unit)
//                .nearestTo(unit);
//
//            if (medic != null && (!medic.hasTarget() || medic.target().equals())) {
//                return unit.move(medic, Actions.MOVE_FOCUS, "Regenerate", false);
//            }
//        }
//
//        return false;
//    }
//}
