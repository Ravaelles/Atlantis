package atlantis.combat.eval;

import atlantis.units.AUnit;
import atlantis.units.Units;
import atlantis.units.select.Select;
import atlantis.util.Cache;

import java.util.HashMap;
import java.util.Map;


public class ACombatEvaluator {

    private static double PERCENT_ADVANTAGE_NEEDED_TO_FIGHT = 7;
    private static double PERCENT_ADVANTAGE_NEEDED_TO_FIGHT_IF_COMBAT_BUILDINGS = 20;

    /** Maximum allowed value as a result of evaluation. */
    private static final double MAX_VALUE = 9876;

    private static Cache<Object> cache = new Cache<>();

    // =========================================================

    /**
     * Returns <b>TRUE</b> if our <b>unit</b> should engage in combat with nearby units or
     * <b>FALSE</b> if enemy is too strong and we should pull back.
     * (retreat otherwise). If false then it means we would engage in new fight, so make sure you've got
     * some safe margin. This feature avoids fighting and immediately running away and fighting again.
     */
    public static boolean isSituationFavorable(AUnit unit) {
        AUnit nearestEnemy = unit.enemiesNearby().canAttack(unit, 4).nearestTo(unit);
        if (nearestEnemy == null || unit.distTo(nearestEnemy) >= 15) {
            return true;
        }

        return (absoluteEvaluation(unit) * (100 - percentOfAdvantageNeeded(unit)) / 100) >= absoluteEvaluation(nearestEnemy);
    }

    /**
     * Calculated per squad, not per unit.
     * 1.6 means ~60% advantage of unit's squad against nearby enemies
     * 1.0 means same strength
     * 0.8 means ~20% disadvantage against enemies
     */
    public static double relativeAdvantage(AUnit unit) {
        AUnit nearestEnemy = unit.enemiesNearby().canAttack(unit, 4).nearestTo(unit);
        if (nearestEnemy == null || unit.distTo(nearestEnemy) >= 15) {
            return MAX_VALUE;
        }

        return absoluteEvaluation(unit) / absoluteEvaluation(nearestEnemy);
    }

    /**
     * Calculated per squad, not per unit.
     * More equals given squad is considered to be stronger.
     */
    public static double absoluteEvaluation(AUnit unit) {
        return evaluateSituation(unit, false);
    }

    /**
     * 
     * Returns <b>POSITIVE</b> value if our unit <b>unit</b> should engage in combat with nearby units or
     * <b>NEGATIVE</b> when enemy is too strong and we should pull back.
     * 
     * When absolute value is true, it returns the evaluation value 
     * (like 3564, more equals higher combat strength).
     */
    private static double evaluateSituation(AUnit unit, boolean relativeToEnemy) {
        return (double) cache.get(
            "evaluateSituation:" + unit.id() + "," + relativeToEnemy,
            3,
            () -> {
//                System.err.println(unit.nameWithId() + " // our:" + unit.isOur() + " // enemy:" + unit.isEnemy());

                // =========================================================
                // Define nearby enemy and our units

                Units opposingUnits = opposingUnits(unit);

                if (opposingUnits.isEmpty()) {
                    return MAX_VALUE;
                }

//                if (unit.isOur() && againstUnits.isNotEmpty()) {
//                    System.out.println(" // " + againstUnits.size());
//                    for (AUnit against : againstUnits.list()) {
//                        System.out.println("   " + against);
//                    }
//                }

                Units theseUnits = theseUnits(unit);

//                theseUnits.print("THESE");
//                opposingUnits.print("OPPOSING");

                // =========================================================
                // Evaluate our and enemy strength

                double againstUnitsEvaluation = Evaluate.evaluateUnitsAgainstUnit(opposingUnits, theseUnits, true);
                double theseUnitsEvaluation = Evaluate.evaluateUnitsAgainstUnit(theseUnits, opposingUnits, false);

//                if (unit.isOur()) {
//                    System.err.println("theseUnitsEvaluation = " + unit + " // " + + theseUnitsEvaluation);
//                    System.err.println("againstUnitsEvaluation = " + againstUnitsEvaluation);
//                }

                // =========================================================

                // Return non-relative absolute value
                if (!relativeToEnemy) {
                    return theseUnitsEvaluation;
                }

                // Return relative value compared to local enemy strength
                else {
//                    System.err.println("Eval for " + unit + " // " + theseUnitsEvaluation + " // " + againstUnitsEvaluation);
//                    System.err.println(theseUnitsEvaluation / againstUnitsEvaluation);
                    return (theseUnitsEvaluation / againstUnitsEvaluation);
                }
            }
        );
    }

    // =========================================================

    private static double percentOfAdvantageNeeded(AUnit unit) {
        if (unit.enemiesNearby().combatBuildings(false).inRadius(8.2, unit).isNotEmpty()) {
            return PERCENT_ADVANTAGE_NEEDED_TO_FIGHT_IF_COMBAT_BUILDINGS;
        }

        return PERCENT_ADVANTAGE_NEEDED_TO_FIGHT;
    }

    private static Units theseUnits(AUnit unit) {
        Units theseUnits;
        double rangedRadius = 13;
        double meleeRadius = 4.5;

        // Our eval
        if (unit.isOur()) {
            theseUnits = Select.ourCombatUnits().ranged().inRadius(rangedRadius, unit).units();
            theseUnits.addAll(Select.ourCombatUnits().melee().inRadius(meleeRadius, unit).list());
        }

        // Enemy eval
        else {
            theseUnits = Select.enemyCombatUnits().ranged().inRadius(rangedRadius, unit).units();
            theseUnits.addAll(Select.ourCombatUnits().melee().inRadius(meleeRadius, unit).list());
        }

        return theseUnits;
    }

    public static Units opposingUnits(AUnit unit) {
        // Ranged
        Units againstUnits = unit.enemiesNearby().ranged()
                .canAttack(unit, 4)
                .units();

        // Melee
        againstUnits.addAll(
                unit.enemiesNearby().melee()
                        .inRadius(6, unit)
                        .canAttack(unit, 6)
                        .listUnits()
        );

        return againstUnits;
    }

    public static boolean advantagePercent(AUnit unit, int percentOfAdvantageRelativeToEnemyStrength) {
        return evaluateSituation(unit, true) >= (1 + percentOfAdvantageRelativeToEnemyStrength / 100.0);
    }

    // =========================================================
    // Safety margin
    
//    private static double calculateFavorableValueThreshold(boolean isPendingFight) {
////        return (isPendingFight ? SAFETY_MARGIN_RETREAT : SAFETY_MARGIN_ATTACK)
////                + Math.min(0.1, AGame.getTimeSeconds() / 3000);
//        return (isPendingFight ? SAFETY_MARGIN_RETREAT : SAFETY_MARGIN_ATTACK);
//    }

    // =========================================================
    // Auxiliary
    /**
     * Auxiliary string with colors.
     */
//    public static String getEvalString(AUnit unit, double forceValue) {
//        double eval = forceValue != 0 ? forceValue : evaluateSituation(unit);
//        if (eval >= MAX_VALUE) {
//            return "+";
//        } else {
//            String string = (eval < 0 ? "" : "+");
//
//            if (eval < 5) {
//                string += String.format("%.1f", eval);
//            }
//            else {
//                string += (int) eval;
//            }
//
//            if (eval < -0.05) {
//                string = ColorUtil.getColorString(Color.Red) + string;
//            } else if (eval < 0.05) {
//                string = ColorUtil.getColorString(Color.Yellow) + string;
//            } else {
//                string = ColorUtil.getColorString(Color.Green) + string;
//            }
//
//            return string;
//        }
//    }

    /**
     * Returns combat eval and caches it for the time of several frames.
     */
//    private static double updateCombatEval(AUnit unit, double combatEval) {
//        checkCombatInfo(unit);
//        combatInfo.get(unit).updateCombatEval(combatEval);
//        //unit.updateCombatEval(combatEval);
//        return combatEval;
//    }

    /**
     * Checks whether AtlantisCombatInformation exists for a given unit, creating an instance if necessary
     *
     * @param unit
     */
//    private static void checkCombatInfo(AUnit unit) {
//        if (!combatInfo.containsKey(unit)) {
//            combatInfo.put(unit, new ACombatInformation(unit));
//        }
//    }

}
