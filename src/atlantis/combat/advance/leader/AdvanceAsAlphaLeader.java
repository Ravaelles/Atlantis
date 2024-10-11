package atlantis.combat.advance.leader;

import atlantis.architecture.Manager;
import atlantis.combat.advance.focus.HandleFocusPointPositioning;
import atlantis.combat.missions.MissionManager;
import atlantis.units.AUnit;

public class AdvanceAsAlphaLeader extends MissionManager {
    private static double leaderDistToTarget = -1;

    public AdvanceAsAlphaLeader(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isLeader()) return false;
        if (!squad.isAlpha()) return false;
        if (!squad.isMissionAttack()) return false;

        if (unit.isAttackingRecently()) return false;
        if (unit.enemiesNear().inRadius(8, unit).notEmpty()) return false;

//        AChoke focusChoke = CurrentFocusChoke.get();
//        System.err.println("B focusChoke = " + focusChoke);
//        if (focusChoke == null) return false;

        if (focusPoint == null) return false;

//        System.err.println("distTargetChoke = " + distTargetChoke);
        leaderDistToTarget = unit.distTo(focusPoint);
//        leaderDistToTarget = unit.distTo(focusChoke);
        return true;
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            LeaderToOther.class,
            HandleFocusPointPositioning.class,
            LeaderTooLowCohesion.class,
//            LeaderWait.class,

//            LeaderProgressFlagToNextFocusChoke.class,
//            LeaderGoToCurrentFocusChoke.class,

        };
    }

    public static double lastLeaderDistToTargetChoke() {
        return leaderDistToTarget;
    }
}
