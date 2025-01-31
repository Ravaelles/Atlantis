package atlantis.combat.squad.positioning.protoss.formation.moon;

import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AliveEnemies;
import atlantis.units.select.Selection;
import atlantis.util.cache.Cache;

import java.util.HashMap;
import java.util.Map;

public class MoonUnitPositions {
    private static Cache<Map<AUnit, APosition>> cache = new Cache<>();
//    private static Map<AUnit, APosition> unitToPositions = new HashMap<>();

    public static APosition positionToGoForUnit(AUnit unit, AUnit leader) {
        if (unit == null) return null;

        Map<AUnit, APosition> positions = getPositionsCreatedForLeader(unit, leader);
        if (positions == null) return null;

        return positions.get(unit);
    }

    private static Map<AUnit, APosition> getPositionsCreatedForLeader(AUnit unit, AUnit leader) {
        return cache.getIfValid(
            "getForLeader:" + leader.id(),
            7,
            () -> refreshEntireMap(unit, leader)
        );
    }

    private static Map<AUnit, APosition> refreshEntireMap(AUnit unit, AUnit leader) {
        HasPosition moonCenter = moonCenter(leader);
        if (moonCenter == null) return null;

        Selection ourUnits = leader.friendsNear().combatUnits().add(unit);
        int radius = 9;
        int separation = 2;

        return MoonUnitPositionsCalculator.calculateUnitPositions(ourUnits, moonCenter, radius, separation);
    }

    private static HasPosition moonCenter(AUnit leader) {
        AUnit enemy = leader.enemiesNear().combatUnits().groundUnits().nearestTo(leader);
        if (enemy != null) return enemy;

        return AliveEnemies.get().groundUnits().combatUnits().nearestTo(leader);
    }
}
