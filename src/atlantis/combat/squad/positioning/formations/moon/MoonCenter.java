package atlantis.combat.squad.positioning.formations.moon;

import atlantis.combat.squad.positioning.choking.ShouldDoChoking;
import atlantis.debug.painter.AAdvancedPainter;
import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.region.ARegion;
import atlantis.units.AUnit;
import atlantis.units.AliveEnemies;
import atlantis.util.cache.Cache;
import bwapi.Color;

import java.util.HashMap;

public class MoonCenter {
    private static Cache<APosition> cachePosition = new Cache<>();
    private static HashMap<Integer, APosition> lastMoons = new HashMap<>();
    private static int _lastMoonCenterChangedAt = -1;

    private static APosition flattenMoonCenter(AUnit leader, APosition moonCenter) {
//        return moonCenter.translateTilesTowards(Math.min(leader.squadSize() * 1.6, 7), leader);
//        if (leader.squadSize() <= ) return moonCenter;
//        return moonCenter.translateTilesTowards(7, leader);
        return moonCenter;
    }

    protected static HasPosition moonCenter(AUnit leader) {
        APosition moonCenter = moonCenterUnit(leader);

        if (moonCenter != null) {
            if (shouldConsiderCurrentCenterAsDifferentOne(leader, moonCenter)) {
//                System.err.println("Moon center changed at " + A.now());
                _lastMoonCenterChangedAt = A.now;
            }

            lastMoons.put(leader.id(), moonCenter);
        }

        if (moonCenter != null) moonCenter = flattenMoonCenter(leader, moonCenter);

//        if (moonCenter != null) {
//            AAdvancedPainter.paintCircle(moonCenter, 30 * 6, Color.Orange);
//        }

        return moonCenter;
    }

    private static APosition moonCenterUnit(AUnit leader) {
        APosition moonCenterUnit = cachePosition.getIfValid(
            "moonCenterUnit:" + leader.id(),
            9,
            () -> {
                MoonUnitPositions.clearCache();

                HasPosition lastEnemyPosition = ShouldDoChoking.lastEnemyPosition();
                if (lastEnemyPosition != null) return lastEnemyPosition;

                HasPosition enemy = leader.enemiesNear().combatUnits().groundUnits().nearestTo(leader);
                if (enemy != null) return enemy.position();

                enemy = AliveEnemies.get().groundUnits().combatUnits().nearestTo(leader);
                if (enemy != null) return enemy.position();

                return null;
            }
        );
        return moonCenterUnit;
    }

    private static boolean shouldConsiderCurrentCenterAsDifferentOne(AUnit leader, APosition moonCenter) {
        return lastMoons.containsKey(leader.id())
            && !lastMoons.get(leader.id()).equals(moonCenter)
            && lastMoons.get(leader.id()).distTo(moonCenter) >= 1.4;
    }

    protected static int moonCenterAssignedAgo() {
        return A.ago(_lastMoonCenterChangedAt);
    }
}
