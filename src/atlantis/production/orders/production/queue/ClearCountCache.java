package atlantis.production.orders.production.queue;

import atlantis.units.select.Count;
import atlantis.units.select.Select;
import tests.unit.helpers.ClearAllCaches;


public class ClearCountCache {
    public static boolean clear() {
        Count.clearCache();
        Select.clearCache();
        return true;
    }
}
