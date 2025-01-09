package atlantis.production.constructing.protoss;

import atlantis.production.constructing.Construction;
import atlantis.production.constructing.ConstructionOrderStatus;
import atlantis.production.constructing.DefineConstructionForNewUnit;
import atlantis.units.AUnit;
import atlantis.units.workers.GatherResources;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;

public class ProtossWarping {

    /**
     * Handle construction fix: detect new Protoss buildings and remove them from construction queue.
     * It's because the construction of Protoss buildings is immediate, and we have no way of telling
     * that the Probe has actually started a construction.
     */
    public static void updateNewBuildingJustWarped(AUnit newBuilding) {
        if (!We.protoss() || !newBuilding.type().isABuilding()) return;

        Construction construction = DefineConstructionForNewUnit.defineConstruction(newBuilding);
        if (construction == null) {
            System.err.println("CANNOT define construction for " + newBuilding);
            return;
        }

        construction.setStatus(ConstructionOrderStatus.IN_PROGRESS);
        construction.setBuildingUnit(newBuilding);

        // Construction
        newBuilding.setConstruction(construction);

        // === Validate ===========================================

        if (!newBuilding.type().equals(construction.buildingType())) {
            ErrorLog.printMaxOncePerMinute("@@@@@ Building type mismatch for " + newBuilding + " / " + construction);
        }

        // ========================================================

        // Production order
        if (newBuilding.productionOrder() == null) {
            newBuilding.setProductionOrder(construction.productionOrder());
        }

        if (construction.builder() != null) (new GatherResources(construction.builder())).forceHandle();
        construction.setBuilder(null);
    }
}
