package atlantis.production.requests;

import atlantis.AGame;
import atlantis.position.APosition;


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
        ADetectorRequest.requestDetectorImmediately(where);
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
