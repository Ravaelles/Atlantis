package atlantis.map.path;

import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.cache.Cache;
import atlantis.util.cache.CacheKey;
import bwem.CPPath;

public class ClosestToEnemyBase {
    private static Cache<AUnit> cache = new Cache<>();

    public static AUnit from(Selection positions) {
        return cache.get(
            "from:" + positions.toString(),
            2,
            () -> {
                AUnit closest = null;
                int closestDistance = 999;

                for (AUnit unit : positions.list()) {
                    int distToEnemyInChokes = distToEnemyInNumOfChokes(unit);
//                    System.out.println("unit = " + unit + " / distToEnemyInChokes=" + distToEnemyInChokes);

                    if (distToEnemyInChokes > 0 && distToEnemyInChokes < closestDistance) {
                        closest = unit;
                        closestDistance = distToEnemyInChokes;
                    }
                }

//                System.err.println("RETURN closest = " + closest + " (" + closestDistance);
                return closest;
            }
        );
    }

    private static int distToEnemyInNumOfChokes(HasPosition from) {
        CPPath chokes = PathToEnemyBase.definePathToEnemy(from);
        return chokes != null ? chokes.size() : -1;
    }
}
