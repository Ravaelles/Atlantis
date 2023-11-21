package atlantis.combat.micro.generic;

import atlantis.architecture.Manager;
import atlantis.debug.painter.APainter;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.UnitStateManager;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import bwapi.Color;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class Unfreezer extends Manager {
    public Unfreezer(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        System.err.println(
//            unit.lastActionAgo(Actions.MOVE_UNFREEZE)
//                + " / " +
//                unit.action()
//                + " / " +
//                unit.lastActionFramesAgo()
//        );
        return unit.isCombatUnit()
            && !unit.isLoaded()
            && !unit.isMoving()
            && A.now() >= 10
            && unit.looksIdle()
//            && (unit.position().equals(unit.lastPosition()))
            && unit.lastStartedRunningMoreThanAgo(50)
            && unit.lastActionMoreThanAgo(150, Actions.MOVE_UNFREEZE)
            && unit.hasNotMovedInAWhile()
            && unit.nearestEnemyDist() >= 4
            && unit.lastActionMoreThanAgo(30);
//            (unit.looksIdle()
//                || (unit.lastActionMoreThanAgo(30) && unit.hasNotMovedInAWhile())
//            );
    }

    @Override
    public Manager handle() {
//        System.err.println(A.now() + " Unfreezing " + unit + " / " + unit.action());

        if (unit.distToFocusPoint() >= 3) {
            unit.moveTactical(unit.micro().focusPoint(), Actions.MOVE_UNFREEZE, "Unfreeze");
        }
        else {
            AUnit goTo = Select.ourBuildings().random();
            if (goTo == null) goTo = unit.friendsNear().mostDistantTo(unit);
            if (goTo == null) return null;

            unit.moveTactical(goTo, Actions.MOVE_UNFREEZE, "Unfreezing");
        }

        return usedManager(this);
    }
}


//public class Unfreezer extends Manager {
//    /**
//     * Some units can get FROZEN (stuck, unable to move/shoot). It's a known Starcraft bug.
//     * This is an ugly way of fixing this.
//     */
//    public boolean handleUnfreeze(AUnit unit) {
////        if (true) return false;
//
//        if (unit.isRunning() && unit.lastActionFramesAgo() >= (UnitStateManager.UPDATE_UNIT_POSITION_EVERY_FRAMES + 20)) {
//            if (unit._lastX == unit.x() && unit._lastY == unit.y()) {
////                System.err.println("UNFREEZE #1!");
////                unit.setTooltip("UNFREEZE!");
//                return unfreeze(unit);
//            }
//        }
//
////        if (
////                unit.lastUnderAttackLessThanAgo(5)
////                        && unit.getLastOrderFramesAgo() >= AUnit.UPDATE_UNIT_POSITION_EVERY_FRAMES
////                        && unit.cooldownRemaining() == 0
////        ) {
////            if (unit._lastX == unit.x() && unit._lastY == unit.y()) {
////                System.err.println("UNFREEZE #2!");
////                unit.setTooltip("UNFREEZE!");
////                return unfreeze();
////            }
////        }
//
//        return false;
//    }
//
//    // =========================================================
//
//    public boolean unfreeze(AUnit unit) {
//        unit.runningManager().stopRunning();
//
////        CameraCommander.centerCameraOn();
//
//        boolean paintingDisabled = APainter.isDisabled();
//        if (paintingDisabled) {
//            APainter.enablePainting();
//        }
//        APainter.paintCircleFilled(unit, 10, Color.Cyan);
//        if (paintingDisabled) {
//            APainter.disablePainting();
//        }
////        GameSpeed.changeSpeedTo(70);
////        GameSpeed.pauseGame();
//
//        if (Select.main() != null && unit.moveTactical(Select.main(), Actions.MOVE_UNFREEZE, "Unfreeze")) return true;
//
//        if (unit.moveTactical(unit.translateByPixels(8, 0), Actions.MOVE_UNFREEZE, "Unfreeze")) return true;
//        if (unit.moveTactical(unit.translateByPixels(-8, 0), Actions.MOVE_UNFREEZE, "Unfreeze")) return true;
//        if (unit.moveTactical(unit.translateByPixels(0, 8), Actions.MOVE_UNFREEZE, "Unfreeze")) return true;
//        if (unit.moveTactical(unit.translateByPixels(0, -8), Actions.MOVE_UNFREEZE, "Unfreeze")) return true;
////        } else {
////            unit.holdPosition("Unfreeze");
////            unit.stop("Unfreeze");
////            unit.holdPosition("Unfreeze");
////            unit.stop("Unfreeze");
////            unit.stop("Unfreeze");
////            unit.holdPosition("Unfreeze");
////        }
//
//        return false;
//    }
//}
