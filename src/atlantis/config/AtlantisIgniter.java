package atlantis.config;

import atlantis.config.env.Env;
import atlantis.game.A;
import atlantis.util.log.ErrorLog;
import main.Main;

import java.io.File;
import java.util.ArrayList;

public class AtlantisIgniter {
    private static boolean shouldUpdateFileContent = false;
    private static String bwapiDataPath = null;
    private static String chaosLauncherPath = null;
    private static String[] fileContent = null;

    // =========================================================

    public static String getBwapiDataPath() {
        if (Env.isTournament()) {
            return "./bwapi-data/";
        }

        if (bwapiDataPath == null && Env.isTesting()) {
//            filePath = "." + filePath; // Fix for tests: Replace ./ with ../
            return "..\\";
        }

        return bwapiDataPath;
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
        bwapiDataPath = getBwapiDataPath();
        if (bwapiDataPath == null) {
            if (Env.isLocal()) {
                ErrorLog.printPlusToFile("Couldn't locate bwapi.ini file. See ENV and ENV-EXAMPLE file.");
                ErrorLog.printPlusToFile("Go to bwapi-data/AI/ENV file and point it to your bwapi-data path.");
                A.quit();
            }
            return;
        }

        String bwapiIniPath = bwapiDataPath + "bwapi.ini";
        if (!A.fileExists(bwapiIniPath)) {
            ErrorLog.printPlusToFile("Couldn't locate bwapi.ini file at: " + bwapiIniPath);
            ErrorLog.printPlusToFile("Go to bwapi-data/AI/ENV file and point it to your bwapi.ini");
            A.quit();
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
                fileContent[i] = "race = " + Main.ourRace();

                if (!fileContent[i].equals(line)) {
                    shouldUpdateFileContent = true;
                    A.println("Updated our race in bwapi.ini to: " + Main.ourRace());
                }
                return;
            }
        }
    }

    private static void updateEnemyRace() {
        for (int i = 0; i < fileContent.length; i++) {
            String line = fileContent[i];
            if (line.startsWith("enemy_race = ")) {
                fileContent[i] = "enemy_race = " + Main.enemyRace();

                if (!fileContent[i].equals(line)) {
                    shouldUpdateFileContent = true;
                    System.out.println("Updated enemy race in bwapi.ini to: " + Main.enemyRace());
                }
                return;
            }
        }
    }

    private static void updateMapAndGameTypeIfNeeded() {
        if (Env.isBenchmark()) {
            fileContent = enforceGameTypeToUseMapSettings();
            shouldUpdateFileContent = true;
        }

        for (int i = 0; i < fileContent.length; i++) {
            String line = fileContent[i];
//            System.err.println("line = " + line);

            if (line.startsWith("map = ")) {
                fileContent[i] = "map = " + ActiveMap.activeMapPath();

                if (!fileContent[i].equals(line)) {
                    shouldUpdateFileContent = true;
                    System.out.println("Updated map in bwapi.ini to: " + ActiveMap.name());
                }
            }

            // game_type = USE_MAP_SETTINGS
            else if (line.startsWith("game_type = ")) {
                String gameType = Env.isBenchmark() || (
                    ActiveMap.activeMapPath().contains("ums/")
                ) ? "USE_MAP_SETTINGS" : "MELEE";
                fileContent[i] = "game_type = " + gameType;

                if (!fileContent[i].equals(line)) {
                    shouldUpdateFileContent = true;
                    System.out.println("Updated game type in bwapi.ini to: " + gameType);
                }
            }
        }

        fileContent = java.util.Arrays.stream(fileContent).distinct().toArray(String[]::new);
    }

    private static String[] enforceGameTypeToUseMapSettings() {
        String[] newFileContent = new String[fileContent.length + 1];
        System.arraycopy(fileContent, 0, newFileContent, 0, fileContent.length);
        newFileContent[fileContent.length] = "game_type = USE_MAP_SETTINGS";

        return newFileContent;
    }

    private static void writeToBwapiIni() {
        String finalContent = "";

        for (String line : fileContent) {
            finalContent += line + "\n";
        }

        A.saveToFile(bwapiDataPath + "bwapi.ini", finalContent, true);
    }

    // =========================================================

    public static void setBwapiDataPath(String bwapiIniPath) {
        AtlantisIgniter.bwapiDataPath = bwapiIniPath;
    }

    public static void setChaosLauncherPath(String chaosLauncherPath) {
        AtlantisIgniter.chaosLauncherPath = chaosLauncherPath;
    }
}
