package atlantis.map.base;

import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.cache.Cache;

import java.util.List;

public class Bases {
    /**
     * The natural re-derives every ~4 seconds; preserved verbatim
     * cadence, tune only with a measured cadence (CONVENTIONS §31).
     */
    private static final int NATURAL_TTL_FRAMES = 93;
    private static final Cache<AUnit> cache = new Cache<>();

    /**
     * Our natural expansion base.
     */
    public static AUnit natural() {
        if (Count.basesWithUnfinished() <= 1) return null;

        return cache.get(
            "natural",
            NATURAL_TTL_FRAMES,
            () -> {
                AChoke mainChoke = Chokes.mainChoke();
                if (mainChoke == null) return null;

                for (AUnit base : Select.ourBasesWithUnfinished().list()) {
                    double distToMainChoke = base.groundDist(mainChoke);
                    if (distToMainChoke <= 15 && base.distToMain() >= 15) return base;
                }

                return null;
            }
        );
    }

    public static List<AUnit> existing() {
        return Select.ourBasesWithUnfinished().list();
    }
}
