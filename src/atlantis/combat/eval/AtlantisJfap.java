package atlantis.combat.eval;

import atlantis.combat.eval.tweaks.AtlantisApplyJfapTweaks;
import atlantis.units.AUnit;
import atlantis.units.fogged.AbstractFoggedUnit;
import jfap.JfapCombatEvaluator;
import tests.fakes.FakeUnit;

/**
 * Fap fap fap.
 */
public class AtlantisJfap {
    public static final int NUM_OF_FRAMES_TO_SIMULATE = 60;
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
//        System.out.println("We = " + unit.typeWithUnitId()
//            + " / hp=" + unit.hp()
//            + " / f_hp=" + unit.friendsNear().totalHp()
//            + " / e_hp= " + unit.enemiesNear().totalHp() + "(" + unit.enemiesNear().size() + ")");
//        unit.friendsNear().print("friends");
//
//        unit.cache.clear();
//        if (true) return 0.1 + unit.hp() + unit.friendsNear().totalHp() - unit.enemiesNear().totalHp();

        scores = jfapEval();

//        System.err.println("--- " + unit + " ---");
//        System.err.println("This score before = " + scores[0]);
//        System.err.println("Enemy score before = " + scores[1]);
        scores = (new AtlantisApplyJfapTweaks(this, unit)).applyTweaks();
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

    public static boolean isValidUnit(AUnit unit) {
        return unit.notImmobilized()
            && unit.hasPosition()
            && unit.isCompleted()
            && !unit.isOverlord()
            && (
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

//            System.out.println("For unit = " + unit);
//            System.out.println("ourScore = " + ourScore);
//            System.out.println("enemyScore = " + enemyScore);

            double ratio = enemyScore / (ourScore + 0.001);

            return ratio;
        }

        return ourScore;
    }
}
