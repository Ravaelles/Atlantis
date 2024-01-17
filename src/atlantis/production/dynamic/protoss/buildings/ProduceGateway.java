package atlantis.production.dynamic.protoss.buildings;

import atlantis.game.A;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;

import static atlantis.units.AUnitType.Protoss_Gateway;

public class ProduceGateway {

    private static int unfinishedGateways;

    public static void produce() {
        if (!A.hasMinerals(220)) return;
        if (Count.freeGateways() > 0) return;
//        if (Count.inQueueOrUnfinished(Protoss_Gateway, 6) >= 2) return;

//        unfinishedGateways = Select.countOurUnfinishedOfType(Protoss_Gateway);
        unfinishedGateways = Count.inProductionOrInQueue(Protoss_Gateway);

        if (unfinishedGateways >= 1 && !A.hasMinerals(550)) return;
        if (unfinishedGateways >= 2 && !A.hasMinerals(450)) return;

        if (tooManyGatewaysForNow()) return;

//        if (
//            GamePhase.isEarlyGame()
//                && EnemyStrategy.get().isRushOrCheese()
//                && Count.ourWithUnfinished(Protoss_Gateway) <= (A.hasMinerals(250) ? 2 : 1)
//        ) {
//            DynamicCommanderHelpers.buildIfHaveMineralsAndGas(Protoss_Gateway);
//            return;
//        }

        AddToQueue.withStandardPriority(Protoss_Gateway);
//        DynamicCommanderHelpers.buildIfAllBusyButCanAfford(Protoss_Gateway, A.supplyUsed() <= 90 ? 260 : 650, 0);
    }

    private static boolean tooManyGatewaysForNow() {
        return Count.gatewaysWithUnfinished() >= 3
            && !A.hasMinerals(260)
            && (!Have.roboticsFacility() || Count.basesWithUnfinished() <= 1);
    }
}
