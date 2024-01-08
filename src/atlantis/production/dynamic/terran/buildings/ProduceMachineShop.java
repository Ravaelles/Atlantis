package atlantis.production.dynamic.terran.buildings;

import atlantis.game.A;
import atlantis.information.decisions.terran.TerranDecisions;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.select.Count;
import atlantis.units.select.Have;

import static atlantis.units.AUnitType.*;

public class ProduceMachineShop {
    /**
     * If there are buildings without addons, build them.
     */
    public static void produce() {
        if (TerranDecisions.DONT_PRODUCE_TANKS_AT_ALL.isTrue()) return;
        if (!Have.factory()) return;

//        if (AreWeGoingBio.check() && AreWeGoingBio.doNotFocusOnTanksForNow()) {
//            return;
//        }

        if (Count.factories() > Count.ofType(Terran_Machine_Shop)) {
            if (Count.inProductionOrInQueue(Terran_Machine_Shop) == 0) {
//                AddToQueue.maxAtATime(Terran_Machine_Shop, 1, ProductionOrderPriority.HIGH);
                if (A.canAfford(Terran_Machine_Shop)) {
                    produceMachineShop();
                }
                return;
            }
        }

//        if (
//                Decisions.wantsToBeAbleToProduceTanksSoon()
//                        || (A.supplyUsed(45) && !Have.machineShop())
//                        || AGame.canAffordWithReserved(150, 150)
//                        || A.supplyUsed(70)
//        ) {

    }

    private static boolean produceMachineShop() {
        return ProduceAddon.buildNow(Terran_Machine_Shop)
            && AddToQueue.withHighPriority(Terran_Siege_Tank_Tank_Mode) != null;
    }
}
