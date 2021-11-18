package atlantis.combat.missions;

import atlantis.AGame;

public class MissionChangerWhenDefend {

    public static void changeMissionIfNeeded() {
        if (AGame.isPlayingAsTerran()) {
            TerranMissionChangerWhenDefend.changeMissionIfNeeded();
        }
        else if (AGame.isPlayingAsProtoss()) {
            ProtossMissionChangerWhenDefend.changeMissionIfNeeded();
        }
        else {
            ZergMissionChangerWhenDefend.changeMissionIfNeeded();
        }
    }

}
