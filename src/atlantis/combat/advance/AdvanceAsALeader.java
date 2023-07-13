package atlantis.combat.advance;

import atlantis.combat.missions.MissionManager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.architecture.Manager;

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
        if (unit.squad().cohesionPercent() <= 75) {
            unit.holdPosition("LeaderWait");
            return usedManager(this);
        }

        return null;
    }
}