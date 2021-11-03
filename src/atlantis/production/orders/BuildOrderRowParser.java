package atlantis.production.orders;

import atlantis.AGame;
import atlantis.combat.missions.MissionsFromBuildOrder;
import atlantis.production.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.util.A;
import atlantis.util.NameUtil;
import bwapi.TechType;
import bwapi.UpgradeType;

public class BuildOrderRowParser {

    /**
     * Analyzes CSV row, where each array element is one column.
     */
    protected static ProductionOrder parseCsvRow(String[] row, ABuildOrder buildOrder, int currentSupply) {

        // =========================================================
        // Ignore comments and blank lines
        if (isCommentLine(row)) {
            return null;
        }

        // Check for special commands that start with #
        if (isSpecialCommand(row)) {
            handleSpecialCommand(row, buildOrder);
            return null;
        }

        // =========================================================

//        // Skip first column as it's only order number / description / whatever
//        int inRowCounter = 1;
//
//        // If only one column in row, don't skip anything as first string is already important
//        if (row.length <= 1) {
//            inRowCounter = 0;
//        }
//
//        // If two rows and last cell start with "x", dont skip first string
//        if (row.length == 2 && row[1].length() > 0 && row[1].charAt(0) == 'x') {
//            inRowCounter = 0;
//            modifier = row[1];
//        }

        ProductionOrder order = null;
        int minSupplyForThisOrder;
        String nameString;
        String modifier = null;

        // Example: Gateway
        if (row.length == 1) {
            minSupplyForThisOrder = currentSupply;
            nameString = row[0];
        }
        // Example: Gateway - x2
        else if (row.length == 2 && row[1].length() > 0 && row[1].charAt(0) == 'x') {
            minSupplyForThisOrder = currentSupply;
            nameString = row[0];
            modifier = row[1];
        }
        // Example: 9 - Gateway
        else if (row.length == 2) {
            minSupplyForThisOrder = Integer.parseInt(row[0]);
            nameString = row[1];
        }
        // Example: 9 - Gateway - x2
        else if (row.length == 3) {
            minSupplyForThisOrder = Integer.parseInt(row[0]);
            nameString = row[1];
            modifier = row[2];
        }
        else {
            throw new RuntimeException("Unhandled parse build order line: " + row[0]);
        }
//        System.out.println(minSupplyForThisOrder + " // " + nameString + " // " +modifier);

        // =========================================================
        // Parse entire row of strings
        // Define type of entry: AUnit / Research / Tech

        nameString = convertIntoValidNames(nameString.toLowerCase().trim());

        // =========================================================
        // Try getting objects of each type as we don't know if it's unit, research or tech.

        // UNIT
        AUnitType unitType = AUnitType.getByName(nameString);
        UpgradeType upgrade = NameUtil.getUpgradeTypeByName(nameString); //TODO: put this in UpgradeUtil
        TechType tech = NameUtil.getTechTypeByName(nameString); //TODO: put this in TechUtil
        String mission = null;

        // Define convenience boolean variables
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
            order = new ProductionOrder(unitType, minSupplyForThisOrder);
        } // Upgrade
        else if (isUpgrade) {
            order = new ProductionOrder(upgrade, minSupplyForThisOrder);
        } // Tech
        else if (isTech) {
            order = new ProductionOrder(tech, minSupplyForThisOrder);
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
//        if (row.length >= 3) {
//            String modifierString = row[inRowCounter++].toUpperCase().trim();
//            order.setModifier(modifierString);
//        }

        // Enqueue created order
//        ProductionQueue.initialProductionQueue.add(order);

        return order;
    }

    // =========================================================


    protected static boolean _isCommentMode = false;

    /**
     * Converts names like "tank" into "Siege Tank Tank Mode".
     */
    protected static String convertIntoValidNames(String nameString) {

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
    protected static boolean isSpecialCommand(String[] row) {
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
    protected static boolean isCommentLine(String[] row) {
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
    protected static void handleSpecialCommand(String[] row, ABuildOrder buildOrder) {
        String commandLine = row[0].toUpperCase();

        if (commandLine.startsWith("//")) {
            return;
        }

        String settingKey;
        if (commandLine.startsWith(settingKey = "#AUTO_PRODUCE_WORKERS_MIN_WORKERS")) {
            buildOrder.addSetting(settingKey, extractSpecialCommandValue(row));
        } else if (commandLine.startsWith(settingKey = "#AUTO_PRODUCE_WORKERS_MAX_WORKERS")) {
            buildOrder.addSetting(settingKey, extractSpecialCommandValue(row));
        } else if (commandLine.startsWith(settingKey = "#SCOUT_IS_NTH_WORKER")) {
            buildOrder.addSetting(settingKey, extractSpecialCommandValue(row));
        } else if (commandLine.startsWith(settingKey = "#AUTO_SUPPLY_MANAGER_WHEN_SUPPLY_EXCEEDS")) {
            buildOrder.addSetting(settingKey, extractSpecialCommandValue(row));
        }
//        else if (commandLine.contains("MISSION - ")) {
//            handleMissionCommand(commandLine);
//        }
        else {
            throw new RuntimeException("Unhandled command in build order: " + commandLine);
        }
    }

    private static void handleMissionCommand(String line) {
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
    protected static int extractSpecialCommandValue(String[] row) {
        return Integer.parseInt(row[0].substring(row[0].lastIndexOf("=") + 1));
    }

}
