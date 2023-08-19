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
        return A.everyFrameExceptNthFrame(9) && unit.isCombatUnit() && unit.enemiesNear().empty();
    }

    protected Manager handle() {
        return usedManager(this);
    }
}

