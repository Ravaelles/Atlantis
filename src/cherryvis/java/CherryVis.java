package cherryvis.java;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// Assuming you have a BWAPI wrapper with these types.
// You might need to adjust the import paths.
import atlantis.game.A;
import bwapi.Color;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitCommandType;
import bwapi.UnitType;
// Placeholder for accessing game state like frame count.
// Replace this with your bot's actual game object access.
import bwapi.BWClient;

/**
 * A Java implementation of CherryVis for logging Starcraft: Broodwar game states.
 * This tool writes data to a file that can be visualized by the CherryVis tool.
 * <p>
 * It is implemented as a singleton, so you should interact with it via `CherryVis.getInstance()`.
 * <p>
 * NOTE: You must replace the placeholder `BWClient.getInstance().getGame()` with the
 * object that provides access to the game state in your specific bot framework.
 */
public class CherryVis {

    // Singleton instance
    private static final CherryVis instance = new CherryVis();

    private PrintWriter fout;
    private int currentFrame = 0;
    private final Map<Integer, Set<Integer>> shapes;
    private final Map<Unit, Integer> lastSeen;

    /**
     * Private constructor for the singleton pattern.
     */
    private CherryVis() {
        this.shapes = new HashMap<>();
        this.lastSeen = new HashMap<>();
    }

    /**
     * Gets the singleton instance of the CherryVis logger.
     *
     * @return The singleton instance.
     */
    public static CherryVis getInstance() {
        return instance;
    }

    // -------------------
    // Game Event Handlers
    // -------------------

    /**
     * Initializes the logger, creating the necessary directory and log file.
     * This should be called once at the start of the game.
     */
    public void initialize() {
        // In Java, File operations are platform-independent.
        // The .mkdirs() method creates the directory and any necessary parent directories.
        // It returns true only if the directory was newly created.
        File dir = new File("bwapi-data/write");
        if (dir.mkdirs()) {
            System.out.println("CherryVis created directory bwapi-data/write");
        }

        try {
            fout = new PrintWriter(new BufferedWriter(new FileWriter("bwapi-data/write/cherryvis.txt")));
            // This logs the "reset" command, which is the first command CherryVis expects.
            log("reset");
        } catch (IOException e) {
            System.err.println("Error opening CherryVis file: " + e.getMessage());
            e.printStackTrace();
            fout = null; // Ensure fout is null if it fails
        }
    }

//    /**
//     * Call this at the beginning of the game.
//     * It opens the log file and writes a reset command.
//     */
//    public void onGameStart() {
//        // Ensure the directory exists before creating the file
//        File dir = new File("bwapi-data/write");
//        if (!dir.exists()) {
//            dir.mkdirs();
//        }
//
//        try {
//            fout = new PrintWriter(new BufferedWriter(new FileWriter("bwapi-data/write/cherryvis.txt")));
//            log("reset");
//        } catch (IOException e) {
//            System.err.println("Error opening CherryVis file: " + e.getMessage());
//            e.printStackTrace();
//            fout = null; // Ensure fout is null if it fails
//        }
//    }

    /**
     * Call this at the end of the game to close the file stream.
     */
    public void onGameEnd() {
        if (fout != null) {
            fout.close();
        }
    }

    /**
     * Call this at the start of each frame.
     * It updates the current frame count and writes the frame marker to the log.
     */
    public void onFrameStart(int framesNow) {
        if (fout == null) return;

        this.currentFrame = framesNow;

        // Write the frame header
        fout.println("f " + this.currentFrame);

        // Clear shapes for the current frame
        this.shapes.remove(this.currentFrame);
    }

    /**
     * Call this at the end of each frame to flush the output buffer.
     */
    public void onFrameEnd() {
        if (fout == null) return;
        fout.flush();
    }

    /**
     * Updates the last seen frame for a unit and logs its state.
     *
     * @param unit The unit that has been seen.
     */
    public void unitSeen(Unit unit) {
        if (fout == null || unit == null) return;
        lastSeen.put(unit, currentFrame);
        log(unit + " seen");
    }


    // ----------------
    // Logging Commands
    // ----------------

    public void log(String key) {
        if (fout == null) return;
        fout.println(key);
    }

    public void log(String key, Unit unit) {
        if (fout == null || unit == null) return;
        fout.println("u " + unitString(unit) + " " + key);
    }

    public void log(String key, Unit unit, Position p) {
        if (fout == null || unit == null) return;
        fout.println("u " + unitString(unit) + " " + key + " " + bwapiPosition(p));
    }

    public void log(String key, Unit unit, String text) {
        if (fout == null || unit == null) return;
        fout.println("u " + unitString(unit) + " " + key + " " + text);
    }
    
    public void log(String key, int shapeId, Position p, String text) {
        if (fout == null) return;
        if (shapes.getOrDefault(currentFrame, Collections.emptySet()).contains(shapeId)) return;
        shapes.computeIfAbsent(currentFrame, k -> new HashSet<>()).add(shapeId);
        fout.println("s text " + bwapiPosition(p) + " " + shapeId + " " + text);
    }

    public void log(String key, Position p1, Position p2, Color color, int shapeId) {
        if (fout == null) return;
        if (shapes.getOrDefault(currentFrame, Collections.emptySet()).contains(shapeId)) return;
        shapes.computeIfAbsent(currentFrame, k -> new HashSet<>()).add(shapeId);
        fout.println("s " + key + " " + bwapiPosition(p1) + " " + bwapiPosition(p2) + " " + bwapiColor(color) + " " + shapeId);
    }
    
    public void log(String key, Position p1, int radius, Color color, int shapeId) {
        if (fout == null) return;
        if (shapes.getOrDefault(currentFrame, Collections.emptySet()).contains(shapeId)) return;
        shapes.computeIfAbsent(currentFrame, k -> new HashSet<>()).add(shapeId);
        fout.println("s " + key + " " + bwapiPosition(p1) + " " + radius + " " + bwapiColor(color) + " " + shapeId);
    }


    // ----------------
    // Helper Methods
    // ----------------

    /**
     * Formats a unit's state into a string for logging.
     *
     * @param unit The unit to format.
     * @return A space-delimited string of unit properties.
     */
    private String unitString(Unit unit) {
        if (unit == null) {
            return "";
        }

        // Calculate health percentage
        double hpRatio = 0.0;
        int maxHp = unit.getType().maxHitPoints() + unit.getType().maxShields();
        if (maxHp > 0) {
            hpRatio = (double) (unit.getHitPoints() + unit.getShields()) / maxHp;
        }

        // Get last command ID, defaults to None's ID if no command
        int lastCommandId = (unit.getLastCommand() != null && unit.getLastCommand().getType() != null)
                ? unit.getLastCommand().getType().id
                : UnitCommandType.None.id;

        return String.format("%d %d %d %s %.2f %d %d %d",
                unit.getID(),
                unit.getPlayer().getID(),
                unit.getType().id,
                bwapiPosition(unit.getPosition()),
                hpRatio,
                unit.isCompleted() ? 1 : 0,
                lastCommandId,
                lastSeen.getOrDefault(unit, 0)
        ).replace(',', '.'); // Use dots for decimals
    }

    /**
     * Converts a BWAPI Position to a "X Y" string.
     */
    private String bwapiPosition(Position p) {
        return p.getX() + " " + p.getY();
    }

    /**
     * Converts a BWAPI Color to an "R G B" string.
     */
    private String bwapiColor(Color c) {
        return c.red() + " " + c.green() + " " + c.blue();
    }
}