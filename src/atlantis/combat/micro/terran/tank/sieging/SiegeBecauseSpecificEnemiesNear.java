package atlantis.combat.micro.terran.tank.sieging;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

public class SiegeBecauseSpecificEnemiesNear extends Manager {
    public SiegeBecauseSpecificEnemiesNear(AUnit unit) {
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

        double maxDist = enemy != null && enemy.isMoving() && unit.isOtherUnitFacingThisUnit(enemy) ? 15.5 : 11.98;
        if (
            enemies
                .ofType(
                    AUnitType.Protoss_Dragoon,
                    AUnitType.Protoss_Reaver,
                    AUnitType.Protoss_High_Templar,
                    AUnitType.Terran_Siege_Tank_Tank_Mode,
                    AUnitType.Terran_Siege_Tank_Siege_Mode,
                    AUnitType.Zerg_Hydralisk,
                    AUnitType.Zerg_Defiler,
                    AUnitType.Zerg_Lurker
                )
                .inRadius(maxDist, unit)
                .isNotEmpty()
        ) {
            if (enemies.inRadius(5 + unit.id() % 4, unit).notEmpty()) {
                return usedManager(WantsToSiege.wantsToSiegeNow(this, "KeyEnemy"));
            }
        }

        return null;
    }
}
