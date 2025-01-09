package atlantis.production.dynamic.terran.buildings;

import atlantis.game.A;
import atlantis.information.generic.Army;
import atlantis.production.orders.production.queue.RemoveFromQueue;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrderPriority;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;

import static atlantis.units.AUnitType.Terran_Engineering_Bay;

public class ProduceEngineeringBay {
    public static boolean engBay() {
        if (Have.engBay() || Count.withPlanned(type()) > 0) return false;

        if (shouldProduce()) {
            RemoveFromQueue.removeBuildingOrdersThatDontHaveConstructionYetSoTheyAreNotStarted(type());

            return AddToQueue.toHave(type(), 1, ProductionOrderPriority.HIGH);
        }

        return false;
    }

    private static AUnitType type() {
        return Terran_Engineering_Bay;
    }

    private static boolean shouldProduce() {
        return (A.supplyUsed(42))
            || (A.supplyUsed() >= 36 && A.canAffordWithReserved(110, 0))
            || A.seconds() >= 60 * 7.5;
    }
}
