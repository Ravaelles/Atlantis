package atlantis.production.dynamic.protoss.buildings;

import atlantis.game.A;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.information.strategy.GamePhase;
import atlantis.production.dynamic.DynamicCommanderHelpers;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.select.Count;

import static atlantis.units.AUnitType.Protoss_Gateway;

public class ProduceGateway {
    public static void produce() {
        if (
            !A.hasMinerals(230)
                || Count.freeGateways() > 0
                || Count.inQueueOrUnfinished(Protoss_Gateway, 10) > (A.minerals() >= 650 ? 2 : 1)
        ) return;

        int gatewaysWithUnfinished = Count.gatewaysWithUnfinished();

        if (gatewaysWithUnfinished >= 1 && !A.hasMinerals(550) && Count.basesWithUnfinished() <= 1) return;
        if (gatewaysWithUnfinished >= 2 && !A.hasMinerals(450) && Count.basesWithUnfinished() <= 1) return;

        if (
            GamePhase.isEarlyGame()
                && EnemyStrategy.get().isRushOrCheese()
                && Count.ourWithUnfinished(Protoss_Gateway) <= (A.hasMinerals(250) ? 2 : 1)
        ) {
            DynamicCommanderHelpers.buildIfHaveMineralsAndGas(Protoss_Gateway);
            return;
        }

        AddToQueue.withStandardPriority(Protoss_Gateway);
//        DynamicCommanderHelpers.buildIfAllBusyButCanAfford(Protoss_Gateway, A.supplyUsed() <= 90 ? 260 : 650, 0);
    }
}
