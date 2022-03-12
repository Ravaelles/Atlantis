package atlantis.combat.eval;

import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;
import atlantis.util.cache.Cache;


public class ACombatEvaluator {

    private static double RANGED_RADIUS = 15;
    private static double MELEE_RADIUS = 5.5;

    private static double PERCENT_ADVANTAGE_NEEDED_TO_FIGHT = 7;
    private static double PERCENT_ADVANTAGE_NEEDED_TO_FIGHT_IF_MISSION_ATTACK = -20;
    private static double PERCENT_ADVANTAGE_NEEDED_TO_FIGHT_IF_COMBAT_BUILDINGS = 20;

    /** Maximum allowed value as a result of evaluation. */
    private static final double MAX_VALUE = 9898;

    private static Cache<Object> cache = new Cache<>();

    // =========================================================

    public static void clearCache() {
        cache.clear();
    }

    /**
     * Returns <b>TRUE</b> if our <b>unit</b> should engage in combat with Near units or
     * <b>FALSE</b> if enemy is too strong and we should pull back.
     * (retreat otherwise). If false then it means we would engage in new fight, so make sure you've got
     * some safe margin. This feature avoids fighting and immediately running away and fighting again.
     */
    public static boolean isSituationFavorable(AUnit unit) {
        AUnit nearestEnemy = unit.enemiesNear().canAttack(unit, 4).nearestTo(unit);
        if (nearestEnemy == null || unit.distTo(nearestEnemy) >= 15) {
            return true;
        }

        return (absoluteEvaluation(unit) * (100 - percentOfAdvantageNeeded(unit)) / 100) >= absoluteEvaluation(nearestEnemy);
    }

    /**
     * Calculated per squad, not per unit.
     * 1.6 means ~60% advantage of unit's squad against Near enemies
     * 1.0 means same strength
     * 0.8 means ~20% disadvantage against enemies
     */
    public static double relativeAdvantage(AUnit unit) {
        AUnit nearestEnemy = unit.enemiesNear().canAttack(unit, 4.2).nearestTo(unit);
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
     * Returns <b>POSITIVE</b> value if our unit <b>unit</b> should engage in combat with Near units or
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
                // Define Near enemy and our units

                Selection opposingUnits = opposingUnits(unit);
//                opposingUnits.print("OPPOSING (for " + unit + ")");

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

//                System.err.println("------ theseUnitsEvaluation --------");
                double theseUnitsEvaluation = Evaluate.evaluateUnitsAgainstUnit(
                        theseUnits.units(), opposingUnits.units(), false
                );
//                System.err.println("------ againstUnitsEvaluation --------");
                double againstUnitsEvaluation = Evaluate.evaluateUnitsAgainstUnit(
                        opposingUnits.units(), theseUnits.units(), true
                );

//                if (unit.isOur()) {
//                    System.err.println("theseUnitsEvaluation = " + unit + " // " + + theseUnitsEvaluation);
//                    System.err.println("againstUnitsEvaluation = " + againstUnitsEvaluation);
//                }
//                System.err.println("These: " + theseUnitsEvaluation + " / Against: " + againstUnitsEvaluation);

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
        if (unit.enemiesNear().combatBuildings(false).inRadius(8.2, unit).isNotEmpty()) {
            return PERCENT_ADVANTAGE_NEEDED_TO_FIGHT_IF_COMBAT_BUILDINGS;
        }

        return (unit.mission() != null && unit.mission().isMissionAttack())
                ? PERCENT_ADVANTAGE_NEEDED_TO_FIGHT_IF_MISSION_ATTACK : PERCENT_ADVANTAGE_NEEDED_TO_FIGHT;
    }

    private static Selection theseUnits(AUnit unit) {
        Selection theseUnits;

        // Our eval
        if (unit.isOur()) {
//            System.out.println("unit = " + unit);
            theseUnits = unit.ourCombatUnitsNear(false).ranged().inRadius(RANGED_RADIUS, unit)
                .add(unit.ourCombatUnitsNear(false).melee().inRadius(MELEE_RADIUS, unit));
        }

        // Enemy eval
        else if (unit.isEnemy()) {
            theseUnits = EnemyUnits.discovered().ranged().inRadius(RANGED_RADIUS, unit)
                .add(EnemyUnits.discovered().melee().inRadius(MELEE_RADIUS, unit))
                .removeDuplicates();
        }

        else {
            throw new RuntimeException("Trying to combat eval neutral unit");
        }

        return theseUnits;
    }

    public static Selection opposingUnits(AUnit unit) {

        // Ranged
        Selection againstUnits = unit.enemiesNear()
                .ranged()
                .canAttack(unit, 6);

//        unit.enemiesNear().print("againstUnits");
//        System.out.println("againstUnits A1 = " + unit.enemiesNear().size());
//        System.out.println("againstUnits A2 = " + unit.enemiesNear().ranged().size());
//        System.out.println("againstUnits A3 = " + unit.enemiesNear().ranged().canAttack(unit, 4).size());
//        System.out.println("againstUnits A = " + againstUnits.size());

        // Melee
        againstUnits = againstUnits.add(
                unit.enemiesNear().melee()
                        .inRadius(5, unit)
                        .canAttack(unit, false, true, 5)
        );

//        System.out.println("againstUnits B1 = " + unit.enemiesNear().size());
//        System.out.println("againstUnits B2 = " + unit.enemiesNear().melee().size());
//        System.out.println("againstUnits B3 = " + unit.enemiesNear().melee().inRadius(5, unit).size());
//        System.out.println("againstUnits B4 = " + unit.enemiesNear().melee().inRadius(5, unit).canAttack(unit, false, true, 5).count());
//        System.out.println("againstUnits B = " + againstUnits.size());

//        if (unit.isOur()) {
//            againstUnits = againstUnits.add(EnemyUnits.combatUnitsToBetterAvoid()).removeDuplicates();
//        }
//        System.out.println("againstUnits C1 = " + againstUnits.size());

        againstUnits = againstUnits.removeDuplicates();

//        System.out.println("againstUnits C2 = " + againstUnits.size());

        return againstUnits;
    }

    public static boolean advantagePercent(AUnit unit, int percentOfAdvantageRelativeToEnemyStrength) {
        return evaluateSituation(unit, true) >= (1 + percentOfAdvantageRelativeToEnemyStrength / 100.0);
    }

}
