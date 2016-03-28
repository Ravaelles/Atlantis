package atlantis.combat.micro;

import atlantis.debug.tooltip.TooltipManager;
import atlantis.units.AUnit;
import atlantis.util.UnitUtil;


/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class AtlantisAttackEnemyUnit {

    /**
     * Selects the best enemy unit and issues attack order.
     * @return <b>true</b> if unit has found valid target and is currently busy with either starting 
     * an attack or just attacking the enemy<br />
     * <b>false</b> if no valid enemy to attack could be found
     */
    public static boolean handleAttackEnemyUnits(AUnit unit) {
        AUnit enemyToAttack = AtlantisEnemyTargeting.defineBestEnemyToAttackFor(unit);
        
        // =========================================================
        
        // We were unable to define enemy unit to attack, just quit
        if (enemyToAttack == null) {
            return false;
        }
        
        // Don't interrupt when shooting or starting to shoot
        if (unit.isAttackFrame() || unit.isStartingAttack()) { //replaces isJustShooting()
            return true;
        }
        
        // Check if weapon cooldown allows to attack this enemy
        if (!UnitUtil.canAttack(unit, enemyToAttack, true)) {
            return false;
        } 
        
        // =========================================================
        
        // If we already are attacking this unit, do not issue double command.
        if (!enemyToAttack.equals(unit.getTarget())) {
            unit.attack(enemyToAttack);
            TooltipManager.setTooltip(unit, "Forward!"); //setTooltip("Forward!");
        } 
        
        TooltipManager.removeTooltip(unit);
        //unit.removeTooltip();
        return true;
    }

}
