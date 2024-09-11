package atlantis.production.constructing;

import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.util.log.ErrorLog;

import java.util.ArrayList;

public class DefineConstructionForNewUnit {
    public static Construction defineConstruction(AUnit newBuilding) {
        ArrayList<Construction> notStartedConstructions = ConstructionRequests.notStartedOfType(newBuilding.type());

        if (notStartedConstructions.isEmpty() && !newBuilding.type().isAddon()) {
            ErrorLog.printMaxOncePerMinute(
                "!!! No not started constructions for " + newBuilding
                    + " / " + ConstructionRequests.countPendingOfType(newBuilding.type())
            );
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
            if (!newBuilding.type().isAddon()) {
                ErrorLog.printMaxOncePerMinute("!!! nearestConstruction not found for " + newBuilding);
            }
            return null;
        }

        return nearestConstruction;
    }
}
