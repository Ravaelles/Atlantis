package atlantis.combat.advance.focus;

import atlantis.map.position.APosition;
import atlantis.units.AUnit;

public class IsOnValidSideOfChoke {
    public static boolean check(AUnit unit, AFocusPoint focus) {
        if (focus == null || !focus.isAroundChoke()) return true;

        APosition choke = focus.choke().center();
        double unitToChoke = unit.distTo(choke);
        double focusToChoke = focus.distTo(choke);

        if (unitToChoke < focusToChoke) return false;

        double unitToFromSide = unit.distTo(focus.fromSide());
        double focusToFromSide = focus.distTo(focus.fromSide());

        return unitToFromSide <= focusToFromSide;
    }
}
