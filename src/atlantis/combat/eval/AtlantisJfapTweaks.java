package atlantis.combat.eval;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Selection;

public class AtlantisJfapTweaks {

    private static final int HYDRA_SINGLE_UNIT_BONUS = 160;

    protected static double forHydralisks(double score, AUnit unit) {
        Selection friendlyHydras = unit.friendsNear().add(unit).ofType(AUnitType.Zerg_Hydralisk);
        int hydraBonus = friendlyHydras.count() * HYDRA_SINGLE_UNIT_BONUS;
        score += hydraBonus;

        return score;
    }
}