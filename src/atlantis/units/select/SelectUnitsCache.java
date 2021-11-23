package atlantis.units.select;

import atlantis.util.Cache;
import atlantis.util.Callback;

public class SelectUnitsCache extends Cache<Selection> {

    public Selection get(String cacheKey, int cacheForFrames, Callback callback) {
        return super.get(cacheKey, cacheForFrames, callback).clone();
    }

}
