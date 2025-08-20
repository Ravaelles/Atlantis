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
            if (unit.isTargetInWeaponRangeAccordingToGame(target)) return false;

            return true;
        }

        return false;
    }
}
