package atlantis.combat.eval.protoss;

import atlantis.combat.squad.Squad;
import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.information.generic.Army;
import atlantis.map.choke.AChoke;
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
            + nearChokePenalty(unit)
            + (unit.lastRetreatedAgo() <= 100 ? -0.25 : 0)
//            + (unit.lastStartedRunningLessThanAgo(30 * 4) ? 0.1 : 0)
            + (unit.distToMain() <= 8 ? +0.15 : 0)
            + (unit.lastUnderAttackLessThanAgo(30 * 4) ? -0.05 : 0)
            + cohesionPenalty(unit)
            + enemyZerglingBonus(unit)
            + combatBuildingPenalty(unit);
    }

    // =========================================================

    private static double combatBuildingPenalty(AUnit unit) {
//        if (true) return 0;
        int combatBuildings = unit.enemiesNear().combatBuildingsAnti(unit).size();

        return combatBuildings == 0 ? 0 : Math.max(-0.3, -0.18 * combatBuildings);
    }

    private static double nearChokePenalty(AUnit unit) {
        AUnit enemy = unit.enemiesNear().combatUnits().groundUnits().nearestTo(unit);
        if (enemy == null) return 0;

        AChoke choke = unit.nearestChoke();
        if (choke == null) return 0;

        double enemyDistToChoke = enemy.distTo(choke);
        if (enemyDistToChoke >= unit.distTo(choke) || enemyDistToChoke >= unit.distTo(enemy)) return 0;

        return unit.distToNearestChokeCenter() <= 8 ? chokePenalty(unit) : 0;
    }

    private static double chokePenalty(AUnit unit) {
        boolean missionDefendOrSparta = unit.isMissionDefendOrSparta();

        if (missionDefendOrSparta && unit.distToMain() <= 30) return 0;

        if (applyHugePenaltyWhenCrossingChoke(unit)) return -2;

        return -0.4
            + (missionDefendOrSparta ? -0.2 : 0)
            + (Army.strengthWithoutCB() <= 150 ? -0.35 : 0);
    }

    private static boolean applyHugePenaltyWhenCrossingChoke(AUnit unit) {
        if (Enemy.terran()) return false;

        return A.s % 12 <= 7
            && unit.enemiesNear().combatUnits().atLeast(3)
            && unit.ourToEnemyRangedUnitRatio() <= 1.7;
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
