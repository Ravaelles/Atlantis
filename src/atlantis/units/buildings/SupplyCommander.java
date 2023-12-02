package atlantis.units.buildings;

import atlantis.architecture.Commander;
import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.build.BuildOrderSettings;
import atlantis.production.orders.production.queue.order.ForcedDirectProductionOrder;
import atlantis.production.orders.zerg.ProduceZergUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;

public class SupplyCommander extends Commander {
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
//        if (A.supplyTotal() <= 50 && A.hasFreeSupply(4)) return;
        if (A.hasFreeSupply(10)) return;

//        if (CountInQueue.count(AtlantisRaceConfig.SUPPLY) >= 2) return;
//        if (Queue.get().nonCompleted().ofType(AtlantisRaceConfig.SUPPLY).size() >= 2) return;

        requestedConstructionsOfSupply = requestedConstructionsOfSupply();

        if (tooManyNotStartedConstructions()) {
//            ErrorLog.printMaxOncePerMinute(
//                "Too many not started constructions of supply: " + requestedConstructionsOfSupply
//            );
            return;
        }

//        if (!A.hasFreeSupply(3) && A.supplyUsed() <= 170 && A.hasMinerals(300)) {
//            if (requestedConstructionsOfSupply <= 2 + A.supplyUsed() / 50) {
//                requestAdditionalSupply();
//                return;
//            }
//        }

        if (requestedConstructionsOfSupply >= 1 && A.supplyTotal() <= 50) return;

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
            else if (supplyTotal <= 46) {
                if (supplyFree <= 8 && noSuppliesBeingBuilt || supplyFree <= 1) {
                    requestAdditionalSupply();
                }
            }
            else if (supplyTotal <= 70) {
                if (supplyFree <= 11 && suppliesBeingBuilt <= 1 || supplyFree <= 1) {
                    requestAdditionalSupply();
                }
            }
            else if (supplyTotal <= 170) {
                if (supplyFree <= 13 && suppliesBeingBuilt <= 1) {
                    requestAdditionalSupply();
                }
            }
            else if (supplyTotal <= 200) {
                if (supplyFree <= 25 && suppliesBeingBuilt <= 1) {
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

        if (requestedConstructionsOfSupply >= 3) {
            System.err.println("TOO MANY REQUESTED SUPPLIES: " + requestedConstructionsOfSupply);
            return;
        }

        if (Queue.get().nonCompleted().ofType(AtlantisRaceConfig.SUPPLY).size() >= 3) {
            System.err.println("EXIT!!!! " + Queue.get().nonCompleted().ofType(AtlantisRaceConfig.SUPPLY).size());
            return;
        }

        // Zerg handles supply a bit differently
        if (AGame.isPlayingAsZerg()) {
            ProduceZergUnit.produceZergUnit(
                AUnitType.Zerg_Overlord, ForcedDirectProductionOrder.create(AtlantisRaceConfig.WORKER)
            );
            return;
        }

        // Terran + Protoss
        AddToQueue.withTopPriority(AtlantisRaceConfig.SUPPLY);
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
            CountInQueue.count(AtlantisRaceConfig.SUPPLY, 10)
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
