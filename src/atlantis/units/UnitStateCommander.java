package atlantis.units;

import atlantis.architecture.Commander;
import atlantis.units.select.Select;

public class UnitStateCommander extends Commander {
    @Override
    protected void handle() {
        for (AUnit unit : Select.ourUnitsWithUnfinishedList()) {
            (new UnitStateManager(unit)).invokeFrom(this);
        }
    }
}
