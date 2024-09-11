package atlantis.combat.advance;

import atlantis.combat.squad.Squad;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class ToLastSquadTarget {
    public static boolean goTo(AUnit unit) {
        Squad squad = unit.squad();
        if (squad == null) return false;

        if (unit.lastActionLessThanAgo(2)) return false;

        AUnit lastTarget = squad.targeting().lastTargetIfAlive();
        if (!isValidTargetForThisUnit(unit, lastTarget, squad)) return false;
        if (lastTarget.effUndetected()) return false;

        if (unit.distTo(lastTarget) > 10) {
            unit.move(lastTarget, Actions.MOVE_FORMATION, "GoToLastTarget");
            return true;
        }

        return false;
    }

    private static boolean isValidTargetForThisUnit(AUnit unit, AUnit lastTarget, Squad squad) {
        if (lastTarget == null) return false;

        if (!lastTarget.isVisibleUnitOnMap() && lastTarget.position().isPositionVisible()) {
            A.errPrintln("Looks like an outdated dead target");
            squad.targeting().setLastTarget(null);
            return false;
        }

        if (!unit.hasWeaponToAttackThisUnit(lastTarget)) return false;

        return true;
    }
}
