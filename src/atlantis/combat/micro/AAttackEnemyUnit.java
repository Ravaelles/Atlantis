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
        AUnit enemyToAttack = AEnemyTargeting.defineBestEnemyToAttackFor(unit, maxDistFromEnemy);

        // We were unable to define enemy unit to attack, just quit
        if (enemyToAttack == null) {
            return false;
        }

        // Check if weapon cooldown allows to attack this enemy
        if (!unit.canAttackThisKindOfUnit(enemyToAttack, false)) {
            unit.setTooltip("Invalid target");
            return false;
        } 

        if (enemyToAttack != null) {
            unit.setTooltip("Attacking " + enemyToAttack.getShortName() + " (" + unit.getCooldownCurrent() + ")");
//            APainter.paintTextCentered(unit, enemyToAttack + ", " + unit.isJustShooting(), Color.Red);
            APainter.paintLine(unit, enemyToAttack, Color.Red);
            if (!enemyToAttack.equals(unit.getTarget())) {
                unit.attackUnit(enemyToAttack);
            }
            return true;
        } 
        
        return false;
    }

}
