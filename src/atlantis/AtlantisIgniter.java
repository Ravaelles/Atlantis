package atlantis;

import atlantis.util.AtlantisUtilities;
import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class AtlantisIgniter {
    
    private static boolean shouldUpdateFileContent = false;
    private static String bwapiIniPath = null;
    private static String[] fileContent = null;
    
    // =========================================================
    
    public static void modifyBwapiFileIfNeeded() {
        
        // Only allow bwapi.ini modification if file MODIFY_BWAPI is found
        // in project root or level up
        if (!(new File("MODIFY_BWAPI")).exists() && !(new File("../MODIFY_BWAPI")).exists()) {
            System.out.println("Tournament mode: don't modify bwapi.ini");
            return;
        }
        
        // =========================================================
        
        // Try locating bwap.ini file
        bwapiIniPath = defineBwapiIniPath();
        if (bwapiIniPath == null) {
            System.err.println("Couldn't locate bwapi.ini file, not changing it.");
            return;
        }
        
        // Read every single line
        ArrayList<String> linesList = AtlantisUtilities.readTextFileToList(bwapiIniPath);
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

    private static String defineBwapiIniPath() {
        File file;
        String path;
        
        String[] potentialBwapiPaths = new String[] {
            "C:/StarCraft/bwapi-data/bwapi.ini",
            "D:/GRY/StarCraft/bwapi-data/bwapi.ini",
            "C:/Program files/StarCraft/bwapi-data/bwapi.ini",
            "C:/Program files (x86)/StarCraft/bwapi-data/bwapi.ini"
        };

        for (String potentialBwapiPath : potentialBwapiPaths) {
            file = new File(potentialBwapiPath);
            if (file.exists()) {
                return potentialBwapiPath;
            }
        }
        
        return null;
    }
    
    private static void updateOurRace() {
        for (int i = 0; i < fileContent.length; i++) {
            String line = fileContent[i];
            if (line.startsWith("race = ")) {
                fileContent[i] = "race = " + AtlantisConfig.OUR_RACE;
                
                if (!fileContent[i].equals(line)) {
                    shouldUpdateFileContent = true;
                    System.out.println("Updated our race in bwapi.ini to: " + AtlantisConfig.OUR_RACE);
                }
                return;
            }
        }
    }

    private static void updateEnemyRace() {
        for (int i = 0; i < fileContent.length; i++) {
            String line = fileContent[i];
            if (line.startsWith("enemy_race = ")) {
                fileContent[i] = "enemy_race = " + AtlantisConfig.ENEMY_RACE;
                
                if (!fileContent[i].equals(line)) {
                    shouldUpdateFileContent = true;
                    System.out.println("Updated enemy race in bwapi.ini to: " + AtlantisConfig.ENEMY_RACE);
                }
                return;
            }
        }
    }
    
    private static void updateMapAndGameTypeIfNeeded() {
        for (int i = 0; i < fileContent.length; i++) {
            String line = fileContent[i];
            if (line.startsWith("map = ")) {
                fileContent[i] = "map = " + AtlantisConfig.MAP;
                
                if (!fileContent[i].equals(line)) {
                    shouldUpdateFileContent = true;
                    System.out.println("Updated map in bwapi.ini to: " + AtlantisConfig.MAP);
                }
            }
            
            // game_type = USE_MAP_SETTINGS
            else if (line.startsWith("game_type = ")) {
                String gameType = AtlantisConfig.MAP.contains("umt/") ? "USE_MAP_SETTINGS" : "MELEE";
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
        
        AtlantisUtilities.saveToFile(bwapiIniPath, finalContent, true);
    }
    
}
