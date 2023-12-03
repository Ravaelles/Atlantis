package atlantis.combat.micro.terran.tank.sieging;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

public class SiegeVsRegularEnemies extends Manager {
    public SiegeVsRegularEnemies(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (Enemy.terran()) return false;
        if (someTanksShouldNotSiege()) return false;
        if (unit.friendsNear().tankSupport().inRadius(5, unit).count() <= 8) return false;

        Selection enemies = unit.enemiesNear().groundUnits().effVisible();

        if (
            enemies.inRadius(15, unit).atLeast(2)
                && unit.friendsNear().inRadius(5, unit).count() >= 7
        ) {
            if (Enemy.terran()) return true;

            return enemies.inRadius(2.8, unit).empty() && enemies.inRadius(5.5, unit).atMost(1);
        }

        return false;
    }

    private boolean someTanksShouldNotSiege() {
        if (Enemy.terran()) return false;

        return unit.id() % 4 != 0 || Count.tanks() <= 3;
    }

    @Override
    protected Manager handle() {
        if (WantsToSiege.wantsToSiegeNow(unit, "Enemies!")) return usedManager(this);

        return null;
    }
}
