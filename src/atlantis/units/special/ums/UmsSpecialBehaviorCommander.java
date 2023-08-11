package atlantis.units.special.ums;

import atlantis.architecture.Commander;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class UmsSpecialBehaviorCommander extends Commander {
    @Override
    protected void handle() {
        for (AUnit unit : Select.ourRealUnits().list()) {
            (new UmsSpecialBehaviorManager(unit)).invoke();
        }
    }
}
