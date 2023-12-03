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
        if (unit.woundHp() <= 15) return false;

        enemies = unit.enemiesNear().combatUnits().nonBuildings().effVisible();
        if (enemies.empty()) return false;

        if (enemies.inRadius(7 + unit.id() % 6, unit).notEmpty()) return false;

        return true;
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
            return wantsToSiegeAgainst(enemy, enemies);
        }

        return null;
    }

    private Manager wantsToSiegeAgainst(AUnit enemy, Selection enemies) {
//        Selection otherEnemyTypes = enemies.excludeTypes(enemy.type());

        if (dangerousEnemiesCloseSoDontSiege()) return null;

        return usedManager(ForceSiege.forceSiegeNow(this, "KeyEnemy"));
    }

    private boolean dangerousEnemiesCloseSoDontSiege() {
        int minEnemiesToCancelSiege = Math.max(
            1, (unit.isWounded() ? 1 : 2) - unit.friendsNear().tanks().size() / 4
        );

        return unit.enemiesNear().inRadius(13, unit).ofType(
            AUnitType.Protoss_Dark_Templar,
            AUnitType.Protoss_High_Templar,
            AUnitType.Protoss_Archon,
            AUnitType.Zerg_Ultralisk
        ).atLeast(minEnemiesToCancelSiege);
    }
}