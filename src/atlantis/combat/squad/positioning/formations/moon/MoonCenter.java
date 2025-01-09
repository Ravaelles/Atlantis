package atlantis.combat.squad.positioning.formations.moon;

import atlantis.combat.squad.positioning.choking.ShouldDoChoking;
import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AliveEnemies;
import atlantis.util.cache.Cache;

import java.util.HashMap;

public class MoonCenter {
    private static Cache<APosition> cachePosition = new Cache<>();
    private static HashMap<Integer, APosition> lastMoons = new HashMap<>();
    private static int _lastMoonCenterChangedAt = -1;

    protected static HasPosition moonCenter(AUnit leader) {
        APosition moonCenter = cachePosition.getIfValid(
            "moonCenter:" + leader.id(),
            7,
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

        if (moonCenter != null) {
            if (shouldConsiderCurrentCenterAsDifferentOne(leader, moonCenter)) {
//                System.err.println("Moon center changed at " + A.now());
                _lastMoonCenterChangedAt = A.now;
            }

            lastMoons.put(leader.id(), moonCenter);
        }

        return moonCenter;
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
