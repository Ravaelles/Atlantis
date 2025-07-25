package atlantis.combat.squad.positioning.formations.moon;

import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.util.cache.Cache;

public class MoonRadius {
    public static final double OVERTIME_RADIUS_SHORTENING_MODIFIER = 0.004;

    protected static final Cache<Double> cacheDouble = new Cache<>();

    protected static double radius(AUnit unit, AUnit leader, HasPosition moonCenter, int moonCenterAssignedAgo) {
        double radius = cacheDouble.get(
            "radius:" + leader.id(),
            13,
            () -> {
                double dist = leader.distTo(moonCenter);

                if (dist <= 8.5 && unit.lastActionMoreThanAgo(30 * 7, Actions.MOVE_FORMATION)) {
                    int raceBonus = A.whenEnemyProtossTerranZerg(4, 3, 4);
                    dist += raceBonus;
                }

                double MAX_FOR_PROTOSS = 13.0;
//                double MAX_FOR_ZERG = 11.0 - Math.min(3, leader.eval());
                double MAX_FOR_ZERG = 13.0;

                if (Enemy.protoss() && dist >= MAX_FOR_PROTOSS) return MAX_FOR_PROTOSS;
                if (Enemy.zerg() && dist >= MAX_FOR_ZERG) return MAX_FOR_ZERG;

                if (dist >= 13.8) return 13.8;
                if (dist <= 5.2) return -1.0;

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

    private static double overtimeRadiusShortening(int moonCenterAssignedAgo) {
        return Math.min(10, moonCenterAssignedAgo * OVERTIME_RADIUS_SHORTENING_MODIFIER);
    }
}
