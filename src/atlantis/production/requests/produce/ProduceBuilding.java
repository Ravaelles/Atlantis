package atlantis.production.requests.produce;

import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.orders.build.ZergBuildOrder;
import atlantis.production.orders.production.ProductionOrder;
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
            ConstructionRequests.requestConstructionOf(order);
        }
    }
}