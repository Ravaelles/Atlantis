package atlantis.combat.squad.positioning.protoss.cluster;

import atlantis.architecture.Manager;
import atlantis.combat.squad.positioning.protoss.ProtossTooFarFromSquadCenter;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.util.We;

public class ProtossKeepUnitsClustered extends Manager {
    public ProtossKeepUnitsClustered(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return We.protoss()
            && unit.lastActionMoreThanAgo(10, Actions.ATTACK_UNIT)
            && !unit.isRunning()
            && !unit.underAttackSecondsAgo(2)
            && unit.lastStartedRunningMoreThanAgo(30 * 3)
            && unit.lastStoppedRunningMoreThanAgo(30)
            && (unit.cooldown() == 0 || unit.cooldown() >= 12)
//            && (unit.cooldown() >= 12 || !unit.underAttackSecondsAgo(2))
            && (
            (unit.distToLeader() >= 6 || unit.friendsInRadiusCount(2) <= 1)
        );
//            && (!unit.isMoving() || unit.lastActionMoreThanAgo(10, Actions.MOVE_FORMATION));
//            && (
//            (unit.distToLeader() >= 6 && unit.friendsInRadiusCount(2) <= 2)
//        );
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ProtossForceClusterDragoon.class,
            ProtossForceClusterZealot.class,
            ProtossTooFarFromSquadCenter.class,
        };
    }
}
