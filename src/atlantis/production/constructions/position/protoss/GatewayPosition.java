package atlantis.production.constructions.position.protoss;

import atlantis.map.position.HasPosition;
import atlantis.production.constructions.position.FindPosition;
import atlantis.production.constructions.position.MaxBuildingDist;
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
