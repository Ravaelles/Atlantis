package atlantis.production.constructing;

import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.Units;
import atlantis.units.select.Select;
import atlantis.units.workers.GatherResources;
import atlantis.util.log.ErrorLog;

import java.util.ArrayList;

public class ProtossWarping {

    /**
     * Handle construction fix: detect new Protoss buildings and remove them from construction queue.
     * It's because the construction of Protoss buildings is immediate, and we have no way of telling
     * that the Probe has actually started a construction.
     */
    public static void handleNewBuildingWarped(AUnit newBuilding) {
        assert newBuilding.isABuilding();

        Construction construction = defineConstruction(newBuilding);

        if (construction == null) return;

        construction.setStatus(ConstructionOrderStatus.IN_PROGRESS);
        construction.setBuild(newBuilding);

        if (construction.builder() != null) (new GatherResources(construction.builder())).forceHandle();

        construction.setBuilder(null);
    }

    private static Construction defineConstruction(AUnit newBuilding) {
        ArrayList<Construction> notStartedConstructions = ConstructionRequests.notStartedOfType(newBuilding.type());

        if (notStartedConstructions.isEmpty()) {
            ErrorLog.printMaxOncePerMinute("!!! No not started constructions for " + newBuilding);
            return null;
        }

        int nearest = -1;
        Construction nearestConstruction = null;

        for (Construction construction : notStartedConstructions) {
            APosition position = construction.buildPosition();
            if (construction.builder() == null || position == null) continue;

            if (nearestConstruction == null || position.distTo(newBuilding) < nearest) {
                nearestConstruction = construction;
                nearest = (int) position.distTo(newBuilding);
            }
        }

        if (nearestConstruction == null) {
            ErrorLog.printMaxOncePerMinute("!!! nearestConstruction not found for " + newBuilding);
            return null;
        }

        return nearestConstruction;
    }

}
