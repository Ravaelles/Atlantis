package atlantis.combat.micro;

import atlantis.debug.APainter;
import atlantis.units.AUnit;
import bwapi.Color;

public class AAttackEnemyUnit {

    public static boolean handleAttackNearbyEnemyUnits(AUnit unit) {
        return handleAttackNearbyEnemyUnits(unit, 999999);
    }

    /**
     * Selects the best enemy unit and issues attack order.
     * @return <b>true</b> if unit has found valid target and is currently busy with either starting
     * an attack or just attacking the enemy<br />
     * <b>false</b> if no valid enemy to attack could be found
     */
    public static boolean handleAttackNearbyEnemyUnits(AUnit unit, double maxDistFromEnemy) {
        AUnit enemy = AEnemyTargeting.defineBestEnemyToAttackFor(unit, maxDistFromEnemy);

        // We were unable to define enemy unit to attack, just quit
        if (enemy == null) {
            return false;
        }

        // Check if weapon cooldown allows to attack this enemy
        if (!unit.canAttackThisKindOfUnit(enemy, false)) {
            unit.setTooltip("Invalid target");
            System.err.println("Invalid target for " + unit + ": " + enemy + " (" + unit.distanceTo(enemy) + ")");
            return false;
        }

        if (enemy != null) {
            unit.setTooltip("Attacking " + enemy.getShortName() + " (" + unit.getCooldownCurrent() + ")");
//            APainter.paintTextCentered(unit, enemyToAttack + ", " + unit.isJustShooting(), Color.Red);
            APainter.paintLine(unit, enemy, Color.Red);
            if (!enemy.equals(unit.getTarget())) {
                if (unit.isMoving() && unit.inRealWeaponRange(enemy)) {
                    unit.stop("Stop&Attack");
                } else {
                    unit.attackUnit(enemy);
                }
            }
            return true;
        } 
        
        return false;
    }

}
