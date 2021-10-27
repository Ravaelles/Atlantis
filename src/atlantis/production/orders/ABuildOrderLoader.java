package atlantis.production.orders;

import atlantis.AGame;
import atlantis.AtlantisConfig;
import atlantis.combat.missions.MissionsFromBuildOrder;
import atlantis.production.ProductionOrder;
import atlantis.strategy.AStrategy;
import atlantis.units.AUnitType;
import atlantis.util.A;
import atlantis.util.NameUtil;
import bwapi.TechType;
import bwapi.UpgradeType;

import java.io.File;
import java.util.ArrayList;


public class ABuildOrderLoader {
    
    /**
     * Directory that contains build orders.
     */
    public static final String BUILD_ORDERS_PATH = "bwapi-data/AI/build_orders/";
    
    // =========================================================
    
    public static ABuildOrder getBuildOrderForStrategy(AStrategy strategy) {
//        String filePath = BUILD_ORDERS_PATH + buildOrder.getBuildOrderRelativePath();
        String filePath = BUILD_ORDERS_PATH + strategy.race() + "/" + strategy.getName() + ".txt";
        System.out.println("\r\nUse build order from file: `" + strategy.getName() + ".txt`");

        File f = new File(filePath);
        if (!f.exists()) {
            String message = "### Build order file does not exist:\r\n" + filePath + "\r\n### Quit ###";

            if (A.seconds() < 1) {
                throw new RuntimeException(message);
            } else {
                System.err.println(message);
            }
        }

        ABuildOrderLoader loader = new ABuildOrderLoader();
        return loader.readBuildOrdersFromFile(strategy.race(), filePath);
    }

    // =========================================================

    /**
     * Reads build orders from CSV file and converts them into ArrayList.
     */
    protected ABuildOrder readBuildOrdersFromFile(String race, String filePath) {
        final int NUMBER_OF_COLUMNS_IN_FILE = 2;

        // Read file into 2D String array
        String buildOrdersFile = filePath;
        System.out.println();
        System.out.println(
                "Using `" + filePath.replace(ABuildOrderLoader.BUILD_ORDERS_PATH, "") + "` build orders file."
        );

        // Parse CSV
        String[][] loadedFile = A.loadCsv(buildOrdersFile, NUMBER_OF_COLUMNS_IN_FILE);

        // We can display file here, if we want to
        //displayLoadedFile(loadedFile);

        // =========================================================
        // Skip first row as it's CSV header
        int counter = 0;

        ArrayList<ProductionOrder> productionOrders = new ArrayList<>();
        for (String[] row : loadedFile) {
//            System.out.print("Processing row:  #" + counter + "/" + loadedFile.length + ":  ");
//            System.out.println(row[0] + " - " + (row.length > 1 ? row[1] : "") + ",   SIZE OF INITIAL: " + initialProductionQueue.size());
            ProductionOrder productionOrder = parseCsvRow(row);
            productionOrders.add(productionOrder);
//            counter++;
        }

        ABuildOrder buildOrder = ABuildOrderFactory.forRace(race, filePath, productionOrders);

        System.out.println("buildOrder");
        System.out.println(buildOrder);

        return buildOrder;

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
//        buildFullBuildOrderSequenceBasedOnRawOrders();

        // === Display initial production queue ====================
//        System.out.println("Initial production order queue:");
//        for (ProductionOrder productionOrder : initialProductionQueue) {
//            System.out.println("   - " + productionOrder.toString());
//        }
//        System.out.println("END OF Initial production order queue");
//        Atlantis.end();
    }

    /**
     * Auxiliary method that can be run to see what was loaded from CSV file.
     */
    @SuppressWarnings("unused")
    protected void displayLoadedFile(String[][] loadedFile) {
        int rowCounter = 0;
        System.out.println();
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
    // Private methods
    
    protected static boolean _isCommentMode = false;
    
    /**
     * Analyzes CSV row, where each array element is one column.
     * @return
     */
    protected ProductionOrder parseCsvRow(String[] row) {

        // =========================================================
        // Ignore comments and blank lines
        if (isCommentLine(row)) {
            return null;
        }

        // Check for special commands that start with #
        if (isSpecialCommand(row)) {
            handleSpecialCommand(row);
            return null;
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
        AUnitType unitType = AUnitType.getByName(nameString);
        UpgradeType upgrade = NameUtil.getUpgradeTypeByName(nameString); //TODO: put this in UpgradeUtil
        TechType tech = NameUtil.getTechTypeByName(nameString); //TODO: put this in TechUtil
        String mission = null;

        // Define convienience boolean variables
        boolean isUnit = unitType != null;
        boolean isUpgrade = upgrade != null;
        boolean isTech = tech != null;
        boolean isMission = mission != null;

        // Check if no error occured like no object found
        if (!isUnit && !isUpgrade && !isTech && !isMission) {
            System.err.println("Invalid production order entry: " + nameString);
            AGame.exit();
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
            AGame.exit();
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
//        AProductionQueue.initialProductionQueue.add(order);

        return order;
    }

    /**
     * Converts names like "tank" into "Siege Tank Tank Mode".
     */
    protected String convertIntoValidNames(String nameString) {
        
        // TERRAN
        if ("siege tank".equals(nameString) || "tank".equals(nameString)) {
            return "Siege Tank Tank Mode";
        }
        else if ("marine range".equals(nameString)) {
            return "U_238_Shells";
        }
        else if ("mines".equals(nameString) || "mine".equals(nameString)) {
            return "Spider_Mines";
        }
        
        // Protoss
        else if ("dragoon range".equals(nameString)) {
            return "Singularity Charge";
        }
        
        return nameString;
    }
    

    // =========================================================
    // Special commands used in build orders file
    
    /**
     * If the first character in column is # it means it's special command.
     */
    protected boolean isSpecialCommand(String[] row) {
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
    protected boolean isCommentLine(String[] row) {
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
    protected void handleSpecialCommand(String[] row) {
        String commandLine = row[0].toUpperCase();

        if (commandLine.startsWith("//")) {
            return;
        }

//        if (command.startsWith("AUTO_PRODUCE_WORKERS_UNTIL_N_WORKERS")) {
//            AtlantisConfig.AUTO_PRODUCE_WORKERS_UNTIL_N_WORKERS = extractSpecialCommandValue(row);
//        } else 
        if (commandLine.startsWith("#AUTO_PRODUCE_WORKERS_SINCE_N_WORKERS")) {
            AtlantisConfig.AUTO_PRODUCE_WORKERS_SINCE_N_WORKERS = extractSpecialCommandValue(row);
        } else if (commandLine.startsWith("#AUTO_PRODUCE_WORKERS_MAX_WORKERS")) {
            AtlantisConfig.AUTO_PRODUCE_WORKERS_MAX_WORKERS = extractSpecialCommandValue(row);
        } else if (commandLine.startsWith("#SCOUT_IS_NTH_WORKER")) {
            AtlantisConfig.SCOUT_IS_NTH_WORKER = extractSpecialCommandValue(row);
        } else if (commandLine.startsWith("#USE_AUTO_SUPPLY_MANAGER_WHEN_SUPPLY_EXCEEDS")) {
            AtlantisConfig.USE_AUTO_SUPPLY_MANAGER_WHEN_SUPPLY_EXCEEDS = extractSpecialCommandValue(row);
        } else if (commandLine.contains("MISSION - ")) {
            handleMissionCommand(commandLine);
        }
    }

    private void handleMissionCommand(String line) {
        if (A.countSubstrings(line, " - ") != 3) {
            System.err.println("Mission modyfing command must use notation: AT_SUPPLY - MISSION=[RUSH|RESET]");
            AGame.exit();
        }

        int supply = Integer.parseInt(line.substring(0, line.indexOf(" - MISSION=")));
        String mission = line.substring(line.lastIndexOf("=") + 1);

        System.out.println("supply = " + supply);
        System.out.println("mission = " + mission);

        MissionsFromBuildOrder.addDynamicMission(mission, supply);
    }

    /**
     * Gets integer value from a row that contains special build order command.
     */
    protected int extractSpecialCommandValue(String[] row) {
        return Integer.parseInt(row[0].substring(row[0].lastIndexOf("=") + 1));
    }
    
}
