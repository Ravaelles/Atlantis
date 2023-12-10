package atlantis.combat.managers;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;

public class ImproveCombatManagerPerformance extends Manager {
    public ImproveCombatManagerPerformance(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.isAttacking()) return false;
        if (A.everyNthGameFrame(9)) return false;
        if (unit.enemiesNear().notEmpty()) return false;

        return true;
//        return !unit.looksIdle() && A.everyFrameExceptNthFrame(A.rand(2, 15)) && unit.isCombatUnit() && unit.enemiesNear().empty();
    }

    protected Manager handle() {
        return usedManager(this);
    }
}

