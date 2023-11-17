package atlantis.game;

import atlantis.debug.painter.AAdvancedPainter;
import atlantis.map.choke.AChoke;
import atlantis.map.path.PathToEnemyBase;
import atlantis.map.position.APosition;

import atlantis.units.AUnit;
import atlantis.units.select.Select;
import bwapi.Color;
import jbweb.Block;
import jbweb.Blocks;

import java.util.ArrayList;

/**
 * Auxiliary class, helpful when there's need to do something every frame and not spam other classes.
 */
public class OnEveryFrameHelper {
    public static void handle() {
//        paintMissionAttackFocusPoint();
//        pathToEnemyBase();

//        paintJbwebBlocks();

//        CurrentProductionQueue.print();

//        Queue.get().readyToProduceOrders().print("All orders");

//        combatUnitInfo();

        paintMyOwnWallInSolution();
    }

    private static void paintMyOwnWallInSolution() {

    }

    private static void combatUnitInfo() {
        AUnit first = Select.ourCombatUnits().nonBuildings().first();
        if (first == null) return;

        A.println(first.typeWithHash() + " / " + first.manager() + " / " + first.combatEvalRelative());

//        if (first.combatEvalRelative() < 1 && first.isActiveManager(AttackNearbyEnemies.class)) {
//            A.printStackTrace("Why is this unit attacking?");
//        }
    }

    private static void paintJbwebBlocks() {
//        Blocks.draw();

        for (Block block : Blocks.getAllBlocks()) {
            AAdvancedPainter.paintRectangle(
                APosition.create(block.getTilePosition().toPosition()),
                block.width() * 32,
                block.height() * 32,
                Color.Grey
            );
        }
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
