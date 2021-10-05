package atlantis.production.requests;

import atlantis.AGame;
import atlantis.AtlantisConfig;
import atlantis.combat.missions.MissionDefend;
import atlantis.constructing.AConstructionManager;
import atlantis.map.AMap;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;


public abstract class ARequests {
    
    private static ARequests instance = null;
    
    public static ARequests getInstance() {
        if (instance == null) {
            if (AGame.isPlayingAsTerran()) {
                instance = new TerranRequests();
            }
            else if (AGame.isPlayingAsProtoss()) {
                instance = new ProtossRequests();
            }
            else {
                instance = new ZergRequests();
            }
        }
        return instance;
    }

    // =========================================================

    public void requestDetectorQuick(APosition where) {
        ADetectorRequest.requestDetectorQuick(where);
    }

    public void requestAntiAirQuick(APosition where) {
        AAntiAirRequest.requestAntiAirQuick(where);
    }

    public void requestDefBuildingAntiAir(APosition where) {
        AAntiAirRequest.requestDefBuildingAntiAir(where);
    }

//    public void requestDefensiveBuildingAntiAir(APosition where) {
//        requestDefensiveBuildingAntiLand(AtlantisConfig.DEFENSIVE_BUILDING_ANTI_AIR);
//    }
    
    public void requestDefensiveBuildingAntiLand(APosition where) {
        AAntiLandRequest.requestDefensiveBuildingAntiLand(where);
    }

}
