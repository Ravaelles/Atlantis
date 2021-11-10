package atlantis.combat.micro;

import atlantis.debug.APainter;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.actions.UnitActions;
import bwapi.Color;

public class Unfreezer {

    /**
     * Some units can get FROZEN (stuck, unable to move/shoot). It's a known Starcraft bug.
     * This is my ugly way of fixing this.
     */
    public static boolean handleUnfreeze(AUnit unit) {
        if (unit.isRunning() && unit.getLastOrderFramesAgo() >= AUnit.UPDATE_UNIT_POSITION_EVERY_FRAMES) {
            if (unit._lastX == unit.x() && unit._lastY == unit.y()) {
//                System.err.println("UNFREEZE #1!");
//                unit.setTooltip("UNFREEZE!");
                return unfreeze(unit);
            }
        }

//        if (
//                unit.lastUnderAttackLessThanAgo(5)
//                        && unit.getLastOrderFramesAgo() >= AUnit.UPDATE_UNIT_POSITION_EVERY_FRAMES
//                        && unit.cooldownRemaining() == 0
//        ) {
//            if (unit._lastX == unit.x() && unit._lastY == unit.y()) {
//                System.err.println("UNFREEZE #2!");
//                unit.setTooltip("UNFREEZE!");
//                return unfreeze(unit);
//            }
//        }

        return false;
    }

    // =========================================================

    public static boolean unfreeze(AUnit unit) {
        unit.runningManager().stopRunning();

//        CameraManager.centerCameraOn(unit);

        boolean paintingDisabled = APainter.isDisabled();
        if (paintingDisabled) {
            APainter.enablePainting();
        }
        APainter.paintCircleFilled(unit, 10, Color.Cyan);
        if (paintingDisabled) {
            APainter.disablePainting();
        }
//        GameSpeed.changeSpeedTo(70);
//        GameSpeed.pauseGame();

        if (Select.main() != null && unit.move(Select.main(), UnitActions.MOVE, "Unfreeze")) {
            return true;
        }

//        if (unit.isHoldingPosition()) {
        if (unit.move(unit.position().translateByPixels(8, 0), UnitActions.MOVE, "Unfreeze")) {
            return true;
        }
        if (unit.move(unit.position().translateByPixels(-8, 0), UnitActions.MOVE, "Unfreeze")) {
            return true;
        }
        if (unit.move(unit.position().translateByPixels(0, 8), UnitActions.MOVE, "Unfreeze")) {
            return true;
        }
        if (unit.move(unit.position().translateByPixels(0, -8), UnitActions.MOVE, "Unfreeze")) {
            return true;
        }
//        } else {
//            unit.holdPosition("Unfreeze");
//            unit.stop("Unfreeze");
//            unit.holdPosition("Unfreeze");
//            unit.stop("Unfreeze");
//            unit.stop("Unfreeze");
//            unit.holdPosition("Unfreeze");
//        }

        return false;
    }

}
