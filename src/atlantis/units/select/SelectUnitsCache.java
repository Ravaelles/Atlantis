package atlantis.units.select;

import atlantis.units.AUnit;
import atlantis.util.Cache;
import atlantis.util.Callback;

public class SelectUnitsCache extends Cache<Selection<? extends AUnit>> {

    public Selection<AUnit> get(String cacheKey, int cacheForFrames, Callback callback) {
        return (Selection<AUnit>) super.get(cacheKey, cacheForFrames, callback).clone();
    }

//    public Select<? extends AUnit> get(String cacheKey, Callback callback) {
//        return super.get(cacheKey, callback).clone();
//    }

}
