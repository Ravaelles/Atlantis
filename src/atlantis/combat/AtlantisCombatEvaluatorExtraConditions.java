package atlantis.combat;

import atlantis.AtlantisGame;
import atlantis.debug.tooltip.TooltipManager;
import atlantis.units.AUnit;
import atlantis.units.Select;
import atlantis.util.PositionUtil;


/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class AtlantisCombatEvaluatorExtraConditions {

    private static AUnit unit = null;
    private static AUnit nearestEnemy = null;
    
    // =========================================================
    
    protected static boolean shouldAlwaysFight(AUnit unit, AUnit nearestEnemy) {
        AtlantisCombatEvaluatorExtraConditions.unit = unit;
        AtlantisCombatEvaluatorExtraConditions.nearestEnemy = nearestEnemy;
        
        if (shouldFightToProtectMainBaseAtAllCosts()) {
            return true;
        }
        
        return false;
    }
    
    protected static boolean shouldAlwaysRetreat(AUnit unit, AUnit nearestEnemy) {
        AtlantisCombatEvaluatorExtraConditions.unit = unit;
        AtlantisCombatEvaluatorExtraConditions.nearestEnemy = nearestEnemy;
        
        if (shouldRetreatBecauseTooFewOurUnitsAround()) {
            TooltipManager.setTooltip(unit, "Closer!");
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
        AUnit mainBase = Select.mainBase();
        if (mainBase != null) {
            if (PositionUtil.distanceTo(mainBase, unit) < 7) {

                // Force to fight units that aren't close to being dead                
                if (unit.getHitPoints() >= 11) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    // =========================================================
    // Always retreat

    private static boolean shouldRetreatBecauseTooFewOurUnitsAround() {
        
        // Flying units can always attack lonely
        if (unit.isAirUnit()) {
            return false;
        }
        
        // If nearest enemy is far, ignore it
        if (isNearestEnemyQuiteFar()) {
            return false;
        }
        
        // If enemy is somewhat near, disallow attacking without support
        else {
            if (AtlantisGame.playsAsTerran()) {
                return Select.ourCombatUnits().inRadius(2.5, unit.getPosition()).count() <= 2
                        || Select.ourCombatUnits().inRadius(5, unit.getPosition()).count() <= 5;
            }
            else {
                return Select.ourCombatUnits().inRadius(8, unit.getPosition()).count() <= 6;
            }
        }
    }
    
    // =========================================================

    private static boolean isNearestEnemyQuiteFar() {
        return nearestEnemy == null || PositionUtil.distanceTo(nearestEnemy, unit) > 12.2;
    }
    
}
