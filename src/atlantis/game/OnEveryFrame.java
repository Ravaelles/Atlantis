package atlantis.game;

import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.debug.painter.APainter;
import atlantis.units.AUnit;
import bwapi.Color;

/**
 * Auxiliary class, helpful when there's need to do something every frame and not spam other classes.
 */
public class OnEveryFrame {
    public static void handle() {
//        paintMissionAttackFocusPoint();
    }

    private static void paintMissionAttackFocusPoint() {
        AFocusPoint focusPoint = Alpha.get().mission().focusPoint();
        AUnit unit = Alpha.get().first();

        if (focusPoint != null) {
            APainter.paintLine(unit, focusPoint, Color.Cyan);
        }

//        AAdvancedPainter.paintSideMessage("Focus: x:" + focusPoint.x() + ", y:" + focusPoint.y(), Color.Yellow);
    }

}
