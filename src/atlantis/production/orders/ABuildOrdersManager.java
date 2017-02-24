package atlantis.production.orders;

import atlantis.Atlantis;
import atlantis.AtlantisConfig;
import atlantis.AGame;
import atlantis.constructing.AConstructionManager;
import atlantis.production.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.util.AtlantisUtilities;
import atlantis.util.NameUtil;
import atlantis.wrappers.ATech;
import atlantis.wrappers.MappingCounter;
import bwapi.TechType;
import bwapi.UnitType;
import bwapi.UpgradeType;
import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents abstract build orders read from the file. Build Orders in Atlantis are called "Production
 * Orders", because you can produce both units and buildings and one couldn't say you build marines, rather
 * produce.
 */
public abstract class ABuildOrdersManager {

    public static final int MODE_ALL_ORDERS = 1;
    public static final int MODE_ONLY_UNITS = 2;

    /**
     * Directory that contains build orders.
     */
    private static final String BUILD_ORDERS_PATH = "bwapi-data/AI/build_orders/";

    // === Internal fields =====================================
    
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

    // === Constructor =========================================
    
    public ABuildOrdersManager() {
        readBuildOrdersFile();
    }

    // =========================================================
    
    /**
     * Returns name of file with build orders.
     */
    protected abstract String getFilename();

    /**
     * Reads build orders from CSV file and converts them into ArrayList.
     */
    private void readBuildOrdersFile() {
        final int NUMBER_OF_COLUMNS_IN_FILE = 2;

        // Read file into 2D String array
        String buildOrdersFile = BUILD_ORDERS_PATH + getFilename();
        System.out.println();
        System.out.println("Using `" + getFilename() + "` build orders file.");
        
        // Parse CSV
        String[][] loadedFile = AtlantisUtilities.loadCsv(buildOrdersFile, NUMBER_OF_COLUMNS_IN_FILE);

        // We can display file here, if we want to
        //displayLoadedFile(loadedFile);

        // =========================================================
        // Skip first row as it's CSV header
        int counter = 0;
        for (String[] row : loadedFile) {
//            System.out.print("Processing row:  #" + counter + "/" + loadedFile.length + ":  ");
//            System.out.println(row[0] + " - " + (row.length > 1 ? row[1] : "") + ",   SIZE OF INITIAL: " + initialProductionQueue.size());
            parseCsvRow(row);
            counter++;
        }

        // =========================================================
        // Converts shortcut notations like:
        //        6 - Barracks
        //        8 - Supply Depot
        //        8 - Marine - x2
        //        Marine - x3
        //        15 - Supply Depot
        //
        // To full build order sequence like this:
        //   - SCV
        //   - SCV
        //   - Barracks
        //   - SCV
        //   - Supply Depot
        //   - Marine
        //   - Marine
        //   - Marine
        //   - Marine
        //   - Marine
        //   - SCV
        //   - SCV
        //   - SCV
        //   - SCV
        //   - Supply Depot
        buildFullBuildOrderSequeneBasedOnRawOrders();

        // === Display initial production queue ====================
//        System.out.println("Initial production order queue:");
//        for (ProductionOrder productionOrder : initialProductionQueue) {
//            System.out.println("   - " + productionOrder.toString());
//        }
//        System.out.println("END OF Initial production order queue");
//        Atlantis.end();
    }

    /**
     * Returns default production strategy according to the race played.
     */
    public static ABuildOrdersManager loadBuildOrders() {
        if (AGame.playsAsTerran()) {
            return new TerranBuildOrders();
        } else if (AGame.playsAsProtoss()) {
            return new ProtossBuildOrders();
        } else if (AGame.playsAsZerg()) {
            return new ZergBuildOrders();
        }

        System.err.println("getAccordingToRace: Unknown race");
        System.exit(-1);
        return null;
    }

    // =========================================================
    
    /**
     * Returns list of things (units and upgrades) that we should produce (train or build) now. Or if you only
     * want to get units, use <b>onlyUnits</b> set to true. This merhod iterates over latest build orders and
     * returns those build orders that we can build in this very moment (we can afford them and they match our
     * strategy).
     *
     * @param int mode use this classes constants; if MODE_ONLY_UNITS it will only return "units" as opposed
     * to buildings (keep in mind AUnit is both "unit" and building)
     */
    public ArrayList<ProductionOrder> getThingsToProduceRightNow(int mode) {
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
            for (AUnitType unitType : produceWhenNoProductionOrders()) {
                if (AGame.hasBuildingsToProduce(unitType, false)) {
                    result.add(new ProductionOrder(unitType));
                }
            }
        }

        return result;
    }

    // === Abstract methods ====================================
    
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
    // Public methods
    
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
    private int countUnitsOfGivenTypeOrSimilar(AUnitType type) {
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
    public ArrayList<ProductionOrder> getProductionQueueNext(int howMany) {
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

    /**
     * Returns object that is responsible for the production queue.
     */
    public static ABuildOrdersManager getBuildOrders() {
        return AtlantisConfig.getBuildOrders();
    }

    // =========================================================
    // Private methods
    
    private static boolean _isCommentMode = false;
    
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

        // =========================================================
        
        ProductionOrder order = null;
        String modifier = null; // Build order modifier, will be assigned later

        // Skip first column as it's only order number / description / whatever
        int inRowCounter = 1;

        // If only one column in row, don't skip anything as first string is already important
        if (row.length <= 1) {
            inRowCounter = 0;
        }

        // If two rows and last cell start with "x", dont skip first string
        if (row.length == 2 && row[1].length() > 0 && row[1].charAt(0) == 'x') {
            inRowCounter = 0;
            modifier = row[1];
        }

        // =========================================================
        // Parse entire row of strings
        // Define type of entry: AUnit / Research / Tech
        String nameString = row[inRowCounter++].toLowerCase().trim();

        // === Parse some strings ==================================
        
        nameString = convertIntoValidNames(nameString);

        // =========================================================
        // Try getting objects of each type as we don't know if it's unit, research or tech.
        // UNIT
//        NameUtil.disableErrorReporting = true;
        AUnitType unitType = AUnitType.getByName(nameString);
//        NameUtil.disableErrorReporting = false;

        // UPGRADE
//        NameUtil.disableErrorReporting = true;
        UpgradeType upgrade = NameUtil.getUpgradeTypeByName(nameString); //TODO: put this in UpgradeUtil
//        NameUtil.disableErrorReporting = false;

        // TECH
//        NameUtil.disableErrorReporting = true;
        TechType tech = NameUtil.getTechTypeByName(nameString); //TODO: put this in TechUtil
//        NameUtil.disableErrorReporting = false;

        // Define convienience boolean variables
        boolean isUnit = unitType != null;
        boolean isUpgrade = upgrade != null;
        boolean isTech = tech != null;

        // Check if no error occured like no object found
        if (!isUnit && !isUpgrade && !isTech) {
            System.out.println("Invalid production order entry: " + nameString);
            System.err.println("Invalid production order entry: " + nameString);
            Atlantis.getInstance().onEnd(false);
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
            System.err.println("Invalid build order: " + nameString);
            System.err.println("Please correct it.");
            Atlantis.end();
        }
        
        // =========================================================
        // Save first column from row as it may contain build order modifiers
        order.setRawFirstColumnInFile(row[0]);
        order.setNumberOfColumnsInRow(row.length);

        // =========================================================
        // Save order modifier
        order.setModifier(modifier);
        if (row.length >= 3) {
            String modifierString = row[inRowCounter++].toUpperCase().trim();
            order.setModifier(modifierString);
        }

        // Enqueue created order
        initialProductionQueue.add(order);
    }

    /**
     * Converts names like "tank" into "Siege Tank Tank Mode".
     */
    private String convertIntoValidNames(String nameString) {
        
        // TERRAN
        if ("siege tank".equals(nameString) || "tank".equals(nameString)) {
            return "Siege Tank Tank Mode";
        }
        else if ("marine range".equals(nameString)) {
            return "U_238_Shells";
        }
        
        // PROTOSS
        else if ("dragoon range".equals(nameString)) {
            return "Singularity Charge";
        }
        
//        UpgradeType.U_238_Shells
        
        return nameString;
    }
    
    /**
     * Converts (and repeats if needed) shortcut build order notations like: 
            6 - Barracks
            8 - Supply Depot
            8 - Marine - x2
            Marine - x3
            15 - Supply Depot
    
     To full build order sequence like this:
       - SCV
       - SCV
       - Barracks
       - SCV
       - Supply Depot
       - Marine
       - Marine
       - Marine
       - Marine
       - Marine
       - SCV
       - SCV
       - SCV
       - SCV
       - Supply Depot
     */
    private void buildFullBuildOrderSequeneBasedOnRawOrders() {
        ArrayList<ProductionOrder> newInitialQueue = new ArrayList<>();
        
//        System.out.println();
//        System.out.println();
//        System.out.println("Initial queue");
//        for (ProductionOrder productionOrder : initialProductionQueue) {
//            System.out.print(productionOrder.getRawFirstColumnInFile() + ":  ");
//            System.out.println(productionOrder.getShortName());
//        }
//        System.out.println();
//        System.out.println();

        int lastSupplyFromFile = -1;
        for (int currentSupply = 4; currentSupply <= 200; currentSupply++) {
            
            // If no more orders left, exit the loop
            if (initialProductionQueue.isEmpty()) {
                break;
            }
            
            ProductionOrder order = initialProductionQueue.get(0);
            
            // === Check if should worker build order ========================================
            
            int orderSupplyRequired;
            try {
                orderSupplyRequired = Integer.parseInt(order.getRawFirstColumnInFile());
            }
            catch (NumberFormatException e) {
                orderSupplyRequired = lastSupplyFromFile + 1; // Take last order supply value and increment it
            }
            lastSupplyFromFile = orderSupplyRequired;

            // =========================================================
            
            // Insert additional worker build order
            if (orderSupplyRequired < 0 || currentSupply < orderSupplyRequired) {
                ProductionOrder workerOrder = new ProductionOrder(AtlantisConfig.WORKER);
                newInitialQueue.add(workerOrder);
            }
            
            // Add build order from file
            else {
//            System.out.println("---------");
//            System.out.println("NAME: " + order.getShortName());
//            System.out.println("MODIFIER: " + order.getModifier());

                if (order.getModifier() != null && order.getModifier().charAt(0) == 'x' && order.getUpgrade() == null) {
                    int timesToMultiply = 1;
                    if (order.getModifier() != null) {
                        timesToMultiply = Integer.parseInt(order.getModifier().substring(1)) - 1;
                    }
                    for (int multiplyCounter = 0; multiplyCounter < timesToMultiply; multiplyCounter++) {
                        ProductionOrder newOrder = order.copy();
                        newInitialQueue.add(newOrder);
                    }
                }

                ProductionOrder newOrder = order.copy();
                initialProductionQueue.remove(0);
                newInitialQueue.add(newOrder);
            }
        }

        // Replace old initial queue with new
        initialProductionQueue.clear();
        initialProductionQueue.addAll(newInitialQueue);
        currentProductionQueue.addAll(newInitialQueue);
    }

    /**
     * Auxiliary method that can be run to see what was loaded from CSV file.
     */
    @SuppressWarnings("unused")
    private void displayLoadedFile(String[][] loadedFile) {
        int rowCounter = 0;
        System.out.println("");
        System.out.println("===== LOADED FILE =====");
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
        System.out.println("===== End of LOADED FILE ==");
        System.out.println();
    }

    // =========================================================
    // Special commands used in build orders file
    
    /**
     * If the first character in column is # it means it's special command.
     */
    private boolean isSpecialCommand(String[] row) {
        if (row.length >= 1) {
            return row[0].charAt(0) == '#';
        }
        else {
            return false;
        }
    }

    /**
     * // Means comment - should skip it. We can also have blank lines.
     */
    private boolean isUnimportantLine(String[] row) {
        if (row.length >= 1 && row[0].length() > 0) {
            
            // Detect multi-line comment end
            if (row[0].contains("*/")) {
                _isCommentMode = false;
                return true;
            }
            
            // Detect multi-line comment start
            if (row[0].contains("/**")) {
                _isCommentMode = true;
                return true;
            }
            
            // Detect being inside multi-line comment
            if (_isCommentMode) {
                return true;
            }
            
            // Detect comments like "//"
            if (row[0].startsWith("//")) {
                System.err.println("skip " + row[0]);
                return true;
            }
        }
        
        return row.length == 0 || row[0].isEmpty() || row[0].equals("")
                || row[0].equals("Number") || row[0].equals("@") || row[0].equals("Order") || row[0].equals(";");
    }

    /**
     * If the first character in column is # it means it's special command. Here we handle all of them.
     */
    private void handleSpecialCommand(String[] row) {
        String command = row[0].substring(1).toUpperCase();

//        if (command.startsWith("AUTO_PRODUCE_WORKERS_UNTIL_N_WORKERS")) {
//            AtlantisConfig.AUTO_PRODUCE_WORKERS_UNTIL_N_WORKERS = extractSpecialCommandValue(row);
//        } else 
        if (command.startsWith("AUTO_PRODUCE_WORKERS_SINCE_N_WORKERS")) {
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
