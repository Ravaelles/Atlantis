package atlantis.combat.eval.tweaks;

import atlantis.combat.eval.AtlantisJfap;
import atlantis.units.AUnit;

public class AtlantisApplyJfapTweaks {
    private final AtlantisJfap atlantisJfap;
    private final double[] scores;
    private final AUnit unit;

    public AtlantisApplyJfapTweaks(AtlantisJfap atlantisJfap, AUnit unit) {
        this.atlantisJfap = atlantisJfap;
        this.scores = atlantisJfap.scores;
        this.unit = unit;
    }

    // =========================================================

    public double[] applyTweaks() {
        int forUs = 0;
        int forThem = 1;

        // =========================================================

        double oldForUs = scores[forUs];
        scores[forUs] = JfapTweaksForSpecificUnits.applyAll(scores[forUs], unit);
        double deltaForUs = scores[forUs] - oldForUs;

        AUnit enemy = unit.enemiesNear().nearestTo(unit);
        double deltaForThem = 0;
        if (enemy != null) {
            double oldForThem = scores[forThem];

            scores[forThem] = JfapTweaksForSpecificUnits.applyAll(scores[forThem], enemy);
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
}