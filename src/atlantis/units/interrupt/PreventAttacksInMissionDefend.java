package atlantis.units.interrupt;

import atlantis.combat.missions.Missions;
import atlantis.units.AUnit;

public class PreventAttacksInMissionDefend {
    public static boolean prevent(AUnit unit) {
        if (unit.isAir()) return false;
        if (!unit.isMissionDefendOrSparta()) return false;
//        if (unit.leaderIsAttacking()) return false;
        if (unit.eval() >= 10) return false;

        if (unit.distToFocusPoint() >= 15) return true;
        if (unit.distToLeader() >= 8) return true;

        double distToMain = unit.groundDistToMain();
        if (distToMain >= 60) return true;

        return false;
//        return unit.distToFocusPoint() >= 6 && unit.distToLeader() >= 4;
    }
}
