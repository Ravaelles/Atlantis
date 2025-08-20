package atlantis.combat.eval.tweaks;

import atlantis.combat.squad.Squad;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

public class ProtossJfapTweaksConsiderChokesEtc {
    private static double rawEval;

    /**
     * Basic eval isn't perfect. We need to be:
     * - way more cautious when crossing choke points,
     * - be more brave near main,
     * - be more defensive when just retreated
     */
    public static double apply(AUnit unit, double eval) {
        rawEval = eval;

        return eval
            + EvalChokeTweaks.nearChokePenalty(unit)
            + penaltyCloseToEnemyBuildings(unit)
            + (unit.lastRetreatedAgo() <= 150 ? -0.4 : 0)
//            + (unit.lastStartedRunningLessThanAgo(30 * 4) ? 0.1 : 0)
            + (unit.distToMain() <= 8 ? +0.15 : 0)
            + (unit.lastUnderAttackLessThanAgo(30 * 4) ? -0.05 : 0)
            + cohesionPenalty(unit)
            + enemyZerglingBonus(unit)
            + combatBuildingPenalty(unit);
    }

    // =========================================================

    private static double penaltyCloseToEnemyBuildings(AUnit unit) {
        if (unit.enemiesNear().buildings().notEmpty()) return -0.1;

        return 0;
    }

    private static double combatBuildingPenalty(AUnit unit) {
//        if (true) return 0;
        int combatBuildings = unit.enemiesNear().combatBuildingsAnti(unit).size();

        return combatBuildings == 0 ? 0 : Math.max(-0.3, -0.18 * combatBuildings);
    }

    private static double cohesionPenalty(AUnit unit) {
        Squad squad = unit.squad();
        if (squad == null || squad.size() <= 1) return 0;

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

        return unit.enemiesNear().inRadius(8, unit).ofType(AUnitType.Zerg_Zergling).count() * 0.04;
    }
}
