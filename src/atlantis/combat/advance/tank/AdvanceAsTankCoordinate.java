package atlantis.combat.advance.tank;

import atlantis.architecture.Manager;
import atlantis.combat.missions.MissionManager;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class AdvanceAsTankCoordinate extends MissionManager {
    private HasPosition goTo;

    public AdvanceAsTankCoordinate(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.squadSize() >= 6 && unit.friendsNear().inRadius(5, unit).atMost(4)) {
            goTo = unit.squadCenter();
            if (goTo != null && unit.distTo(goTo) >= 5) return true;
        }
        
        return false;
    }

    @Override
    protected Manager handle() {
        if (goTo != null) {
            unit.move(goTo, Actions.MOVE_FORMATION, "AdvanceAsTank");
            return usedManager(this);
        }

        return null;
    }
}
