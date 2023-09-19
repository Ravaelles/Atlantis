package atlantis.production.dynamic.terran.buildings;

import atlantis.information.strategy.services.AreWeGoingBio;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.select.Count;
import atlantis.units.select.Have;

import static atlantis.units.AUnitType.Terran_Machine_Shop;

public class ProduceMachineShop {
    /**
     * If there are buildings without addons, build them.
     */
    public static void machineShop() {
        if (!Have.factory()) {
            return;
        }

//        if (
//            GamePhase.isEarlyGame()
//                && Count.vultures() <= 3
//                && EnemyUnits.discovered().ofType(Protoss_Zealot).atLeast(5)
//        ) {
//            return;
//        }

        if (AreWeGoingBio.check() && AreWeGoingBio.doNotFocusOnTanksForNow()) {
            return;
        }

        if (Count.factories() > Count.ofType(Terran_Machine_Shop)) {
            if (Count.inProductionOrInQueue(Terran_Machine_Shop) <= 1) {
                AddToQueue.withHighPriority(Terran_Machine_Shop);
                return;
            }
        }

//        if (
//                Decisions.wantsToBeAbleToProduceTanksSoon()
//                        || (A.supplyUsed(45) && !Have.machineShop())
//                        || AGame.canAffordWithReserved(150, 150)
//                        || A.supplyUsed(70)
//        ) {
//
//            for (AUnit building : Select.ourBuildings().list()) {
//                if (building.type().isFactory() && !building.hasAddon()) {
//                    AUnitType addonType = building.type().getRelatedAddon();
//                    if (addonType != null) {
//
//                        if (AGame.canAfford(addonType) && Count.inQueueOrUnfinished(addonType, 3) <= 1) {
////                            AddToQueue.withHighPriority(addonType);
//                            building.buildAddon(addonType);
//                            return;
//                        }
//                    }
//                }
//            }
//        }
    }
}
