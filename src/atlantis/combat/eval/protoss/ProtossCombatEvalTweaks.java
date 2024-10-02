package atlantis.combat.eval.protoss;

import atlantis.combat.squad.Squad;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

public class ProtossCombatEvalTweaks {

    /**
     * Basic eval isn't perfect. We need to be:
     * - way more cautious when crossing choke points,
     * - be more brave near main,
     * - be more defensive when just retreated
     */
    public static double apply(AUnit unit, double eval) {
        return eval
            + (unit.isMissionDefendOrSparta() ? 0 : (unit.distToNearestChokeLessThan(6) ? -0.5 : 0))
            + (unit.lastRetreatedAgo() <= 100 ? -0.25 : 0)
//            + (unit.lastStartedRunningLessThanAgo(30 * 4) ? 0.1 : 0)
            + (unit.distToMain() <= 20 ? +0.15 : 0)
            + (unit.lastUnderAttackLessThanAgo(30 * 4) ? -0.05 : 0)
            + cohesionPenalty(unit)
//            + combatBuildingPenalty(unit)
            + enemyZerglingBonus(unit);
    }

    // =========================================================

    private static double cohesionPenalty(AUnit unit) {
        Squad squad = unit.squad();
        if (squad == null) return 0;

        int norm = 70;
        int percentBelowNorm = squad.cohesionPercent() - norm;

        if (percentBelowNorm >= 0) return 0;

        return percentBelowNorm / 100.0;
    }

    /**
     * Lower value of enemy zerglings.
     */
    private static double enemyZerglingBonus(AUnit unit) {
        if (unit.friendsNear().inRadius(3, unit).atMost(1)) return 0;

        return unit.enemiesNear().inRadius(8, unit).ofType(AUnitType.Zerg_Zergling).count() * 0.3;
    }
}
