package atlantis.production.orders;

import atlantis.AGame;
import atlantis.constructing.AConstructionRequests;
import atlantis.production.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.units.Count;
import atlantis.units.Select;
import atlantis.wrappers.ATech;
import atlantis.wrappers.MappingCounter;
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
        AProductionQueue.initialProductionQueue.clear();
        AProductionQueue.currentProductionQueue.clear();

        AProductionQueue.currentBuildOrder = buildOrder;
        ABuildOrderLoader.loadBuildOrderFromFile(AProductionQueue.currentBuildOrder);
        
        rebuildQueue();
    }

    // =========================================================

    /**
     * Returns <b>howMany</b> of next units to build, no matter if we can afford them or not.
     */
//    public static ArrayList<ProductionOrder> getProductionQueueNext(int howMany) {
//        ArrayList<ProductionOrder> result = new ArrayList<>();
//
//        for (int i = 0; i < howMany && i < AProductionQueue.currentProductionQueue.size(); i++) {
//            ProductionOrder productionOrder = AProductionQueue.currentProductionQueue.get(i);
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
        AProductionQueue.mineralsNeeded = resourcesNeededForNotStartedBuildings[0];
        AProductionQueue.gasNeeded = resourcesNeededForNotStartedBuildings[1];

        // =========================================================
        // The idea as follows: as long as we can afford next enqueued production order,
        // add it to the list. So at any given moment we can either produce nothing, one unit
        // or even multiple units (if we have all the minerals, gas and techs/buildings required).

        for (ProductionOrder order : AProductionQueue.currentProductionQueue) {
            AUnitType unitOrBuilding = order.getUnitOrBuilding();
            UpgradeType upgrade = order.getUpgrade();
            TechType tech = order.getTech();

            // Check if include only units
            if (mode == AProductionQueue.MODE_ONLY_UNITS && unitOrBuilding == null) {
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

                AProductionQueue.mineralsNeeded += unitOrBuilding.getMineralPrice();
                AProductionQueue.gasNeeded += unitOrBuilding.getGasPrice();
            }

            // UPGRADE
            else if (upgrade != null) {
                AProductionQueue.mineralsNeeded += upgrade.mineralPrice() * (1 + ATech.getUpgradeLevel(upgrade));
                AProductionQueue.gasNeeded += upgrade.gasPrice() * (1 + ATech.getUpgradeLevel(upgrade));
            }

            // TECH
            else if (tech != null) {
                AProductionQueue.mineralsNeeded += tech.mineralPrice();
                AProductionQueue.gasNeeded += tech.gasPrice();
            }

            // =========================================================
            // If we can afford this order (and all previous ones as well), add it to CurrentToProduceList.

            if (AGame.canAfford(AProductionQueue.mineralsNeeded, AProductionQueue.gasNeeded)) {
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
                && (AGame.getSupplyUsed() >= 25 || AProductionQueue.initialProductionQueue.isEmpty())) {
            for (AUnitType unitType : AProductionQueue.currentBuildOrder.produceWhenNoProductionOrders()) {
                if (AGame.hasBuildingsToProduce(unitType, false)) {
                    result.add(new ProductionOrder(unitType));
                }
            }
        }

        return result;
    }

    /**
     * If new unit is created (it doesn't need to exist, it's enough that it's just started training) or your
     * unit is destroyed, we need to rebuild the production orders queue from the beginning (based on initial
     * queue read from file). <br />
     * This method will detect which units we lack and assign to <b>currentProductionQueue</b> list next units
     * that we need. Note this method doesn't check if we can afford them, it only sets up proper sequence of
     * next units to produce.
     */
    public static void rebuildQueue() {

        // Clear old production queue.
        AProductionQueue.currentProductionQueue.clear();

        // It will store [UnitType->(int)howMany] mapping as we gonna process initial production queue and check if we
        // currently have units needed
        MappingCounter<AUnitType> virtualCounter = new MappingCounter<>();

        // =========================================================

        for (ProductionOrder order : AProductionQueue.initialProductionQueue) {
            boolean isOkayToAdd = false;

            // === Unit ========================================

            if (order.getUnitOrBuilding() != null) {
                AUnitType type = order.getUnitOrBuilding();
                virtualCounter.incrementValueFor(type);

                int shouldHaveThisManyUnits = (type.isWorker() ? 4 : 0)
                        + (type.isBase() ? (type.isPrimaryBase() ? 1 : 0) : 0)
                        + (type.isOverlord() ? 1 : 0) + virtualCounter.getValueFor(type);

                int weHaveThisManyUnits = Count.unitsOfGivenTypeOrSimilar(type);

                if (type.isBuilding()) {
                    weHaveThisManyUnits += AConstructionRequests.countNotFinishedConstructionsOfType(type);
                }

                // If we don't have this unit, add it to the current production queue.
                if (weHaveThisManyUnits < shouldHaveThisManyUnits) {
                    isOkayToAdd = true;
                }
            }

            // === Tech ========================================

            else if (order.getTech() != null) {
                isOkayToAdd = !ATech.isResearched(order.getTech(), order);
            }

            // === Upgrade ========================================

            else if (order.getUpgrade() != null) {
                isOkayToAdd = !ATech.isResearched(order.getUpgrade(), order);
            }

            // =========================================================
            if (isOkayToAdd) {
                AProductionQueue.currentProductionQueue.add(order);
                if (AProductionQueue.currentProductionQueue.size() >= 15) {
                    break;
                }
            }
        }
    }
}
