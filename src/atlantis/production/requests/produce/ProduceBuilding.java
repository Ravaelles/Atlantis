package atlantis.production.requests.produce;

import atlantis.production.constructing.NewConstructionRequest;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.orders.zerg.ProduceZergUnit;
import atlantis.units.AUnitType;

public class ProduceBuilding {
    public static void produceBuilding(AUnitType type, ProductionOrder order) {
        assert type.isABuilding();

        if (type.isZerg()) {
            ProduceZergUnit.produceZergBuilding(type, order);
            return;
        }

        if (type.isAddon()) {
            ProduceAddon.produceAddon(type);
        }
        else {
            NewConstructionRequest.requestConstructionOf(order);
        }
    }
}