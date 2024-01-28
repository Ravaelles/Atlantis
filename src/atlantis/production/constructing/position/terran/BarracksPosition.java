package atlantis.production.constructing.position.terran;

import atlantis.map.position.HasPosition;
import atlantis.production.constructing.position.FindPosition;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.workers.FreeWorkers;

public class BarracksPosition {
    public static HasPosition nextPosition() {
        return FindPosition.findForBuilding(
            FreeWorkers.get().first(),
            AUnitType.Terran_Barracks,
            null,
            Select.mainOrAnyBuilding(),
            -1
        );
    }
}
