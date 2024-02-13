package atlantis.combat.advance;

import atlantis.combat.squad.Squad;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class ToLastSquadTarget {
    public static boolean goTo(AUnit unit) {
        Squad squad = unit.squad();
        if (squad == null) return false;

        AUnit lastTarget = squad.targeting().lastTargetIfAlive();
        if (!isValidTargetForThisUnit(unit, lastTarget, squad)) return false;

        if (unit.distTo(lastTarget) > 10) {
            unit.move(lastTarget, Actions.MOVE_FORMATION, "LeaderToLastTarget");
            return true;
        }

        return false;
    }

    private static boolean isValidTargetForThisUnit(AUnit unit, AUnit lastTarget, Squad squad) {
        if (lastTarget == null) return false;

        if (!lastTarget.isVisibleUnitOnMap() && lastTarget.position().isPositionVisible()) {
            A.errPrintln("Looks like an outdated dead target");
            squad.targeting().forceTarget(null);
            return false;
        }

        if (!unit.hasWeaponToAttackThisUnit(lastTarget)) return false;

        return true;
    }
}