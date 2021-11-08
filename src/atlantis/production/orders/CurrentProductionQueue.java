package atlantis.production.orders;

import atlantis.AGame;
import atlantis.combat.missions.Mission;
import atlantis.production.constructing.AConstructionRequests;
import atlantis.production.ProductionOrder;
import atlantis.production.Requirements;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.A;
import atlantis.util.We;
import atlantis.wrappers.ATech;
import bwapi.TechType;
import bwapi.UpgradeType;

import java.util.ArrayList;

/**
 * Current production queue
 */
public abstract class CurrentProductionQueue {

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
//        boolean hasGas
        int[] resourcesNeededForNotStartedBuildings = AConstructionRequests.resourcesNeededForNotStartedConstructions();
        ProductionQueue.mineralsNeeded = resourcesNeededForNotStartedBuildings[0];
        ProductionQueue.gasNeeded = resourcesNeededForNotStartedBuildings[1];

        // =========================================================
        // The idea as follows: as long as we can afford next enqueued production order,
        // add it to the list. So at any given moment we can either produce nothing, one unit
        // or even multiple units (if we have all the minerals, gas and techs/buildings required).

        int countCanNotAfford = 0;
        for (ProductionOrder order : ProductionQueue.currentProductionQueue) {
            boolean hasRequirements = AGame.hasSupply(order.minSupply()) && Requirements.hasRequirements(order);
            AUnitType unitOrBuilding = order.unitType();
            UpgradeType upgrade = order.upgrade();
            TechType tech = order.tech();
            Mission mission = order.mission();

            // ===  Protoss fix: wait for at least one Pylon ============

            if (
                    We.protoss()
                    && mode == ProductionQueueMode.ONLY_WHAT_CAN_AFFORD
                    && (unitOrBuilding != null && !unitOrBuilding.isPylon())
                    && Count.existingOrInProductionOrInQueue(AUnitType.Protoss_Pylon) == 0
            ) {
                continue;
            }

            // === Define order type: UNIT/BUILDING or UPGRADE or TECH ==

            // UNIT/BUILDING
            if (unitOrBuilding != null && A.supplyAtLeast(order.minSupply()) && hasRequirements) {
//                System.out.println(unitOrBuilding + " // req:" + hasRequirements);
                if (hasFreeBuildingFor(unitOrBuilding) && (unitOrBuilding.isBuilding() || !hasUnitInQueue(unitOrBuilding, queue))) {
                    ProductionQueue.mineralsNeeded += unitOrBuilding.getMineralPrice();
                    ProductionQueue.gasNeeded += unitOrBuilding.getGasPrice();
                }
            }

            // UPGRADE
            else if (upgrade != null && A.supplyAtLeast(order.minSupply()) && hasRequirements) {
//                System.out.println("====== WE'RE AT " + upgrade.name() + " --> " + hasWhatRequired);
//                System.out.println("lvl = " + ATech.getUpgradeLevel(upgrade));
//                System.out.println(upgrade.mineralPrice() + " // " + upgrade.gasPrice());
                ProductionQueue.mineralsNeeded += upgrade.mineralPrice() * (1 + ATech.getUpgradeLevel(upgrade));
                ProductionQueue.gasNeeded += upgrade.gasPrice() * (1 + ATech.getUpgradeLevel(upgrade));
            }

            // TECH
            else if (tech != null && A.supplyAtLeast(order.minSupply()) && hasRequirements) {
                ProductionQueue.mineralsNeeded += tech.mineralPrice();
                ProductionQueue.gasNeeded += tech.gasPrice();
            }

            // MISSION - handled in AProductionManager
            else if (mission != null && A.supplyAtLeast(order.minSupply())) {
                continue;
            }

            // =========================================================
            // If we can afford this order (and all previous ones as well), add it to CurrentToProduceList.

            boolean canAfford = AGame.canAfford(ProductionQueue.mineralsNeeded, ProductionQueue.gasNeeded);
            order.setCanAffordNow(canAfford);
            order.setHasWhatRequired(hasRequirements);

//            if (AGame.supplyUsed() >= 11 && unitOrBuilding != null && unitOrBuilding.isPylon()) {
//                System.out.println(order.shortName() + " // aff=" + canAfford + " // req=" + hasWhatRequired);
//            }
//            System.out.println(order.shortName() + " has requirements = " + hasWhatRequired);

            if (
                    mode == ProductionQueueMode.ENTIRE_QUEUE || (canAfford && hasRequirements)
            ) {
                if (unitOrBuilding != null && !A.hasFreeSupply(unitOrBuilding.supplyNeeded())) {
                    continue;
                }

                queue.add(order);
            }

            // We can't afford to produce this order (possibly other, previous orders are blocking it).
            // Return current list of production orders (can be empty).
            else if (++countCanNotAfford >= 5) {
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

    private static boolean hasUnitInQueue(AUnitType type, ArrayList<ProductionOrder> queue) {
        for (ProductionOrder order : queue) {
            if (order.unitType() != null && order.unitType().equals(type)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasFreeBuildingFor(AUnitType unit) {
        AUnitType building = unit.getWhatBuildsIt();
        if (building != null) {
            return Select.ourOfType(building).free().isNotEmpty();
        }

        return false;
    }

    // =========================================================

    public static int[] resourcesReserved() {
//        int reservedMinerals = 0;
//        int reservedGas = 0;
//
//        for (int i = 0; i <= 1; i++) {
//
//        }

//        thingsToProduce(ProductionQueueMode.ONLY_WHAT_CAN_AFFORD);

        return new int[] { ProductionQueue.mineralsNeeded, ProductionQueue.gasNeeded };
    }

}
