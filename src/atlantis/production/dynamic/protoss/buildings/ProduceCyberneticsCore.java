package atlantis.production.dynamic.protoss.buildings;

import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.OurArmy;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.information.decisions.protoss.dragoon.ProduceDragoonInsteadZealot;
import atlantis.information.strategy.OurStrategy;
import atlantis.production.dynamic.protoss.units.ProduceZealot;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.Orders;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.util.Enemy;

import static atlantis.units.AUnitType.Protoss_Assimilator;
import static atlantis.units.AUnitType.Protoss_Cybernetics_Core;

public class ProduceCyberneticsCore {
    public static boolean produce() {
        if (Have.cyberneticsCore()) return false;
        if (Count.inProduction(type()) > 0) return false;

//        int buildAtSupply = buildAtSupply();
        if (needOne()) {
            return addCyberneticsToQueue();
        }
//        return DynamicCommanderHelpers.buildToHaveOne(buildAtSupply, Protoss_Cybernetics_Core);

        return false;
    }

    private static boolean addCyberneticsToQueue() {
//        A.errPrintln("Add ZZZ Cybernetics Core to queue at " + A.minSec());

        Orders productionOrders = Queue.get().nonCompletedNext30();
        productionOrders.ofType(type()).forEach(ProductionOrder::cancel);

//        ProductionOrder existingOrder = productionOrders.ofType(type()).first();
////        if (existingOrder != null && existingOrder.requestedAgo() >= 30 * 10) {
//        if (existingOrder != null) {
//            PreventDuplicateOrders.cancelPreviousNonStartedOrdersOf(type());
//        }

        ProductionOrder order = AddToQueue.withTopPriority(type());
//        A.errPrintln("addCybernetics ORDER " + order);
        if (order != null) {
            order.setMinSupply(4);

            if (Count.existingOrInProductionOrInQueue(Protoss_Assimilator) == 0) {
                ProductionOrder gasOrder = AddToQueue.withTopPriority(Protoss_Assimilator);
                if (gasOrder != null) gasOrder.setMinSupply(6);
            }

            return true;
        }

        return false;
    }

    private static boolean needOne() {
        if (A.supplyUsed() >= 50) return true;

        if (OurStrategy.get().nameContains("Forge FE")) return A.supplyUsed(24) && (
            A.hasMinerals(170) || Count.zealotsWithUnfinished() >= 2
        );

        if (Enemy.zerg() && ProduceZealot.producedCount >= 5 && (
            OurArmy.strength() >= 140 || EnemyUnits.hydras() > 0 || EnemyInfo.combatBuildingsAntiLand() > 0
        )) return true;

        if (Enemy.zerg() && A.s <= 300 && Count.zealots() <= 3 && !A.hasMinerals(192)) return false;

        if (A.minerals() >= 210) return true;
        if (Count.gasBuildings() > 0) return true;

        return (
            (A.supplyUsed() >= 25 && Count.ourCombatUnits() >= 6)
                || A.supplyUsed(buildAtSupply())
                || ProduceDragoonInsteadZealot.dragoonInsteadOfZealot()
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
