package atlantis.production.requests;

import atlantis.game.A;
import atlantis.production.constructing.Construction;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.util.We;

import java.util.ArrayList;
import java.util.List;

import static atlantis.units.AUnitType.*;

public class RemoveExcessiveOrders {
    public static void removeExcessive(AUnitType type) {
        if (We.protoss()) {
            limitToMaxAtOnce(type, limitFor(type));
        }
        else if (We.terran()) {
            limitToMaxAtOnce(type, limitFor(type));
        }
    }

    private static int limitFor(AUnitType type) {
        if (type.isProtoss()) {
            if (Protoss_Pylon.equals(type)) return 2 + A.minerals() >= 500 ? 1 : (A.supplyUsed(60) ? 1 : 0);
            if (Protoss_Assimilator.equals(type)) return 2;
            if (Protoss_Nexus.equals(type)) return 1 + (A.hasMinerals(700) ? 1 : 0);
            if (Protoss_Observatory.equals(type)) return 1;
            if (Protoss_Observer.equals(type)) return 4;
        }
        else {
            if (Terran_Supply_Depot.equals(type)) return 2;
            if (Terran_Bunker.equals(type)) return 1;
        }

        return 9;
    }

    private static boolean limitToMaxAtOnce(AUnitType type, int max) {
        return type.isABuilding() ? limitBuildingsToMax(type, max) : limitUnitsToMax(type, max);
    }

    private static boolean limitUnitsToMax(AUnitType type, int max) {
        List<ProductionOrder> orders = Queue.get().nonCompleted().ofType(type).list();

        if (orders.size() > max) {
            for (int i = orders.size() - 1; i >= 0; i--) {
//                if (orders.get(i).isUnit() && orders.get(i).unitType().is(Protoss_Observer)) {
//                    A.printStackTrace("Observer excessive");
//                }

                orders.get(i).cancel();
//                A.errPrintln("@" + A.now() + " - CANCEL EXCESSIVE UNIT " + type);

            }
            return true;
        }

        return false;
    }

    private static boolean limitBuildingsToMax(AUnitType type, int max) {
        ArrayList<Construction> constructions = ConstructionRequests.notStartedOfType(type);

        if (constructions.size() > max) {
            for (int i = constructions.size() - 1; i >= 0; i--) {
                constructions.get(i).productionOrder().cancel();
                A.errPrintln("@" + A.now() + " - CANCEL EXCESSIVE BUILDING " + type);
            }
            return true;
        }

        return false;
    }
}
