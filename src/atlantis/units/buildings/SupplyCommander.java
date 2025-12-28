package atlantis.units.buildings;

import atlantis.architecture.Commander;
import atlantis.cherryvis.CV;
import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.build.BuildOrderSettings;
import atlantis.production.orders.production.queue.order.ForcedDirectProductionOrder;
import atlantis.production.orders.production.queue.order.OrderStatus;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.orders.zerg.ProduceZergUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.TimeMoment;
import atlantis.util.We;

public class SupplyCommander extends Commander {
    private TimeMoment lastAdded = new TimeMoment(0);
    private int supplyTotal;
    private int supplyFree;
    private int requestedConstructionsOfSupply;

    @Override
    public boolean applies() {
        return A.everyNthGameFrame(11);
    }

    @Override
    protected boolean handle() {
        supplyTotal = AGame.supplyTotal();
        supplyFree = AGame.supplyFree();

        if (supplyTotal >= 200) return false;
        if (supplyFree >= 12) return false;
        if (lastAdded.lessThanSecondsAgo(5)) return false;

        if (supplyFree >= 4 && supplyTotal <= 80 && Count.ourUnfinished(type()) > (A.minerals() <= 310 ? 0 : 1))
            return false;

//        if (CountInQueue.count(AtlantisRaceConfig.SUPPLY) >= 2) return;
//        if (Queue.get().nonCompleted().ofType(AtlantisRaceConfig.SUPPLY).size() >= 2) return;

        requestedConstructionsOfSupply = requestedConstructionsOfSupply();

        if (tooManyNotStartedConstructions()) {
//            ErrorLog.printMaxOncePerMinute(
//                "Too many not started constructions of supply: " + requestedConstructionsOfSupply
//            );
            return false;
        }

//        if (isSupplyVeryLow()) {
////            ErrorLog.printMaxOncePerMinute(
////                "Supply free is very low, force additional ("
////                    + A.supplyUsed() + "/" + A.supplyTotal() + ")"
////            );
//            requestAdditionalSupply();
//            return;
//        }

        if (tooFewSupplyLeftAsForGateways()) {
            requestAdditionalSupply();
            return false;
        }

        // Fix for UMS maps
        if (A.isUms() && AGame.supplyFree() <= 1) {
            requestAdditionalSupply();
            return false;
        }

        // Should use auto supply manager

        if (supplyTotal >= BuildOrderSettings.autoSupplyManagerWhenSupplyExceeds()) {
            int suppliesBeingBuilt = requestedConstructionsOfSupply;
            boolean noSuppliesBeingBuilt = suppliesBeingBuilt == 0;
            if (supplyTotal <= 11) {
                if (supplyFree <= 2 && noSuppliesBeingBuilt) {
                    requestAdditionalSupply();
                }
            }
            else if (supplyTotal <= 20) {
                if (supplyFree <= 3 && noSuppliesBeingBuilt) {
                    requestAdditionalSupply();
                }
            }
            else if (supplyTotal <= 36) {
                if (supplyFree <= 4 && noSuppliesBeingBuilt || (supplyFree <= 0 && A.hasMinerals(300))) {
                    requestAdditionalSupply();
                }
            }
            else if (supplyTotal <= 46) {
                if (supplyFree <= 8 && noSuppliesBeingBuilt || (supplyFree <= 1 && A.hasMinerals(350))) {
                    requestAdditionalSupply();
                }
            }
            else if (supplyTotal <= 75) {
                if (
                    (supplyFree <= 10 && suppliesBeingBuilt <= (A.hasMinerals(400) ? 2 : 1))
                        || (supplyFree <= 7 && suppliesBeingBuilt <= (1 + A.minerals() / 250))
                        || (supplyFree <= 4 && suppliesBeingBuilt <= (1 + A.minerals() / 200))
                ) {
                    requestAdditionalSupply();
                }
            }
            else {
//                if (supplyFree <= 7 && suppliesBeingBuilt <= (1 + A.minerals() / 250)) {
//                    requestAdditionalSupply();
//                    return;
//                }

                if (supplyFree <= 4 && suppliesBeingBuilt <= 2) {
                    requestAdditionalSupply();
                    return noSuppliesBeingBuilt;
                }

                if (supplyFree <= 6 && suppliesBeingBuilt <= 1) {
                    requestAdditionalSupply();
                    return noSuppliesBeingBuilt;
                }

                if (supplyFree <= 12 && suppliesBeingBuilt <= 0) {
                    requestAdditionalSupply();
                    return noSuppliesBeingBuilt;
                }

                if (supplyTotal <= 120) {
                    if (suppliesBeingBuilt <= 1 && supplyFree <= (12 / Math.max(1, suppliesBeingBuilt))) {
                        requestAdditionalSupply();
                    }
                }
                else if (supplyTotal <= 170) {
                    if (suppliesBeingBuilt <= 1 && supplyFree <= 12 || suppliesBeingBuilt <= (A.minerals() / 300)) {
                        requestAdditionalSupply();
                    }
                }
                else if (supplyTotal <= 200) {
                    if (suppliesBeingBuilt <= 1 && supplyFree <= 19) {
                        requestAdditionalSupply();
                    }
                }
            }
        }
        return false;
    }

    private AUnitType type() {
        return AtlantisRaceConfig.SUPPLY;
    }

    private boolean tooFewSupplyLeftAsForGateways() {
        return We.protoss()
            && A.supplyTotal() >= 30
            && !A.hasFreeSupply((A.hasMinerals(120) ? 1 : 0) + 2 * Count.gateways());
    }

    private boolean isSupplyVeryLow() {
        return supplyFree <= (A.supplyTotal() >= 50 ? 6 : 2)
            && A.hasMinerals(84)
            && requestedConstructionsOfSupply <= (A.supplyUsed() <= 33 ? 2 : 3);
    }

    // =========================================================

    private void requestAdditionalSupply() {
        int requestedConstructionsOfSupply = requestedConstructionsOfSupply();

//        A.printStackTrace("Supply request: "
//            + A.supplyUsed() + " // "
//            + A.supplyTotal() + " // F="
//            + requestedConstructionsOfSupply + " // G="
//            + Queue.get().nonCompleted().ofType(AtlantisRaceConfig.SUPPLY).size()
//        );

        int maxAtOnce = maxAtOnce();

        if (requestedConstructionsOfSupply >= maxAtOnce) {
//            System.err.println("TOO MANY REQUESTED SUPPLIES: " + requestedConstructionsOfSupply);
            return;
        }

        int lastRequestedAgo = Queue.get().history().lastHappenedAgo(AtlantisRaceConfig.SUPPLY.name());
        if (lastRequestedAgo <= 30 * 2) {
//            System.err.println("@" + A.now() + " SUPPLY TOO RECENTLY REQUESTED " + lastRequestedAgo);
            return;
        }

        if (Queue.get().inProgressOrders().ofType(AtlantisRaceConfig.SUPPLY).size() >= maxAtOnce) {
//            System.err.println("Too many SUPPLY!!!! " + Queue.get().inProgressOrders().ofType(AtlantisRaceConfig.SUPPLY).size());
            return;
        }

        int notFinished = ConstructionRequests.countNotFinishedOfType(AtlantisRaceConfig.SUPPLY);
        if (notFinished >= maxAtOnce) {
//            System.err.println("TOO MANY CONSTRS! " + notFinished);
            return;
        }

        // Zerg handles supply a bit differently
        if (We.zerg()) {
            ProduceZergUnit.produceZergUnit(
                AUnitType.Zerg_Overlord, ForcedDirectProductionOrder.create(AtlantisRaceConfig.WORKER)
            );
            return;
        }

        // Terran + Protoss
//        System.err.println("------------------ REQUEST SUPPLY AT " + A.supplyUsed() + " / " + A.supplyTotal()
//            + " / InProgress=" + Count.inProductionOrInQueue(AtlantisRaceConfig.SUPPLY));
        ProductionOrder order = AddToQueue.withTopPriority(AtlantisRaceConfig.SUPPLY);
        if (order != null) order.setStatus(OrderStatus.READY_TO_PRODUCE);
        if (order != null) order.setMinSupply(A.supplyUsed() - 1);

        if (order != null) {
//            int notFinished = ConstructionRequests.countNotFinishedOfType(AtlantisRaceConfig.SUPPLY);
            int pending = ConstructionRequests.countPendingOfType(AtlantisRaceConfig.SUPPLY);
            CV.globalLog("Requested SUPPLY: (" + A.supplyUsed() + "/ " + A.supplyTotal() + "), "
                + "nf:" + notFinished
                + (pending > 0 ? (", pen:" + pending) : "")
            );
        }
    }

    private static int maxAtOnce() {
        if (We.protoss() && !A.hasFreeSupply(1)) return 2;

        return A.seconds() <= 300 ? 1 : 2;
    }

    private boolean tooManyNotStartedConstructions() {
        if (requestedConstructionsOfSupply >= 1 && A.supplyTotal() <= 38) return true;

        int limit = (A.supplyUsed() >= 70 ? 3 : 2)
            + A.minerals() / 300
            + A.supplyFree() <= 2 ? 1 : 0
            + A.supplyFree() <= 1 ? 1 : 0
            + A.supplyFree() <= 0 ? 1 : 0;

        if (requestedConstructionsOfSupply >= limit) return true;

        if (preventDoubleSupplyInEarlyGameWhenConstructionExists()) return true;

        return false;
    }

    private boolean preventDoubleSupplyInEarlyGameWhenConstructionExists() {
        if (A.supplyUsed() <= 80 && !A.hasMinerals(400)) {
            int existing = ConstructionRequests.countPendingOfType(AtlantisRaceConfig.SUPPLY);
            if (existing >= 1) {
                return true;
            }
        }

        return false;
    }

    private int requestedConstructionsOfSupply() {
        if (We.zerg()) {
//            return Count.ourOfTypeWithUnfinished(AUnitType.Zerg_Overlord);
            return Count.inProductionOrInQueue(AUnitType.Zerg_Overlord);
        }

        return Math.max(
            ConstructionRequests.countNotFinishedOfType(AtlantisRaceConfig.SUPPLY),
            CountInQueue.countInProgress(AtlantisRaceConfig.SUPPLY)
        );

//        // =========================================================
//        // Terran + Protoss
//        else {
//            return Select.ourUnfinished().ofType(AUnitType.UnitTypes.Zerg_Overlord).count();
//        }
    }

}
