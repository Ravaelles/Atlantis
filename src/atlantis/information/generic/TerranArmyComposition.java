package atlantis.information.generic;

import atlantis.units.AUnitType;
import atlantis.units.select.Select;

public class TerranArmyComposition {

    public static boolean medicsToInfantryRatioTooLow() {
        return ((double) Select.countOurOfTypeWithUnfinished(AUnitType.Terran_Medic) / Select.ourTerranInfantry().count()) <= 0.21;
    }

}
