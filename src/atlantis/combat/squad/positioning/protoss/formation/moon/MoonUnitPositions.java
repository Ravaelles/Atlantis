package atlantis.combat.squad.positioning.protoss.formation.moon;

import atlantis.game.A;
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
    private static int _lastRadiusTimestamp = -1;

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

    private static void clearCache() {
        cacheMap.clear();
        cachePosition.clear();
        cacheDouble.clear();
    }

    private static Map<AUnit, APosition> refreshEntireMap(AUnit unit, AUnit leader) {
        moonCenter = moonCenter(leader);
        if (moonCenter == null) return null;

        ourUnits = leader.friendsNear().combatUnits().inSquad(unit.squad()).add(unit);
        double radius = radius(unit, leader);
        if (radius == -1) return null;

        double separation = 0.5;

        return MoonUnitPositionsCalculator.calculateUnitPositions(ourUnits, moonCenter, radius, separation);
    }

    private static double radius(AUnit unit, AUnit leader) {
        double radius = cacheDouble.get(
            "radius:" + leader.id(),
            1,
            () -> {
                double dist = leader.distTo(moonCenter) + 4;

                if (dist >= 14.8) return 14.8;
                if (dist <= 5.2) return -1;

                return dist;
            }
        );

        radius -= A.ago(_lastRadiusTimestamp) / 60.0;

        _lastRadiusTimestamp = A.now;

        return radius;
    }

    private static HasPosition moonCenter(AUnit leader) {
        return cachePosition.get(
            "moonCenter",
            30 * 5,
            () -> {
                clearCache();

                AUnit enemy = leader.enemiesNear().combatUnits().groundUnits().nearestTo(leader);
                if (enemy != null) return enemy;

                return AliveEnemies.get().groundUnits().combatUnits().nearestTo(leader);
            }
        );
    }
}
