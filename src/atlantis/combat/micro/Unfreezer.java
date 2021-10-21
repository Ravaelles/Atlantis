package atlantis.combat.micro;

import atlantis.ACamera;
import atlantis.AGameSpeed;
import atlantis.debug.APainter;
import atlantis.units.AUnit;
import atlantis.units.Select;
import atlantis.units.actions.UnitActions;
import bwapi.Color;

public class Unfreezer {

    /**
     * Some units can get FROZEN (stuck, unable to move/shoot). It's a known Starcraft bug.
     * This is my ugly way of fixing this.
     */
    public static boolean handleUnfreeze(AUnit unit) {
        if (unit.isRunning() && unit.getLastOrderFramesAgo() >= AUnit.UPDATE_UNIT_POSITION_EVERY_FRAMES) {
            if (unit.lastX == unit.getX() && unit.lastY == unit.getY()) {
                System.err.println("UNFREEZE #1!");
                unit.setTooltip("UNFREEZE!");
                return unfreeze(unit);
            }
        } else if (
                unit.lastUnderAttackLessThanAgo(5)
                        && unit.getLastOrderFramesAgo() >= AUnit.UPDATE_UNIT_POSITION_EVERY_FRAMES
                        && unit.getCooldownCurrent() == 0
        ) {
            if (unit.lastX == unit.getX() && unit.lastY == unit.getY()) {
                System.err.println("UNFREEZE #2!");
                unit.setTooltip("UNFREEZE!");
                return unfreeze(unit);
            }
        }

        return false;
    }

    // =========================================================

    public static boolean unfreeze(AUnit unit) {
        unit.runningManager().stopRunning();

//        ACamera.centerCameraOn(unit);

        boolean paintingDisabled = APainter.isDisabled();
        if (paintingDisabled) {
            APainter.enablePainting();
        }
        APainter.paintCircleFilled(unit, 10, Color.Cyan);
        if (paintingDisabled) {
            APainter.disablePainting();
        }
//        AGameSpeed.changeSpeedTo(70);
//        AGameSpeed.pauseGame();

        if (Select.mainBase() != null && unit.move(Select.mainBase(), UnitActions.MOVE, "Unfreeze")) {
            return true;
        }

//        if (unit.isHoldingPosition()) {
        if (unit.move(unit.getPosition().translateByPixels(8, 0), UnitActions.MOVE, "Unfreeze")) {
            return true;
        }
        if (unit.move(unit.getPosition().translateByPixels(-8, 0), UnitActions.MOVE, "Unfreeze")) {
            return true;
        }
        if (unit.move(unit.getPosition().translateByPixels(0, 8), UnitActions.MOVE, "Unfreeze")) {
            return true;
        }
        if (unit.move(unit.getPosition().translateByPixels(0, -8), UnitActions.MOVE, "Unfreeze")) {
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
