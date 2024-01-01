package atlantis.production.constructing.position.terran;

import atlantis.map.position.HasPosition;
import atlantis.production.constructing.position.APositionFinder;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.workers.FreeWorkers;

public class TerranSupplyDepot {
    public HasPosition nextPosition() {
        return APositionFinder.findStandardPosition(
            FreeWorkers.get().first(),
            AUnitType.Terran_Supply_Depot,
            Select.mainOrAnyBuilding(),
            -1
        );
    }
}
