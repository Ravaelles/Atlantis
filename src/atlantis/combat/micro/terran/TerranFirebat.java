package atlantis.combat.micro.terran;

import atlantis.combat.micro.AAttackEnemyUnit;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.util.Enemy;

public class TerranFirebat {

    public static boolean update(AUnit unit) {
        if (!unit.isFirebat()) {
            return false;
        }

        if (unit.cooldown() >= 4 || !shouldContinueMeleeFighting(unit)) {
            AUnit nearestEnemy = unit.nearestEnemy();
            return nearestEnemy != null && unit.runningManager().runFrom(
                nearestEnemy, 1.0, Actions.RUN_ENEMY, false
            );
        }

        if (
            unit.hp() >= 37
            && unit.enemiesNear().melee().inRadius(1.6, unit).atMost(Enemy.protoss() ? 2 : 3)
        ) {
            if (AAttackEnemyUnit.handleAttackNearEnemyUnits(unit)) {
                unit.setTooltip("Napalm");
                return true;
            }
        }

        return false;
    }

    protected static boolean shouldContinueMeleeFighting(AUnit unit) {
        if (unit.hp() >= 40) {
            return true;
        }

        int medics = Select.ourOfType(AUnitType.Terran_Medic).havingEnergy(30).inRadius(1.85, unit).count();

        if (medics >= 1) {
            return true;
        }

        int enemies = Select.enemyCombatUnits().canAttack(unit, 0).count();
        int enemyModifier = Enemy.zerg() ? 25 : 40;

        return unit.hpPercent(Math.min(50, enemies * enemyModifier));
    }
}
