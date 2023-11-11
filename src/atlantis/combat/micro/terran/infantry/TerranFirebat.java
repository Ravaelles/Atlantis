package atlantis.combat.micro.terran.infantry;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.util.Enemy;

public class TerranFirebat extends Manager {
    public TerranFirebat(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isFirebat();
    }

    @Override
    protected Manager handle() {
        return null; // This works worse than without it :)

//        if (!shouldContinueMeleeFighting()) {
//            AUnit enemy = unit.nearestEnemy();
//            boolean shouldRun = (enemy != null && unit.distTo(enemy) <= 1.8);
//            if (shouldRun) {
//                if (unit.runningManager().runFrom(
//                    enemy, 2, Actions.RUN_ENEMY, false
//                )) {
//                    return usedManager(this);
//                }
//            }
//        }
//
//        if (
//            (unit.hp() >= 25 || unit.lastStartedAttackAgo() >= 30 * 10)
//                && unit.cooldown() <= 3
//                && unit.enemiesNear().melee().inRadius(1.6, unit).atMost(Enemy.protoss() ? 1 : 3)
//                && unit.friendsNear().medics().inRadius(1.4, unit).notEmpty()
//        ) {
//            if ((new AttackNearbyEnemies(unit)).handleAttackNearEnemyUnits()) {
//                unit.setTooltip("Napalm");
//                return usedManager(this);
//            }
//        }
//
//        return null;
    }

    protected boolean shouldContinueMeleeFighting() {
        if (unit.hp() >= 40) return true;

        int minHp = Enemy.protoss() ? 37 : 28;
        if (unit.hp() <= minHp || unit.cooldown() >= 3) return false;

        int medics = Select.ourOfType(AUnitType.Terran_Medic)
            .havingEnergy(30)
            .inRadius(1.85, unit)
            .count();

        if (medics >= 1) return true;

        int enemies = Select.enemyCombatUnits().canAttack(unit, 0).count();
        int enemyModifier = Enemy.zerg() ? 25 : 40;

        return unit.hpPercent(Math.min(50, enemies * enemyModifier));
    }
}
