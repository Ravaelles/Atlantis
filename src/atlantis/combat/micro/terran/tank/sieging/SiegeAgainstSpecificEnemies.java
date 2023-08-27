package atlantis.combat.micro.terran.tank.sieging;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

public class SiegeAgainstSpecificEnemies extends Manager {
    public SiegeAgainstSpecificEnemies(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return true;
    }

    protected Manager handle() {
        Selection enemies = unit.enemiesNear().combatUnits().nonBuildings().effVisible();

        if (!Enemy.terran()) {
            enemies = enemies.visibleOnMap();
        }

        AUnit enemy = unit.nearestEnemy();

        double maxDist = enemy != null && enemy.isMoving() ? 14.5 : 11.98;
        if (
            enemies
                .ofType(
                    AUnitType.Protoss_Dragoon,
                    AUnitType.Protoss_Reaver,
                    AUnitType.Protoss_High_Templar,
                    AUnitType.Zerg_Hydralisk,
                    AUnitType.Zerg_Defiler,
                    AUnitType.Zerg_Lurker
                )
                .inRadius(maxDist, unit)
                .notEmpty()
        ) {
            if (unit.idIsOdd() || enemies.inRadius(2 + unit.id() % 4, unit).notEmpty()) {
                return usedManager(ForceSiege.forceSiegeNow(this, "KeyEnemy"));
            }
        }

        return null;
    }
}
