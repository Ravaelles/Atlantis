package atlantis.production;

import atlantis.combat.micro.terran.TerranCommandCenter;
import atlantis.combat.micro.terran.TerranComsatStation;
import atlantis.protoss.ProtossShieldBattery;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

public class ABuildingManager {

    public static boolean update() {
        for (AUnit building : Select.our().list()) {
//            System.out.println("#### = " + building + " // " + building.type() + " // " + building.bwapiType());

            if (building.is(AUnitType.Terran_Comsat_Station)) {
                TerranComsatStation.update(building);
            }
            else if (building.is(AUnitType.Terran_Command_Center)) {
                TerranCommandCenter.update(building);
            }
            else if (building.is(AUnitType.Protoss_Shield_Battery)) {
                ProtossShieldBattery.update(building);
            }
        }

        return false;
    }

}
