package atlantis.production;

import atlantis.combat.micro.terran.TerranCommandCenter;
import atlantis.combat.micro.terran.TerranComsatStation;
import atlantis.protoss.ProtossShieldBattery;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class ABuildingManager {

    public static boolean update() {
        for (AUnit building : Select.ourBuildings().listUnits()) {

            if (building.isType(AUnitType.Terran_Comsat_Station)) {
                return TerranComsatStation.update(building);
            }
            else if (building.isType(AUnitType.Terran_Command_Center)) {
                return TerranCommandCenter.update(building);
            }
            else if (building.isType(AUnitType.Protoss_Shield_Battery)) {
                return ProtossShieldBattery.update(building);
            }

        }

        return false;
    }

}
