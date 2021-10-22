package atlantis.combat.missions;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.units.actions.UnitActions;

public class UmsActionsManager {

    public static void update() {
        Select<AUnit> ours = Select.our();
        Select<AUnit> beacons = Select.neutral().ofType(
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
            unit.move(nearestBeacon, UnitActions.MOVE, "To beacon");
            return;
        }
    }

}
