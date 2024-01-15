package atlantis.production.dynamic.protoss.units;

import atlantis.game.A;
import atlantis.information.decisions.Decisions;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.strategy.GamePhase;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ForcedDirectProductionOrder;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;

import static atlantis.production.AbstractDynamicUnits.trainIfPossible;
import static atlantis.production.dynamic.protoss.ProtossDynamicUnitsCommander.freeGateways;
import static atlantis.units.AUnitType.*;
import static atlantis.units.AUnitType.Terran_Marine;

public class ProduceDragoon {
    public static boolean dragoon() {
        if (!A.hasGas(50)) return false;

        int dragoons = Count.dragoons();

        if (noProperBuildings()) return false;
        if (dragoons >= 3 && (!A.hasMinerals(250) || !A.hasGas(150))) return false;
        if (A.supplyUsed() >= 50 && (!A.hasMinerals(300) || !A.hasGas(200))) return false;
        if (Decisions.needToProduceZealotsNow() && !A.hasGas(50)) return false;
        if (!A.hasMineralsAndGas(700, 250) && !A.canAffordWithReserved(125, 50)) return false;

        if ((A.supplyUsed() <= 38 || Count.observers() >= 1)) {
//            trainIfPossible(AUnitType.Protoss_Dragoon, false, 125, 50);
            return produceDragoon();
        }

        if (A.hasGas(50) && A.supplyUsed() <= 38) {
            return produceDragoon();
        }

        return A.hasGas(200) && produceDragoon();
    }

    private static boolean noProperBuildings() {
        return Select.ourFree(Protoss_Gateway).isEmpty()
            || Have.notEvenPlanned(AUnitType.Protoss_Gateway)
            || Have.notEvenPlanned(AUnitType.Protoss_Cybernetics_Core);
    }

    private static boolean produceDragoon() {
        return Select.ourFree(Protoss_Gateway).random().train(
            Protoss_Dragoon, ForcedDirectProductionOrder.create(Protoss_Dragoon)
        );
//        return AddToQueue.maxAtATime(Protoss_Dragoon, freeGateways()) != null;
    }
}
