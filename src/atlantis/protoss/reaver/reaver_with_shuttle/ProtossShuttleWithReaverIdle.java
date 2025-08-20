package atlantis.protoss.reaver.reaver_with_shuttle;

import atlantis.architecture.Manager;
import atlantis.combat.squad.squads.alpha.Alpha;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class ProtossShuttleWithReaverIdle extends Manager {
    private AUnit alphaLeader;

    public ProtossShuttleWithReaverIdle(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.lastPositionChangedAgo() >= 30 * 4
            && unit.distTo(alphaLeader = Alpha.alphaLeader()) >= 10;
    }

    @Override
    public Manager handle() {
        if (alphaLeader != null && unit.move(alphaLeader, Actions.MOVE_FOLLOW)) {
            return usedManager(this);
        }

        return null;
    }
}
