package atlantis.combat.micro;

import jnibwapi.Unit;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class AtlantisAttackEnemyUnit {

    public static boolean handleAttackEnemyUnits(Unit unit) {
        Unit enemyToAttack = AtlantisEnemyTargeting.defineEnemyToAttackFor(unit);
        
        // =========================================================
        
        // Nothing to attack
        if (enemyToAttack == null) {
            unit.setTooltip("No enemy");
            return false;
        }
        
        // Don't interrupt when shooting
        if (unit.isJustShooting()) {
            return true;
        }
        
        // =========================================================
        
        unit.setTooltip("-> " + enemyToAttack.getShortName() + "/" + unit.getLastUnitActionWasFramesAgo());
        if (unit.getGroundWeaponCooldown() <= 0) {
//                unit.attackUnit(enemyToAttack, false);
            if (!enemyToAttack.equals(unit.getTarget())) {
//                unit.attack(enemyToAttack, false);
                unit.attackUnit(enemyToAttack, false);
                unit.setTooltip("#" + enemyToAttack.getShortName() + "/" + (int) (unit.getLastUnitActionWasFramesAgo() / 30) + "#");
            } else {
                unit.setTooltip(">" + enemyToAttack.getShortName() + " " + unit.getLastUnitActionWasFramesAgo() + "ago<");
            }
        } else {
            unit.setTooltip("Attack " + unit.getLastUnitActionWasFramesAgo());
        }
//            unit.removeTooltip();
        return true;
    }

    // =========================================================
}
