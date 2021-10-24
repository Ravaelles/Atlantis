package atlantis.units;

import atlantis.util.Cache;
import atlantis.util.Callback;

public class SelectUnitsCache extends Cache<Select<? extends AUnit>> {

    public Select<? extends AUnit> get(String cacheKey, int cacheForFrames, Callback callback) {
        return super.get(cacheKey, cacheForFrames, callback).clone();
    }

    public Select<? extends AUnit> get(String cacheKey, Callback callback) {
        return super.get(cacheKey, callback).clone();
    }

}
