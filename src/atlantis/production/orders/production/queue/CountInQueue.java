package atlantis.production.orders.production.queue;

import atlantis.config.AtlantisRaceConfig;
import atlantis.production.orders.production.queue.order.ProductionOrderPriority;
import atlantis.units.AUnitType;
import bwapi.TechType;
import bwapi.UpgradeType;

public class CountInQueue {
    public static int count(AUnitType type) {
        return count(type, 999);
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
        return Queue.get().notFinished().priorityAtLeast(priority).size();
    }

    public static int countDynamicBuildingsOrders() {
        return Queue.get().notFinished().dynamic().buildings().size();
    }

    public static int countDynamicUnitsOrders() {
        return Queue.get().notFinished().dynamic().units().size();
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

    public static int countInProgress(AUnitType type) {
        return Queue.get().inProgressOrders().ofType(type).size();
    }

    public static int countNotStarted(AUnitType type) {
        return Queue.get().notStarted().ofType(type).size();
    }

    public static int countNotFinished(AUnitType type) {
        return Queue.get().notFinished().ofType(type).size();
    }

    /**
     * Owner 2026-10-18: every unfinished building of a type, counted once.
     * Undispatched queue rows (no linked construction) + warping units +
     * all unfinished constructions (PREPARING included - the old
     * notStarted-only counts were blind to picked-early sites). Lives here
     * (not in select.Count) because core must not depend on production
     * (coreMustNotDependOnOtherContexts). Terran over-counts by the
     * construction phase (unfinished unit + live construction overlap);
     * Protoss is exact, and the direction is safe (a delayed structure,
     * never a duplicate).
     */
    public static int outstandingOfType(AUnitType type) {
        return countNotFinishedWithoutConstruction(type)
            + atlantis.units.select.Select.ourUnfinished().ofType(type).count()
            + atlantis.production.constructions.ConstructionRequests.notFinishedOfType(type).size();
    }

    /**
     * Owner 2026-10-18: queue rows still waiting for dispatch (no linked
     * construction). Dispatched rows live in the constructions list - and
     * counting both double-counts one building (measured shape behind the
     * supply-12 third Gateway and the double Core). Pair with
     * constructions + warping units, never with the raw queue count.
     */
    public static int countNotFinishedWithoutConstruction(AUnitType type) {
        int total = 0;
        for (atlantis.production.orders.production.queue.order.ProductionOrder row
            : Queue.get().notFinished().ofType(type).list()) {
            if (row.construction() == null) total++;
        }
        return total;
    }
}
