package atlantis.production.dynamic.terran.buildings;

import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.production.dynamic.DynamicCommanderHelpers;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrderPriority;
import atlantis.units.select.Have;
import atlantis.game.player.Enemy;

import static atlantis.units.AUnitType.*;

public class ProduceScienceFacility {
    private static boolean needToProduce;

    public static boolean scienceFacilities() {
        if (Have.scienceFacility()) return false;

        needToProduce = needToProduce();
        if (!needToProduce) return false;

        if (!Have.starport()) return dontHaveStarport();

//        if (
//            Have.a(Terran_Science_Facility)
//                || CountInQueue.count(Terran_Science_Facility, 6) > 0
//        ) return false;

//        if (A.supplyUsed(39)) System.err.println("shouldProduce() SCI FACI A = " + shouldProduce());

        if (needToProduce) {
//            if (DynamicCommanderHelpers.haveNoExistingOrPlanned(Terran_Starport)) {
////                if (A.supplyUsed(39)) System.err.println("shouldProduce() SCI FACI B = ");
//                return AddToQueue.toHave(Terran_Starport, 1, ProductionOrderPriority.TOP);
//            }
            if (DynamicCommanderHelpers.haveNoExistingOrPlanned(Terran_Control_Tower)) {
//                if (A.supplyUsed(39)) System.err.println("shouldProduce() SCI FACI C = ");
                if (AddToQueue.toHave(Terran_Control_Tower, 1, ProductionOrderPriority.TOP)) {
                    System.err.println("--------------- Enqueue CONTROL TOWER");
                    return true;
                }
            }

//            if (A.supplyUsed(39)) System.err.println("shouldProduce() SCI FACI YEEEEEEEES = ");
            return produceScienceFacility();
        }

//        if (A.supplyUsed() >= 120 && needScienceFacility) {
//            int covertOps = Count.existingOrInProductionOrInQueue(Terran_Covert_Ops);
//            if (covertOps == 0) {
//                AddToQueue.toHave(Terran_Covert_Ops);
//                return;
//            }
//        }
        return false;
    }

    private static boolean dontHaveStarport() {
        if (!Have.haveExistingOrInPlans(Terran_Science_Facility)) {
            if (AddToQueue.toHave(Terran_Starport, 1, ProductionOrderPriority.TOP)) {
                System.err.println("--------------- Enqueue STARPORT");
            }
        }

        AddToQueue.toHave(Terran_Science_Facility, 1, ProductionOrderPriority.TOP);

        return true;
    }

    private static boolean needToProduce() {
        if (A.supplyUsed() <= 60) return false;

        return A.supplyUsed() >= (Enemy.terran() ? 90 : 50)
            || A.canAfford(550, 200)
            || DynamicCommanderHelpers.enemyStrategy().isGoingHiddenUnits()
            || EnemyInfo.hasHiddenUnits();
    }

    public static boolean produceScienceFacility() {
        return AddToQueue.toHave(Terran_Science_Facility, 1, ProductionOrderPriority.TOP);
    }
}
