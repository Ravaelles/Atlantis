package atlantis.production.requests.produce;

import atlantis.production.constructing.NewConstructionRequest;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.orders.zerg.ProduceZergUnit;
import atlantis.units.AUnitType;

public class ProduceBuilding {
    public static boolean produceBuilding(AUnitType type, ProductionOrder order) {
        assert type.isABuilding();

        if (type.isZerg()) {
            return ProduceZergUnit.produceZergBuilding(type, order);
        }

        if (type.isAddon()) {
            return ProduceAddon.produceAddon(type);
        }
        else {
            return NewConstructionRequest.requestConstructionOf(order);
        }
    }
}
