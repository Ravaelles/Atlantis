package atlantis.units;

import atlantis.architecture.Commander;
import atlantis.units.attacked_by.Bullets;
import atlantis.units.select.Select;

public class StateCommander extends Commander {

    @Override
    protected void handle() {
        for (AUnit unit : Select.ourUnitsWithUnfinishedList()) {
            (new UnitStateManager(unit)).invokeFrom(this);
        }

        Bullets.updateKnown();
    }
}
