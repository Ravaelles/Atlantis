package atlantis.combat.missions;

import atlantis.AGame;

public abstract class MissionChangerWhenContain extends MissionChanger {

    public static void changeMissionIfNeeded() {
        if (AGame.isPlayingAsTerran()) {
            TerranMissionChangerWhenContain.changeMissionIfNeeded();
            return;
        }
        else if (AGame.isPlayingAsProtoss()) {
            ProtossMissionChangerWhenContain.changeMissionIfNeeded();
            return;
        }
        else {
            ZergMissionChangerWhenContain.changeMissionIfNeeded();
            return;
        }
    }

}
