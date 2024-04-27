package atlantis.units.special;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;

public class PreventTooManyActions extends Manager {
    public PreventTooManyActions(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isCombatUnit()
            && A.now() % 2 == 0;
    }

    @Override
    public Manager handle() {
        return usedManager(this);
    }
}
