package atlantis.production.orders;

import atlantis.AGame;
import atlantis.production.constructing.AConstructionRequests;
import atlantis.production.ProductionOrder;
import atlantis.production.Requirements;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.Us;
import atlantis.wrappers.ATech;
import bwapi.TechType;
import bwapi.UpgradeType;

import java.util.ArrayList;

/**
 * Current production queue
 */
public abstract class CurrentProductionOrders {

    /**
     * Returns list of orders (units and upgrades) that we should produce now.
     * This method iterates over all production orders, taken from the active build orders
     * and returns those we can afford at this moment.
     *
     * Notice that dynamic actions (like requesting a detector quickly) may insert
     * a unit dynamically with top priority.
     */
    public static ArrayList<ProductionOrder> thingsToProduce(ProductionQueueMode mode) {
        ArrayList<ProductionOrder> queue = new ArrayList<>();
        int[] resourcesNeededForNotStartedBuildings = AConstructionRequests.resourcesNeededForNotStartedConstructions();
        ProductionQueue.mineralsNeeded = resourcesNeededForNotStartedBuildings[0];
        ProductionQueue.gasNeeded = resourcesNeededForNotStartedBuildings[1];

        // =========================================================
        // The idea as follows: as long as we can afford next enqueued production order,
        // add it to the list. So at any given moment we can either produce nothing, one unit
        // or even multiple units (if we have all the minerals, gas and techs/buildings required).

        for (ProductionOrder order : ProductionQueue.currentProductionQueue) {
            boolean hasWhatRequired = Requirements.hasRequirements(order);
            AUnitType unitOrBuilding = order.getUnitOrBuilding();
            UpgradeType upgrade = order.getUpgrade();
            TechType tech = order.getTech();

            // ===  Protoss fix: wait for at least one Pylon ============

            if (
                    Us.isProtoss()
                    && mode == ProductionQueueMode.ONLY_WHAT_CAN_AFFORD
                    && (unitOrBuilding != null && !unitOrBuilding.isPylon())
                    && Count.pylons() == 0
            ) {
                continue;
            }

            // === Define order type: UNIT/BUILDING or UPGRADE or TECH ==

            // UNIT/BUILDING
            if (unitOrBuilding != null && hasWhatRequired) {
                ProductionQueue.mineralsNeeded += unitOrBuilding.getMineralPrice();
                ProductionQueue.gasNeeded += unitOrBuilding.getGasPrice();
            }

            // UPGRADE
            else if (upgrade != null && hasWhatRequired) {
                ProductionQueue.mineralsNeeded += upgrade.mineralPrice() * (1 + ATech.getUpgradeLevel(upgrade));
                ProductionQueue.gasNeeded += upgrade.gasPrice() * (1 + ATech.getUpgradeLevel(upgrade));
            }

            // TECH
            else if (tech != null && hasWhatRequired) {
                ProductionQueue.mineralsNeeded += tech.mineralPrice();
                ProductionQueue.gasNeeded += tech.gasPrice();
            }

            // =========================================================
            // If we can afford this order (and all previous ones as well), add it to CurrentToProduceList.

            boolean canAfford = AGame.canAfford(ProductionQueue.mineralsNeeded, ProductionQueue.gasNeeded);
            order.setCanAffordNow(canAfford);
            order.setHasWhatRequired(canAfford);

//            if (unitOrBuilding != null && unitOrBuilding.isPylon()) {
//                System.out.println(order.shortName() + " // aff=" + canAfford + " // req=" + hasWhatRequired);
//            }
//            System.out.println(order.shortName() + " has requirements = " + hasWhatRequired);
            if (
                    mode == ProductionQueueMode.ENTIRE_QUEUE || (canAfford && hasWhatRequired)
            ) {
                queue.add(order);
            }

            // We can't afford to produce this order (possibly other, previous orders are blocking it).
            // Return current list of production orders (can be empty).
            else {
                break;
            }
        }

        // At this moment production queue coming from build order might be empty.
        // Race-specific dynamic production managers should take care of that by
        // adding production orders dynamically.

//        if (mode == ProductionQueueMode.ONLY_WHAT_CAN_AFFORD && queue.size() > 0) {
//            System.out.println("----- " + queue.size());
//            for (ProductionOrder order : queue) {
//                System.out.println(order);
//            }
//        }

        return queue;
    }

}
