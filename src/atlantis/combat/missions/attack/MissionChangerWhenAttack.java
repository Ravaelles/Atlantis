package atlantis.combat.missions.attack;

import atlantis.combat.missions.MissionChanger;
import atlantis.util.We;

public abstract class MissionChangerWhenAttack extends MissionChanger {
    public abstract boolean shouldChangeMissionToDefend();
    public abstract boolean shouldChangeMissionToContain();

    public void changeMissionIfNeeded() {
        if (shouldChangeMissionToDefend()) {
            changeMissionTo(MissionChanger.defendOrSpartaMission());
        }
//        else if (shouldChangeMissionToContain()) {
//            changeMissionTo(Missions.CONTAIN);
//        }
    }

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
}
