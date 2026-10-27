package atlantis.production.dynamic.terran;

import atlantis.game.A;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;

public class TerranDynamicInfantry extends TerranDynamicUnitsCommander {

    public static boolean DEBUG = false;
//    public static boolean DEBUG = true;

    public static boolean needToSaveForFactory() {
        if (!A.hasMinerals(200)) {
            return Count.existingOrInProductionOrInQueue(AUnitType.Terran_Factory) > 0;
        }

        return false;
    }

//    private static void produceUnit(AUnit building, AUnitType type) {
//        building.tra in(type);
//    }
}
