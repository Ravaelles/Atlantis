package atlantis.units.special.ums;

import atlantis.architecture.Commander;
import atlantis.config.env.Env;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class UmsSpecialBehaviorCommander extends Commander {
    @Override
    public boolean applies() {
        return A.isUms() && !Env.isTesting();
    }

    @Override
    protected void handle() {
        for (AUnit unit : Select.ourRealUnits().list()) {
            (new UmsSpecialBehaviorManager(unit)).invoke();
        }
    }
}
