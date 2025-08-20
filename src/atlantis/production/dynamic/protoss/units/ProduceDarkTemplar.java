package atlantis.production.dynamic.protoss.units;

import atlantis.game.A;
import atlantis.production.orders.production.queue.order.ForcedDirectProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;

import static atlantis.production.AbstractDynamicUnits.buildToHave;

public class ProduceDarkTemplar {
    public static int requested = 0;

    public static boolean dt() {
//        if (true) return false;

        if (Have.no(requiredBuilding())) return false;

        int maxDT = haveThisManyHT();
        if (requested >= 10 && Count.ourWithUnfinished(type()) >= maxDT) return false;

        return produce();
    }

    private static boolean produce() {
//        if (buildToHave(type(), maxDT)) {
//            requested++;
//            return true;
//        }

        AUnit freeGateway = GatewayClosestToEnemy.get();
        if (freeGateway == null) return false;

        boolean result = freeGateway.train(
            type(), ForcedDirectProductionOrder.create(type())
        );
        if (result) requested++;

        return result;
    }

    private static AUnitType type() {
        return AUnitType.Protoss_Dark_Templar;
    }

    private static AUnitType requiredBuilding() {
        return AUnitType.Protoss_Templar_Archives;
    }

    private static int haveThisManyHT() {
        int supply = A.supplyUsed();

        if (supply <= 80) return 2;
        if (supply <= 110) return 3;
        if (supply <= 140) return 4;
        if (supply <= 160) return 5;

        return 6;
    }
}
