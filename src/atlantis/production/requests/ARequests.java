package atlantis.production.requests;

import atlantis.AGame;
import atlantis.AtlantisConfig;
import atlantis.combat.squad.missions.MissionDefend;
import atlantis.constructing.AConstructionManager;
import atlantis.information.AMap;
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
    
    public void requestDefBuildingAntiLand(APosition where) {
        AUnitType building = AtlantisConfig.DEF_BUILDING_ANTI_LAND;
        APosition nearTo = where;
        
        AUnit previousBuilding = Select.ourBuildingsIncludingUnfinished().ofType(building).first();
        if (where == null) {
            if (previousBuilding != null) {
//            AGame.sendMessage("New bunker near " + previousBuilding);
//            System.out.println("New bunker near " + previousBuilding);
            nearTo = previousBuilding.getPosition();
            }
            else {
    //            System.out.println("New bunker at default");
                nearTo = null;
            }
        }
        
        AConstructionManager.requestConstructionOf(building, nearTo);
    }
    
    public void requestDefBuildingAntiAir(APosition where) {
        AUnitType building = AtlantisConfig.DEFENSIVE_BUILDING_ANTI_AIR;
        APosition nearTo = where;
        
//        if (where == null) {
//            
//        }
        
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
            AConstructionManager.requestConstructionOf(building.getWhatIsRequired());
            return;
        }

        // === Protect every base ==========================================
        
        for (AUnit base : Select.ourBases().listUnits()) {
            int numberOfAntiAirBuildingsNearBase = AConstructionManager.countExistingAndPlannedConstructionsInRadius(
                    building, 8, base.getPosition()
            );
            
            for (int i = 0; i < 2 - numberOfAntiAirBuildingsNearBase; i++) {
                AConstructionManager.requestConstructionOf(building, base.getPosition());
            }
        }
    }
    
    /**
     * Request quickest possible detector to be built (e.g. Comsat Station for Terran, not Science Vessel).
     */
    public void requestDetectorQuick(APosition where) {
        AUnitType building = null;
        if (AGame.playsAsTerran()) {
            building = AtlantisConfig.DEFENSIVE_BUILDING_ANTI_AIR;
        }
        else if (AGame.playsAsProtoss()) {
            building = AtlantisConfig.DEF_BUILDING_ANTI_LAND;
        }
        else {
            return;
        }
        
        // =========================================================
        
        int antiAirBuildings = AConstructionManager.countExistingAndPlannedConstructions(building);

        // === Ensure parent exists ========================================
        
        int requiredParents = AConstructionManager.countExistingAndPlannedConstructions(building.getWhatIsRequired());
        if (requiredParents == 0) {
            AConstructionManager.requestConstructionOf(building.getWhatIsRequired());
            return;
        }

        // === Protect every base ==========================================
//        
//        for (AUnit base : Select.ourBases().listUnits()) {
//            int numberOfAntiAirBuildingsNearBase = AConstructionManager.countExistingAndPlannedConstructionsInRadius(
//                    building, 8, base.getPosition()
//            );
//            
//            for (int i = 0; i < 2 - numberOfAntiAirBuildingsNearBase; i++) {
//                AConstructionManager.requestConstructionOf(building, base.getPosition());
//            }
//        }
        
        // === Protect choke point =========================================

        if (where == null) {
            AUnit nearestBunker = Select.ourOfType(AUnitType.Terran_Bunker).nearestTo(MissionDefend.getInstance().getFocusPoint());
            if (nearestBunker != null) {
                where = nearestBunker.getPosition();
            }
        }
        
        if (where == null) {
            where = MissionDefend.getInstance().getFocusPoint().translateTowards(AMap.getNaturalBaseLocation(), 32);
        }
        
        int numberOfDetectors = AConstructionManager.countExistingAndPlannedConstructionsInRadius(
                building, 8, where
        );

        for (int i = 0; i < 2 - numberOfDetectors; i++) {
            AConstructionManager.requestConstructionOf(building, where);
        }
    }
    
    // === To be overriden =====================================
    
}
