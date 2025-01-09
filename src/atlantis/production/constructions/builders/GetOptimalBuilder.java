package atlantis.production.constructions.builders;

import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.production.constructions.Construction;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.workers.FreeWorkers;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;

public class GetOptimalBuilder {
    public static AUnit forPosition(Construction construction, ProductionOrder order) {
        HasPosition positionToBuild = construction.buildPosition();

        if (positionToBuild == null) {
            ErrorLog.printMaxOncePerMinute("No position to build, so can't find builder (" + order.unitType() + ")");

            return Select.ourWorkersFreeToBuildOrRepair(false).random();
        }

//        boolean allowRepairers = true;

//        return Select.ourWorkersFreeToBuildOrRepair(allowRepairers).nearestTo(positionToBuild);
        AUnit nearest = defineWorker(positionToBuild);

        if (nearest != null) return nearest;

        return Select.ourWorkers()
            .notSpecialAction()
            .notStuck()
            .nearestTo(positionToBuild);
    }

    private static AUnit defineWorker(HasPosition positionToBuild) {
        AUnit otherBuilder = useAnotherProtossBuilderInsteadOfAssigningNewOne(positionToBuild);
        if (otherBuilder != null) return otherBuilder;

        return FreeWorkers.get().nearestTo(positionToBuild);
    }

    private static AUnit useAnotherProtossBuilderInsteadOfAssigningNewOne(HasPosition positionToBuild) {
        if (!We.protoss()) return null;
        if (!A.hasFreeSupply(1)) return null;
        if (A.hasMinerals(600)) return null;
        if (Select.ourWorkers().inRadius(12, positionToBuild).count() >= 4) return null;

        return Select.ourWorkers()
            .builders()
            .inRadius(13, positionToBuild)
            .nearestTo(positionToBuild);
    }
}
