package atlantis.combat.advance;

import atlantis.architecture.Manager;
import atlantis.combat.missions.MissionManager;
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
    public Manager handle() {
        if (unit.isMissionAttack()) return null;

        if (unit.squad().cohesionPercent() <= 78) {
            unit.holdPosition("LeaderWait");
            return usedManager(this);
        }

        return null;
    }
}
