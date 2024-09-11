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
        if (true) return false;

        return unit.isCombatUnit()
            && A.now() % 2 == 0
            && !unit.isRetreating();
    }

    @Override
    public Manager handle() {
        return usedManager(this);
    }
}
