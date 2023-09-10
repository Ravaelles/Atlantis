package atlantis.production.requests.produce;

import atlantis.production.constructing.NewConstructionRequest;
import atlantis.production.orders.build.ZergBuildOrder;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;

public class ProduceBuilding {
    public static void produceBuilding(AUnitType type, ProductionOrder order) {
        assert type.isBuilding();

        if (type.isZerg()) {
            ZergBuildOrder.produceZergBuilding(type, order);
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