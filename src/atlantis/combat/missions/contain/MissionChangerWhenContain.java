package atlantis.combat.missions.contain;

import atlantis.combat.missions.MissionChanger;
import atlantis.combat.missions.Missions;
import atlantis.util.We;

public abstract class MissionChangerWhenContain extends MissionChanger {
    public abstract boolean shouldChangeMissionToDefend();
    public abstract boolean shouldChangeMissionToAttack();

    public void changeMissionIfNeeded() {
        if (shouldChangeMissionToDefend()) {
            changeMissionTo(MissionChanger.defendOrSpartaMission());
        }
        else if (shouldChangeMissionToAttack()) {
            changeMissionTo(Missions.ATTACK);
        }
    }

    public static MissionChanger get() {
        if (We.terran()) {
            return new TerranMissionChangerWhenContain();
        }
        else if (We.protoss()) {
            return new ProtossMissionChangerWhenContain();
        }
        else {
            return new ZergMissionChangerWhenContain();
        }
    }
}
