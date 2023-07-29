package atlantis.map.choke;

import atlantis.units.AUnit;
import atlantis.util.cache.Cache;

public class IsUnitNearChoke {
    private static Cache<Boolean> cache = new Cache<>();

    public static boolean check(AUnit unit, double maxDistToChoke) {
        return cache.get(
            "check:" + unit.id() + "," + maxDistToChoke,
            19,
            ()-> {
                AChoke choke = Chokes.nearestChoke(unit);
                if (choke == null) return false;

                return choke.distToLessThan(unit, maxDistToChoke);
            }
        );
    }
}
