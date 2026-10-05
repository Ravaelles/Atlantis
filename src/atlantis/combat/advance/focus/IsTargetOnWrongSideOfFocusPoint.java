package atlantis.combat.advance.focus;

import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;

public class IsTargetOnWrongSideOfFocusPoint {
    public static boolean isTargetOnWrongSideOfFocusPoint(AUnit unit, AUnit target) {
        AFocusPoint focus = unit.focusPoint();
        if (focus == null || !focus.isAroundChoke()) return false;

        if (target.isAir()) return true;

        HasPosition fromSide = focus.fromSide();

        if (target.groundDist(fromSide) > focus.groundDist(fromSide)) {
            // Already in range means the fight is here, not across the focus - and
            // so does an enemy 3 tiles out: walking one step to a fight that found
            // you is not crossing anything (measured: a zealot 2 tiles from a
            // marine, 21 past its focus, eval 7.7, held DoNothing for 30 frames
            // while every other gate already allowed the fight). The comparison
            // above still vetoes everything that would require actual travel.
            if (unit.isTargetInWeaponRangeAccordingToGame(target)) return false;
            if (unit.distTo(target) <= 3) return false;

            return true;
        }

        return false;
    }
}
