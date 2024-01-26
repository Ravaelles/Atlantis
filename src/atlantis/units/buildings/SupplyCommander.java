package atlantis.units.buildings;

import atlantis.architecture.Commander;
import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.game.race.MyRace;
import atlantis.production.constructing.ConstructionRequests;
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
    protected void handle() {
        supplyTotal = AGame.supplyTotal();

        if (supplyTotal >= 200) return;
        if (A.hasFreeSupply(9)) return;
        if (lastAdded.lessThanSecondsAgo(7)) return;

//        if (CountInQueue.count(AtlantisRaceConfig.SUPPLY) >= 2) return;
//        if (Queue.get().nonCompleted().ofType(AtlantisRaceConfig.SUPPLY).size() >= 2) return;

        requestedConstructionsOfSupply = requestedConstructionsOfSupply();

        if (tooManyNotStartedConstructions()) {
//            ErrorLog.printMaxOncePerMinute(
//                "Too many not started constructions of supply: " + requestedConstructionsOfSupply
//            );
            return;
        }

        if (requestedConstructionsOfSupply >= 1 && A.supplyTotal() <= 48) return;

        // Fix for UMS maps
        if (A.isUms() && AGame.supplyFree() <= 1) {
            requestAdditionalSupply();
            return;
        }

        // Should use auto supply manager

        if (supplyTotal >= BuildOrderSettings.autoSupplyManagerWhenSupplyExceeds()) {
            supplyFree = AGame.supplyFree();

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
                if (supplyFree <= 4 && noSuppliesBeingBuilt || supplyFree <= 1) {
                    requestAdditionalSupply();
                }
            }
            else if (supplyTotal <= 46) {
                if (supplyFree <= 6 && noSuppliesBeingBuilt || supplyFree <= 1) {
                    requestAdditionalSupply();
                }
            }
            else if (supplyTotal <= 70) {
                if (supplyFree <= 9 && suppliesBeingBuilt <= 1 || supplyFree <= 1) {
                    requestAdditionalSupply();
                }
            }
            else if (supplyTotal <= 170) {
                if (supplyFree <= 12 && suppliesBeingBuilt <= 1) {
                    requestAdditionalSupply();
                }
            }
            else if (supplyTotal <= 200) {
                if (supplyFree <= 15 && suppliesBeingBuilt <= 1) {
                    requestAdditionalSupply();
                }
            }
        }
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

        if (Queue.get().nonCompleted().ofType(AtlantisRaceConfig.SUPPLY).size() >= maxAtOnce) {
//            System.err.println("Too many SUPPLY!!!! " + Queue.get().nonCompleted().ofType(AtlantisRaceConfig.SUPPLY).size());
            return;
        }

        int notFinished = ConstructionRequests.countNotFinishedOfType(AUnitType.Terran_Supply_Depot);
        if (notFinished >= maxAtOnce) {
            System.err.println("TOO MANY CONSTRS! " + notFinished);
            return;
        }

        // Zerg handles supply a bit differently
        if (MyRace.isPlayingAsZerg()) {
            ProduceZergUnit.produceZergUnit(
                AUnitType.Zerg_Overlord, ForcedDirectProductionOrder.create(AtlantisRaceConfig.WORKER)
            );
            return;
        }

        // Terran + Protoss
//        System.err.println("------------------ REQUEST SUPPLY AT " + A.supplyUsed() + " / " + A.supplyTotal());
        ProductionOrder order = AddToQueue.withTopPriority(AtlantisRaceConfig.SUPPLY);
        if (order != null) order.setStatus(OrderStatus.READY_TO_PRODUCE);
    }

    private static int maxAtOnce() {
        if (We.protoss() && !A.hasFreeSupply(1)) return 1;

        return A.seconds() <= 300 ? 1 : 2;
    }

    private boolean tooManyNotStartedConstructions() {
        if (requestedConstructionsOfSupply >= 3) return true;

        return false;
//        return ConstructionRequests.countNotStartedOfType(AtlantisRaceConfig.SUPPLY) >= 4;
    }

    private int requestedConstructionsOfSupply() {
        if (We.zerg()) {
//            return Count.ourOfTypeWithUnfinished(AUnitType.Zerg_Overlord);
            return Count.inProductionOrInQueue(AUnitType.Zerg_Overlord);
        }

//        return Count.inProductionOrInQueue(AtlantisRaceConfig.SUPPLY);
//        System.out.println("A= " + Count.inProductionOrInQueue(AtlantisRaceConfig.SUPPLY));
//        System.out.println("B = " + Queue.get().nonCompleted().ofType(AtlantisRaceConfig.SUPPLY).size());
        return Math.max(
//            Queue.get().nonCompleted().ofType(AtlantisRaceConfig.SUPPLY).size(),
            ConstructionRequests.countNotFinishedOfType(AtlantisRaceConfig.SUPPLY),
            CountInQueue.count(AtlantisRaceConfig.SUPPLY)
        );
//        return ConstructionRequests.countNotFinishedOfType(AtlantisRaceConfig.SUPPLY);

//        return ConstructionRequests.countNotFinishedConstructionsOfType(AtlantisRaceConfig.SUPPLY);
//
//        // =========================================================
//        // Terran + Protoss
//        else {
//            return Select.ourUnfinished().ofType(AUnitType.UnitTypes.Zerg_Overlord).count();
//        }
    }

}
