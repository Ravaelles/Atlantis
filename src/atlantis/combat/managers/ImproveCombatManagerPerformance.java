package atlantis.combat.managers;

import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.managers.Manager;

public class ImproveCombatManagerPerformance extends Manager {

    public ImproveCombatManagerPerformance(AUnit unit) {
        super(unit);
    }

    public Manager handle() {
        if (unit.enemiesNear().empty() && A.notNthGameFrame(5)) {
            return usedManager(this);
        }

        return null;
    }
}

