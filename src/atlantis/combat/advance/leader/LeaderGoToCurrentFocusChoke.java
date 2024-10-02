package atlantis.combat.advance.leader;

import atlantis.architecture.Manager;
import atlantis.combat.missions.MissionManager;
import atlantis.map.choke.AChoke;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class LeaderGoToCurrentFocusChoke extends MissionManager {
    private AChoke focusChoke;

    public LeaderGoToCurrentFocusChoke(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        focusChoke = CurrentFocusChoke.get();

        return focusChoke != null;
    }

    @Override
    protected Manager handle() {
        if (goTowards()) return usedManager(this);
        else goAway();

        return null;
    }

    private void goAway() {
        unit.moveAwayFrom(focusChoke, 0.15, Actions.MOVE_FORMATION, "LeaderAway");
    }

    private boolean goTowards() {
        if (focusChoke.distTo(unit) > preferredDistToChoke()) {
            return unit.move(focusChoke, Actions.MOVE_FORMATION, "LeaderToCFC");
        }

        return false;
    }

    private static int preferredDistToChoke() {
        return 8;
    }
}
