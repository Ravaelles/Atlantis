package atlantis.combat.micro.terran.tank.unsieging.kursk;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;

public class DontUnsiegeEnemyTanksNear extends Manager {
    private Selection enemyTanks;

    public DontUnsiegeEnemyTanksNear(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isSieged()
            && !unit.isAttacking()
            && (enemyTanks = unit.enemiesNear().tanksSieged().inRadius(18, unit)).notEmpty();
    }

    @Override
    public Manager handle() {
        if (enemyTanks.inRadius(13.1, unit).notEmpty()) {
            return usedManager(this);
        }

        return null;
    }
}
