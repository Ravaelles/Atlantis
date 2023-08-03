package atlantis.combat.missions.defend;

import atlantis.combat.missions.MissionChanger;
import atlantis.combat.missions.Missions;
import atlantis.combat.missions.attack.MissionChangerWhenAttack;
import atlantis.util.We;

public abstract class MissionChangerWhenDefend extends MissionChanger {

    public abstract boolean shouldChangeMissionToContain();

    public abstract boolean shouldChangeMissionToAttack();

    public final void changeMissionIfNeeded() {
//        if (shouldChangeMissionToContain()) {
//            changeMissionTo(Missions.CONTAIN);
//        }
        if (shouldChangeMissionToAttack() && !MissionChangerWhenAttack.get().shouldChangeMissionToDefend()) {
            changeMissionTo(Missions.ATTACK);
        }
    }

    public static MissionChangerWhenDefend get() {
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
