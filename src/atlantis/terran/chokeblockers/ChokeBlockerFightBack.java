package atlantis.terran.chokeblockers;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.ProcessAttackUnit;
import atlantis.units.AUnit;

public class ChokeBlockerFightBack extends Manager {
    public ChokeBlockerFightBack(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return !unit.isScv()
            && unit.lastUnderAttackLessThanAgo(40);
    }

    @Override
    public Manager handle() {
        AUnit enemyInRange = enemyInRange();
        if (enemyInRange != null) {
            if ((new ProcessAttackUnit(unit)).processAttackOtherUnit(enemyInRange)) return usedManager(this);
        }

        return null;
    }

    private AUnit enemyInRange() {
        return unit.enemiesNear().inRadius(1.05, unit).groundUnits().mostWounded();
    }
}
