package atlantis.combat.advance.focus;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.util.AConsole;

public class IsOnValidSideOfChoke {
    public static boolean check(AUnit unit, AFocusPoint focus) {
        if (focus == null || !focus.isAroundChoke()) return true;

        APosition choke = focus.choke().center();
        double unitToChoke = unit.distTo(choke);
        double focusToChoke = focus.distTo(choke);

        if (unitToChoke < focusToChoke) return false;

        double unitToFromSide = unit.distTo(focus.fromSide());
        double focusToFromSide = focus.distTo(focus.fromSide());

//        AConsole.errPrintln("@ " + A.now() + " - " + unit.typeWithUnitId() + " / " + unitToFromSide + " / " + focusToFromSide);

        return unitToFromSide <= focusToFromSide;
    }
}
