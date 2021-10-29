package atlantis.production.orders;

import atlantis.AGame;
import atlantis.constructing.AConstructionRequests;
import atlantis.production.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.wrappers.ATech;
import bwapi.TechType;
import bwapi.UpgradeType;

import java.util.ArrayList;

/**
 * Current production queue
 */
public abstract class AProductionQueueManager {

    /**
     * Indicates that we should now be using given build order.<br /><br />
     * When that happens its file is read and parsed and <b>initialProductionQueue</b> and 
     * <b>currentProductionQueue</b> are populated.
     */
    public static void switchToBuildOrder(ABuildOrder buildOrder) {
        ProductionQueue.setBuildOrder(buildOrder);

//        ProductionQueueRefresher.applyNow(buildOrder);

//        ProductionQueue.initialProductionQueue.clear();
//        ProductionQueue.currentProductionQueue.clear();

        ProductionQueueRefresher.rebuildProductionQueue();
    }

    // =========================================================

    /**
     * Returns <b>howMany</b> of next units to build, no matter if we can afford them or not.
     */
//    public static ArrayList<ProductionOrder> getProductionQueueNext(int howMany) {
//        ArrayList<ProductionOrder> result = new ArrayList<>();
//
//        for (int i = 0; i < howMany && i < ProductionQueue.currentProductionQueue.size(); i++) {
//            ProductionOrder productionOrder = ProductionQueue.currentProductionQueue.get(i);
//            result.add(productionOrder);
//        }
//
//        return result;
//    }

    // === Getters =============================================

    /**
     * Returns list of things (units and upgrades) that we should produce (train or build) now. Or if you only
     * want to get units, use <b>onlyUnits</b> set to true. This merhod iterates over latest build orders and
     * returns those build orders that we can build in this very moment (we can afford them and they match our
     * strategy).
     *
     * @param mode use this classes constants; if MODE_ONLY_UNITS it will only return "units" as opposed
     * to buildings (keep in mind AUnit is both "unit" and building)
     */
    public static ArrayList<ProductionOrder> getThingsToProduceRightNow(int mode) {
        ArrayList<ProductionOrder> result = new ArrayList<>();
        int[] resourcesNeededForNotStartedBuildings
                = AConstructionRequests.countResourcesNeededForNotStartedConstructions();
        ProductionQueue.mineralsNeeded = resourcesNeededForNotStartedBuildings[0];
        ProductionQueue.gasNeeded = resourcesNeededForNotStartedBuildings[1];

        // =========================================================
        // The idea as follows: as long as we can afford next enqueued production order,
        // add it to the list. So at any given moment we can either produce nothing, one unit
        // or even multiple units (if we have all the minerals, gas and techs/buildings required).

        for (ProductionOrder order : ProductionQueue.currentProductionQueue) {
            AUnitType unitOrBuilding = order.getUnitOrBuilding();
            UpgradeType upgrade = order.getUpgrade();
            TechType tech = order.getTech();

            // Check if include only units
            if (mode == ProductionQueue.MODE_ONLY_UNITS && unitOrBuilding == null) {
                continue;
            }

            // ===  Protoss fix: wait for at least one Pylon ============

            if (AGame.isPlayingAsProtoss() && unitOrBuilding != null
                    && !unitOrBuilding.is(AUnitType.Protoss_Pylon, AUnitType.Protoss_Assimilator)
                    && Select.our().countUnitsOfType(AUnitType.Protoss_Pylon) == 0) {
                continue;
            }

            // === Define order type: UNIT/BUILDING or UPGRADE or TECH ==

            // UNIT/BUILDING
            if (unitOrBuilding != null) {
                if (!AGame.hasBuildingsToProduce(unitOrBuilding, true)) {
                    continue;
                }

                ProductionQueue.mineralsNeeded += unitOrBuilding.getMineralPrice();
                ProductionQueue.gasNeeded += unitOrBuilding.getGasPrice();
            }

            // UPGRADE
            else if (upgrade != null) {
                ProductionQueue.mineralsNeeded += upgrade.mineralPrice() * (1 + ATech.getUpgradeLevel(upgrade));
                ProductionQueue.gasNeeded += upgrade.gasPrice() * (1 + ATech.getUpgradeLevel(upgrade));
            }

            // TECH
            else if (tech != null) {
                ProductionQueue.mineralsNeeded += tech.mineralPrice();
                ProductionQueue.gasNeeded += tech.gasPrice();
            }

            // =========================================================
            // If we can afford this order (and all previous ones as well), add it to CurrentToProduceList.

            if (AGame.canAfford(ProductionQueue.mineralsNeeded, ProductionQueue.gasNeeded)) {
                result.add(order);
            }

            // We can't afford to produce this order (possibly other, previous orders are blocking it).
            // Return current list of production orders (can be empty).
            else {
                break;
            }
        }

        // =========================================================
        // === Special case ========================================
        // =========================================================
        // Produce some generic units (preferably combat units) if queue is empty.
        // This can mean that we run out of build orders from build order file.
        // For proper build order files this feature will activate in late game.

        if (result.isEmpty() && AGame.canAfford(350, 250)
                // @TODO WHAT THE FUCK
//                && (AGame.getSupplyUsed() >= 25 || ProductionQueue.get().isEmpty())) {
                && (AGame.getSupplyUsed() >= 25 || ProductionQueue.hasNothingToProduce())) {
            for (AUnitType unitType : ProductionQueue.get().produceWhenNoProductionOrders()) {
                if (AGame.hasBuildingsToProduce(unitType, false)) {
                    result.add(new ProductionOrder(unitType));
                }
            }
        }

        return result;
    }

}
