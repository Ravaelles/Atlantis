package atlantis.production.orders.production.queue;

import atlantis.config.AtlantisRaceConfig;
import atlantis.production.orders.production.queue.order.ProductionOrderPriority;
import atlantis.units.AUnitType;
import bwapi.TechType;
import bwapi.UpgradeType;

public class CountInQueue {
    public static int count(AUnitType type) {
        return count(type, 50);
    }

    public static int count(AUnitType type, int numberOfNextOrdersToCheck) {
        return Queue.get().nextOrders(numberOfNextOrdersToCheck).ofType(type).size();
    }

    public static int count(TechType tech, int numberOfNextOrdersToCheck) {
        return Queue.get().nextOrders(numberOfNextOrdersToCheck).techType(tech).size();
    }

    public static int count(UpgradeType upgrade, int numberOfNextOrdersToCheck) {
        return Queue.get().nextOrders(numberOfNextOrdersToCheck).upgradeType(upgrade).size();
    }

    public static int countOrdersWithPriorityAtLeast(ProductionOrderPriority priority) {
        return Queue.get().nonCompleted().priorityAtLeast(priority).size();
    }

    public static int countDynamicBuildingsOrders() {
        return Queue.get().nonCompleted().dynamic().buildings().size();
    }

    public static int countDynamicUnitsOrders() {
        return Queue.get().nonCompleted().dynamic().units().size();
    }

    public static int countNextOrdersWithSupplyRequirementFilled(int supply) {
        return Queue.get().allOrders().supplyAtMost(supply).size();
    }

    public static int countInfantry() {
        return Queue.get().allOrders().infantry().size();
    }

    public static int bases() {
        return count(AtlantisRaceConfig.BASE);
    }
}
