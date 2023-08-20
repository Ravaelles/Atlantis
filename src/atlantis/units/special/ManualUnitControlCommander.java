package atlantis.units.special;

import atlantis.architecture.Commander;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class ManualUnitControlCommander extends Commander {
    @Override
    protected void handle() {
        for (AUnit unit : Select.ourRealUnits().list()) {
            (new ManualOverrideManager(unit)).invoke();
        }
    }
}
