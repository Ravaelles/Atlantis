package atlantis.combat.missions.defend;

import atlantis.game.AGame;

public class MissionChangerWhenDefend {

    public  void changeMissionIfNeeded() {
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
