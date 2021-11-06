package atlantis.information;

import atlantis.units.AUnitType;
import atlantis.units.select.Select;

public class TerranArmyComposition {

    public static double medicsToInfantry() {
        return (double) Select.countOurOfType(AUnitType.Terran_Medic) / Select.ourTerranInfantry().count();
    }

}
