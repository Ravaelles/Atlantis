package atlantis.production.orders;

import atlantis.AtlantisConfig;
import atlantis.AtlantisGame;
import atlantis.constructing.AtlantisConstructingManager;
import atlantis.production.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.util.AtlantisUtilities;
import atlantis.util.NameUtil;
import atlantis.wrappers.AtlantisTech;
import atlantis.wrappers.MappingCounter;
import bwapi.TechType;
import bwapi.UpgradeType;
import java.io.File;
import java.util.ArrayList;

/**
 * Represents abstract build orders read from the file.
 */
public abstract class AtlantisBuildOrders {

    /**
     * Directory that contains build orders.
     */
    private static final String BUILD_ORDERS_PATH = "bwapi-data/AI/build_orders/";

    // =========================================================
    
    /**
     * Ordered list of production orders as initially read from the file. It never changes
     */
    private final ArrayList<ProductionOrder> initialProductionQueue = new ArrayList<>();

    /**
     * Ordered list of next units we should build. It is re-generated when events like "started
     * training/building new unit"
     */
    private final ArrayList<ProductionOrder> currentProductionQueue = new ArrayList<>();

    /**
     * Number of minerals reserved to produce some units/buildings.
     */
    private static int mineralsNeeded = 0;
    
    /**
     * Number of gas reserved to produce some units/buildings.
     */
    private static int gasNeeded = 0;
    
    // =========================================================
    // Constructor
    
    public AtlantisBuildOrders() {
        readBuildOrdersFile();
    }
    
    /**
     * Returns name of file with build orders.
     */
    protected String getFilename() {
        String ourRaceLetter = AtlantisGame.getPlayerUs().getRace().toString().substring(0, 1);
        String enemyRaceLetter = AtlantisGame.getEnemy().getRace().toString().substring(0, 1);
        String customBuildOrders = ourRaceLetter + "v" + enemyRaceLetter + ".csv";
        
        // Check if user has defined his own build orders file for this race and if so, use it.
        File buildOrdersFile = new File(BUILD_ORDERS_PATH + customBuildOrders);
        if (buildOrdersFile.exists()) {
            return customBuildOrders;
        }
        
        // Return default Atlantis build orders for this race vs race matchup.
        else {
            String atlantisDefaultBuildOrders = "Atlantis_" + customBuildOrders;
            return atlantisDefaultBuildOrders;
        }
    }
    
    /**
     * Returns default production strategy according to the race played.
     */
    public static AtlantisBuildOrders loadBuildOrders() {
        if (AtlantisGame.playsAsTerran()) {
            return new TerranBuildOrders();
        } else if (AtlantisGame.playsAsProtoss()) {
            return new ProtossBuildOrders();
        } else if (AtlantisGame.playsAsZerg()) {
            return new ZergBuildOrders();
        }

        System.err.println("getAccordingToRace: Unknown race");
        System.exit(-1);
        return null;
    }
    
    // =========================================================
    // Probably most important method in the world
    
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
        mineralsNeeded = resourcesNeededForNotStartedBuildings[0];
        gasNeeded = resourcesNeededForNotStartedBuildings[1];

        // =========================================================
        // The idea as follows: as long as we can afford next enqueued production order, add it to the
        // CurrentToProduceList.
        
//        System.out.println("// =========================================================");
//        for (ProductionOrder order : currentProductionQueue) {
//        System.out.println(order.getUnitType());
//        }
        
        for (ProductionOrder order : currentProductionQueue) {
            AUnitType unitType = order.getUnitType();
            UpgradeType upgrade = order.getUpgrade();
            TechType tech = order.getTech();

            // Check if include only units
            if (onlyUnits && unitType == null) {
                continue;
            }
            
            // =========================================================
            // Protoss fix: wait for at least one Pylon
            if (AtlantisGame.playsAsProtoss() && unitType != null
                    && !AUnitType.Protoss_Pylon.equals(unitType)
                    && Select.our().countUnitsOfType(AUnitType.Protoss_Pylon) == 0) {
                continue;
            }
            
            // =========================================================

            if (unitType != null) {
                if (!AtlantisGame.hasBuildingsToProduce(unitType, true)) {
                    continue;
                }
                
                mineralsNeeded += unitType.getMineralPrice();
                gasNeeded += unitType.getGasPrice();
            } else if (upgrade != null) {
                mineralsNeeded += upgrade.mineralPrice() * upgrade.mineralPriceFactor();
                gasNeeded += upgrade.gasPrice() * upgrade.gasPriceFactor();
            } else if (tech != null) {
                mineralsNeeded += tech.mineralPrice();
                gasNeeded += tech.gasPrice();	//previous was `getMineralPrice()`, this seems to be a bugfix
            }

            // =========================================================
            // If we can afford this order and the previous, add it to CurrentToProduceList.
            if (AtlantisGame.canAfford(mineralsNeeded, gasNeeded)) {
                result.add(order);
            } // We can't afford to produce this order along with all previous ones. Return currently list.
            else {
//                System.out.println("-----break at: " + unitType);
                break;
            }
        }

        // =========================================================
        // Produce something if queue is empty
        if (result.isEmpty() && AtlantisGame.getSupplyUsed() >= 9) {
            for (AUnitType unitType : produceWhenNoProductionOrders()) {
                result.add(new ProductionOrder(unitType));
            }
        }

        return result;
    }
    
    // =========================================================
    // Abstract methods

    /**
     * Request to produce worker (Zerg Drone, Terran SCV or Protoss Probe) that should be handled according to
     * the race played.
     */
    public abstract void produceWorker();

    /**
     * Request to produce non-building and non-worker unit. Should be handled according to the race played.
     */
    public abstract void produceUnit(AUnitType unitType);

    /**
     * When production orders run out, we should always produce some units.
     */
    public abstract ArrayList<AUnitType> produceWhenNoProductionOrders();

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
        MappingCounter<AUnitType> virtualCounter = new MappingCounter<>();

        // =========================================================
        for (ProductionOrder order : initialProductionQueue) {
            boolean isOkayToAdd = false;
            
            // =========================================================
            // Unit
            if (order.getUnitType() != null) {
                AUnitType type = order.getUnitType();
                virtualCounter.incrementValueFor(type);

                int shouldHaveThisManyUnits = (type.isWorker() ? 4 : 0) + virtualCounter.getValueFor(type);
                int weHaveThisManyUnits = countUnitsOfGivenTypeOrSimilar(type);

                if (type.isBuilding()) {
                    weHaveThisManyUnits += AtlantisConstructingManager.countNotFinishedConstructionsOfType(type);
                }
                
//                System.out.println("       " + weHaveThisManyUnits + " / " + shouldHaveThisManyUnits);

                // If we don't have this unit, add it to the current production queue.
                if (weHaveThisManyUnits < shouldHaveThisManyUnits) {
                    isOkayToAdd = true;
                }
            } 
            // Upgrade
            else if (order.getUpgrade() != null) {
                isOkayToAdd = !AtlantisTech.isResearched(order.getUpgrade());
            } 
            // Tech
            else if (order.getTech() != null) {
                isOkayToAdd = !AtlantisTech.isResearched(order.getTech());
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
     * Some buildings like Sunken Colony are morphed into from Creep Colony. When counting Creep Colonies,
     * we need to count sunkens as well.
     */
    private int countUnitsOfGivenTypeOrSimilar(AUnitType type) {
        if (type.equals(AUnitType.Zerg_Creep_Colony)) {
            return Select.ourIncludingUnfinished().ofType(type).count() 
                    + Select.ourIncludingUnfinished().ofType(AUnitType.Zerg_Spore_Colony).count()
                    + Select.ourIncludingUnfinished().ofType(AUnitType.Zerg_Sunken_Colony).count();
        }
        else {
            return Select.ourIncludingUnfinished().ofType(type).count();
        }
    }

    /**
     * Returns <b>howMany</b> of next units to build, no matter if we can afford them or not.
     */
    public ArrayList<ProductionOrder> getProductionQueueNext(int howMany) {
        ArrayList<ProductionOrder> result = new ArrayList<>();

        for (int i = 0; i < howMany && i < currentProductionQueue.size(); i++) {
            ProductionOrder productionOrder = currentProductionQueue.get(i);
//            if (productionOrder.getUnitType() != null 
//                    && !AtlantisGame.hasBuildingsToProduce(productionOrder.getUnitType())) {
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
    
    /**
     * Returns object that is responsible for the production queue.
     */
    public static AtlantisBuildOrders getBuildOrders() {
        return AtlantisConfig.getBuildOrders();
    }
    
    // =========================================================
    // Private defined methods
    
    /**
     * Populates <b>productionOrdersFromFile</b> with data from CSV file.
     */
    private void createProductionOrderListFromStringArray() {
        final int NUMBER_OF_COLUMNS_IN_FILE = 2;

        // Read file into 2D String array
        String buildOrdersFile = BUILD_ORDERS_PATH + getFilename();
        System.out.println("Using `" + getFilename() + "` build orders file.");
        String[][] loadedFile = AtlantisUtilities.loadCsv(buildOrdersFile, NUMBER_OF_COLUMNS_IN_FILE);

        // We can display file here, if we want to
//         displayLoadedFile(loadedFile);

        // =========================================================
        // Skip first row as it's CSV header
        for (String[] row : loadedFile) {
            parseCsvRow(row);
        }
    }
   
    /**
     * Analyzes CSV row, where each array element is one column.
     */
    private void parseCsvRow(String[] row) {
        
        // =========================================================
        // Ignore comments and blank lines
        if (isUnimportantLine(row)) {
            return;
        }

        // Check for special commands that start with #
        if (isSpecialCommand(row)) {
            handleSpecialCommand(row);
            return;
        }
        
        int inRowCounter = 1; // Skip first column as it's only order number / description / whatever
        ProductionOrder order = null;

        // =========================================================
        // Parse entire row of strings
        // Define type of entry: AUnit / Research / Tech
        String nameString = row[inRowCounter++].toLowerCase().trim();

        // =========================================================
        // Try getting objects of each type as we don't know if it's unit, research or tech.
        // UNIT
        NameUtil.disableErrorReporting = true;
        AUnitType unitType = AUnitType.getByName(nameString);	
        NameUtil.disableErrorReporting = false;

        // UPGRADE
        NameUtil.disableErrorReporting = true;
        UpgradeType upgrade = NameUtil.getUpgradeTypeByName(nameString); //TODO: put this in UpgradeUtil
        NameUtil.disableErrorReporting = false;

        // TECH
        NameUtil.disableErrorReporting = true;
        TechType tech = NameUtil.getTechTypeByName(nameString); //TODO: put this in TechUtil
        NameUtil.disableErrorReporting = false;

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
        // Check for modifiers
        
        if (row.length >= 3) {
            String modifierString = row[inRowCounter++].toUpperCase().trim();
            order.setModifier(modifierString);
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

    /**
     * Reads build orders from CSV file and converts them into ArrayList.
     */
    private void readBuildOrdersFile() {

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
//            if (rowCounter == 0) {
//                rowCounter++;
//                continue;
//            }

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

    // =========================================================
    // Special commands
    
    /**
     * If the first character in column is # it means it's special command.
     */
    private boolean isSpecialCommand(String[] row) {
        return (row.length >= 1 && row[0].charAt(0) == '#');
    }

    /**
     * // Means comment - should skip it. We can also have blank lines.
     */
    private boolean isUnimportantLine(String[] row) {
        return row.length == 0 || row[0].isEmpty() || row[0].equals("")
                || row[0].equals("Number")|| row[0].equals("Order") || row[0].equals(";");
    }

    /**
     * If the first character in column is # it means it's special command. Here we handle all of them.
     */
    private void handleSpecialCommand(String[] row) {
        String command = row[0].substring(1).toUpperCase();

        if (command.startsWith("AUTO_PRODUCE_WORKERS_UNTIL_N_WORKERS")) {
            AtlantisConfig.AUTO_PRODUCE_WORKERS_UNTIL_N_WORKERS = extractSpecialCommandValue(row);
        } else if (command.startsWith("AUTO_PRODUCE_WORKERS_SINCE_N_WORKERS")) {
            AtlantisConfig.AUTO_PRODUCE_WORKERS_SINCE_N_WORKERS = extractSpecialCommandValue(row);
        } else if (command.startsWith("AUTO_PRODUCE_WORKERS_MAX_WORKERS")) {
            AtlantisConfig.AUTO_PRODUCE_WORKERS_MAX_WORKERS = extractSpecialCommandValue(row);
        } else if (command.startsWith("SCOUT_IS_NTH_WORKER")) {
            AtlantisConfig.SCOUT_IS_NTH_WORKER = extractSpecialCommandValue(row);
        } else if (command.startsWith("USE_AUTO_SUPPLY_MANAGER_WHEN_SUPPLY_EXCEEDS")) {
            AtlantisConfig.USE_AUTO_SUPPLY_MANAGER_WHEN_SUPPLY_EXCEEDS = extractSpecialCommandValue(row);
        }
    }

    /**
     * Gets integer value from a row that contains special build order command.
     */
    private int extractSpecialCommandValue(String[] row) {
        return Integer.parseInt(row[0].substring(row[0].lastIndexOf("=") + 1));
    }

    // =========================================================
    // Getters

    /**
     * Number of minerals reserved to produce some units/buildings.
     */
    public static int getMineralsNeeded() {
        return mineralsNeeded;
    }

    /**
     * Number of gas reserved to produce some units/buildings.
     */
    public static int getGasNeeded() {
        return gasNeeded;
    }
    
}
