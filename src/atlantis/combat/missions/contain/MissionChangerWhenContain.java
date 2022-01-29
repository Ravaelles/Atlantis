package atlantis.combat.missions.contain;

import atlantis.combat.missions.MissionChanger;
import atlantis.combat.missions.Missions;
import atlantis.game.AGame;

public abstract class MissionChangerWhenContain extends MissionChanger {

    public static void changeMissionIfNeeded() {
        if (Missions.recentlyChangedMission()) {
            return;
        }

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
