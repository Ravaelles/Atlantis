package atlantis.production.constructing.protoss;

import atlantis.production.constructing.Construction;
import atlantis.production.constructing.ConstructionOrderStatus;
import atlantis.production.constructing.DefineConstructionForNewUnit;
import atlantis.units.AUnit;
import atlantis.units.workers.GatherResources;
import atlantis.util.We;

public class ProtossWarping {

    /**
     * Handle construction fix: detect new Protoss buildings and remove them from construction queue.
     * It's because the construction of Protoss buildings is immediate, and we have no way of telling
     * that the Probe has actually started a construction.
     */
    public static void updateNewBuildingJustWarped(AUnit newBuilding) {
        if (!We.protoss() || !newBuilding.type().isABuilding()) return;

        Construction construction = DefineConstructionForNewUnit.defineConstruction(newBuilding);

        if (construction == null) return;

        construction.setStatus(ConstructionOrderStatus.IN_PROGRESS);
        construction.setBuild(newBuilding);
        newBuilding.setConstruction(construction);

        if (construction.builder() != null) (new GatherResources(construction.builder())).forceHandle();

        construction.setBuilder(null);
    }
}
