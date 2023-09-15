package atlantis.production.dynamic.terran;

import atlantis.game.AGame;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.production.dynamic.terran.units.ProduceTanks;
import atlantis.production.dynamic.terran.units.ProduceVultures;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;

public class TerranDynamicFactoryUnits extends TerranDynamicUnitsCommander {

    protected static void handleFactoryProduction() {
//        if (!AGame.canAfford(200, 150) && !AGame.canAffordWithReserved(150, 100)) {
//            return;
//        }

        if (!Have.factory()) {
            return;
        }

        for (AUnit factory : Select.ourOfType(AUnitType.Terran_Factory).free().list()) {
            requestFactoryUnit(factory);
        }
    }

    protected static boolean requestFactoryUnit(AUnit factory) {
        if (goliaths(factory)) {
//            return true;
        }
        if (ProduceTanks.tanks(factory)) {
//            return true;
        }
        if (ProduceVultures.vultures()) {
//            return true;
        }

        return false;
    }

    private static boolean goliaths(AUnit factory) {
        if (!Have.armory()) return false;

        if (EnemyStrategy.get().isAirUnits() && Count.withPlanned(AUnitType.Terran_Goliath) <= 20) {
            if (AGame.canAffordWithReserved(150, 100)) {
                return AddToQueue.addToQueueIfNotAlreadyThere(AUnitType.Terran_Goliath);
            }
        }

        return false;
    }

}
