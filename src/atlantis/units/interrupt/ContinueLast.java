package atlantis.units.interrupt;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;

public class ContinueLast extends Manager {
    public ContinueLast(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.isStopped()) return false;
        if (unit.lastActionMoreThanAgo(2)) return false;

        return true;
    }

    public Manager handle() {
        return usedManager(this);
    }
}
