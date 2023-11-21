package atlantis.combat.advance;

import atlantis.architecture.Manager;
import atlantis.combat.missions.MissionManager;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;

public class AdvanceAsTank extends MissionManager {
    public AdvanceAsTank(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isTank() && unit.squadSize() >= 6 && unit.friendsNear().inRadius(5, unit).atMost(4);
    }

    @Override
    protected Manager handle() {
        HasPosition squadCenter = unit.squadCenter();

        if (squadCenter != null && unit.distTo(squadCenter) >= 5) {
            if (moveForBetterCohesion(squadCenter)) return usedManager(this);
        }

        return null;
    }

    private boolean moveForBetterCohesion(HasPosition moveTo) {
        if (moveTo != null) {
            unit.move(moveTo, Actions.MOVE_FORMATION, "AdvanceAsTank");
            return true;
        }

        return false;
    }
}
