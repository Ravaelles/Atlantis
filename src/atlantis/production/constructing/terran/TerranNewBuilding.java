package atlantis.production.constructing.terran;

import atlantis.production.constructing.Construction;
import atlantis.production.constructing.ConstructionOrderStatus;
import atlantis.production.constructing.DefineConstructionForNewUnit;
import atlantis.units.AUnit;
import atlantis.units.workers.GatherResources;
import atlantis.util.We;

public class TerranNewBuilding {
    public static void updateNewBuilding(AUnit newBuilding) {
        if (!We.terran() || !newBuilding.isABuilding()) return;

        Construction construction = DefineConstructionForNewUnit.defineConstruction(newBuilding);

        if (construction == null) return;

        construction.setStatus(ConstructionOrderStatus.IN_PROGRESS);
        construction.setBuild(newBuilding);
        newBuilding.setConstruction(construction);

        if (construction.builder() != null) (new GatherResources(construction.builder())).forceHandle();

        construction.setBuilder(null);
    }
}
