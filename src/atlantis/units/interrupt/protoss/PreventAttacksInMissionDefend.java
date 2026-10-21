package atlantis.units.interrupt.protoss;

import atlantis.units.AUnit;

public class PreventAttacksInMissionDefend {
    public static boolean prevent(AUnit unit) {
        if (unit.isAir()) return false;
        if (!unit.isMissionDefendOrSparta()) return false;
//        if (unit.leaderIsAttacking()) return false;
        if (unit.eval() >= 10) return false;

        // Self-defense is not chasing. This rule exists to keep defend-mission units
        // from running across the map after a target - not to make them stand while an
        // enemy kills them where they stand. Measured (ProbeDoNothingTest): a zealot 2
        // tiles from a marine, 21 from its focus and 18 from its leader, eval 7.7, held
        // DoNothing for 30 frames - while its own mission authorizes the fight
        // (eval >= 1.3). Shots incoming, or an enemy inside 3 tiles, means the fight is
        // here whether the unit wants it or not; the chase-prevention below still
        // applies to everything farther away.
        if (unit.lastUnderAttackLessThanAgo(45)) return false;
        if (unit.enemiesNear().nearestToDistLess(unit, 3)) return false;

        if (unit.distToFocusPoint() >= 15) return true;
        if (unit.distToLeader() >= 8) return true;

        double distToMain = unit.groundDistToMain();
        if (distToMain >= 60) return true;

        return false;
//        return unit.distToFocusPoint() >= 6 && unit.distToLeader() >= 4;
    }
}
