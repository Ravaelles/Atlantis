package atlantis.config;

import atlantis.game.A;

import java.io.File;
import java.util.ArrayList;

public class AtlantisIgniter {

    private static boolean shouldUpdateFileContent = false;
    private static String bwapiIniPath = null;
    private static String chaosLauncherPath = null;
    private static String[] fileContent = null;

    // =========================================================

    public static String getBwapiIniPath() {
        return bwapiIniPath;
    }

    public static String getChaosLauncherPath() {
        return chaosLauncherPath;
    }

    // =========================================================

    public static void modifyBwapiFileIfNeeded() {

        // Only allow bwapi.ini modification if file MODIFY_BWAPI is found
        // in project root or level up
        if (!(new File("MODIFY_BWAPI")).exists() && !(new File("../MODIFY_BWAPI")).exists()) {
            return; // Tournament mode: don't modify bwapi.ini
        }

        // =========================================================

        // Try locating bwap.ini file
        bwapiIniPath = getBwapiIniPath();
        if (bwapiIniPath == null) {
            System.err.println("Couldn't locate bwapi.ini file. See ENV and ENV-EXAMPLE file.");
            return;
        }

        // Read every single line
        ArrayList<String> linesList = A.readTextFileToList(bwapiIniPath);
        fileContent = new String[linesList.size()];
        fileContent = linesList.toArray(fileContent);

        // === Modify some sections ================================

        updateOurRace();
        updateEnemyRace();
        updateMapAndGameTypeIfNeeded();

        // =========================================================

        // Write to bwapi.ini new, modified file version
        if (shouldUpdateFileContent) {
            writeToBwapiIni();
        }
    }

    // =========================================================

    private static void updateOurRace() {
        for (int i = 0; i < fileContent.length; i++) {
            String line = fileContent[i];
            if (line.startsWith("race = ")) {
                fileContent[i] = "race = " + MapAndRace.OUR_RACE;

                if (!fileContent[i].equals(line)) {
                    shouldUpdateFileContent = true;
                    System.out.println("Updated our race in bwapi.ini to: " + MapAndRace.OUR_RACE);
                }
                return;
            }
        }
    }

    private static void updateEnemyRace() {
        for (int i = 0; i < fileContent.length; i++) {
            String line = fileContent[i];
            if (line.startsWith("enemy_race = ")) {
                fileContent[i] = "enemy_race = " + MapAndRace.ENEMY_RACE;

                if (!fileContent[i].equals(line)) {
                    shouldUpdateFileContent = true;
                    System.out.println("Updated enemy race in bwapi.ini to: " + MapAndRace.ENEMY_RACE);
                }
                return;
            }
        }
    }

    private static void updateMapAndGameTypeIfNeeded() {
        for (int i = 0; i < fileContent.length; i++) {
            String line = fileContent[i];
            if (line.startsWith("map = ")) {
                fileContent[i] = "map = " + MapAndRace.MAP;

                if (!fileContent[i].equals(line)) {
                    shouldUpdateFileContent = true;
                    System.out.println("Updated map in bwapi.ini to: " + MapAndRace.MAP);
                }
            }

            // game_type = USE_MAP_SETTINGS
            else if (line.startsWith("game_type = ")) {
                String gameType = (MapAndRace.MAP.contains("ums/") || MapAndRace.MAP.contains("Atlantis/"))
                        ? "USE_MAP_SETTINGS" : "MELEE";
                fileContent[i] = "game_type = " + gameType;

                if (!fileContent[i].equals(line)) {
                    shouldUpdateFileContent = true;
                    System.out.println("Updated game type in bwapi.ini to: " + gameType);
                }
            }
        }
    }

    private static void writeToBwapiIni() {
        String finalContent = "";

        for (String line : fileContent) {
            finalContent += line + "\n";
        }

        A.saveToFile(bwapiIniPath, finalContent, true);
    }

    // =========================================================

    public static void setBwapiIniPath(String bwapiIniPath) {
        AtlantisIgniter.bwapiIniPath = bwapiIniPath;
    }

    public static void setChaosLauncherPath(String chaosLauncherPath) {
        AtlantisIgniter.chaosLauncherPath = chaosLauncherPath;
    }
}
