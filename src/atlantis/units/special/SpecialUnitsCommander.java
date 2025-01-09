package atlantis.units.special;

import atlantis.architecture.Commander;
import atlantis.protoss.ProtossShieldBattery;
import atlantis.protoss.observer.ProtossObserver;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

public class SpecialUnitsCommander extends Commander {
    @Override
    protected void handle() {
        for (AUnit building : Select.ourBuildings().list()) {
            if (building.is(AUnitType.Protoss_Shield_Battery)) {
                (new ProtossShieldBattery(building)).invokeFrom(this);
            }
        }

        for (AUnit unit : Select.ourOfType(AUnitType.Protoss_Observer).list()) {
            (new ProtossObserver(unit)).invokeFrom(this);
        }
    }
}
