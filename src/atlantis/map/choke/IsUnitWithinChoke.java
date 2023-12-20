package atlantis.map.choke;

import atlantis.units.AUnit;
import atlantis.util.cache.Cache;
import atlantis.util.cache.CacheKey;
import atlantis.util.log.ErrorLog;

public class IsUnitWithinChoke {
    private static Cache<Boolean> cache = new Cache<>();

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
            19,
            () -> (choke.distTo(unit) - choke.width()) <= 1
        );
    }
}
