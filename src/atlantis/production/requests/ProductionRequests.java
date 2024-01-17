package atlantis.production.requests;

import atlantis.game.race.MyRace;
import atlantis.map.position.APosition;
import atlantis.production.requests.zerg.ZergRequests;


public abstract class ProductionRequests {

    private static ProductionRequests instance = null;

    public static ProductionRequests getInstance() {
        if (instance == null) {
            if (MyRace.isPlayingAsTerran()) {
                instance = new TerranRequests();
            }
            else if (MyRace.isPlayingAsProtoss()) {
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
