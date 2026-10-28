package atlantis.production.dynamic.protoss.buildings;

import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.information.decisions.protoss.dragoon.ProduceDragoonInsteadZealot;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.Army;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.information.strategy.Strategy;
import atlantis.production.constructions.OutstandingBuildings;
import atlantis.production.dynamic.protoss.units.ProduceZealot;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.Orders;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;

import static atlantis.units.AUnitType.Protoss_Cybernetics_Core;

public class ProduceCyberneticsCore {
    public static boolean produce() {
        if (Have.cyberneticsCore()) return false;
        if (Count.inProduction(type()) > 0) return false;
        if (Count.inQueue(type(), 3) > 0) return false;
        // Owner 2026-10-18: one Core per game, never two coming. The guards
        // above are blind to picked-early PREPARING sites (measured: dynamic
        // Core warped 16 s after the BO Core), so count everything once.
        if (OutstandingBuildings.ofType(type()) >= 1) return false;

        if (needOne()) {
            return addCyberneticsToQueue();
        }

        return false;
    }

    private static boolean addCyberneticsToQueue() {

        Orders productionOrders = Queue.get().notFinishedNext30().ofType(type());
        ProductionOrder existing = productionOrders.first();


        ProductionOrder order = existing != null ? existing : AddToQueue.withTopPriority(type());
        if (order != null) {
            order.setMinSupply(4);


            return true;
        }

        return false;
    }

    private static boolean needOne() {
        if (A.supplyUsed() >= 50) return true;

        if (Strategy.get().nameContains("Forge FE")) return A.supplyUsed(24) && (
            A.hasMinerals(170) || Count.zealotsWithUnfinished() >= 2
        );

        if (Enemy.zerg() && ProduceZealot.producedCount >= 5 && (
            Army.strength() >= 140 || EnemyUnits.hydras() > 0 || EnemyInfo.combatBuildingsAntiLand() > 0
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


        return Army.strength() >= 120 ? 20 : 30;
    }

    private static AUnitType type() {
        return Protoss_Cybernetics_Core;
    }
}
