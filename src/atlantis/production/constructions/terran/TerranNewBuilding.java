package atlantis.production.constructions.terran;

import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.DefineConstructionForNewUnit;
import atlantis.units.AUnit;
import atlantis.units.workers.GatherResources;
import atlantis.util.We;

public class TerranNewBuilding {
    public static void updateNewBuilding(AUnit newBuilding) {
        if (!We.terran() || !newBuilding.isABuilding()) return;

        Construction construction = DefineConstructionForNewUnit.defineConstruction(newBuilding);

        if (construction == null) return;

        construction.setStatus(ConstructionOrderStatus.IN_PROGRESS);
        construction.setBuildingUnit(newBuilding);
        newBuilding.setConstruction(construction);

        if (construction.builder() != null) (new GatherResources(construction.builder())).forceHandle();

        construction.setBuilder(null);
    }
}
