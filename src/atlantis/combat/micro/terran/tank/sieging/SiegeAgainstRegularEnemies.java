package atlantis.combat.micro.terran.tank.sieging;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

public class SiegeAgainstRegularEnemies extends Manager {
    public SiegeAgainstRegularEnemies(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        // Only one in three tanks should siege
        if (unit.id() % 3 != 0 || Count.tanks() <= 3) return false;

        if (unit.friendsNear().tankSupport().inRadius(5, unit).count() <= 8) return false;

        Selection enemies = unit.enemiesNear();

        if (
            enemies.inRadius(15, unit).atLeast(2)
                && unit.friendsNear().inRadius(5, unit).count() >= 7
        ) {
            if (Enemy.terran()) return true;

            return enemies.inRadius(2.8, unit).empty() && enemies.inRadius(5.5, unit).atMost(1);
        }

        return false;
    }

    @Override
    protected Manager handle() {
        return usedManager(WantsToSiege.wantsToSiegeNow(this, "Enemies!"));
    }
}
