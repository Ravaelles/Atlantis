package atlantis.units.buildings;

import atlantis.architecture.Commander;
import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.build.BuildOrderSettings;
import atlantis.production.orders.build.CurrentBuildOrder;
import atlantis.production.orders.build.ZergBuildOrder;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.We;

public class SupplyCommander extends Commander {
    private int supplyTotal;
    private int supplyFree;

    @Override
    protected void handle() {
        supplyTotal = AGame.supplyTotal();

        if (supplyTotal >= 200) {
            return;
        }

        int requestedConstructionsOfSupply = requestedConstructionsOfSupply();

        if (requestedConstructionsOfSupply >= 3) return;

        if (!A.hasFreeSupply(1) && A.supplyUsed() <= 170 && A.hasMinerals(300)) {
            if (requestedConstructionsOfSupply <= 2) {
                requestAdditionalSupply();
                return;
            }
        }

        if (requestedConstructionsOfSupply >= 1 && A.supplyTotal() <= 50) {
            return;
        }

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
            else if (supplyTotal <= 43) {
                if (supplyFree <= 5 && noSuppliesBeingBuilt) {
                    requestAdditionalSupply();
                }
            }
            else if (supplyTotal <= 70) {
                if (supplyFree <= 8 && suppliesBeingBuilt <= 1) {
                    requestAdditionalSupply();
                }
            }
            else if (supplyTotal <= 170) {
                if (supplyFree <= 12 && suppliesBeingBuilt <= 1) {
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
//        A.printStackTrace("Supply request: " + A.supplyUsed() + " // " + A.supplyTotal());

        int requestedConstructionsOfSupply = requestedConstructionsOfSupply();

//        if (requestedConstructionsOfSupply >= 2) System.err.println("@ " + A.now() + " - requestedConstructionsOfSupply = " + requestedConstructionsOfSupply);

        if (requestedConstructionsOfSupply >= 2) return;

        // Zerg handles supply a bit differently
        if (AGame.isPlayingAsZerg()) {
            ((ZergBuildOrder) CurrentBuildOrder.get()).produceZergUnit(AUnitType.Zerg_Overlord);
            return;
        }

        // Terran + Protoss
        AddToQueue.withHighPriority(AtlantisRaceConfig.SUPPLY);
    }

    private boolean requestedConstructionOfSupply() {
        return ConstructionRequests.countNotStartedOfType(AtlantisRaceConfig.SUPPLY) > 0;
    }

    private int requestedConstructionsOfSupply() {
        if (We.zerg()) {
//            return Count.ourOfTypeWithUnfinished(AUnitType.Zerg_Overlord);
            return Count.inProductionOrInQueue(AUnitType.Zerg_Overlord);
        }

        return Count.inProductionOrInQueue(AtlantisRaceConfig.SUPPLY);
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
