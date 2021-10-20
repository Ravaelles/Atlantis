package atlantis.production;

import atlantis.combat.micro.terran.TerranComsatStation;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;

public class ABuildingManager {

    public static boolean update() {
        for (AUnit building : Select.ourBuildings().listUnits()) {

            if (building.isType(AUnitType.Terran_Comsat_Station)) {
                return TerranComsatStation.update(building);
            }

        }

        return false;
    }

}
