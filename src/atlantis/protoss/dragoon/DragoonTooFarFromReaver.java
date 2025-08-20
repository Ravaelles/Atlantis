package atlantis.protoss.dragoon;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.select.Count;

public class DragoonTooFarFromReaver extends Manager {
    public static final int MAX_DIST = 5;
    private AUnit reaver;

    public DragoonTooFarFromReaver(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isMissionAttack()) return false;
        if (A.supplyUsed() >= 180 || A.minerals() >= 1500) return false;
        if (Count.reavers() == 0) return false;
        if (unit.isRunning()) return false;
        if (unit.hp() <= 38) return false;
        if (unit.hasCooldown()) return false;
        if (unit.groundDistToMain() <= 35) return false;

        reaver = unit.friendsNear().reavers().nearestTo(unit);
        if (reaver == null) return false;

        return unit.distTo(reaver) > MAX_DIST;
    }
}
