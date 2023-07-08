package atlantis.combat.missions.defend;

import atlantis.game.AGame;
import atlantis.util.We;

public class MissionChangerWhenDefend {

    public  void changeMissionIfNeeded() {
        if (We.terran()) {
            TerranMissionChangerWhenDefend.changeMissionIfNeeded();
        }
        else if (We.protoss()) {
            ProtossMissionChangerWhenDefend.changeMissionIfNeeded();
        }
        else if (We.zerg()) {
            ZergMissionChangerWhenDefend.changeMissionIfNeeded();
        }
    }

}
