package atlantis.production.orders;

import atlantis.AGame;
import atlantis.constructing.AConstructionManager;
import atlantis.production.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.wrappers.ATech;
import atlantis.wrappers.MappingCounter;
import bwapi.TechType;
import bwapi.UpgradeType;
import java.util.ArrayList;

/**
 * Represents abstract build orders read from the file. Build Orders in Atlantis are called "Production
 * Orders", because you can produce both units and buildings and one couldn't say you build marines, rather
 * produce.
 */
public abstract class ABuildOrderManager {

    /**
     * Build order currently in use.
     * @see switchToBuildOrder(ABuildOrder buildOrder)
     */
    private static ABuildOrder currentBuildOrder = null;
    
    // =========================================================
    
    /**
     * Ordered list of production orders as initially read from the file. It never changes
     */
    protected static ArrayList<ProductionOrder> initialProductionQueue = new ArrayList<>();

    /**
     * Ordered list of next units we should build. It is re-generated when events like "started
     * training/building new unit"
     */
    protected static ArrayList<ProductionOrder> currentProductionQueue = new ArrayList<>();

    /**
     * Number of minerals reserved to produce some units/buildings.
     */
    private static int mineralsNeeded = 0;

    /**
     * Number of gas reserved to produce some units/buildings.
     */
    private static int gasNeeded = 0;
    
    // =========================================================
    
    /**
     * Indicates that we should now be using given build order.<br /><br />
     * When that happens its file is read and parsed and <b>initialProductionQueue</b> and 
     * <b>currentProductionQueue</b> are populated.
     */
    public static void switchToBuildOrder(ABuildOrder buildOrder) {
        initialProductionQueue.clear();
        currentProductionQueue.clear();
        
        currentBuildOrder = buildOrder;
        ABuildOrderLoader.loadBuildOrderFromFile(currentBuildOrder);
        
        rebuildQueue();
    }
    
    // === Build Order manager functionality ===================
    
    public static final int MODE_ALL_ORDERS = 1;
    public static final int MODE_ONLY_UNITS = 2;
    
    /**
     * Returns list of things (units and upgrades) that we should produce (train or build) now. Or if you only
     * want to get units, use <b>onlyUnits</b> set to true. This merhod iterates over latest build orders and
     * returns those build orders that we can build in this very moment (we can afford them and they match our
     * strategy).
     *
     * @param int mode use this classes constants; if MODE_ONLY_UNITS it will only return "units" as opposed
     * to buildings (keep in mind AUnit is both "unit" and building)
     */
    public static ArrayList<ProductionOrder> getThingsToProduceRightNow(int mode) {
        ArrayList<ProductionOrder> result = new ArrayList<>();
        int[] resourcesNeededForNotStartedBuildings
                = AConstructionManager.countResourcesNeededForNotStartedConstructions();
        mineralsNeeded = resourcesNeededForNotStartedBuildings[0];
        gasNeeded = resourcesNeededForNotStartedBuildings[1];

        // =========================================================
        // The idea as follows: as long as we can afford next enqueued production order, 
        // add it to the list. So at any given moment we can either produce nothing, one unit
        // or even multiple units (if we have all the minerals, gas and techs/buildings required).
        for (ProductionOrder order : currentProductionQueue) {
            AUnitType unitOrBuilding = order.getUnitOrBuilding();
            UpgradeType upgrade = order.getUpgrade();
            TechType tech = order.getTech();

            // Check if include only units
            if (mode == MODE_ONLY_UNITS && unitOrBuilding == null) {
                continue;
            }

            // ===  Protoss fix: wait for at least one Pylon ============
            if (AGame.playsAsProtoss() && unitOrBuilding != null
                    && !unitOrBuilding.isType(AUnitType.Protoss_Pylon, AUnitType.Protoss_Assimilator)
                    && Select.our().countUnitsOfType(AUnitType.Protoss_Pylon) == 0) {
                continue;
            }

            // === Define order type: UNIT/BUILDING or UPGRADE or TECH ==
            // UNIT/BUILDING
            if (unitOrBuilding != null) {
                if (!AGame.hasBuildingsToProduce(unitOrBuilding, true)) {
                    continue;
                }

                mineralsNeeded += unitOrBuilding.getMineralPrice();
                gasNeeded += unitOrBuilding.getGasPrice();
            } // UPGRADE
            else if (upgrade != null) {
                mineralsNeeded += upgrade.mineralPrice() * (1 + ATech.getUpgradeLevel(upgrade));
                gasNeeded += upgrade.gasPrice() * (1 + ATech.getUpgradeLevel(upgrade));
            } // TECH
            else if (tech != null) {
                mineralsNeeded += tech.mineralPrice();
                gasNeeded += tech.gasPrice();
            }

            // =========================================================
            // If we can afford this order (and all previous ones as well), add it to CurrentToProduceList.
            if (AGame.canAfford(mineralsNeeded, gasNeeded)) {
                result.add(order);
            } // We can't afford to produce this order (possibly other, previous orders are blocking it). 
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
        if (result.isEmpty() && AGame.canAfford(300, 200)
                && (AGame.getSupplyUsed() >= 30 || initialProductionQueue.isEmpty())) {
            for (AUnitType unitType : currentBuildOrder.produceWhenNoProductionOrders()) {
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
        currentProductionQueue.clear();

        // It will store [UnitType->(int)howMany] mapping as we gonna process initial production queue and check if we
        // currently have units needed
        MappingCounter<AUnitType> virtualCounter = new MappingCounter<>();

        // =========================================================

        for (ProductionOrder order : initialProductionQueue) {
            boolean isOkayToAdd = false;

            // =========================================================
            // Unit
            if (order.getUnitOrBuilding() != null) {
                AUnitType type = order.getUnitOrBuilding();
                virtualCounter.incrementValueFor(type);

                int shouldHaveThisManyUnits = (type.isWorker() ? 4 : 0) 
                        + (type.isBase() ? (type.isPrimaryBase() ? 1 : 0) : 0)
                        + (type.isOverlord() ? 1 : 0) + virtualCounter.getValueFor(type);
                
                int weHaveThisManyUnits = countUnitsOfGivenTypeOrSimilar(type);

                if (type.isBuilding()) {
                    weHaveThisManyUnits += AConstructionManager.countNotFinishedConstructionsOfType(type);
                }

//                if (type.isBase()) {
//                    System.out.println("      " + type + ": " 
//                            + weHaveThisManyUnits + " / " + shouldHaveThisManyUnits);
//                }
                
                // If we don't have this unit, add it to the current production queue.
                if (weHaveThisManyUnits < shouldHaveThisManyUnits) {
//                    if (type.isBase()) {
//                        AGame.sendMessage("Request " + type.getShortName());
//                    }
                    isOkayToAdd = true;
                }
            } // Tech
            else if (order.getTech() != null) {
                isOkayToAdd = !ATech.isResearched(order.getTech(), order);
            } // Upgrade
            else if (order.getUpgrade() != null) {
                isOkayToAdd = !ATech.isResearched(order.getUpgrade(), order);
            }

            // =========================================================
            if (isOkayToAdd) {
                currentProductionQueue.add(order);
                if (currentProductionQueue.size() >= 15) {
                    break;
                }
            }
        }
    }

    /**
     * Some buildings like Sunken Colony are morphed into from Creep Colony. When counting Creep Colonies, we
     * need to count sunkens as well.
     */
    private static int countUnitsOfGivenTypeOrSimilar(AUnitType type) {
        if (type.equals(AUnitType.Zerg_Creep_Colony)) {
            return Select.ourIncludingUnfinished().ofType(type).count()
                    + Select.ourIncludingUnfinished().ofType(AUnitType.Zerg_Spore_Colony).count()
                    + Select.ourIncludingUnfinished().ofType(AUnitType.Zerg_Sunken_Colony).count();
        } 
        else if (type.isPrimaryBase()) {
            return Select.ourIncludingUnfinished().bases().count() 
                    + AConstructionManager.countNotStartedConstructionsOfType(type)
                    + AConstructionManager.countNotStartedConstructionsOfType(AUnitType.Zerg_Lair)
                    + AConstructionManager.countNotStartedConstructionsOfType(AUnitType.Zerg_Hive);
        }
        else if (type.isBase() && !type.isPrimaryBase()) {
            return Select.ourIncludingUnfinished().ofType(type).count()
                    + AConstructionManager.countNotStartedConstructionsOfType(type);
        }
        else {
            return Select.ourIncludingUnfinished().ofType(type).count();
        }
    }

    /**
     * Returns <b>howMany</b> of next units to build, no matter if we can afford them or not.
     */
    public static ArrayList<ProductionOrder> getProductionQueueNext(int howMany) {
        ArrayList<ProductionOrder> result = new ArrayList<>();

        for (int i = 0; i < howMany && i < currentProductionQueue.size(); i++) {
            ProductionOrder productionOrder = currentProductionQueue.get(i);
//            if (productionOrder.getUnitType() != null 
//                    && !AGame.hasBuildingsToProduce(productionOrder.getUnitType())) {
//                continue;
//            }
            result.add(productionOrder);
        }

//        System.out.println("// =========================================================");
//        for (ProductionOrder productionOrder : result) {
//            System.out.println("CURRENT: " + productionOrder.getUnitType());
//        }
        return result;
    }

    // =========================================================
    // Getters
    
    /**
     * Returns currently active build order.
     */
    public static ABuildOrder getCurrentBuildOrder() {
        return currentBuildOrder;
    }
    
    /**
     * Number of minerals reserved to produce some units/buildings in the build order that according to it
     * should be produced right now (judging by the supply used).
     */
    public static int getMineralsReserved() {
        return mineralsNeeded;
    }

    /**
     * Number of gas reserved to produce some units/buildings in the build order that according to it
     * should be produced right now (judging by the supply used).
     */
    public static int getGasReserved() {
        return gasNeeded;
    }    

}
