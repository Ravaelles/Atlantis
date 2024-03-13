package atlantis.production.dynamic.expansion.decision;

import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.production.constructing.Construction;
import atlantis.production.orders.production.queue.Queue;
import atlantis.units.select.Count;

public class CancelNotStartedBases {
    public static void cancelNotStartedOrEarlyBases() {
        if (A.seconds() >= 700 || Count.bases() >= 3) return;

//        if (CountInQueue.bases() > 0) {
//            List<ProductionOrder> orders = Queue.get().nonCompleted().ofType(AtlantisRaceConfig.BASE).list();
//            for (ProductionOrder order : orders) {
//                if (order.progressPercent() <= 49) {
//                    A.println("Cancel " + order.unitType() + " (" + order.progressPercent() + "%) - much weaker");
//                    order.cancel();
//                }
//
////                int progress = construction.progressPercent();
////                if (progress <= 49) {
////                    A.println("Cancel " + construction.buildingType() + " (" + progress + "%) - much weaker");
////                    construction.cancel();
////                }
//            }
//        }

        Queue.get().nonCompleted().ofType(AtlantisRaceConfig.BASE).forEach((order) -> {
            Construction construction = order.construction();
            if (shouldCancelBase(construction)) {
                A.errPrintln(A.now() + " Cancelling pending base " + order + " as other just finished!");
                order.cancel();
                return;
            }
        });
    }

    private static boolean shouldCancelBase(Construction construction) {
        return construction != null
            && (!construction.hasStarted() || construction.progressPercent() <= 49);
    }
}
