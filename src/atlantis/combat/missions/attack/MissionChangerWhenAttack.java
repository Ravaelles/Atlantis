package atlantis.combat.missions.attack;

import atlantis.combat.missions.MissionChanger;
import atlantis.util.We;

public abstract class MissionChangerWhenAttack {

    public static MissionChanger get() {
        if (We.terran()) {
            return new TerranMissionChangerWhenAttack();
        }
        else if (We.protoss()) {
            return new ProtossMissionChangerWhenAttack();
        }
        else {
            return new ZergMissionChangerWhenAttack();
        }
    }

    public abstract void changeMissionIfNeeded();

//    public void changeMissionIfNeeded() {
//        if (AGame.isPlayingAsTerran()) {
//            TerranMissionChangerWhenAttack.changeMissionIfNeeded();
//        }
//        else if (AGame.isPlayingAsProtoss()) {
//            ProtossMissionChangerWhenAttack.changeMissionIfNeeded();
//        }
//        else {
//            ZergMissionChangerWhenAttack.changeMissionIfNeeded();
//        }
//    }

}
