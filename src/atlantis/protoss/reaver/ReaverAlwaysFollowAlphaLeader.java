package atlantis.protoss.reaver;

import atlantis.architecture.Manager;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;

public class ReaverAlwaysFollowAlphaLeader extends Manager {
    public ReaverAlwaysFollowAlphaLeader(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return !unit.isRunning() && !unit.isAttacking();
    }

    @Override
    public Manager handle() {
        AUnit leader = Alpha.get().leader();
        if (leader == null) return null;

        if (unit.distTo(leader) >= 5) {
            unit.move(leader, Actions.MOVE_FORMATION, "ReaverToLeader");
        }

        return usedManager(this);
    }
}
