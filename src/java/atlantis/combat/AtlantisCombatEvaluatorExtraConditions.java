package atlantis.combat;

import atlantis.AtlantisGame;
import atlantis.wrappers.SelectUnits;
import jnibwapi.Unit;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class AtlantisCombatEvaluatorExtraConditions {

    private static Unit unit = null;
    private static Unit nearestEnemy = null;
    
    // =========================================================
    
    protected static boolean shouldAlwaysFight(Unit unit, Unit nearestEnemy) {
        AtlantisCombatEvaluatorExtraConditions.unit = unit;
        AtlantisCombatEvaluatorExtraConditions.nearestEnemy = nearestEnemy;
        
        if (shouldFightToProtectMainBaseAtAllCosts()) {
            return true;
        }
        
        return false;
    }
    
    protected static boolean shouldAlwaysRetreat(Unit unit, Unit nearestEnemy) {
        AtlantisCombatEvaluatorExtraConditions.unit = unit;
        AtlantisCombatEvaluatorExtraConditions.nearestEnemy = nearestEnemy;
        
        if (shouldRetreatBecauseTooFewOurUnitsAround()) {
            unit.setTooltip("Closer!");
            return true;
        }
        
        return false;
    }
    
    // =========================================================
    // Always fight

    private static boolean shouldFightToProtectMainBaseAtAllCosts() {
//        if (isNearestEnemyQuiteFar()) {
//            return false;
//        }
        
        // If you're near the main base, force the fight
        Unit mainBase = SelectUnits.mainBase();
        if (mainBase != null) {
            if (mainBase.distanceTo(unit) < 7) {

                // Force to fight units that aren't close to being dead                
                if (unit.getHP() >= 11) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    // =========================================================
    // Always retreat

    private static boolean shouldRetreatBecauseTooFewOurUnitsAround() {
        
        // If nearest enemy is far, ignore it
        if (isNearestEnemyQuiteFar()) {
            return false;
        }
        
        // If enemy is somewhat near, disallow attacking without support
        else {
            if (AtlantisGame.playsAsTerran()) {
                return SelectUnits.ourCombatUnits().inRadius(2.5, unit).count() <= 2
                        || SelectUnits.ourCombatUnits().inRadius(5, unit).count() <= 5;
            }
            else {
                return SelectUnits.ourCombatUnits().inRadius(8, unit).count() <= 6;
            }
        }
    }
    
    // =========================================================

    private static boolean isNearestEnemyQuiteFar() {
        return nearestEnemy == null || nearestEnemy.distanceTo(unit) > 12.2;
    }
    
}
