package atlantis.production.dynamic.protoss.buildings;

import atlantis.game.A;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.util.Enemy;

import static atlantis.units.AUnitType.Protoss_Gateway;

public class ProduceGateway {

    private static int allGateways;
    private static int unfinishedGateways;
    private static int freeGateways;
    private static int existingGateways;
    private static int minerals;

    public static boolean produce() {
        minerals = A.minerals();

        existingGateways = Count.gateways();
        freeGateways = Count.freeGateways();
        unfinishedGateways = Count.inProductionOrInQueue(Protoss_Gateway);
        allGateways = existingGateways + unfinishedGateways;

        if (freeGateways <= 2 && minerals >= 600) return produceGateway();
        if (minerals >= 167 && allGateways <= 4 * Count.bases()) return produceGateway();

        if (minerals < 205 && existingGateways >= 3) return false;
        if (existingGateways >= 5 && freeGateways > 0 && !A.hasMinerals(600)) return false;

//        if (unfinishedGateways >= 3 || !Enemy.zerg()) {
//            if (unfinishedGateways >= 1 && !A.hasMinerals(500)) return false;
        if (unfinishedGateways >= 2 && !A.hasMinerals(550)) return false;
//        }

        if (tooManyGatewaysForNow()) return false;

        if (continuousGatewayProduction()) return produceGateway();

        return false;
    }

    private static boolean continuousGatewayProduction() {
        return freeGateways <= 1 && (A.hasMinerals(570) || A.canAffordWithReserved(170, 0));
    }

    private static boolean produceGateway() {
        AddToQueue.withStandardPriority(Protoss_Gateway);
        return true;
    }

    private static boolean tooManyGatewaysForNow() {
        int enough = Enemy.zerg() ? 5 : 4;

        return !A.hasMinerals(450)
            && Count.gatewaysWithUnfinished() >= enough
            && (!Have.roboticsFacility() || Count.basesWithUnfinished() <= 1);
    }
}
