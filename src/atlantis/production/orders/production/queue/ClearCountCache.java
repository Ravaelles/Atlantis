package atlantis.production.orders.production.queue;

import atlantis.units.select.Count;
import atlantis.units.select.Select;


public class ClearCountCache {
    public static boolean clear() {
        Select.clearCache();
        Count.clearCache();
        Queue.get().refresh();
        return true;
    }
}
