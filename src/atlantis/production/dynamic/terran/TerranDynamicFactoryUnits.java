package atlantis.production.dynamic.terran;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.information.decisions.Decisions;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;

public class TerranDynamicFactoryUnits extends TerranDynamicUnitsManager {

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
        if (tanks(factory)) {
//            return true;
        }
        if (vultures()) {
//            return true;
        }

        return false;
    }

    private static boolean tanks(AUnit factory) {
        if (!Have.machineShop() || !Have.factory()) {
            return false;
        }

        if (Decisions.dontProduceVultures() || Count.tanks() <= 0.4 * Count.vultures()) {
            return addToQueueIfNotAlreadyThere(AUnitType.Terran_Siege_Tank_Tank_Mode);
        }

        return false;
    }

    private static boolean vultures() {
        if (Decisions.dontProduceVultures()) {
            return false;
        }

        return addToQueueIfNotAlreadyThere(AUnitType.Terran_Vulture);
    }

    private static boolean goliaths(AUnit factory) {
        if (!Have.armory()) {
            return false;
        }

        if (EnemyStrategy.get().isAirUnits() && Count.WithPlanned(AUnitType.Terran_Goliath) <= 20) {
            if (AGame.canAffordWithReserved(150, 100)) {
                return addToQueueIfNotAlreadyThere(AUnitType.Terran_Goliath);
            }
        }

        return false;
    }

}
