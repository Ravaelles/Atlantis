package atlantis.production.strategies;

import atlantis.AtlantisGame;
import atlantis.constructing.AtlantisConstructingManager;
import atlantis.information.AtlantisUnitInformationManager;
import atlantis.production.ProductionOrder;
import atlantis.util.RUtilities;
import atlantis.wrappers.AtlantisTech;
import atlantis.wrappers.MappingCounter;
import java.util.ArrayList;
import jnibwapi.types.TechType;
import jnibwapi.types.UnitType;
import jnibwapi.types.UpgradeType;

public abstract class AtlantisProductionStrategy {

    /**
     * Ordered list of production orders as initially read from the file. It never changes
     */
    private final ArrayList<ProductionOrder> initialProductionQueue = new ArrayList<>();

    /**
     * Ordered list of next units we should build. It is re-generated when events like "started
     * training/building new unit"
     */
    private ArrayList<ProductionOrder> currentProductionQueue = new ArrayList<>();

    // /**
    // * Order list counter.
    // */
    // private int lastCounterInOrdersQueue = 0;
    // =========================================================
    // Constructor
    public AtlantisProductionStrategy() {
        initializeProductionQueue();
    }

    /**
     * Returns default production strategy according to the race played.
     */
    public static AtlantisProductionStrategy getAccordingToRace() {
        if (AtlantisGame.playsAsTerran()) {
            return new TerranProductionStrategy();
        } else if (AtlantisGame.playsAsProtoss()) {
            return new ProtossProductionStrategy();
        } else if (AtlantisGame.playsAsZerg()) {
            return new ZergProductionStrategy();
        }

        System.err.println("getAccordingToRace: Unknown race");
        System.exit(-1);
        return null;
    }

    // =========================================================
    // Abstract methods
    /**
     * Returns name of file with build orders.
     */
    protected abstract String getFilename();

    /**
     * Request to produce worker (Zerg Drone, Terran SCV or Protoss Probe) that should be handled according to
     * the race played.
     */
    public abstract void produceWorker();

    /**
     * Request to produce infantry unit that should be handled according to the race played.
     */
    public abstract void produceInfantry(UnitType infantryType);

    /**
     * When production orders run out, we should always produce some units.
     */
    public abstract ArrayList<UnitType> produceWhenNoProductionOrders();

    // =========================================================
    // Public defined methods
    /**
     * If new unit is created (it doesn't need to exist, it's enough that it's just started training) or your
     * unit is destroyed, we need to rebuild the production orders queue from the beginning (based on initial
     * queue read from file). <br />
     * This method will detect which units we lack and assign to <b>currentProductionQueue</b> list next units
     * that we need. Note this method doesn't check if we can afford them, it only sets up proper sequence of
     * next units to produce.
     */
    public void rebuildQueue() {

        // Clear old production queue.
        currentProductionQueue.clear();

        // It will store [UnitType->(int)howMany] mapping as we gonna process initial production queue and check if we
        // currently have units needed
        MappingCounter<UnitType> virtualCounter = new MappingCounter<>();

        for (ProductionOrder order : initialProductionQueue) {
            boolean isOkayToAdd = false;

            // =========================================================
            // Unit
            if (order.getUnitType() != null) {
                UnitType type = order.getUnitType();
                virtualCounter.incrementValueFor(type);

                int shouldHaveThisManyUnits = virtualCounter.getValueFor(type);
                int weHaveThisManyUnits = AtlantisUnitInformationManager.countOurUnitsOfType(type);

                if (order.getUnitType().isBuilding()) {
                    weHaveThisManyUnits += AtlantisConstructingManager.countNotStartedConstructionsOfType(type);
                    // System.out.println("@@@ Not started constructions of '" + type + "': "
                    // + AtlantisConstructingManager.countNotStartedConstructionsOfType(type));
                }

                // If we don't have this unit, add it to the current production queue.
                if (weHaveThisManyUnits < shouldHaveThisManyUnits) {
                    isOkayToAdd = true;
                }
            } // Upgrade
            else if (order.getUpgrade() != null) {
                isOkayToAdd = !AtlantisTech.isResearched(order.getUpgrade());
            } // Tech
            else if (order.getTech() != null) {
                isOkayToAdd = !AtlantisTech.isResearched(order.getTech());
            }

            // =========================================================
            if (isOkayToAdd) {
                currentProductionQueue.add(order);
                if (currentProductionQueue.size() >= 8) {
                    break;
                }
            }
        }
    }

    /**
     * Returns list of things (units and upgrades) that we should produce (train or build) now. Or if you only
     * want to get units, use <b>onlyUnits</b> set to true. This merhod iterates over latest build orders and
     * returns those build orders that we can build in this very moment (we can afford them and they match our
     * strategy).
     */
    public ArrayList<ProductionOrder> getThingsToProduceRightNow(boolean onlyUnits) {
        ArrayList<ProductionOrder> result = new ArrayList<>();
        int[] resourcesNeededForNotStartedBuildings
                = AtlantisConstructingManager.countResourcesNeededForNotStartedConstructions();
        int mineralsNeeded = resourcesNeededForNotStartedBuildings[0];
        int gasNeeded = resourcesNeededForNotStartedBuildings[1];

        // The idea as follows: as long as we can afford next enqueued production order, add it to the
        // CurrentToProduceList.
        for (ProductionOrder order : currentProductionQueue) {
            UnitType unitType = order.getUnitType();

            // Check if include only units
            if (onlyUnits && unitType == null) {
                continue;
            }

            UpgradeType upgrade = order.getUpgrade();
            TechType tech = order.getTech();

            if (unitType != null) {
                mineralsNeeded += unitType.getMineralPrice();
                gasNeeded += unitType.getGasPrice();
            } else if (upgrade != null) {
                mineralsNeeded += upgrade.getMineralPriceBase() * upgrade.getMineralPriceFactor();
                gasNeeded += upgrade.getGasPriceBase() * upgrade.getGasPriceFactor();
            } else if (tech != null) {
                mineralsNeeded += tech.getMineralPrice();
                gasNeeded += tech.getMineralPrice();
            }

            // If we can afford this order and the previous, add it to CurrentToProduceList.
            if (AtlantisGame.canAfford(mineralsNeeded, gasNeeded)) {
                result.add(order);
            } // We can't afford to produce this order along with all previous ones. Return currently list.
            else {
                break;
            }
        }

        // --------------------------------------------------------------------
        // Produce something if queue is empty
        if (result.isEmpty()) {
            for (UnitType unitType : produceWhenNoProductionOrders()) {
                result.add(new ProductionOrder(unitType));
            }
        }

        return result;
    }

    /**
     * Returns true if we should produce this unit now.
     */
    public boolean shouldProduceNow(UnitType type) {
        return getThingsToProduceRightNow(true).contains(type);
    }

    /**
     * Returns true if we should produce this upgrade now.
     */
    public boolean shouldProduceNow(UpgradeType upgrade) {
        return getThingsToProduceRightNow(false).contains(upgrade);
    }

    /**
     * Returns <b>howMany</b> of next units to build, no matter if we can afford them or not.
     */
    public ArrayList<ProductionOrder> getProductionQueueNext(int howMany) {
        ArrayList<ProductionOrder> result = new ArrayList<>();

        for (int i = 0; i < howMany && i < currentProductionQueue.size(); i++) {
            result.add(currentProductionQueue.get(i));
        }

        return result;
    }

    // =========================================================
    // Private defined methods
    /**
     * Populates <b>productionOrdersFromFile</b> with data from CSV file.
     */
    private void createProductionOrderListFromStringArray() {
        final int NUMBER_OF_COLUMNS_IN_FILE = 2;

        // Read file into 2D String array
        String path = "bwapi-data/read/build_orders/" + getFilename();
        String[][] loadedFile = RUtilities.loadCsv(path, NUMBER_OF_COLUMNS_IN_FILE);

        // We can display file here, if we want to
        // displayLoadedFile(loadedFile);
        // =========================================================
        // Skip first row as it's CSV header
        for (int i = 1; i < loadedFile.length; i++) {
            String[] row = loadedFile[i];
            int inRowCounter = 1; // Skip first column as it's only description
            ProductionOrder order = null;

            // =========================================================
            // Parse entire row of strings
            // Define type of entry: Unit / Research / Tech
            String nameString = row[inRowCounter++].toLowerCase().trim();

            // =========================================================
            // Try getting objects of each type as we don't know if it's unit, research or tech.
            // UNIT
            UnitType.disableErrorReporting = true;
            UnitType unitType = UnitType.getByName(nameString);
            UnitType.disableErrorReporting = false;

            // UPGRADE
            UpgradeType.disableErrorReporting = true;
            UpgradeType upgrade = UpgradeType.getByName(nameString);
            UpgradeType.disableErrorReporting = false;

            // TECH
            TechType.disableErrorReporting = true;
            TechType tech = TechType.getByName(nameString);
            TechType.disableErrorReporting = false;

            // Define convienience boolean variables
            boolean isUnit = unitType != null;
            boolean isUpgrade = upgrade != null;
            boolean isTech = tech != null;

            // Check if no error occured like no object found
            if (!isUnit && !isUpgrade && !isTech) {
                System.err.println("Invalid production order entry: " + nameString);
                System.exit(-1);
            }

            // =========================================================
            // Unit
            if (isUnit) {
                order = new ProductionOrder(unitType);
            } // Upgrade
            else if (isUpgrade) {
                order = new ProductionOrder(upgrade);
            } // Tech
            else if (isTech) {
                order = new ProductionOrder(tech);
            } // Invalid entry type
            else {
                System.err.println("Invalid entry type: " + nameString);
                System.exit(-1);
            }

            // =========================================================
            // Blocking
            // boolean isBlocking;
            // String blockingString = row[inRowCounter++].toLowerCase().trim();
            // if (blockingString.isEmpty() || blockingString.equals("") || blockingString.toLowerCase().equals("no")) {
            // isBlocking = false;
            // } else {
            // isBlocking = true;
            // }
            // Priority
            // boolean isLowestPriority = false;
            // boolean isHighestPriority = false;
            // String priorityString = row[inRowCounter++].toLowerCase().trim();
            // if (!priorityString.isEmpty()) {
            // priorityString = priorityString.toLowerCase();
            // if (priorityString.contains("low")) {
            // isLowestPriority = true;
            // } else if (priorityString.contains("high")) {
            // isHighestPriority = true;
            // }
            // }
            // =========================================================
            // Create ProductionOrder object from strings-row
            // if (isBlocking) {
            // order.markAsBlocking();
            // }
            // if (isHighestPriority) {
            // order.priorityHighest();
            // }
            // if (isLowestPriority) {
            // order.priorityLowest();
            // }
            // Enqueue created order
            initialProductionQueue.add(order);
            currentProductionQueue.add(order);
        }
    }

    /**
     * Reads build orders from CSV file and converts them into ArrayList.
     */
    private void initializeProductionQueue() {

        // Convert 2D String array into ArrayList of ProductionOrder
        createProductionOrderListFromStringArray();
    }

    /**
     * Auxiliary method that can be run to see what was loaded from CSV file.
     */
    @SuppressWarnings("unused")
    private void displayLoadedFile(String[][] loadedFile) {
        int rowCounter = 0;
        for (String[] rows : loadedFile) {
            if (rowCounter == 0) {
                rowCounter++;
                continue;
            }

            // =========================================================
            for (String value : rows) {
                System.out.print(value + " | ");
            }
            System.out.println();

            // =========================================================
            rowCounter++;
        }

        System.exit(0);
    }

}
