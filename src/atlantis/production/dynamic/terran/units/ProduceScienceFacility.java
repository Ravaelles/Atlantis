package atlantis.production.dynamic.terran.units;

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
        if (Have.haveExistingOrInPlans(Terran_Science_Facility)) return;

        boolean needScienceFacility = DynamicCommanderHelpers.haveNoExistingOrPlanned(Terran_Science_Facility);

        if (A.supplyUsed() >= (Enemy.terran() ? 90 : 50) || DynamicCommanderHelpers.enemyStrategy().isGoingHiddenUnits()) {
            if (DynamicCommanderHelpers.haveNoExistingOrPlanned(Terran_Starport)) {
                AddToQueue.toHave(Terran_Starport, 1, ProductionOrderPriority.HIGH);
                return;
            }
            if (DynamicCommanderHelpers.haveNoExistingOrPlanned(Terran_Control_Tower)) {
                AddToQueue.toHave(Terran_Control_Tower, 1, ProductionOrderPriority.HIGH);
                return;
            }
            if (needScienceFacility) {
                AddToQueue.toHave(Terran_Science_Facility, 1, ProductionOrderPriority.HIGH);
                return;
            }
        }

//        int scienceFacilities = Count.existingOrInProductionOrInQueue(Terran_Science_Facility);
//        if (A.supplyUsed() >= 60) {
//            if (scienceFacilities == 0) {
//                AddToQueue.withHighPriority(Terran_Science_Facility);
//                return;
//            }
//        }

        if (A.supplyUsed() >= 120 && needScienceFacility) {
            int covertOps = Count.existingOrInProductionOrInQueue(Terran_Covert_Ops);
            if (covertOps == 0) {
                AddToQueue.withHighPriority(Terran_Covert_Ops);
                return;
            }
        }
    }
}
