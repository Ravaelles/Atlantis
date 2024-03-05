package atlantis.production.constructing.builders;

import atlantis.map.position.HasPosition;
import atlantis.production.constructing.Construction;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.workers.FreeWorkers;
import atlantis.util.log.ErrorLog;

public class GetOptimalBuilder {
    public static AUnit forPosition(Construction construction, ProductionOrder order) {
        HasPosition positionToBuild = construction.buildPosition();

        if (positionToBuild == null) {
            ErrorLog.printMaxOncePerMinute("No position to build, so can't find builder (" + order.unitType() + ")");

            return Select.ourWorkersFreeToBuildOrRepair(false).random();
        }

        boolean allowRepairers = true;

//        return Select.ourWorkersFreeToBuildOrRepair(allowRepairers).nearestTo(positionToBuild);
        AUnit nearest = FreeWorkers.get().nearestTo(positionToBuild);

        if (nearest != null) return nearest;

        return Select.ourWorkers().notSpecialAction().nearestTo(positionToBuild);
    }
}
