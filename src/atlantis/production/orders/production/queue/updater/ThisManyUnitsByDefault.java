package atlantis.production.orders.production.queue.updater;

import atlantis.production.orders.production.queue.Queue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.Counter;

public class ThisManyUnitsByDefault {
    public static int numOfUnits(AUnitType type) {
        return (type.isWorker() ? 4 : 0)
            + (type.isBase() ? (type.isPrimaryBase() ? 1 : 0) : 0)
            + (type.isOverlord() ? 1 : 0);
    }
}
