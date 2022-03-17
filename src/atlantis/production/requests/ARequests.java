package atlantis.production.requests;

import atlantis.game.AGame;
import atlantis.map.position.APosition;
import atlantis.production.requests.zerg.ZergRequests;


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

//    public void requestAntiAirQuick(APosition where) {
//        AAntiAirBuildingRequests.requestAntiAirQuick(where);
//    }
//
//    public void requestDefBuildingAntiAir(APosition where) {
//        AAntiAirBuildingRequests.requestCombatBuildingAntiAir(where);
//    }
//
//    public void requestCombatBuildingAntiAir(APosition where) {
//        requestCombatBuildingAntiAir(where);
//    }
//
//    public void requestCombatBuildingAntiLand(APosition where) {
//        AAntiLandBuildingRequests.requestCombatBuildingAntiLand(where);
//    }

}
