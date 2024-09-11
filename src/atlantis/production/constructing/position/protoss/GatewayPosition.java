package atlantis.production.constructing.position.protoss;

import atlantis.map.position.HasPosition;
import atlantis.production.constructing.position.FindPosition;
import atlantis.production.constructing.position.MaxBuildingDist;
import atlantis.units.AUnitType;
import atlantis.units.workers.FreeWorkers;

public class GatewayPosition {
    public static HasPosition nextPosition() {
//        return APositionFinder.findStandardPosition(
        return FindPosition.findForBuilding(
            FreeWorkers.get().first(),
            AUnitType.Protoss_Gateway,
            null,
            null,
            MaxBuildingDist.MAX_DIST
        );
    }
}
