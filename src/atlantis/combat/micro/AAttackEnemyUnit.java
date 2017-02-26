package atlantis.combat.micro;

import atlantis.units.AUnit;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class AAttackEnemyUnit {

    /**
     * Selects the best enemy unit and issues attack order.
     * @return <b>true</b> if unit has found valid target and is currently busy with either starting 
     * an attack or just attacking the enemy<br />
     * <b>false</b> if no valid enemy to attack could be found
     */
    public static boolean handleAttackEnemyUnits(AUnit unit) {
        AUnit enemyToAttack = AEnemyTargeting.defineBestEnemyToAttackFor(unit);
        
        // =========================================================
        
        // We were unable to define enemy unit to attack, just quit
        if (enemyToAttack == null) {
            return false;
        }
        
        // Don't interrupt when shooting or starting to shoot
        if (unit.isJustShooting()) {
            unit.setTooltip("Shooting");
            return true;
        }
        
        // Check if weapon cooldown allows to attack this enemy
        if (!unit.canAttackThisKindOfUnit(enemyToAttack, true)) {
            unit.setTooltip("Invalid target");
            return false;
        } 
        
        // =========================================================
        
        // If we already are attacking this unit, do not issue double command.
        if (enemyToAttack != null && !unit.isJustShooting()) {
            unit.setTooltip("Attacking " + enemyToAttack.getShortName());
            return unit.attackUnit(enemyToAttack);
        } 
        
        return false;
    }

}
