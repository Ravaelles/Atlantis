package atlantis.combat.eval;

import atlantis.units.AUnit;
import atlantis.units.fogged.AbstractFoggedUnit;
import jfap.JfapCombatEvaluator;
import tests.unit.FakeUnit;

/**
 * Fap fap fap.
 */
public class AtlantisJfap {

    public AUnit unit;
    public boolean relativeToEnemy;
    public double[] scores;

    public AtlantisJfap(AUnit unit, boolean relativeToEnemy) {
        this.unit = unit;
        this.relativeToEnemy = relativeToEnemy;
    }

    private double[] jfapEval() {
        if (noEnemiesNear()) {
            return new double[] { 9874, -9874 };
        }

        return JfapCombatEvaluator.eval(unit);
    }

    public double evaluateCombatSituation() {
        scores = jfapEval();

//        System.err.println("--- " + unit + " ---");
//        System.err.println("This score before = " + scores[0]);
//        System.err.println("Enemy score before = " + scores[1]);
        scores = applyTweaks();
//        System.err.println("This score AFTER = " + scores[0]);
//        System.err.println("Other score AFTER  = " + scores[1]);

//        if (relativeToEnemy) {
//                double myEval = Math.abs((double) myScoreDiff / enemyScoreDiff);
//                double enemyEval = Math.abs((double) enemyScoreDiff / myScoreDiff);
//                System.out.println("myEval = " + myEval);
//                System.out.println("enemyEval = " + enemyEval);
//                return new double[] { myEval, enemyEval };
//            }
//        }

        double ourScore = scores[0];
        double enemyScore = scores[1];

        return calculateToRelativeScoreIfNeeded(ourScore, enemyScore, relativeToEnemy);
    }

    private double[] applyTweaks() {
//        double oldScore0 = scores[0];
//        double oldScore1 = scores[1];
//
//        double newScore0 = AtlantisJfapTweaks.forHydralisks(scores[0], unit);

        scores[0] = AtlantisJfapTweaks.forHydralisks(scores[0], unit);

        AUnit enemy = unit.enemiesNear().nearestTo(unit);
        if (enemy != null) {
            scores[1] = AtlantisJfapTweaks.forHydralisks(scores[1], enemy);
        }

        if (scores[0] > -1) {
//            System.err.println("Prevent positive AJFAP value");
            scores[0] = -1;
        }
        if (scores[1] > -1) {
//            System.err.println("Prevent positive AJFAP value");
            scores[1] = -1;
        }

        return scores;
    }

    public static boolean isValidUnit(AUnit unit) {
        return unit.notImmobilized() && unit.hasPosition() && (
            unit.u() != null || unit instanceof FakeUnit || unit instanceof AbstractFoggedUnit
        );
    }

    protected boolean noEnemiesNear() {
        return unit.enemiesNear().notImmobilized().havingWeapon().empty();
    }

    private double calculateToRelativeScoreIfNeeded(double ourScore, double enemyScore, boolean relativeToEnemy) {
        if (relativeToEnemy) {
//            System.out.println("\n===== " + unit);
//            System.out.println("ourScore = " + ourScore);
//            System.out.println("enemyScore = " + enemyScore);
            if (enemyScore < -9000) {
                return Math.abs(enemyScore);
            }

            return enemyScore / (ourScore + 0.001);
        }

        return ourScore;
    }
}
