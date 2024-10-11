package atlantis.production.requests.produce;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructing.NewConstructionRequest;
import atlantis.production.constructing.position.FindPosition;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.orders.zerg.ProduceZergUnit;
import atlantis.units.AUnitType;
import atlantis.units.workers.FreeWorkers;

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
            validatePosition(order, type);

            return NewConstructionRequest.requestConstructionOf(order);
        }
    }

    private static void validatePosition(ProductionOrder order, AUnitType type) {
        HasPosition atPosition = order.atPosition();
        if (atPosition == null) return;

        if (type.isBase() || type.isGasBuilding()) return;

        APosition newPosition = FindPosition.findForBuilding(
            FreeWorkers.get().nearestTo(atPosition), type, null, atPosition, order.maximumDistance()
        );

        if (newPosition != null) {
            order.forceSetPosition(newPosition);
        }
        else {
            A.errPrintln("^^^^^^^^^^^^^^^^^^^ CAN'T FIND POSITION FOR " + type + " at " + atPosition);
        }
    }
}
