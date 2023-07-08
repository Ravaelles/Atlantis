package atlantis.combat.micro.generic;

import atlantis.debug.painter.APainter;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import bwapi.Color;

public class Unfreezer {

    public  final int UPDATE_UNIT_POSITION_EVERY_FRAMES = 30;

    /**
     * Some units can get FROZEN (stuck, unable to move/shoot). It's a known Starcraft bug.
     * This is an ugly way of fixing this.
     */
    public  boolean handleUnfreeze(AUnit unit) {
//        if (true) return false;

        if (unit.isRunning() && unit.lastActionFramesAgo() >= (UPDATE_UNIT_POSITION_EVERY_FRAMES + 20)) {
            if (unit._lastX == unit.x() && unit._lastY == unit.y()) {
//                System.err.println("UNFREEZE #1!");
//                unit.setTooltip("UNFREEZE!");
                return unfreeze();
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
//                return unfreeze();
//            }
//        }

        return false;
    }

    // =========================================================

    public  boolean unfreeze(AUnit unit) {
        unit.runningManager().stopRunning();

//        CameraManager.centerCameraOn();

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

        if (Select.main() != null && unit.moveTactical(Select.main(), Actions.MOVE_UNFREEZE, "Unfreeze")) {
            return true;
        }

        if (unit.moveTactical(unit.translateByPixels(8, 0), Actions.MOVE_UNFREEZE, "Unfreeze")) {
            return true;
        }
        if (unit.moveTactical(unit.translateByPixels(-8, 0), Actions.MOVE_UNFREEZE, "Unfreeze")) {
            return true;
        }
        if (unit.moveTactical(unit.translateByPixels(0, 8), Actions.MOVE_UNFREEZE, "Unfreeze")) {
            return true;
        }
        if (unit.moveTactical(unit.translateByPixels(0, -8), Actions.MOVE_UNFREEZE, "Unfreeze")) {
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
