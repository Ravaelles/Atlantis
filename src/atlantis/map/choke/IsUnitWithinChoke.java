package atlantis.map.choke;

import atlantis.map.position.PositionUtil;
import atlantis.units.AUnit;
import atlantis.util.cache.Cache;
import atlantis.units.select.CacheKey;
import atlantis.util.log.ErrorLog;

public class IsUnitWithinChoke {
    private static Cache<Boolean> cache = new Cache<>();

    /**
     * Containment re-derives ~3x per second; preserved verbatim cadence,
     * tune only with a measured cadence (CONVENTIONS §31).
     */
    private static final int WITHIN_TTL_FRAMES = 19;

    public static boolean check(AChoke choke, AUnit unit) {
        if (choke == null) {
            ErrorLog.printMaxOncePerMinutePlusPrintStackTrace("Null choke in IsUnitWithinChoke.check");

            return false;
        }
        if (unit == null) {
            ErrorLog.printMaxOncePerMinutePlusPrintStackTrace("Null unit in IsUnitWithinChoke.check");

            return false;
        }

        return cache.get(
            CacheKey.create("check", choke, unit),
            WITHIN_TTL_FRAMES,
            () -> {
                // Chokes are ground concepts (NEXT #193): flyers are never
                // inside; containment is a short ground path within the
                // mouth's own width, never an air reading.
                if (unit.isFlying()) return false;

                return PositionUtil.groundDistanceTo(unit, choke.center()) <= choke.width();
            }
        );
    }
}
