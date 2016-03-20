package atlantis.combat;

import atlantis.AtlantisGame;
import atlantis.wrappers.SelectUnits;
import java.util.Collection;
import jnibwapi.Unit;
import jnibwapi.types.UnitType;
import jnibwapi.types.WeaponType;
import jnibwapi.util.BWColor;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class AtlantisCombatEvaluator {

    /**
     * Fight only if our army is locally stronger X% than enemy army. 0.5 = 50%.
     */
    private static double SAFETY_MARGIN = 0.03;
    
    /**
     * Multiplier for hit points factor when evaluating unit's combat value.
     */
    private static double EVAL_HIT_POINTS_FACTOR = 0.2;
    
    /**
     * Multiplier for damage factor when evaluating unit's combat value.
     */
    private static double EVAL_DAMAGE_FACTOR = 1.0;

    // =========================================================
    
    /**
     * Returns <b>TRUE</b> if our <b>unit</b> should engage in combat with nearby units or
     * <b>FALSE</b> if enemy is too strong and we should pull back.
     */
    public static boolean isSituationFavorable(Unit unit) {
        Unit nearestEnemy = SelectUnits.enemy().nearestTo(unit);
        
        if (AtlantisCombatEvaluatorExtraConditions.shouldAlwaysFight(unit, nearestEnemy)) {
            return true;
        }
        
        if (AtlantisCombatEvaluatorExtraConditions.shouldAlwaysRetreat(unit, nearestEnemy)) {
            return false;
        }
        
        return evaluateSituation(unit) >= calculateSafetyMarginOverTime();
    }
    
    /**
     * Returns <b>TRUE</b> if our <b>unit</b> has overwhelmingly high chances to win nearby fight and 
     * should engage in combat with nearby enemy units. Returns
     * <b>FALSE</b> if enemy is too strong and we should pull back.
     */
    public static boolean isSituationExtremelyFavorable(Unit unit) {
        Unit nearestEnemy = SelectUnits.enemy().nearestTo(unit);
        
        if (AtlantisCombatEvaluatorExtraConditions.shouldAlwaysRetreat(unit, nearestEnemy)) {
            return false;
        }
        
        return evaluateSituation(unit) >= calculateSafetyMarginOverTime() + 0.5;
    }

    /**
     * Returns <b>POSITIVE</b> value if our unit <b>unit</b> should engage in combat with nearby units or
     * <b>NEGATIVE</b> when enemy is too strong and we should pull back.
     */
    public static double evaluateSituation(Unit unit) {
        
        // Try using cached value
        double combatEvalCachedValueIfNotExpired = unit.getCombatEvalCachedValueIfNotExpired();
        if (combatEvalCachedValueIfNotExpired > -12345) {
            return updateCombatEval(unit, combatEvalCachedValueIfNotExpired);
        }
        
        // =========================================================
        // Define nearby enemy and our units
        
        Collection<Unit> enemyUnits = SelectUnits.enemy().combatUnits().inRadius(12, unit).list();
        if (enemyUnits.isEmpty()) {
            return updateCombatEval(unit, +999);
        }
        Collection<Unit> ourUnits = SelectUnits.our().combatUnits().inRadius(8.5, unit).list();
        
        // =========================================================
        // Evaluate our and enemy strength

        double enemyEvaluation = evaluateUnitsAgainstUnit(enemyUnits, unit, true);
        double ourEvaluation = evaluateUnitsAgainstUnit(ourUnits, enemyUnits.iterator().next(), false);
        double lowHealthPenalty = (100 - unit.getHPPercent()) / 80;
        double combatEval = ourEvaluation / enemyEvaluation - 1 - lowHealthPenalty;
        
        return updateCombatEval(unit, combatEval);
    }
    
    // =========================================================
    // Safety margin
    
    private static double calculateSafetyMarginOverTime() {
        return SAFETY_MARGIN + Math.min(0.1, AtlantisGame.getTimeSeconds() / 3000);
    }

    // =========================================================
    
    private static double evaluateUnitsAgainstUnit(Collection<Unit> units, Unit againstUnit, boolean isEvalForEnemy) {
        double strength = 0;
        boolean enemyDefensiveBuildingFound = false;
        boolean enemyDefensiveBuildingInRange = false;
        
        // =========================================================
        
        for (Unit otherUnit : units) {
            double unitStrengthEval = evaluateUnitHPandDamage(otherUnit, againstUnit);
            
            // WORKER
            if (otherUnit.isWorker()) {
                strength += 0.2 * unitStrengthEval;
            } 
            
            // =========================================================
            // BUILDING
            else if (otherUnit.isBuilding() && otherUnit.isCompleted()) {
                boolean antiGround = (againstUnit != null ? againstUnit.isGroundUnit() : true);
                boolean antiAir = (againstUnit != null ? againstUnit.isAirUnit() : true);
                if (otherUnit.isMilitaryBuilding(antiGround, antiAir)) {
                    enemyDefensiveBuildingFound = true;
                    if (otherUnit.isBunker()) {
                        strength += 7 * evaluateUnitHPandDamage(UnitType.UnitTypes.Terran_Marine, againstUnit);
                    }
                    else {
                        strength += 1.3 * unitStrengthEval;
                    }
                    
                    if (otherUnit.distanceTo(againstUnit) <= 8.5) {
                        enemyDefensiveBuildingInRange = true;
                    }
                }
            } 
            
            // =========================================================
            // Ordinary MILITARY UNIT
            else {
                if (otherUnit.canAttackThisKindOfUnit(againstUnit, false)) {
                    strength += unitStrengthEval;
                }
            }
        }
        
        // =========================================================
        // Extra bonus for DEFENSIVE BUILDING PRESENCE
        if (!isEvalForEnemy) {
            if (enemyDefensiveBuildingFound) {
                strength += 100;
            }
            if (enemyDefensiveBuildingInRange) {
                strength += 100;
            }
        }
        
        return strength;
    }
    
    // =========================================================

    private static double evaluateUnitHPandDamage(Unit evaluate, Unit againstUnit) {
        return evaluateUnitHPandDamage(evaluate.getType(), evaluate.getHP(), againstUnit);
    }

    private static double evaluateUnitHPandDamage(UnitType evaluate, Unit againstUnit) {
//        System.out.println(evaluate.getType() + " damage: " + evaluate.getType().getGroundWeapon().getDamageNormalized());
        return evaluateUnitHPandDamage(evaluate, evaluate.getMaxHitPoints(), againstUnit);
    }

    private static double evaluateUnitHPandDamage(UnitType evaluateType, int hp, Unit againstUnit) {
        double damage = (againstUnit.isGroundUnit() ? 
                evaluateType.getGroundWeapon().getDamageNormalized() : 
                evaluateType.getAirWeapon().getDamageNormalized());
        double total = hp * EVAL_HIT_POINTS_FACTOR + damage * EVAL_DAMAGE_FACTOR;
        
        // =========================================================
        // Deminish role of NON-SHOOTING units
        if (damage == 0 && !evaluateType.isMedic()) {
            total /= 15;
        }
        
        return total;
    }

    // =========================================================
    // Auxiliary
    
    /**
     * Auxiliary string with colors.
     */
    public static String getEvalString(Unit unit) {
        double eval = evaluateSituation(unit);
        if (eval > 998) {
            return "";
        } else {
            String string = (eval < 0 ? "" : "+") + String.format("%.1f", eval);

            if (eval < -0.05) {
                string = BWColor.getColorString(BWColor.Red) + string;
            } else if (eval < 0.05) {
                string = BWColor.getColorString(BWColor.Yellow) + string;
            } else {
                string = BWColor.getColorString(BWColor.Green) + string;
            }

            return string;
        }
    }

    /**
     * Returns combat eval and caches it for the time of several frames.
     */
    private static double updateCombatEval(Unit unit, double combatEval) {
        unit.updateCombatEval(combatEval);
        return combatEval;
    }

}
