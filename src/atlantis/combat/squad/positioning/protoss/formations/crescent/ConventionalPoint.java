package atlantis.combat.squad.positioning.protoss.formations.crescent;

import atlantis.combat.squad.Squad;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.util.cache.Cache;

public class ConventionalPoint {
    private static Cache<AUnit> cache = new Cache<>();

    public static AUnit get(Squad squad) {
        return cache.get(
            "get:" + squad.name(),
            3,
            () -> {
                if (squad == null) return null;

                HasPosition ourCenter = squad.center();
                if (ourCenter == null) return null;

                AUnit enemy;
                AUnit leader = squad.leader();

                if (leader != null) {
                    enemy = leader.enemiesNear().groundUnits().combatUnits().nearestTo(ourCenter);
                    if (enemy != null) return newPoint(enemy, squad);
                }

                enemy = Select.enemy().groundUnits().combatUnits().nearestTo(ourCenter);
                if (enemy != null) return newPoint(enemy, squad);

                return null;
            }
        );
    }

    private static HasPosition newPoint(AUnit enemy, Squad squad) {
        return enemy;
    }
}
