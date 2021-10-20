package atlantis.combat.micro;

import atlantis.combat.targeting.AEnemyTargeting;
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
        if (enemy == null) {
            return false;
        }

        if (!unit.canAttackThisUnit(enemy, false, true)) {
            unit.setTooltip("Invalid target");
            System.err.println("Invalid target for " + unit + ": " + enemy + " (" + unit.distanceTo(enemy) + ")");
            return false;
        }

        if (!missionAllowsToAttack(unit, enemy)) {
            return false;
        }

        if (enemy != null) {
            unit.setTooltip("Attacking " + enemy.shortName() + " (" + unit.getCooldownCurrent() + ")");
//            APainter.paintTextCentered(unit, enemyToAttack + ", " + unit.isJustShooting(), Color.Red);
            APainter.paintLine(unit, enemy, Color.Red);
            if (!enemy.equals(unit.getTarget())) {
//                if (unit.isMoving() && unit.inRealWeaponRange(enemy)) {
//                    unit.stop("Stop&Attack");
//                } else {
                    unit.attackUnit(enemy);
//                }
            }
            return true;
        } 
        
        return false;
    }

    private static boolean missionAllowsToAttack(AUnit unit, AUnit enemy) {
        return unit.squad().mission().allowsToAttackEnemyUnit(unit, enemy);
    }

}
