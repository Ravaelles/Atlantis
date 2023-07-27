package atlantis.combat.missions.defend;

import atlantis.combat.missions.MissionChanger;
import atlantis.combat.missions.Missions;
import atlantis.util.We;

public abstract class MissionChangerWhenDefend extends MissionChanger {

    public abstract boolean shouldChangeMissionToContain();
    public abstract boolean shouldChangeMissionToAttack();

    protected final void changeMissionIfNeeded() {
        if (shouldChangeMissionToContain()) {
            changeMissionTo(Missions.CONTAIN);
        }
        else if (shouldChangeMissionToAttack()) {
            changeMissionTo(Missions.ATTACK);
        }
    }

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
}
