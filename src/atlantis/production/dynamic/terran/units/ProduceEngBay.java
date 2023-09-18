package atlantis.production.dynamic.terran.units;

import atlantis.game.A;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrderPriority;
import atlantis.units.select.Count;
import atlantis.units.select.Have;

import static atlantis.units.AUnitType.Terran_Engineering_Bay;

public class ProduceEngBay {
    public static void engBay() {
        if (Have.engBay() || Count.withPlanned(Terran_Engineering_Bay) > 0) return;

        if (A.supplyUsed(45) || A.hasMinerals(550) || A.seconds() >= 500) {
            if (AddToQueue.toHave(Terran_Engineering_Bay, 1, ProductionOrderPriority.HIGH)) {
                A.printStackTrace("Requested ENGINEERING BAY");
            }
        }
    }
}
