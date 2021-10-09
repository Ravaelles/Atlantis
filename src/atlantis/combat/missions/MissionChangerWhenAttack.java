package atlantis.combat.missions;

import atlantis.AGame;

public class MissionChangerWhenAttack {

    public static void changeMissionIfNeeded() {
        if (AGame.isPlayingAsTerran()) {
//            TerranMissionChangerWhenAttack.changeMissionIfNeeded();
            return;
        }
        else if (AGame.isPlayingAsProtoss()) {
            ProtossMissionChangerWhenAttack.changeMissionIfNeeded();
            return;
        }
        else {
//            ZergMissionChangerWhenAttack.changeMissionIfNeeded();
            return;
        }
    }

}
