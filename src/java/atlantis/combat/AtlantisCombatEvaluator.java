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
    private static double SAFETY_MARGIN = 0.11;

    // =========================================================
    
    /**
     * Returns <b>TRUE</b> if our unit <b>unit</b> should engage in combat with nearby units or
     * <b>FALSE</b> if enemy is too strong and we should pull back.
     */
    public static boolean isSituationFavorable(Unit unit) {
        Unit mainBase = SelectUnits.mainBase();
        if (mainBase != null) {
            if (mainBase.distanceTo(unit) < 15) {
                return true;
            }
        }
        
        return evaluateSituation(unit) >= calculateSafetyMarginOverTime();
    }

    /**
     * Returns <b>POSITIVE</b> value if our unit <b>unit</b> should engage in combat with nearby units or
     * <b>NEGATIVE</b> when enemy is too strong and we should pull back.
     */
    public static double evaluateSituation(Unit unit) {
        
        // =========================================================
        // Try using cached value
        
        double combatEvalCachedValueIfNotExpired = unit.getCombatEvalCachedValueIfNotExpired();
        if ((int) combatEvalCachedValueIfNotExpired != -123456) {
            return combatEvalCachedValueIfNotExpired;
        }
        
        // =========================================================
        // Define nearby enemy and our units
        
        Collection<Unit> enemyUnits = SelectUnits.enemy().combatUnits().inRadius(12, unit).list();
        if (enemyUnits.isEmpty()) {
            return +999;
        }
        Collection<Unit> ourUnits = SelectUnits.our().combatUnits().inRadius(10, unit).list();
        
        // =========================================================
        // Evaluate our and enemy strength

        double enemyEvaluation = evaluateUnitsAgainstUnit(enemyUnits, unit, true);
        double ourEvaluation = evaluateUnitsAgainstUnit(ourUnits, enemyUnits.iterator().next(), false);
        double eval = ourEvaluation / enemyEvaluation - 1;
        unit.updateCombatEval(eval);
        
        return eval;
    }
    
    // =========================================================
    // Safety margin
    
    private static double calculateSafetyMarginOverTime() {
        return SAFETY_MARGIN + Math.min(0.15, AtlantisGame.getTimeSeconds() / 3000);
    }

    // =========================================================
    
    private static double evaluateUnitsAgainstUnit(Collection<Unit> units, Unit againstUnit, boolean isEnemyEval) {
        double strength = 0;
        boolean enemyDefensiveBuildingFound = false;
        boolean enemyDefensiveBuildingInRange = false;
        
        // =========================================================
        
        for (Unit unit : units) {
            double unitStrengthEval = evaluateUnitHPandDamage(unit, againstUnit);
            
            // =========================================================
            // WORKER
            if (unit.isWorker()) {
                strength += 0.2 * unitStrengthEval;
            } 
            
            // =========================================================
            // BUILDING
            else if (unit.isBuilding() && unit.isCompleted()) {
                boolean antiGround = (againstUnit != null ? againstUnit.isGroundUnit() : true);
                boolean antiAir = (againstUnit != null ? againstUnit.isAirUnit() : true);
                if (unit.isMilitaryBuilding(antiGround, antiAir)) {
                    enemyDefensiveBuildingFound = true;
                    if (unit.isBunker()) {
                        strength += 7 * evaluateUnitHPandDamage(UnitType.UnitTypes.Terran_Marine, againstUnit);
                    }
                    else {
                        strength += 1.3 * unitStrengthEval;
                    }
                    
                    if (unit.distanceTo(againstUnit) <= 8.5) {
                        enemyDefensiveBuildingInRange = true;
                    }
                }
            } 
            
            // =========================================================
            // Ordinary MILITARY UNIT
            else {
                strength += unitStrengthEval;
            }
        }
        
        // =========================================================
        // Extra bonus for DEFENSIVE BUILDING PRESENCE
        if (!isEnemyEval) {
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
        double total = hp / 5 + damage;
        
        // =========================================================
        // Diminish role of NON-SHOOTING units
        if (damage == 0 && !evaluateType.isTerranInfantry()) {
            total /= 15;
        }
        
        // =========================================================
        // Diminish role of WORKERS
        if (evaluateType.isWorker()) {
            total /= 4;
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

}
