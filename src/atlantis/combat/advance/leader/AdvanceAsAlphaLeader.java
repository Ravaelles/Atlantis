package atlantis.combat.advance.leader;

import atlantis.architecture.Manager;
import atlantis.combat.missions.MissionManager;
import atlantis.map.choke.AChoke;
import atlantis.units.AUnit;

public class AdvanceAsAlphaLeader extends MissionManager {
    private static double distTargetChoke = -1;

    public AdvanceAsAlphaLeader(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isLeader()) return false;
        if (!squad.isAlpha()) return false;
        if (!squad.isMissionAttack()) return false;

        if (unit.isAttacking()) return false;
        if (unit.enemiesNear().inRadius(8, unit).notEmpty()) return false;

        AChoke focusChoke = CurrentFocusChoke.get();
//        System.err.println("B focusChoke = " + focusChoke);
        if (focusChoke == null) return false;

//        System.err.println("distTargetChoke = " + distTargetChoke);
        distTargetChoke = unit.distTo(focusChoke);
        return true;
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            LeaderWait.class,
            LeaderProgressFlagToNextFocusChoke.class,
            LeaderGoToCurrentFocusChoke.class,
        };
    }

    public static double lastLeaderDistToTargetChoke() {
        return distTargetChoke;
    }
}
