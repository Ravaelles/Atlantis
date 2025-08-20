package atlantis.protoss.reaver;

import atlantis.architecture.Manager;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnit;
import atlantis.units.AliveEnemies;
import atlantis.units.select.Selection;

public class ReaverAlwaysAttack extends Manager {
    private Selection enemies;

    public ReaverAlwaysAttack(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.cooldownRemaining() >= 13) return false;

        enemies = enemies();
        if (enemies.empty()) return false;

        return enemies.notEmpty() || unit.lastActionMoreThanAgo(15);
    }

    @Override
    public Manager handle() {
        AUnit enemy;

        // First attack very close enemies
        if ((enemy = enemies.inRadius(3, unit).nearestTo(unit)) != null) {
            unit.attackUnit(enemy);
            unit.setTooltipTactical("Closest" + enemy.name());
            return usedManager(this);
        }

        if ((enemy = enemies.inRadius(6, unit).mostDistantTo(unit)) != null) {
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
        Selection CBs = enemies.inRadius(8.1, unit).combatBuildings(true);
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
        if ((enemy = enemies.canBeAttackedBy(unit, 2).nearestTo(unit)) != null) {
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

//        // Attack truly anything
//        if ((enemy = EnemyUnits.discovered().groundUnits().nearestTo(unit)) != null) {
//            unit.attackUnit(enemy);
//            unit.setTooltipTactical("AnyfreakingThing" + enemy.name());
//            return usedManager(this);
//        }

        return null;
    }

    private Selection enemies() {
        enemies = AliveEnemies.get()
            .realUnitsAndCombatBuildings()
            .groundUnits()
            .effVisible()
            .havingAtLeastHp(1)
            .canBeAttackedBy(unit, -0.1)
            .notDeadMan()
            .inGroundRadius(12, unit)
            .hasPathFrom(unit);

        if (enemies.empty()) {
            enemies = EnemyUnits.discovered()
                .realUnitsAndCombatBuildings()
                .groundUnits()
                .effVisible()
                .canBeAttackedBy(unit, 999)
                .notDeadMan()
                .hasPathFrom(unit);
        }


        return enemies;
    }
}
