package atlantis.combat.missions.defend;

import atlantis.combat.missions.MissionChanger;
import atlantis.combat.missions.contain.ProtossMissionChangerWhenContain;
import atlantis.combat.missions.contain.TerranMissionChangerWhenContain;
import atlantis.combat.missions.contain.ZergMissionChangerWhenContain;
import atlantis.util.We;

public class MissionChangerWhenDefend {

    public static MissionChanger get() {
        if (We.terran()) {
            return new TerranMissionChangerWhenDefend();
        }
        else if (We.protoss()) {
            return new ProtossMissionChangerWhenDefend();
        }
        else {
            return new ZergMissionChangerWhenDefend();
        }
    }

//    public  void changeMissionIfNeeded() {
//        if (We.terran()) {
//            TerranMissionChangerWhenDefend.changeMissionIfNeeded();
//        }
//        else if (We.protoss()) {
//            ProtossMissionChangerWhenDefend.changeMissionIfNeeded();
//        }
//        else if (We.zerg()) {
//            ZergMissionChangerWhenDefend.changeMissionIfNeeded();
//        }
//    }

}
