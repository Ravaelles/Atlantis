package atlantis.production.dynamic.protoss.units;

import atlantis.game.A;
import atlantis.information.decisions.Decisions;
import atlantis.information.generic.OurArmy;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.order.ForcedDirectProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.util.Enemy;

import static atlantis.production.AbstractDynamicUnits.trainIfPossible;
import static atlantis.units.AUnitType.*;

public class ProduceDragoon {
    public static boolean dragoon() {
        if (!A.hasGas(50) || !A.hasMinerals(125)) return false;
        if (noProperBuildings()) return false;
        if (waitForDT()) return false;

        int dragoons = Count.dragoons();

        if (dragoons <= 6) return produceDragoon();
        if (againstEarlyProtossRush()) return produceDragoon();

        if (dragoons <= 15 && Count.basesWithUnfinished() >= 2) return produceDragoon();

//        if (A.hasGas(50) && A.supplyUsed() <= 38) return produceDragoon();

        if ((!A.hasMinerals(200) || !A.hasGas(100))) return false;
        if (A.supplyUsed() >= 50 && (!A.hasMinerals(210))) return false;

        if (dragoons >= 6) {
            if (!A.hasGas(50) && Decisions.needToProduceZealotsNow()) return false;
            if (!A.hasMineralsAndGas(300, 170) && !A.canAffordWithReserved(125, 50)) return false;
        }

        if ((A.supplyUsed() <= 38 || Count.observers() >= 1)) {
//            trainIfPossible(AUnitType.Protoss_Dragoon, false, 125, 50);
            return produceDragoon();
        }

        return A.hasGas(175) && produceDragoon();
    }

    private static boolean waitForDT() {
        return A.seconds() <= 500
            && !A.canAfford(300, 220)
            && CountInQueue.count(Protoss_Dark_Templar, 6) > 0;
    }

    private static boolean againstEarlyProtossRush() {
        return Enemy.protoss()
            && (OurArmy.strength() <= 95 || OurArmy.strengthWithoutCB() <= 80)
            && A.seconds() <= 12 * 60;
    }

    private static boolean noProperBuildings() {
        return Count.freeGateways() == 0 || !Have.cyberneticsCore();
    }

    private static boolean produceDragoon() {
        boolean result = GatewayClosestToEnemy.get().train(
            Protoss_Dragoon, ForcedDirectProductionOrder.create(Protoss_Dragoon)
        );
//        System.err.println("ProduceDragoon = " + result);
        return result;
    }
}
