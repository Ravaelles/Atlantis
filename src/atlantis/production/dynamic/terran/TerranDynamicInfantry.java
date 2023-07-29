package atlantis.production.dynamic.terran;

import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.Enemy;

public class TerranDynamicInfantry extends TerranDynamicUnitsCommander {

    public static boolean DEBUG = false;
//    public static boolean DEBUG = true;

    public static boolean needToSaveForFactory() {
        if (!A.hasMinerals(200)) {
            if (
                    Count.existingOrInProduction(AUnitType.Terran_Factory) == 0
                    && Count.inQueue(AUnitType.Terran_Factory, 2) > 0
            ) {
                return true;
            }
        }

        return false;
    }

    private static void produceUnit(AUnit building, AUnitType type) {
        building.train(type);
    }
}
