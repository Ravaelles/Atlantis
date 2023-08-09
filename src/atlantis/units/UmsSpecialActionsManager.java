package atlantis.units;

import atlantis.architecture.Commander;
import atlantis.debug.painter.APainter;
import atlantis.game.AGame;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import bwapi.Color;

/**
 * Special manager for UMS maps (Use Map Settings type of maps). Great for testing macro on custom maps.
 */
public class UmsSpecialActionsManager extends Commander {

    public static AUnit NEW_NEUTRAL_THAT_WILL_RENEGADE_TO_US = null;

    // =========================================================

    protected void handle() {
        if (AGame.isUms()) {
            goToBeaconsIfNeeded();
            goToNewCompanionsButStillNeutral();
        }
    }

    // =========================================================

    private boolean goToNewCompanionsButStillNeutral() {
        if (NEW_NEUTRAL_THAT_WILL_RENEGADE_TO_US != null) {
            AUnit goToRenegade = NEW_NEUTRAL_THAT_WILL_RENEGADE_TO_US;

//            System.out.println("Haaa! New companion!");

            for (AUnit unit : Select.our().inRadius(10, goToRenegade).list()) {
                if (unit.distTo(goToRenegade) > 0.5) {
                    unit.move(goToRenegade, Actions.MOVE_SPECIAL, "Friendly Renegade!", true);
                } else {
                    NEW_NEUTRAL_THAT_WILL_RENEGADE_TO_US = null;
                }
            }
            return true;
        }
        return false;
    }

    private boolean goToBeaconsIfNeeded() {
        Selection ours = Select.our();
        Selection beacons = Select.neutral().ofType(
                AUnitType.Special_Terran_Beacon,
                AUnitType.Special_Terran_Flag_Beacon,
                AUnitType.Special_Protoss_Beacon,
                AUnitType.Special_Protoss_Flag_Beacon,
                AUnitType.Special_Zerg_Beacon,
                AUnitType.Special_Zerg_Flag_Beacon
        );

        for (AUnit beacon : beacons.clone().list()) {
            AUnit unit = ours.clone().inRadius(9, beacon).nearestTo(beacon);
            AUnit nearestBeacon = beacons.clone().nearestTo(unit);
            if (unit != null && beacon != null) {
                unit.move(nearestBeacon, Actions.MOVE_SPECIAL, "To beacon", true);
                APainter.paintLine(unit, nearestBeacon, Color.White);
            }
            return true;
        }
        return false;
    }

}
