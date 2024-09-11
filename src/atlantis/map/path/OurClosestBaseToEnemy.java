package atlantis.map.path;

import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
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
                    if (base.isLifted() && base.lastActionMoreThanAgo(90, Actions.LAND)) continue;

                    int distToEnemyInChokes = distToEnemyInNumOfChokes(base);
//                    System.out.println("base = " + base + " / distToEnemyInChokes=" + distToEnemyInChokes);

                    if (distToEnemyInChokes > 0 && distToEnemyInChokes < closestDistance) {
                        closest = base;
                        closestDistance = distToEnemyInChokes;
                    }
                }

//                System.err.println("RETURN closest = " + closest + " (" + closestDistance);
                return closest;
            }
        );
    }

    public static void clearCache() {
        cache.clear();
    }

    private static int distToEnemyInNumOfChokes(HasPosition from) {
        CPPath chokes = PathToEnemyBase.definePathToEnemy(from);
        return chokes != null ? chokes.size() : -1;
    }
}
