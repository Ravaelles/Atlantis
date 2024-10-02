package atlantis.production.dynamic.protoss.buildings;

import atlantis.production.dynamic.DynamicCommanderHelpers;
import atlantis.production.dynamic.protoss.units.ProduceObserver;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrderPriority;
import atlantis.units.AUnitType;
import atlantis.units.select.Have;

import static atlantis.units.AUnitType.Protoss_Observatory;
import static atlantis.units.AUnitType.Protoss_Robotics_Facility;

public class ProduceObservatory {
    public static boolean produce() {
        if (Have.a(type())) return false;
        if (!ProduceObserver.needObservers()) return false;

        if (Have.notEvenPlanned(Protoss_Robotics_Facility)) {
            if (AddToQueue.toHave(Protoss_Robotics_Facility, 1, ProductionOrderPriority.HIGH)) return true;
        }

        if (Have.notEvenPlanned(type())) {
            if (DynamicCommanderHelpers.buildNow(type(), true)) return true;
        }

        return false;
    }

    private static AUnitType type() {
        return Protoss_Observatory;
    }
}
