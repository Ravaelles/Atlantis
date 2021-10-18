package atlantis.combat.missions;

import atlantis.AGame;

public class MissionChangerWhenAttack {

    public static void changeMissionIfNeeded() {
        if (AGame.isPlayingAsTerran()) {
            TerranMissionChangerWhenAttack.changeMissionIfNeeded();
        }
        else if (AGame.isPlayingAsProtoss()) {
            ProtossMissionChangerWhenAttack.changeMissionIfNeeded();
        }
        else {
            ZergMissionChangerWhenAttack.changeMissionIfNeeded();
        }
    }

}
