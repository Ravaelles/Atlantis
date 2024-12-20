package atlantis.protoss.reaver;

import atlantis.architecture.Manager;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;

public class ReaverAlwaysAttack extends Manager {
    private Selection enemies;

    public ReaverAlwaysAttack(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.cooldownRemaining() >= 10) return false;

        enemies = enemies();

        return enemies.notEmpty() || unit.lastActionMoreThanAgo(15);
    }

    @Override
    public Manager handle() {
        AUnit enemy;

        // First attack very close enemies
        if ((enemy = enemies.inRadius(2, unit).nearestTo(unit)) != null) {
            unit.attackUnit(enemy);
            unit.setTooltipTactical("Closest" + enemy.name());
            return usedManager(this);
        }

        if ((enemy = enemies.inRadius(4, unit).mostDistantTo(unit)) != null) {
            unit.attackUnit(enemy);
            unit.setTooltipTactical("Near" + enemy.name());
            return usedManager(this);
        }

        // If no very close enemy, then attack the one most distant
        if ((enemy = enemies.canBeAttackedBy(unit, -0.9).mostDistantTo(unit)) != null) {
            unit.attackUnit(enemy);
            unit.setTooltipTactical("Nice" + enemy.name());
            return usedManager(this);
        }

        // Attack CB
        Selection CBs = enemies.inRadius(9, unit).combatBuildings(true);
        if ((enemy = CBs.inRadius(7.8, unit).nearestTo(unit)) != null) {
            unit.attackUnit(enemy);
            unit.setTooltipTactical("Juicy" + enemy.name());
            return usedManager(this);
        }
        if ((enemy = CBs.mostWounded()) != null) {
            unit.attackUnit(enemy);
            unit.setTooltipTactical("Tasty" + enemy.name());
            return usedManager(this);
        }

        // Attack the one most distant
        if ((enemy = enemies.canBeAttackedBy(unit, 5).nearestTo(unit)) != null) {
            unit.attackUnit(enemy);
            unit.setTooltipTactical("Fancy" + enemy.name());
            return usedManager(this);
        }

        // Attack any
        if ((enemy = enemies.nearestTo(unit)) != null) {
            unit.attackUnit(enemy);
            unit.setTooltipTactical("FarAway" + enemy.name());
            return usedManager(this);
        }

        return null;
    }

    private Selection enemies() {
        enemies = unit.enemiesNear()
            .groundUnits()
            .excludeEggsAndLarvae()
            .effVisible()
            .canBeAttackedBy(unit, 6)
            .notDeadMan();

        if (enemies.empty()) {
            enemies = EnemyUnits.discovered()
                .groundUnits()
                .excludeEggsAndLarvae()
                .effVisible()
                .canBeAttackedBy(unit, 999)
                .notDeadMan();
        }

        return enemies;
    }
}
