package atlantis.combat.eval;

import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Cache;


public class ACombatEvaluator {

    private static double RANGED_RADIUS = 15;
    private static double MELEE_RADIUS = 5.5;

    private static double PERCENT_ADVANTAGE_NEEDED_TO_FIGHT = 7;
    private static double PERCENT_ADVANTAGE_NEEDED_TO_FIGHT_IF_MISSION_ATTACK = -20;
    private static double PERCENT_ADVANTAGE_NEEDED_TO_FIGHT_IF_COMBAT_BUILDINGS = 20;

    /** Maximum allowed value as a result of evaluation. */
    private static final double MAX_VALUE = 9876;

    private static Cache<Object> cache = new Cache<>();

    // =========================================================

    public static void clearCache() {
        cache.clear();
    }

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

                Selection opposingUnits = opposingUnits(unit);

                if (opposingUnits.isEmpty()) {
                    return MAX_VALUE;
                }

//                if (unit.isOur() && againstUnits.isNotEmpty()) {
//                    System.out.println(" // " + againstUnits.size());
//                    for (AUnit against : againstUnits.list()) {
//                        System.out.println("   " + against);
//                    }
//                }

                Selection theseUnits = theseUnits(unit);

//                theseUnits.print("THESE");
//                opposingUnits.print("OPPOSING");

                // =========================================================
                // Evaluate our and enemy strength

                double theseUnitsEvaluation = Evaluate.evaluateUnitsAgainstUnit(
                        theseUnits.units(), opposingUnits.units(), false
                );
                double againstUnitsEvaluation = Evaluate.evaluateUnitsAgainstUnit(
                        opposingUnits.units(), theseUnits.units(), true
                );

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

        return unit.mission().isMissionAttack()
                ? PERCENT_ADVANTAGE_NEEDED_TO_FIGHT_IF_MISSION_ATTACK : PERCENT_ADVANTAGE_NEEDED_TO_FIGHT;
    }

    private static Selection theseUnits(AUnit unit) {
        Selection theseUnits;

        // Our eval
        if (unit.isOur()) {
            theseUnits = Select.ourCombatUnits().ranged().inRadius(RANGED_RADIUS, unit);
            theseUnits.add(Select.ourCombatUnits().melee().inRadius(MELEE_RADIUS, unit));
        }

        // Enemy eval
        else if (unit.isEnemy()) {
            theseUnits = Select.enemyCombatUnits().ranged().inRadius(RANGED_RADIUS, unit);
            theseUnits.add(Select.enemyCombatUnits().melee().inRadius(MELEE_RADIUS, unit));
            theseUnits.add(EnemyUnits.combatUnitsToBetterAvoid().havingPosition().inRadius(RANGED_RADIUS, unit));
            theseUnits.removeDuplicates();
        }

        else {
            throw new RuntimeException("Trying to combat eval neutral unit");
        }

        return theseUnits;
    }

    public static Selection opposingUnits(AUnit unit) {

        // Ranged
        Selection againstUnits = unit.enemiesNearby()
                .ranged()
                .canAttack(unit, 4);

        // Melee
        againstUnits.add(
                unit.enemiesNearby().melee()
                        .inRadius(6, unit)
                        .canAttack(unit, 6)
        );

        if (unit.isOur()) {
            againstUnits = againstUnits.add(EnemyUnits.combatUnitsToBetterAvoid()).removeDuplicates();
        }

        return againstUnits;
    }

    public static boolean advantagePercent(AUnit unit, int percentOfAdvantageRelativeToEnemyStrength) {
        return evaluateSituation(unit, true) >= (1 + percentOfAdvantageRelativeToEnemyStrength / 100.0);
    }

}
