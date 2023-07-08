package atlantis.protoss;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

public class ProtossSpecificBuildingsCommander {

    public  void update() {
        for (AUnit building : Select.ourBuildings().list()) {

            if (building.is(AUnitType.Protoss_Shield_Battery)) {
                ProtossShieldBattery.update(building);
            }

        }
    }

}
