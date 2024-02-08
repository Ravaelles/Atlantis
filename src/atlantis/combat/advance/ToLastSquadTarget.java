package atlantis.combat.advance;

import atlantis.combat.squad.Squad;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class ToLastSquadTarget {
    public static boolean goToSquadTarget(AUnit unit) {
        Squad squad = unit.squad();
        if (squad == null) return false;

        AUnit lastTarget = squad.targeting().lastTargetIfAlive();
        if (lastTarget == null) return false;
        if (!unit.hasWeaponToAttackThisUnit(lastTarget)) return false;

        if (unit.distTo(lastTarget) > 10) {
            unit.move(lastTarget, Actions.MOVE_FORMATION, "LeaderToLastTarget");
            return true;
        }

        return false;
    }
}
