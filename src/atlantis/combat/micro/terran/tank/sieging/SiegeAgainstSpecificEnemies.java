package atlantis.combat.micro.terran.tank.sieging;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

public class SiegeAgainstSpecificEnemies extends Manager {

    private Selection enemies;

    public SiegeAgainstSpecificEnemies(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        enemies = unit.enemiesNear().combatUnits().nonBuildings().effVisible();

        return enemies.notEmpty();
    }

    protected Manager handle() {
        if (!Enemy.terran()) {
            enemies = enemies
                .visibleOnMap()
                .ofType(
                    AUnitType.Protoss_Dragoon,
                    AUnitType.Protoss_Reaver,
                    AUnitType.Protoss_High_Templar,
                    AUnitType.Zerg_Hydralisk,
                    AUnitType.Zerg_Defiler,
                    AUnitType.Zerg_Lurker
                )
                .inRadius(17, unit);
        }

        AUnit enemy = unit.nearestEnemy();

        double minDist = enemy != null && enemy.isMoving() && enemy.isFacing(unit) ? 15.5 : 11.98;
        if (enemies.notEmpty()) {
            if (unit.woundHp() <= 15 || enemies.inRadius(2 + unit.id() % 4, unit).notEmpty()) {
                return usedManager(ForceSiege.forceSiegeNow(this, "KeyEnemy"));
            }
        }

        return null;
    }
}
