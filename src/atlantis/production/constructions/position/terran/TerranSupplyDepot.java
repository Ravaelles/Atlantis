package atlantis.production.constructions.position.terran;

import atlantis.map.position.HasPosition;
import atlantis.production.constructions.position.FindPosition;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.workers.FreeWorkers;

public class TerranSupplyDepot {
    public static HasPosition nextPosition() {
        return FindPosition.findForBuilding(
            FreeWorkers.get().first(),
            AUnitType.Terran_Supply_Depot,
            null,
            Select.mainOrAnyBuilding(),
            -1
        );
    }
}
