package atlantis.production.orders.production.queue;

import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;

public class RemoveFromQueue {
    public static boolean removeBuildingOrdersThatDontHaveConstructionYetSoTheyAreNotStarted(AUnitType type) {
        boolean removed = false;

        for (ProductionOrder order : Queue.get().notStarted().ofType(type).list()) {
            if (order.construction() == null) {
                order.cancel();
                removed = true;
            }
        }

        return removed;
    }
}
