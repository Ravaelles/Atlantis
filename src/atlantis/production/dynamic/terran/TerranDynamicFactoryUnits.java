package atlantis.production.dynamic.terran;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.production.dynamic.terran.units.ProduceGoliaths;
import atlantis.production.dynamic.terran.units.ProduceTanks;
import atlantis.production.dynamic.terran.units.ProduceVultures;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;

public class TerranDynamicFactoryUnits extends TerranDynamicUnitsCommander {

    protected static boolean handleFactoryProduction() {
        return false;

//        if (!Have.factory()) return false;
//
//        for (AUnit factory : Select.ourOfType(AUnitType.Terran_Factory).free().list()) {
//            requestFactoryUnit(factory);
//        }
//
//        return false;
    }

//    protected static boolean requestFactoryUnit(AUnit factory) {
//        if (ProduceGoliaths.goliaths(factory)) {
////            return true;
//        }
//        if (ProduceTanks.tanks(factory)) {
////            return true;
//        }
//        if (ProduceVultures.vultures()) {
////            return true;
//        }
//
//        return false;
//    }
}
