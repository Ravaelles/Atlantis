package atlantis.production.dynamic.terran;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.production.dynamic.terran.units.ProduceTanks;
import atlantis.production.orders.build.AddToQueue;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.util.Enemy;

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
        if (vultures()) {
//            return true;
        }

        return false;
    }

    private static boolean vultures() {
//        return false;

        if (Enemy.terran()) {
            if (Count.ofType(AUnitType.Terran_Vulture) < 2 || (A.hasMinerals(600) && !A.hasGas(100))) {
                return AddToQueue.maxAtATime(AUnitType.Terran_Vulture, 5);
            }
        }

        return false;

//        if (true) return false;
//
//        if (!Decisions.produceVultures()) {
//            return false;
//        }
//
//        return AddToQueue.addToQueueIfNotAlreadyThere(AUnitType.Terran_Vulture);
    }

    private static boolean goliaths(AUnit factory) {
        if (!Have.armory()) {
            return false;
        }

        if (EnemyStrategy.get().isAirUnits() && Count.withPlanned(AUnitType.Terran_Goliath) <= 20) {
            if (AGame.canAffordWithReserved(150, 100)) {
                return AddToQueue.addToQueueIfNotAlreadyThere(AUnitType.Terran_Goliath);
            }
        }

        return false;
    }

}
