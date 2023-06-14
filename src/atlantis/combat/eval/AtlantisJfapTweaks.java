package atlantis.combat.eval;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Selection;

public class AtlantisJfapTweaks {

    private static final int HYDRA_SINGLE_UNIT_BONUS = 80;

    protected static double forHydralisks(double score, AUnit unit) {
//        Selection enemies = atlantisJfap.unit.enemiesNear();
//        Selection hydras = enemies.ofType(AUnitType.Zerg_Hydralisk);
//
//        if (hydras.empty()) {
//            return atlantisJfap.scores;
//        }
//
////        double hydras = (double) hydras.count() / enemies.combatUnits().count();
////        double hydras = (double) hydras.count() / enemies.combatUnits().count();
////        double enemyBonus = 1 + HYDRA_SINGLE_UNIT_BONUS * hydras;
//        double enemyBonus = HYDRA_SINGLE_UNIT_BONUS * hydras.count();
//
//        atlantisJfap.scores[1] += enemyBonus;
////        atlantisJfap.scores[1] = applyScoresModifier(atlantisJfap.scores, enemyBonus);

        Selection friendlyHydras = unit.friendsNear().add(unit).ofType(AUnitType.Zerg_Hydralisk);
        int hydraBonus = friendlyHydras.count() * HYDRA_SINGLE_UNIT_BONUS;
        score += hydraBonus;

        return score;
    }
}