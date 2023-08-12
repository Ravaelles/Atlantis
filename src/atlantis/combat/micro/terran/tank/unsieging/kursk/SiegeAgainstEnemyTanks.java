package atlantis.combat.micro.terran.tank.unsieging.kursk;

import atlantis.architecture.Manager;
import atlantis.combat.micro.terran.tank.sieging.WantsToSiege;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

public class SiegeAgainstEnemyTanks extends Manager {
    public SiegeAgainstEnemyTanks(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return Enemy.terran() && unit.isTank();
    }

    protected Manager handle() {
        Selection enemyTanks = unit.enemiesNear().tanks().inRadius(13.2, unit);

        if (enemyTanks.isEmpty()) {
            return null;
        }

        return usedManager(WantsToSiege.wantsToSiegeNow(this, "ENEMY_TANKS"));
    }
}
