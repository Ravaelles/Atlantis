package atlantis.zerg;

import atlantis.protoss.ProtossShieldBattery;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;

public class ZergSpecificBuildingsCommander {

    public static void update() {
        for (AUnit building : Select.ourBuildings().listUnits()) {

//            if (building.isType(AUnitType.Zerg_Sunken_Colony)) {
//                ZergSunken.handle(building);
//            }

        }
    }

}
