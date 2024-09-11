package atlantis.combat.generic;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;

public class ImproveCombatPerformance extends Manager {
    public ImproveCombatPerformance(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (true) return false;

        if (unit.enemiesNear().notEmpty()) return false;
        if (unit.isAttacking()) return false;
        if (unit.squad().isLeader(unit)) return false;
        if (unit.lastActionMoreThanAgo(21)) return false;

        if (!A.everyNthGameFrame(21)) return true;

//        if (unit.isMoving() && unit.lastPositionChangedLessThanAgo(5)) return true;

        return false;
    }

    protected Manager handle() {
        return usedManager(this);
    }
}

