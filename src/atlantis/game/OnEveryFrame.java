package atlantis.game;

import atlantis.Atlantis;
import atlantis.combat.missions.attack.MissionAttackFocusPoint;
import atlantis.combat.missions.focus.AFocusPoint;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.debug.painter.AAdvancedPainter;
import atlantis.debug.painter.APainter;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import bwapi.Color;

public class OnEveryFrame {

//    private static CappedList<Integer> frames = new CappedList<>(4);

    public static void update() {
//        Selection buildings = EnemyUnits.foggedUnits().buildings();

//        if (buildings.count() > 0) {
//            buildings.print("Enemy fogged buildings");
//        }

//        paintMissionAttackFocusPoint();

//        AUnit firstEnemyBuilding = Select.enemyRealUnitsWithBuildings().buildings().first();
//        AUnit firstEnemyBuilding = buildings.first();
//        if (firstEnemyBuilding != null) {
//            CameraManager.centerCameraOn(firstEnemyBuilding);
//        }

        AAdvancedPainter.paintFoggedUnits();

        paintMissionAttackFocusPoint();
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
