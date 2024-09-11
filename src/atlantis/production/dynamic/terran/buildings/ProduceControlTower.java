package atlantis.production.dynamic.terran.buildings;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrderPriority;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;

import static atlantis.units.AUnitType.*;

public class ProduceControlTower {
    /**
     * If there are buildings without addons, build them.
     */
    public static void controlTowers() {
        if (!Have.starport() || Have.controlTower()) return;

        if (A.canAfford(Terran_Control_Tower)) {
//            AddToQueue.maxAtATime(Terran_Control_Tower, 1, ProductionOrderPriority.HIGH);
            ProduceAddon.buildNow(Terran_Control_Tower);
        }
    }
}