package atlantis.production.dynamic.terran;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.decisions.Decisions;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.information.tech.ATech;
import atlantis.production.orders.build.AddToQueue;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.util.Enemy;

import static bwapi.TechType.Tank_Siege_Mode;

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

        int tanks = Select.ourWithUnfinished().tanks().count();

        if (tanks >= 5) {
            if (!AGame.canAffordWithReserved(250, 150)) {
                return false;
            }
        }

        if (tanks >= 2 && Enemy.zerg() && Count.ofType(AUnitType.Terran_Wraith) <= 1) {
            if (!AGame.canAffordWithReserved(300, 200)) {
                return false;
            }
        }

        if (Count.infantry() >= 6 && Count.medics() <= 1) {
            return A.canAfford(175, 75);
        }

        if (Enemy.terran() && tanks >= 1 && !ATech.isResearched(Tank_Siege_Mode)) {
            if (!AGame.canAffordWithReserved(150, 100)) {
                return false;
            }
        }

        if (Enemy.protoss() && tanks >= 4 && Count.scienceVessels() == 0) {
            return AGame.canAffordWithReserved(200, 200);
        }

        int vultures = Count.vultures();
        if (!Decisions.produceVultures() || tanks <= 5 || tanks <= 0.4 * vultures) {
            return AddToQueue.addToQueueIfNotAlreadyThere(AUnitType.Terran_Siege_Tank_Tank_Mode);
        }

        return false;
    }

    private static boolean vultures() {
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
