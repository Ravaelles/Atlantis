package atlantis.production.requests;

import atlantis.AGame;
import atlantis.AtlantisConfig;
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
public abstract class ARequests {
    
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
    
//    public void requestDefensiveBuildingAntiAir(APosition where) {
//        requestDefensiveBuildingAntiLand(AtlantisConfig.DEFENSIVE_BUILDING_ANTI_AIR);
//    }
    
    public void requestDefensiveBuildingAntiLand(APosition where) {
        AUnitType building = AtlantisConfig.DEFENSIVE_BUILDING_ANTI_LAND;
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
    
    /**
     * Quick air units are: Mutalisk, Wraith, Protoss Scout.
     */
//    public abstract void requestAntiAirQuick(APosition where);
    public void requestAntiAirQuick(APosition where) {
        AUnitType building = AtlantisConfig.DEFENSIVE_BUILDING_ANTI_AIR;
        int antiAirBuildings = AConstructionManager.countExistingAndPlannedConstructions(building);

        // === Ensure parent exists ========================================
        
        int requiredParents = AConstructionManager.countExistingAndPlannedConstructions(building.getWhatIsRequired());
        if (requiredParents == 0) {
            AConstructionManager.requestConstructionOf(AUnitType.Buildings);
            return;
        }

        // === Protect every base ==========================================
        
        for (AUnit base : Select.ourBases().listUnits()) {
            int numberOfAntiAirBuildingsNearBase = AConstructionManager.countExistingAndPlannedConstructionsInRadius(
                    building, 8, base.getPosition()
            );
            
            for (int i = 0; i < 2 - numberOfAntiAirBuildingsNearBase; i++) {
                
            }
        }
    }
    
    // === To be overriden =====================================
    
    /**
     * Request quickest possible detector to be built (e.g. Comsat Station for Terran, not Science Vessel).
     */
    public abstract void requestDetectorQuick(APosition where);
    
}
