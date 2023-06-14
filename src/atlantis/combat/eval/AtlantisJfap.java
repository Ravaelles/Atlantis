package atlantis.combat.eval;

import atlantis.units.AUnit;
import jfap.JfapCombatEvaluator;

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

    public double evaluateCombatSituation() {
        scores = JfapCombatEvaluator.eval(unit, relativeToEnemy);

//        System.out.println("--- " + unit + " ---");
//        System.out.println("Our score before = " + scores[0]);
//        System.out.println("Enemy score before = " + scores[1]);
        scores = applyTweaks();
//        System.out.println("Our score AFTER = " + scores[0]);
//        System.out.println("Enemy score AFTER  = " + scores[1]);

        double ourScoreDiff = scores[0];
        double enemyScoreDiff = scores[1];

        return result(ourScoreDiff, enemyScoreDiff, relativeToEnemy);
    }

    private double[] applyTweaks() {
        scores[0] = AtlantisJfapTweaks.forHydralisks(scores[0], unit);
        scores[1] = AtlantisJfapTweaks.forHydralisks(scores[1], unit);

        if (scores[0] > 0) {
//            System.err.println("Prevent positive AJFAP value");
            scores[0] = -1;
        }
        if (scores[1] > 0) {
//            System.err.println("Prevent positive AJFAP value");
            scores[1] = -1;
        }

        return scores;
    }

    private double result(double ourScore, double enemyScore, boolean relativeToEnemy) {
        if (relativeToEnemy) {
            return enemyScore / (ourScore + 0.001);
        }

        return ourScore;
    }
}
