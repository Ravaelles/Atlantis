package atlantis.combat.missions;

import atlantis.AGame;

public class MissionChangerWhenDefend {

    public static void changeMissionIfNeeded() {
        if (AGame.isPlayingAsTerran()) {
//            TerranMissionChangerWhenDefend.changeMissionIfNeeded();
            return;
        }
        else if (AGame.isPlayingAsProtoss()) {
            ProtossMissionChangerWhenDefend.changeMissionIfNeeded();
            return;
        }
        else {
//            ZergMissionChangerWhenDefend.changeMissionIfNeeded();
            return;
        }
    }

}
