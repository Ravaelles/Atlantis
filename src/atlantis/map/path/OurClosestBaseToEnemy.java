package atlantis.map.path;

import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.cache.Cache;
import bwem.CPPath;

public class OurClosestBaseToEnemy {
    private static Cache<AUnit> cache = new Cache<>();

    public static AUnit get() {
        return cache.get(
            "get:" + Count.basesWithUnfinished(),
            -1,
            () -> {
                AUnit closest = null;
                int closestDistance = 999;

                for (AUnit base : Select.ourBasesWithUnfinished().list()) {
                    int chokeDistance = distToEnemyInNumOfChokes(base);
                    System.out.println("base = " + base + " / chokeDistance=" + chokeDistance);

                    if (chokeDistance > 0 && chokeDistance < closestDistance) {
                        closest = base;
                        closestDistance = chokeDistance;
                    }
                }

                return closest;
            }
        );
    }

    private static int distToEnemyInNumOfChokes(HasPosition from) {
        CPPath chokes = PathToEnemyBase.definePathToEnemy(from);
        return chokes != null ? chokes.size() : -1;
    }
}
