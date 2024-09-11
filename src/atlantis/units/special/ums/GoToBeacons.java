package atlantis.units.special.ums;

import atlantis.architecture.Manager;
import atlantis.debug.painter.APainter;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import bwapi.Color;

public class GoToBeacons extends Manager {
    public GoToBeacons(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return A.isUms();
    }

    protected Manager handle() {
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
                unit.move(nearestBeacon, Actions.SPECIAL, "To beacon", true);
                APainter.paintLine(unit, nearestBeacon, Color.White);
            }
            return usedManager(this);
        }

        return null;
    }
}
