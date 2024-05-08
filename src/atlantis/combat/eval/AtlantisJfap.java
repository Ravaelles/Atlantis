package atlantis.combat.eval;

import atlantis.units.AUnit;
import atlantis.units.fogged.AbstractFoggedUnit;
import jfap.JfapCombatEvaluator;
import tests.fakes.FakeUnit;

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
            return new double[]{9874, -9874};
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


//                return new double[] { myEval, enemyEval };
//            }
//        }

        double ourScore = scores[0];
        double enemyScore = scores[1];

        return calculateToRelativeScoreIfNeeded(ourScore, enemyScore, relativeToEnemy);
    }

    private double[] applyTweaks() {
        int forUs = 0;
        int forThem = 1;

        // =========================================================

        double oldForUs = scores[forUs];
        scores[forUs] = AtlantisJfapTweaks.forHydralisks(scores[forUs], unit);
        double deltaForUs = scores[forUs] - oldForUs;

        AUnit enemy = unit.enemiesNear().nearestTo(unit);
        double deltaForThem = 0;
        if (enemy != null) {
            double oldForThem = scores[forThem];

            scores[forThem] = AtlantisJfapTweaks.forHydralisks(scores[forThem], enemy);
            deltaForThem = scores[forThem] - oldForThem;

        }

        // === Apply bi-directional tweaks relative to enemy =======

        scores[forUs] -= deltaForThem;
        scores[forThem] -= deltaForUs;

        // =========================================================

        // Prevent positive AJFAP value
        if (scores[forUs] > -1) {
//            System.err.println("Prevent positive AJFAP value");
            scores[forUs] = -1;
        }

        // Prevent positive AJFAP value
        if (scores[forThem] > -1) {
//            System.err.println("Prevent positive AJFAP value");
            scores[forThem] = -1;
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


            if (enemyScore < -9000) {
                return Math.abs(enemyScore);
            }

            return enemyScore / (ourScore + 0.001);
        }

        return ourScore;
    }
}
