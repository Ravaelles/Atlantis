package atlantis.production.dynamic.protoss.buildings;

import atlantis.game.A;
import atlantis.information.generic.OurArmy;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.information.decisions.protoss.dragoon.DragoonInsteadZealot;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.add.PreventDuplicateOrders;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.util.Enemy;

import static atlantis.units.AUnitType.Protoss_Cybernetics_Core;

public class ProduceCyberneticsCore {
    public static boolean produce() {
        if (Have.cyberneticsCore()) return false;
        if (Count.inProductionOrInQueue(type()) > 0) return A.minerals() <= 260;

//        int buildAtSupply = buildAtSupply();
        if (needOne()) {
            return addCyberneticsToQueue();
        }
//        return DynamicCommanderHelpers.buildToHaveOne(buildAtSupply, Protoss_Cybernetics_Core);

        return false;
    }

    private static boolean addCyberneticsToQueue() {
        ProductionOrder existingOrder = Queue.get().nonCompletedNext30().ofType(type()).first();
        if (existingOrder != null && existingOrder.requestedAgo() >= 30 * 6) {
            PreventDuplicateOrders.cancelPreviousNonStartedOrdersOf(type());
        }

        ProductionOrder order = AddToQueue.withTopPriority(type());
        if (order != null) {
            order.setMinSupply(4);

            ProductionOrder gasOrder = AddToQueue.withHighPriority(type());
            if (gasOrder != null) gasOrder.setMinSupply(A.supplyUsed() + 1);
            return true;
        }

        return false;
    }

    private static boolean needOne() {
        if (Enemy.zerg() && A.s <= 300 && Count.zealots() <= 3 && !A.hasMinerals(192)) return false;

        if (A.minerals() >= 210) return true;
        if (Count.gasBuildings() > 0) return true;

        return (
            (A.supplyUsed() >= 25 && Count.ourCombatUnits() >= 6)
                || A.supplyUsed(buildAtSupply())
                || DragoonInsteadZealot.dragoonInsteadOfZealot()
        ) && Count.withPlanned(type()) == 0;
    }

    private static int buildAtSupply() {
        if (A.hasMinerals(250) && A.supplyUsed(16)) return 0;

        if (EnemyStrategy.get().isGoingHiddenUnits()) return 34;

//        if (EnemyStrategy.get().isRushOrCheese()) return 10;

        return OurArmy.strength() >= 120 ? 20 : 30;
    }

    private static AUnitType type() {
        return Protoss_Cybernetics_Core;
    }
}
