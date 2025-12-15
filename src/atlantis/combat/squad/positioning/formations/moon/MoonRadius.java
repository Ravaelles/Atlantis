package atlantis.combat.squad.positioning.formations.moon;

import atlantis.combat.squad.squads.alpha.Alpha;
import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.information.generic.Army;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.util.cache.Cache;

public class MoonRadius {
    public static final double MAX_FOR_PROTOSS = 11.0;
    public static final double MAX_FOR_ZERG = 12.0;
    public static final double OVERTIME_RADIUS_SHORTENING_MODIFIER = 0.012;

    protected static final Cache<Double> cacheDouble = new Cache<>();

    protected static double radius(AUnit unit, AUnit leader, HasPosition moonCenter, int moonCenterAssignedAgo) {
        double radius = cacheDouble.get(
            "radius:" + leader.id(),
            13,
            () -> {
                double dist = leader.distTo(moonCenter);

                if (dist <= 8.5 && unit.lastActionMoreThanAgo(30 * 5, Actions.MOVE_FORMATION)) {
                    dist += applyDistBonusWhenRecentlyDoingFormation(leader);
                }

                //                double MAX_FOR_ZERG = 11.0 - Math.min(3, leader.eval());

                double eval = Math.max(0.3, unit.eval() + 0.1);
                if (eval <= 2) {
                    dist += 1 / eval;
                }

                if (unit.isMelee()) dist += 2.5;
                if (unit.isLeader()) dist -= 0.4;

                if (Enemy.protoss() && dist >= MAX_FOR_PROTOSS) return MAX_FOR_PROTOSS;
                if (Enemy.zerg() && dist >= MAX_FOR_ZERG) return MAX_FOR_ZERG;

                if (dist >= 13.8) return 13.8;
                if (dist <= 3.2) return -1.0;

                return dist;
            }
        );

        double changeOverTime = overtimeRadiusShortening(moonCenterAssignedAgo);
        radius -= changeOverTime;

//        System.err.println("radius = " + radius + " / overtime:" + changeOverTime);

//        A.errPrintln(
//            "radius: " + A.digit(radius)
//                + " / ago: " + A.ago(_lastMoonCenterTimestamp)
//                + " / center: " + moonCenter
//        );

        return radius;
    }

    private static double applyDistBonusWhenRecentlyDoingFormation(AUnit leader) {
        double raceBonus = A.whenEnemyProtossTerranZerg(2, 3, 3);

        if (leader.shotSecondsAgo() >= 10) {
            raceBonus = 0.1;
        }

        return raceBonus;
    }

    private static double overtimeRadiusShortening(int moonCenterAssignedAgo) {
        double leaderBonus = 1;
        AUnit leader = Alpha.alphaLeader();
        if (leader != null) {
            double baseEval = leader.eval() - leader.enemiesNear().combatUnits().size() / 30.0;
            leaderBonus = 0.99 + (A.inRange(0.2, baseEval, 10)) / 40.0;
        }

        return Math.min(
            10,
            moonCenterAssignedAgo
                * OVERTIME_RADIUS_SHORTENING_MODIFIER
                * leaderBonus
            );
    }
}
