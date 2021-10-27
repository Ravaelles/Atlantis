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
        AAntiAirBuildingRequests.requestAntiAirQuick(where);
    }

    public void requestDefBuildingAntiAir(APosition where) {
        AAntiAirBuildingRequests.requestDefensiveBuildingAntiAir(where);
    }

    public void requestDefensiveBuildingAntiAir(APosition where) {
        requestDefensiveBuildingAntiAir(where);
    }
    
    public void requestDefensiveBuildingAntiLand(APosition where) {
        AAntiLandBuildingRequests.requestDefensiveBuildingAntiLand(where);
    }

}
