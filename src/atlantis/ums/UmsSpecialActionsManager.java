package atlantis.ums;

import atlantis.AGame;
import atlantis.debug.APainter;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.units.actions.UnitActions;
import bwapi.Color;

public class UmsSpecialActionsManager {

    public static AUnit NEW_NEUTRAL_THAT_WILL_RENEGADE_TO_US = null;

    // =========================================================

    public static void update() {
        if (AGame.isUms()) {
            goToBeaconsIfNeeded();
            goToNewCompanionsButStillNeutral();
        }
    }

    private static boolean goToNewCompanionsButStillNeutral() {
        if (NEW_NEUTRAL_THAT_WILL_RENEGADE_TO_US != null) {
            AUnit goToRenegade = NEW_NEUTRAL_THAT_WILL_RENEGADE_TO_US;

            System.out.println("Haaa! New companion!");

            for (AUnit unit : Select.our().inRadius(10, goToRenegade).listUnits()) {
                if (unit.distTo(goToRenegade) > 0.5) {
                    unit.move(goToRenegade, UnitActions.MOVE, "Friendly Renegade!");
                } else {
                    NEW_NEUTRAL_THAT_WILL_RENEGADE_TO_US = null;
                }
            }
            return true;
        }
        return false;
    }

    private static boolean goToBeaconsIfNeeded() {
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
                unit.move(nearestBeacon, UnitActions.MOVE, "To beacon");
                APainter.paintLine(unit, nearestBeacon, Color.White);
            }
            return true;
        }
        return false;
    }

}
