package atlantis.production;

import atlantis.combat.micro.terran.TerranCommandCenter;
import atlantis.combat.micro.terran.TerranComsatStation;
import atlantis.protoss.ProtossShieldBattery;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

public class ABuildingManager {

    public static boolean update() {
        for (AUnit building : Select.ourBuildings().listUnits()) {
            System.out.println(building.shortName());

            if (building.is(AUnitType.Terran_Comsat_Station)) {
                return TerranComsatStation.update(building);
            }
            else if (building.is(AUnitType.Terran_Command_Center)) {
                return TerranCommandCenter.update(building);
            }
            else if (building.is(AUnitType.Protoss_Shield_Battery)) {
                return ProtossShieldBattery.update(building);
            }
        }

        return false;
    }

}
