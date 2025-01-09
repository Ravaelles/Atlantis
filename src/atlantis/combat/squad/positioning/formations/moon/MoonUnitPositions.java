package atlantis.combat.squad.positioning.formations.moon;

import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.position.Positions;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;
import atlantis.util.We;
import atlantis.util.cache.Cache;
import atlantis.util.log.ErrorLog;

import java.util.Map;

public class MoonUnitPositions {
    private static Cache<Map<AUnit, APosition>> cacheMap = new Cache<>();

    private static HasPosition moonCenter;
    private static Selection ourUnits;
    private static double separation;
    private static double radius;

    public static APosition positionToGoForUnit(AUnit unit, AUnit leader) {
        if (unit == null) return null;

        Map<AUnit, APosition> positions = getPositionsCreatedForLeader(unit, leader);
        if (positions == null) return null;

        APosition positionSpecificForUnit = positions.get(unit);
        if (positionSpecificForUnit != null) return positionSpecificForUnit;

        HasPosition position = Positions.nearestToFrom(unit, positions.values());
        return position != null ? position.position() : null;
    }

    private static Map<AUnit, APosition> getPositionsCreatedForLeader(AUnit unit, AUnit leader) {
        int cacheForFrames = We.protoss() ? 6 : 10;

        return cacheMap.getIfValid(
            "getPositionsCreatedForLeader:" + leader.id(),
            cacheForFrames,
            () -> refreshEntireMap(unit, leader)
        );
    }

    // =========================================================

    private static Map<AUnit, APosition> refreshEntireMap(AUnit unit, AUnit leader) {
        if (!defineAndValidateFormationParameters(unit, leader)) {
//            ErrorLog.printMaxOncePerMinute("Failed to define and validate formation parameters");
            return null;
        }

        return MoonUnitPositionsCalculator.calculateUnitPositions(ourUnits, moonCenter, leader, radius, separation);
    }

    private static boolean defineAndValidateFormationParameters(AUnit unit, AUnit leader) {
        moonCenter = MoonCenter.moonCenter(leader);
        if (moonCenter == null) return false;

        ourUnits = leader.friendsNear().combatUnits().inSquad(unit.squad()).add(unit);

        radius = MoonRadius.radius(unit, leader, moonCenter, MoonCenter.moonCenterAssignedAgo());
        if (radius <= 3.3) return false;

        separation = MoonSeparation.defineSeparation(ourUnits, leader);

        return true;
    }

    protected static void clearCache() {
        cacheMap.clear();
        MoonRadius.cacheDouble.clear();
    }
}
