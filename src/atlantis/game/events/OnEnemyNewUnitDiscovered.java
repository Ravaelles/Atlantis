package atlantis.game.events;

import atlantis.combat.missions.MissionChanger;
import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnitsUpdater;
import atlantis.information.generic.OurArmyStrength;
import atlantis.production.constructing.Construction;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.Orders;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.select.Count;

import java.util.ArrayList;
import java.util.List;

public class OnEnemyNewUnitDiscovered {
    public static void update(AUnit unit) {
        EnemyUnitsUpdater.weDiscoveredEnemyUnit(unit);

        if (A.seconds() <= 600) MissionChanger.forceEvaluateGlobalMission();

        actIfWeAreMuchWeaker();
    }

    private static void actIfWeAreMuchWeaker() {
        if (OurArmyStrength.relative() >= 0.8) return;

        cancelBasesConstructions();
    }

    private static void cancelBasesConstructions() {
        if (A.seconds() >= 700 || Count.bases() >= 3) return;

        if (CountInQueue.bases() > 0) {
            List<ProductionOrder> orders = Queue.get().nonCompleted().ofType(AtlantisRaceConfig.BASE).list();
            for (ProductionOrder order : orders) {
                if (order.progressPercent() <= 49) {
                    A.println("Cancel " + order.unitType() + " (" + order.progressPercent() + "%) - much weaker");
                    order.cancel();
                }

//                int progress = construction.progressPercent();
//                if (progress <= 49) {
//                    A.println("Cancel " + construction.buildingType() + " (" + progress + "%) - much weaker");
//                    construction.cancel();
//                }
            }
        }
    }
}
