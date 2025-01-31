package atlantis.combat.squad.positioning.protoss.formation.moon;

import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AliveEnemies;
import atlantis.units.select.Selection;
import atlantis.util.cache.Cache;

import java.util.Map;

public class MoonUnitPositions {
    private static Cache<Map<AUnit, APosition>> cacheMap = new Cache<>();
    private static Cache<APosition> cachePosition = new Cache<>();
    private static Cache<Double> cacheDouble = new Cache<>();
    private static int _lastMoonCenterTimestamp = -1;

    private static HasPosition moonCenter;
    private static Selection ourUnits;
    //    private static Map<AUnit, APosition> unitToPositions = new HashMap<>();

    public static APosition positionToGoForUnit(AUnit unit, AUnit leader) {
        if (unit == null) return null;

        Map<AUnit, APosition> positions = getPositionsCreatedForLeader(unit, leader);
        if (positions == null) return null;

        return positions.get(unit);
    }

    private static Map<AUnit, APosition> getPositionsCreatedForLeader(AUnit unit, AUnit leader) {
        return cacheMap.getIfValid(
            "getForLeader:" + leader.id(),
            7,
            () -> refreshEntireMap(unit, leader)
        );
    }

    // =========================================================

    private static Map<AUnit, APosition> refreshEntireMap(AUnit unit, AUnit leader) {
        moonCenter = moonCenter(leader);
        if (moonCenter == null) return null;

        ourUnits = leader.friendsNear().combatUnits().inSquad(unit.squad()).add(unit);
        double radius = radius(unit, leader);
        if (radius == -1) return null;

//        double separation = 0.5;
        double separation = A.inRange(0.4, 20.0 / (10 + ourUnits.size()), 1.5);

        return MoonUnitPositionsCalculator.calculateUnitPositions(ourUnits, moonCenter, radius, separation);
    }

    private static double radius(AUnit unit, AUnit leader) {
        double radius = cacheDouble.get(
            "radius:" + leader.id(),
            1,
            () -> {
                int raceBonus = A.whenEnemyProtossTerranZerg(2, 3, 4);
                double dist = leader.distTo(moonCenter) + raceBonus;

                double MAX_FOR_PROTOSS = 10.0;
                double MAX_FOR_ZERG = 12.0;

                if (Enemy.protoss() && dist >= MAX_FOR_PROTOSS) return MAX_FOR_PROTOSS;
                if (Enemy.zerg() && dist >= MAX_FOR_ZERG) return MAX_FOR_ZERG;

                if (dist >= 13.8) return 13.8;
                if (dist <= 5.2) return -1.0;

                return dist;
            }
        );

        radius -= Math.min(10, A.ago(_lastMoonCenterTimestamp)) / 30.0;

//        A.errPrintln(
//            "radius: " + A.digit(radius)
//                + " / ago: " + A.ago(_lastMoonCenterTimestamp)
//                + " / center: " + moonCenter
//        );

        return radius;
    }

    private static HasPosition moonCenter(AUnit leader) {
        return cachePosition.getIfValid(
            "moonCenter",
            30 * 7,
            () -> {
                clearCache();
                _lastMoonCenterTimestamp = A.now;

                HasPosition enemy = leader.enemiesNear().combatUnits().groundUnits().nearestTo(leader);
                if (enemy != null) return enemy.position();

                enemy = AliveEnemies.get().groundUnits().combatUnits().nearestTo(leader);
                if (enemy != null) return enemy.position();

                return null;
            }
        );
    }

    private static void clearCache() {
        cacheMap.clear();
        cachePosition.clear();
        cacheDouble.clear();
    }
}
