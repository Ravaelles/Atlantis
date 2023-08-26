package atlantis.combat.advance;

import atlantis.architecture.Manager;
import atlantis.combat.missions.MissionManager;
import atlantis.combat.missions.Missions;
import atlantis.units.AUnit;

public class AdvanceAsALeader extends MissionManager {
    public AdvanceAsALeader(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return squad.isLeader(unit);
    }

    @Override
    protected Manager handle() {
        if (unit.isMissionAttackOrGlobalAttack()) return null;

        int cohesionPercent = unit.squad().cohesionPercent();
        int friendsNear = unit.friendsInRadius(7).count();

        if (cohesionPercent <= 84 && friendsNear <= squad.size() * 0.7) {
            unit.holdPosition("LeaderWaitA");
            return usedManager(this);
        }

        if (cohesionPercent <= 74 && friendsNear <= squad.size() * 0.8) {
            unit.holdPosition("LeaderWaitB");
            return usedManager(this);
        }

        if (cohesionPercent <= 69) {
            unit.holdPosition("LeaderWaitC");
            return usedManager(this);
        }

        return null;
    }
}
