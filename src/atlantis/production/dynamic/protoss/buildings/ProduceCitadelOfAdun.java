package atlantis.production.dynamic.protoss.buildings;

import atlantis.game.A;
import atlantis.production.dynamic.DynamicCommanderHelpers;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Have;

import static atlantis.units.AUnitType.*;

public class ProduceCitadelOfAdun {
    public static boolean produce() {
        if (A.supplyUsed() <= 60) return false;
        if (Have.a(type())) return false;
        if (!Have.a(Protoss_Observer)) return false;

        if (CountInQueue.count(type(), 6) == 0) {
            A.errPrintln("ProduceCitadelOfAdun: Requested Citadel of Adun at " + A.s);
            AddToQueue.withHighPriority(type());
        }

        return false;
    }

    private static AUnitType type() {
        return Protoss_Citadel_of_Adun;
    }
}
