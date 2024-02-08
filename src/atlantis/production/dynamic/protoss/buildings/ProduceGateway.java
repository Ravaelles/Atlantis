package atlantis.production.dynamic.protoss.buildings;

import atlantis.game.A;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.select.Count;
import atlantis.units.select.Have;

import static atlantis.units.AUnitType.Protoss_Gateway;

public class ProduceGateway {

    private static int unfinishedGateways;
    private static int freeGateways;
    private static int minerals;

    public static boolean produce() {
        minerals = A.minerals();

        if (minerals < 205) return false;

        freeGateways = Count.freeGateways();

        if (freeGateways > 0) return false;

        unfinishedGateways = Count.inProductionOrInQueue(Protoss_Gateway);

        if (unfinishedGateways >= 1 && !A.hasMinerals(500)) return false;
        if (unfinishedGateways >= 2 && !A.hasMinerals(700)) return false;
        if (tooManyGatewaysForNow()) return false;

        if (A.hasMinerals(350) && freeGateways == 0) return produceGateway();

        return produceGateway();
    }

    private static boolean produceGateway() {
        AddToQueue.withStandardPriority(Protoss_Gateway);
        return true;
    }

    private static boolean tooManyGatewaysForNow() {
        return Count.gatewaysWithUnfinished() >= 3
            && !A.hasMinerals(520)
            && (!Have.roboticsFacility() || Count.basesWithUnfinished() <= 1);
    }
}
