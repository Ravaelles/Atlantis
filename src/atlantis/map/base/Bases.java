package atlantis.map.base;

import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.cache.Cache;

public class Bases {
    private static Cache<AUnit> cache = new Cache<>();

    /**
     * Our natural expansion base.
     */
    public static AUnit natural() {
        if (Count.basesWithUnfinished() <= 1) return null;

        return cache.get(
            "natural",
            93,
            () -> {
                AChoke mainChoke = Chokes.mainChoke();
                if (mainChoke == null) return null;

                for (AUnit base : Select.ourBasesWithUnfinished().list()) {
                    double distToMainChoke = base.distTo(mainChoke);
                    if (distToMainChoke <= 15 && base.distToMain() >= 15) return base;
                }

                return null;
            }
        );
    }
}
