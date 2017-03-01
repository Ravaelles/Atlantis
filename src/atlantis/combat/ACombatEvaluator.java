package atlantis.combat;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.util.ColorUtil;
import atlantis.util.PositionUtil;
import atlantis.util.WeaponUtil;
import bwapi.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class ACombatEvaluator {
    
    /**
     * Fight only if our army is locally stronger X% than enemy army. 0.5 = 50%.
     */
    private static double SAFETY_MARGIN_ATTACK = 0.4;
    private static double SAFETY_MARGIN_RETREAT = -0.4;

    /**
     * Multiplier for hit points factor when evaluating unit's combat value.
     */
    private static double EVAL_HIT_POINTS_FACTOR = 0.3;

    /**
     * Multiplier for damage factor when evaluating unit's combat value.
     */
    private static double EVAL_DAMAGE_FACTOR = 1.0;

    /**
     * Maximum allowed value as a result of evaluation.
     */
    private static int MAX_VALUE = 999999999;

    /**
     * Stores the instances of AtlantisCombatInformation for each unit
     */
    private static Map<AUnit, ACombatInformation> combatInfo = new HashMap<>();

    // =========================================================
    /**
     * Returns <b>TRUE</b> if our <b>unit</b> should engage in combat with nearby units or
     * <b>FALSE</b> if enemy is too strong and we should pull back.
     * @param boolean isPendingFight if true then it will check if unit should continue fighting 
     * (retreat otherwise). If false then it means we would engage in new fight, so make sure you've got
     * some safe margin. This feature avoids fighting and immediately running away and fighting again.
     */
    public static boolean isSituationFavorable(AUnit unit, boolean isPendingFight) {
        AUnit nearestEnemy = Select.enemy().nearestTo(unit);
        if (nearestEnemy == null || unit.distanceTo(nearestEnemy) >= 15) {
            return true;
        }

        if (ACombatEvaluatorExtraConditions.shouldAlwaysFight(unit, nearestEnemy)) {
            return true;
        }

//        if (AtlantisCombatEvaluatorExtraConditions.shouldAlwaysRetreat(unit, nearestEnemy)) {
//            return false;
//        }

        return evaluateSituation(unit) >= calculateFavorableValueThreshold(isPendingFight);
    }

    /**
     * Returns <b>TRUE</b> if our <b>unit</b> has overwhelmingly high chances to win nearby fight and should
     * engage in combat with nearby enemy units. Returns
     * <b>FALSE</b> if enemy is too strong and we should pull back.
     */
    public static boolean isSituationExtremelyFavorable(AUnit unit, boolean isPendingFight) {
        AUnit nearestEnemy = Select.enemy().nearestTo(unit);

        if (ACombatEvaluatorExtraConditions.shouldAlwaysRetreat(unit, nearestEnemy)) {
            return false;
        }

        return evaluateSituation(unit) >= calculateFavorableValueThreshold(isPendingFight) + 0.5;
    }

    /**
     * Returns <b>POSITIVE</b> value if our unit <b>unit</b> should engage in combat with nearby units or
     * <b>NEGATIVE</b> when enemy is too strong and we should pull back.
     */
    public static double evaluateSituation(AUnit unit) {
        return evaluateSituation(unit, false, false);
    }

    /**
     * 
     * Returns <b>POSITIVE</b> value if our unit <b>unit</b> should engage in combat with nearby units or
     * <b>NEGATIVE</b> when enemy is too strong and we should pull back.
     * 
     * When absolute value is true, it returns the evaluation value 
     * (like 3564, more equals higher combat strength).
     */
    public static double evaluateSituation(AUnit unit, boolean returnAbsoluteValue, boolean calculateForEnemy) {
        checkCombatInfo(unit);

//        // Try using cached value
//        double combatEvalCachedValueIfNotExpired = combatInfo.get(unit).getCombatEvalCachedValueIfNotExpired();
//        if (combatEvalCachedValueIfNotExpired > -12345) {
//            return updateCombatEval(unit, combatEvalCachedValueIfNotExpired);
//        }

        // =========================================================
        // Define nearby enemy and our units
        //TODO: check safety of these casts
        Collection<AUnit> enemyUnits = (Collection<AUnit>) Select.enemy().combatUnits().inRadius(12, unit).listUnits();
        if (enemyUnits.isEmpty()) {
//            return updateCombatEval(unit, +999);
            return MAX_VALUE;
        }
        Collection<AUnit> ourUnits = (Collection<AUnit>) Select.our().combatUnits().inRadius(8.5, unit).listUnits();

        // =========================================================
        // Evaluate our and enemy strength
        double enemyEvaluation = evaluateUnitsAgainstUnit(enemyUnits, unit, true);
        double ourEvaluation = evaluateUnitsAgainstUnit(ourUnits, enemyUnits.iterator().next(), false);

        // Return non-relative absolute value
        if (returnAbsoluteValue) {
            if (calculateForEnemy) {
                return enemyEvaluation;
            }
            else {
                return ourEvaluation;
            }
        }
        
        // Return relative value compared to local enemy strength
        else {
            double lowHealthPenalty = (100 - unit.getHPPercent()) / 80;
            double combatEval = ourEvaluation / enemyEvaluation - 1 - lowHealthPenalty;

            return updateCombatEval(unit, combatEval);
        }
    }

    // =========================================================
    // Safety margin
    
    private static double calculateFavorableValueThreshold(boolean isPendingFight) {
//        return (isPendingFight ? SAFETY_MARGIN_RETREAT : SAFETY_MARGIN_ATTACK) 
//                + Math.min(0.1, AGame.getTimeSeconds() / 3000);
        return (isPendingFight ? SAFETY_MARGIN_RETREAT : SAFETY_MARGIN_ATTACK);
    }

    // =========================================================
    
    /**
     * Calculate total strength value of given units set. 
     * It's always evaluated from a perspective of particular unit, in this case <b>againstUnit</b>.
     * Also it makes sense to distguish before enemy evaluation (we will almost always understimate enemy
     * strength) or our own evaluation (we're likely to overestimate our strength).
     */
    private static double evaluateUnitsAgainstUnit(Collection<AUnit> units, AUnit againstUnit, boolean isEnemyEval) {
        double strength = 0;
        boolean enemyDefensiveBuildingFound = false;
        boolean enemyDefensiveBuildingInRange = false;

        // =========================================================
        for (AUnit unit : units) {
            double unitStrengthEval = evaluateUnitHPandDamage(unit, againstUnit);

            // =========================================================
            // WORKER
            if (unit.isWorker()) {
                strength += 0.2 * unitStrengthEval;
            } // =========================================================
            // BUILDING
            else if (unit.getType().isBuilding() && unit.isCompleted()) {
                boolean antiGround = (againstUnit != null ? !againstUnit.isAirUnit() : true);
                boolean antiAir = (againstUnit != null ? againstUnit.isAirUnit() : true);
                if (unit.getType().isMilitaryBuilding(antiGround, antiAir)) {
                    enemyDefensiveBuildingFound = true;
                    if (unit.getType().equals(AUnitType.Terran_Bunker)) {
                        strength += 7 * evaluateUnitHPandDamage(AUnitType.Terran_Marine, againstUnit);
                    } else {
                        strength += 1.3 * unitStrengthEval;
                    }

                    if (PositionUtil.distanceTo(unit, againstUnit) <= 8.5) {
                        enemyDefensiveBuildingInRange = true;
                    }
                }
            } // =========================================================
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
    
    private static double evaluateUnitHPandDamage(AUnit evaluate, AUnit againstUnit) {
        return evaluateUnitHPandDamage(evaluate.getType(), evaluate.getHitPoints(), againstUnit);
    }

    private static double evaluateUnitHPandDamage(AUnitType evaluate, AUnit againstUnit) {
//        System.out.println(evaluate.getType() + " damage: " + evaluate.getType().getGroundWeapon().getDamageNormalized());
        return evaluateUnitHPandDamage(evaluate, evaluate.getMaxHitPoints(), againstUnit);
    }

    private static double evaluateUnitHPandDamage(AUnitType evaluateType, int hp, AUnit againstUnit) {
        if (evaluateType.isSpiderMine() || evaluateType.isNeutralType() || evaluateType.isInvincible()) {
            return 0;
        }
        
        // =========================================================
        
        double damage = ( againstUnit.isAirUnit()
            ? WeaponUtil.getDamageNormalized(evaluateType.getAirWeapon())
            : WeaponUtil.getDamageNormalized(evaluateType.getGroundWeapon())
        );
        double total = hp * EVAL_HIT_POINTS_FACTOR + damage * EVAL_DAMAGE_FACTOR;

        // =========================================================
        // Diminish role of NON-SHOOTING units
        if (damage == 0 && !evaluateType.equals(AUnitType.Terran_Medic)) {
            total /= 15;
        }

        return total;
    }

    // =========================================================
    // Auxiliary
    /**
     * Auxiliary string with colors.
     */
    public static String getEvalString(AUnit unit, double forceValue) {
        double eval = forceValue != 0 ? forceValue : evaluateSituation(unit);
        if (eval >= MAX_VALUE) {
            return "+";
        } else {
            String string = (eval < 0 ? "" : "+");
            
            if (eval < 5) {
                string += String.format("%.1f", eval);
            }
            else {
                string += (int) eval;
            }

            if (eval < -0.05) {
                string = ColorUtil.getColorString(Color.Red) + string;
            } else if (eval < 0.05) {
                string = ColorUtil.getColorString(Color.Yellow) + string;
            } else {
                string = ColorUtil.getColorString(Color.Green) + string;
            }

            return string;
        }
    }

    /**
     * Returns combat eval and caches it for the time of several frames.
     */
    private static double updateCombatEval(AUnit unit, double combatEval) {
        checkCombatInfo(unit);
        combatInfo.get(unit).updateCombatEval(combatEval);
        //unit.updateCombatEval(combatEval);
        return combatEval;
    }

    /**
     * Checks whether AtlantisCombatInformation exists for a given unit, creating an instance if necessary
     *
     * @param unit
     */
    private static void checkCombatInfo(AUnit unit) {
        if (!combatInfo.containsKey(unit)) {
            combatInfo.put(unit, new ACombatInformation(unit));
        }
    }

}
