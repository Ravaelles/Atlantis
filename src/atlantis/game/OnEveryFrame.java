package atlantis.game;

import atlantis.debug.painter.AAdvancedPainter;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.AMap;
import atlantis.map.choke.AChoke;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import bwapi.Color;
import bwem.CPPath;
import bwem.ChokePoint;

import java.util.Iterator;

/**
 * Auxiliary class, helpful when there's need to do something every frame and not spam other classes.
 */
public class OnEveryFrame {
    public static void handle() {
//        paintMissionAttackFocusPoint();

        pathToEnemyBase();
    }

    private static void pathToEnemyBase() {
        AUnit enemy = EnemyUnits.nearestEnemyBuilding();

        if (enemy == null || !enemy.hasPosition()) {
            return;
        }

        CPPath path = AMap.getMap().getPath(Select.main().position().p(), enemy.position().p());

//        if (A.everyNthGameFrame(30 * 5)) {
//            System.err.println("===============================");
//            System.err.println(path.size() + " path size");
//            System.err.println("===============================");
//        }

        paintPathToEnemyBase(path);
    }

    private static void paintPathToEnemyBase(CPPath path) {
        int chokeIndex = 0;
        APosition prevPoint = null;

        for (Iterator<ChokePoint> iterator = path.iterator(); iterator.hasNext(); ) {
            AChoke choke = AChoke.from(iterator.next());
            AAdvancedPainter.paintChoke(choke, Color.Orange, "Milestone= " + chokeIndex);

            if (prevPoint != null) {
                AAdvancedPainter.paintLine(prevPoint, choke.center(), Color.Orange);
            }
            prevPoint = choke.center();

            chokeIndex++;
        }
    }

    private static void paintMissionAttackFocusPoint() {
//        AFocusPoint focusPoint = Alpha.get().mission().focusPoint();
//        AUnit unit = Alpha.get().first();
//
//        if (focusPoint != null) {
//            APainter.paintLine(unit, focusPoint, Color.Cyan);
//        }

//        AAdvancedPainter.paintSideMessage("Focus: x:" + focusPoint.x() + ", y:" + focusPoint.y(), Color.Yellow);
    }

}
