package atlantis.units;

import atlantis.architecture.Commander;
import atlantis.protoss.ProtossShieldBattery;
import atlantis.units.select.Select;

public class SpecialUnitsCommander extends Commander {
    @Override
    protected void handle() {
        for (AUnit building : Select.ourBuildings().list()) {
            if (building.is(AUnitType.Protoss_Shield_Battery)) {
                (new ProtossShieldBattery(building)).invoke();
            }
        }
    }
}
