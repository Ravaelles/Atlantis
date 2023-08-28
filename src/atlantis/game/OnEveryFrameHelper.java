package atlantis.game;

import atlantis.debug.painter.AAdvancedPainter;
import atlantis.map.choke.AChoke;
import atlantis.map.path.PathToEnemyBase;
import atlantis.map.position.APosition;
import bwapi.Color;

import java.util.ArrayList;

/**
 * Auxiliary class, helpful when there's need to do something every frame and not spam other classes.
 */
public class OnEveryFrameHelper {
    public static void handle() {
//        paintMissionAttackFocusPoint();

//        pathToEnemyBase();
    }

    private static void pathToEnemyBase() {
        paintPathToEnemyBase(PathToEnemyBase.chokesLeadingToEnemyBase());
    }

    private static void paintPathToEnemyBase(ArrayList<AChoke> chokes) {
        int chokeIndex = 0;
        APosition prevPoint = null;

        for (AChoke choke : chokes) {
            AAdvancedPainter.paintChoke(choke, Color.Orange, "Milestone=" + chokeIndex);

            if (prevPoint != null) {
                AAdvancedPainter.paintLine(prevPoint, choke.center(), Color.Orange);
            }
            prevPoint = choke.center();

            chokeIndex++;
        }
    }

//    private static void paintMissionAttackFocusPoint() {
//        AFocusPoint focusPoint = Alpha.get().mission().focusPoint();
//        AUnit unit = Alpha.get().first();
//
//        if (focusPoint != null) {
//            APainter.paintLine(unit, focusPoint, Color.Cyan);
//        }

//        AAdvancedPainter.paintSideMessage("Focus: x:" + focusPoint.x() + ", y:" + focusPoint.y(), Color.Yellow);
//    }

}