package atlantis.combat.micro;

import atlantis.units.AUnit;
import atlantis.units.Select;
import atlantis.units.actions.UnitActions;

public class Unfreezer {

    /**
     * Some units can get FROZEN (stuck, unable to move/shoot). It's a known Starcraft bug.
     * This is my ugly way of fixing this.
     */
    public static boolean handleUnfreeze(AUnit unit) {
        if (unit.isRunning() && unit.getLastOrderFramesAgo() >= 40) {
            if (unit.lastX == unit.getX() && unit.lastY == unit.getY()) {
                System.err.println("UNFREEZE #1!");
                unit.setTooltip("UNFREEZE!");
                unfreeze(unit);
                return true;
            }
        } else if (unit.isUnderAttack() && unit.getLastOrderFramesAgo() >= 40) {
            if (unit.lastX == unit.getX() && unit.lastY == unit.getY()) {
                System.err.println("UNFREEZE #2!");
                unit.setTooltip("UNFREEZE!");
                unfreeze(unit);
                return true;
            }
        }

        return false;
    }

    // =========================================================

    public static void unfreeze(AUnit unit) {
        unit.runningManager().stopRunning();

        if (Select.mainBase() != null && unit.move(Select.mainBase(), UnitActions.MOVE, "Unfreeze")) {
            return;
        }

//        if (unit.isHoldingPosition()) {
            if (unit.move(unit.getPosition().translateByPixels(8, 0), UnitActions.MOVE, "Unfreeze")) {
                return;
            }
            if (unit.move(unit.getPosition().translateByPixels(-8, 0), UnitActions.MOVE, "Unfreeze")) {
                return;
            }
            if (unit.move(unit.getPosition().translateByPixels(0, 8), UnitActions.MOVE, "Unfreeze")) {
                return;
            }
            if (unit.move(unit.getPosition().translateByPixels(0, -8), UnitActions.MOVE, "Unfreeze")) {
                return;
            }
//        } else {
//            unit.holdPosition("Unfreeze");
//            unit.stop("Unfreeze");
//            unit.holdPosition("Unfreeze");
//            unit.stop("Unfreeze");
//            unit.stop("Unfreeze");
//            unit.holdPosition("Unfreeze");
//        }
    }

}
