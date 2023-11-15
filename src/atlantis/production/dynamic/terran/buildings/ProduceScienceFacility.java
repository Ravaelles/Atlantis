package atlantis.production.dynamic.terran.buildings;

import atlantis.game.A;
import atlantis.production.dynamic.DynamicCommanderHelpers;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrderPriority;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.util.Enemy;

import static atlantis.units.AUnitType.*;

public class ProduceScienceFacility {
    public static void scienceFacilities() {
        if (!Have.starport()) return;
        if (Have.haveExistingOrInPlans(Terran_Science_Facility)) return;

        if (A.supplyUsed(39)) System.err.println("shouldProduce() SCI FACI = " + shouldProduce());

        if (shouldProduce()) {
            if (DynamicCommanderHelpers.haveNoExistingOrPlanned(Terran_Starport)) {
                AddToQueue.toHave(Terran_Starport, 1, ProductionOrderPriority.HIGH);
                return;
            }
            if (DynamicCommanderHelpers.haveNoExistingOrPlanned(Terran_Control_Tower)) {
                AddToQueue.toHave(Terran_Control_Tower, 1, ProductionOrderPriority.HIGH);
                return;
            }

            produceScienceFacility();
        }

//        if (A.supplyUsed() >= 120 && needScienceFacility) {
//            int covertOps = Count.existingOrInProductionOrInQueue(Terran_Covert_Ops);
//            if (covertOps == 0) {
//                AddToQueue.toHave(Terran_Covert_Ops);
//                return;
//            }
//        }
    }

    private static boolean shouldProduce() {
        return A.supplyUsed() >= (Enemy.terran() ? 90 : 50)
            || A.canAfford(550, 200)
            || DynamicCommanderHelpers.enemyStrategy().isGoingHiddenUnits();
    }

    public static boolean produceScienceFacility() {
        return AddToQueue.toHave(Terran_Science_Facility, 1, ProductionOrderPriority.TOP);
    }
}
