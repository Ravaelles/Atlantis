package atlantis.production.dynamic.terran;

import atlantis.game.AGame;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.information.decisions.OurDecisions;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;

public class TerranDynamicFactoryUnits extends TerranDynamicUnitsManager {

    protected static void handleFactoryProduction() {
        if (!AGame.canAffordWithReserved(150, 100)) {
            return;
        }

        for (AUnit factory : Select.ourOfType(AUnitType.Terran_Factory).free().list()) {
            requestFactoryUnit(factory);
        }
    }

    protected static boolean requestFactoryUnit(AUnit factory) {
        if (goliaths(factory)) {
            return true;
        }
        else if (tanks(factory)) {
            return true;
        }
        else if (vultures()) {
            return true;
        }

        return false;
    }

    private static boolean tanks(AUnit factory) {
        if (OurDecisions.dontProduceVultures() || Count.tanks() <= 0.4 * Count.vultures()) {
            return addToQueue(AUnitType.Terran_Siege_Tank_Tank_Mode);
        }

        return false;
    }

    private static boolean vultures() {
        if (OurDecisions.dontProduceVultures()) {
            return false;
        }
        return addToQueue(AUnitType.Terran_Vulture);
    }

    private static boolean goliaths(AUnit factory) {
        if (!Have.armory()) {
            return false;
        }

        if (EnemyStrategy.get().isAirUnits() && Count.includingPlanned(AUnitType.Terran_Goliath) <= 20) {
            if (AGame.canAffordWithReserved(150, 100)) {
                return addToQueue(AUnitType.Terran_Goliath);
            }
        }

        return false;
    }

}
