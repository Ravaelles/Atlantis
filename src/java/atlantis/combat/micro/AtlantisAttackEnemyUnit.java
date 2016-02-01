package atlantis.combat.micro;

import jnibwapi.Unit;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class AtlantisAttackEnemyUnit {

    public static boolean handleAttackEnemyUnits(Unit unit) {
        Unit enemyToAttack = AtlantisEnemyTargeting.defineBestEnemyToAttackFor(unit);
        
        // =========================================================
        
        // Nothing to attack
        if (enemyToAttack == null) {
            return false;
        }
        
        // Don't interrupt when shooting
        if (unit.isJustShooting()) {
            return true;
        }
        
        // Check if weapon cooldown allows to attack this enemy
        if (unit.canAttackThisKindOfUnit(enemyToAttack, true)) {
            return false;
        } 
        
        // =========================================================
        
        // If we already are attacking this unit, do not issue double command.
        if (!enemyToAttack.equals(unit.getTarget())) {
            unit.attackUnit(enemyToAttack, false);
        } 
        
        unit.removeTooltip();
        return true;
    }

}
