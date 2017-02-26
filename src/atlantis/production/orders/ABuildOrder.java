package atlantis.production.orders;

import atlantis.units.AUnitType;
import java.util.ArrayList;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public abstract class ABuildOrder {

    /**
     * Relative path to build order file as seen from project root.
     */
    private String buildOrderRelativePath;
    
    // === Constructor =========================================

    public ABuildOrder(String filename) {
        this.buildOrderRelativePath = filename + ".txt";
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

    // === Getters =============================================

    /**
     * Returns relative file path as seen from project root.
     */
    public String getBuildOrderRelativePath() {
        return buildOrderRelativePath;
    }

    /**
     * Returns human readable name of the file.
     */
    public String getName() {
        String name = buildOrderRelativePath;
        
        name = name.substring(name.lastIndexOf("/") + 1);
        name = name.substring(0, name.lastIndexOf(".txt"));
        
        return name;
    }
    
}
