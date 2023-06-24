package atlantis.game;

import atlantis.Atlantis;
import atlantis.combat.missions.attack.MissionAttackFocusPoint;
import atlantis.combat.missions.focus.AFocusPoint;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.debug.painter.APainter;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import bwapi.Color;

public class OnEveryFrame {

//    private static CappedList<Integer> frames = new CappedList<>(4);

    public static void update() {
//        if (EnemyUnits.foggedUnits().count() > 0) {
//            EnemyUnits.foggedUnits().print("Enemy fogged");
//        }

//        paintMissionAttackFocusPoint();
    }

    private static void paintMissionAttackFocusPoint() {
        AFocusPoint focusPoint = Alpha.get().mission().focusPoint();
        AUnit unit = Alpha.get().first();

        if (focusPoint != null) {
            APainter.paintLine(unit, focusPoint, Color.Cyan);
        }
    }

}
