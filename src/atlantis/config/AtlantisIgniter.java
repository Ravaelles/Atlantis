package atlantis.config;

import atlantis.game.A;

import java.io.File;
import java.util.ArrayList;

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

    private static String defineBwapiIniPath() {
        File file;
        String path;

        // Only one will be used, but we can have many
        String[] potentialBwapiPaths = new String[] {
            "D:/JAVA/StarCraft/bwapi-data/bwapi.ini",
            "C:/StarCraft/bwapi-data/bwapi.ini",
            "D:/GAMES/StarCraft/bwapi-data/bwapi.ini",
            "C:/Program files/StarCraft/bwapi-data/bwapi.ini",
            "C:/Program files (x86)/StarCraft/bwapi-data/bwapi.ini"
        };

        for (String potentialBwapiPath : potentialBwapiPaths) {
            file = new File(potentialBwapiPath);

            // Return the first found from the list above, order matters.
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
    
}
