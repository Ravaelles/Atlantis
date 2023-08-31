package atlantis.combat.micro.terran.tank.sieging;

import atlantis.architecture.Manager;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

public class SiegeVsTerran extends Manager {
    public SiegeVsTerran(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!Enemy.terran()) return false;
        if (!unit.isTankUnsieged()) return false;
        if (unit.enemiesNear().groundUnits().empty()) return false;

        return areEnemiesInRange();
    }

    private boolean areEnemiesInRange() {
        Selection enemies = EnemyUnits.discovered().groundUnits().nonBuildings().combatUnits();

        if (enemies.empty()) return false;
        if (enemies.atLeast(2)) return true;

        return enemies.inRadius(11.9 + (unit.id() % 3) / 40.0, unit).notEmpty();
    }

    @Override
    protected Manager handle() {
        if (WantsToSiege.wantsToSiegeNow(unit, "vTerran!")) return usedManager(this);

        return null;
    }
}
