package atlantis.combat.micro.terran.tank.sieging;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

public class SiegeVsSpecificEnemies extends Manager {

    private Selection enemies;

    public SiegeVsSpecificEnemies(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        enemies = unit.enemiesNear().combatUnits().nonBuildings().effVisible();

        return enemies.notEmpty();
    }

    protected Manager handle() {
        if (!Enemy.terran()) {
            enemies = enemies.visibleOnMap();
        }

        AUnit enemy = unit.nearestEnemy();

        double maxDist = enemy != null && !unit.isMoving() && enemy.isMoving() && enemy.isFacing(unit) ? 17.5 : 11.98;
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
