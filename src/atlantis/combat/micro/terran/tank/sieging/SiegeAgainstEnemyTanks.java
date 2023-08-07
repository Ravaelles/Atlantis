package atlantis.combat.micro.terran.tank.sieging;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

public class SiegeAgainstEnemyTanks extends Manager {
    public SiegeAgainstEnemyTanks(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return true;
    }

    public Manager handle() {
        Selection enemies = unit.enemiesNear().groundUnits().nonBuildings().nonWorkers().effVisible();

        if (!Enemy.terran()) {
            enemies = enemies.visibleOnMap();
        }

        AUnit enemy = unit.nearestEnemy();

        double maxDist = enemy != null && enemy.isMoving() && unit.isOtherUnitFacingThisUnit(enemy) ? 14.8 : 11.98;
        if (
            enemies
                .tanks()
                .inRadius(maxDist, unit)
                .isNotEmpty()
        ) {
            return usedManager(WantsToSiege.wantsToSiegeNow(this, "ENEMY_TANKS"));
        }

        return null;
    }
}
