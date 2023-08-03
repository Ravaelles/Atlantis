package atlantis.combat.missions.attack;

import atlantis.combat.missions.MissionChanger;
import atlantis.combat.missions.defend.MissionChangerWhenDefend;
import atlantis.util.We;

public abstract class MissionChangerWhenAttack extends MissionChanger {
    public abstract boolean shouldChangeMissionToDefend();

    public abstract boolean shouldChangeMissionToContain();

    @Override
    public void changeMissionIfNeeded() {
        if (shouldChangeMissionToDefend() && !MissionChangerWhenDefend.get().shouldChangeMissionToAttack()) {
            changeMissionTo(MissionChanger.defendOrSpartaMission());
        }
//        else if (shouldChangeMissionToContain()) {
//            changeMissionTo(Missions.CONTAIN);
//        }
    }

    public static MissionChangerWhenAttack get() {
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
