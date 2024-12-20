package atlantis.production.dynamic.protoss.buildings;

import atlantis.game.A;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrderPriority;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;

import static atlantis.units.AUnitType.*;

public class ProduceCitadelOfAdun {
    public static boolean produce() {
        if (true) return false;

        if (A.supplyUsed() <= 60) return false;
        if (Have.a(type())) return false;
        if (Count.ofType(Protoss_Observatory) == 0) return false;

        if (A.supplyUsed() >= 140 && A.hasGas(180) && A.now % 41 == 0 && Have.notEvenPlanned(type())) {
            A.errPrintln("TEMP Citadel of Adun at " + A.s);
            return AddToQueue.toHave(type(), 1, ProductionOrderPriority.HIGH);
        }

//        if (CountInQueue.count(type(), 6) == 0) {
//            A.errPrintln("ProduceCitadelOfAdun: Requested Citadel of Adun at " + A.s);
//            return AddToQueue.withHighPriority(type()) != null;
//        }

        return false;
    }

    private static AUnitType type() {
        return Protoss_Citadel_of_Adun;
    }
}
