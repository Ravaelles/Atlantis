package atlantis.production.requests;

import atlantis.AGame;
import atlantis.constructing.AConstructionManager;
import atlantis.position.APosition;
import atlantis.production.ADynamicConstructionManager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class ARequests {
    
    private static ARequests instance = null;
    
    public static ARequests getInstance() {
        if (instance == null) {
            if (AGame.playsAsTerran()) {
                instance = new TerranRequests();
            }
            else if (AGame.playsAsProtoss()) {
                instance = new ProtossRequests();
            }
            else {
                instance = new ZergRequests();
            }
        }
        return instance;
    }

    // =========================================================
    
    protected static void requestDefensiveBuilding(AUnitType building) {
        APosition nearTo = null;
        
        AUnit previousBuilding = Select.ourBuildingsIncludingUnfinished().ofType(building).first();
        if (previousBuilding != null) {
//            AGame.sendMessage("New bunker near " + previousBuilding);
//            System.out.println("New bunker near " + previousBuilding);
            nearTo = previousBuilding.getPosition();
        }
        else {
//            System.out.println("New bunker at default");
            nearTo = null;
        }
        
        AConstructionManager.requestConstructionOf(building, nearTo);
    }
    
    // === To be overriden =====================================
    // These are not abstract not to make working with one race only easier.
    
    /**
     * Request quickest possible detector to be built (e.g. Comsat Station for Terran, not Science Vessel).
     */
    public void requestDetectorQuick() {
        // To be overriden in race-specific class
    }

    public void requestAntiAirQuick() {
        // To be overriden in race-specific class
    }
    
}
