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
        if (
            Have.notEvenPlanned(AUnitType.Protoss_Gateway) || Have.notEvenPlanned(AUnitType.Protoss_Cybernetics_Core)
        ) return false;
        if (Select.ourFree(Protoss_Gateway).isEmpty()) return false;

//        if (!A.hasGas(50) && !A.hasMinerals(125)) {
//            return;
//        }

        if (
            Decisions.needToProduceZealotsNow()
                && !A.hasGas(50)
//                && !A.hasMinerals(225)
        ) return false;

        if (!A.hasMineralsAndGas(700, 250) && !A.canAffordWithReserved(125, 50)) return false;

        if ((A.supplyUsed() <= 38 || Count.observers() >= 1)) {
//            trainIfPossible(AUnitType.Protoss_Dragoon, false, 125, 50);
            return produceDragoon();
        }

        if (A.hasGas(100) && A.supplyUsed() <= 38) {
            return produceDragoon();
        }

//        if (ProtossArmyComposition.zealotsToDragoonsRatioTooLow()) {
//            return;
//        }

        return trainIfPossible(Protoss_Dragoon);
    }

    private static boolean produceDragoon() {
        return Select.ourFree(Protoss_Gateway).random().train(
            Protoss_Dragoon, ForcedDirectProductionOrder.create(Protoss_Dragoon)
        );
//        return AddToQueue.maxAtATime(Protoss_Dragoon, freeGateways()) != null;
    }
}
