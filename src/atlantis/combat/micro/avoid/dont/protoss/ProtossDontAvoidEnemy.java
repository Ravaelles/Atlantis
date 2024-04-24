package atlantis.combat.micro.avoid.dont.protoss;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class ProtossDontAvoidEnemy extends Manager {
    public ProtossDontAvoidEnemy(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isProtoss()) return false;
        if (unit.looksIdle() || unit.lastActionMoreThanAgo(40)) return false;

        if (DragoonDontAvoidEnemy.dontAvoid(unit)) return true;

        return false;
    }

    @Override
    public Manager handle() {
        return usedManager(this);
    }
}
